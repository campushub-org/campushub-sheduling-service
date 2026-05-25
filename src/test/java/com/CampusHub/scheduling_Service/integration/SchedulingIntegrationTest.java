package com.CampusHub.scheduling_Service.integration;

import com.CampusHub.scheduling_Service.client.SalleClient;
import com.CampusHub.scheduling_Service.client.UserClient;
import com.CampusHub.scheduling_Service.dto.ScheduleEventDTO;
import com.CampusHub.scheduling_Service.entity.SchedulePlan;
import com.CampusHub.scheduling_Service.repository.SchedulePlanRepository;
import com.CampusHub.scheduling_Service.repository.ScheduleEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SchedulingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SchedulePlanRepository planRepository;

    @Autowired
    private ScheduleEventRepository eventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @MockBean
    private SalleClient salleClient;

    @BeforeEach
    void setup() {
        eventRepository.deleteAll();
        planRepository.deleteAll();
    }

    @Test
    void shouldCreateAndListPlansWithDates() throws Exception {
        SchedulePlan plan = new SchedulePlan();
        plan.setName("Plan L1 2024");
        plan.setLevel("L1");
        plan.setStatus(SchedulePlan.PlanStatus.DRAFT);
        plan.setAcademicYear("2024-2025");
        plan.setSemester(1);
        plan.setStartDate(LocalDate.of(2024, 9, 1));
        plan.setEndDate(LocalDate.of(2025, 1, 31));

        mockMvc.perform(post("/api/scheduling/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plan)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Plan L1 2024")))
                .andExpect(jsonPath("$.startDate", is("2024-09-01")))
                .andExpect(jsonPath("$.endDate", is("2025-01-31")));
    }

    @Test
    void shouldUpdatePlan() throws Exception {
        SchedulePlan plan = new SchedulePlan();
        plan.setName("Version Initiale");
        plan.setLevel("L1");
        plan.setStatus(SchedulePlan.PlanStatus.DRAFT);
        plan = planRepository.save(plan);

        plan.setName("Version Mise à jour");
        plan.setStatus(SchedulePlan.PlanStatus.ACTIVE);

        mockMvc.perform(put("/api/scheduling/plans/" + plan.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plan)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Version Mise à jour")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void shouldCreateEventLinkedToPlan() throws Exception {
        // 1. Create a plan
        SchedulePlan plan = new SchedulePlan();
        plan.setName("Semestre 1");
        plan.setLevel("L1");
        plan.setStatus(SchedulePlan.PlanStatus.ACTIVE);
        plan = planRepository.save(plan);

        // 2. Create an event linked to this plan
        ScheduleEventDTO eventDTO = new ScheduleEventDTO();
        eventDTO.setTitle("Algorithmique");
        eventDTO.setType("lecture");
        eventDTO.setDay(0); // Lundi
        eventDTO.setStartTime("08:00");
        eventDTO.setEndTime("10:00");
        eventDTO.setRoomId(101L);
        eventDTO.setPlanId(plan.getId().toString());

        mockMvc.perform(post("/api/scheduling/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planId", is(plan.getId().toString())));

        // 3. Verify it's returned when filtering by plan
        mockMvc.perform(get("/api/scheduling/events?planId=" + plan.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Algorithmique")));
    }

    @Test
    void shouldDeletePlanAndCascadeEvents() throws Exception {
        // 1. Create plan and event
        SchedulePlan plan = new SchedulePlan();
        plan.setName("Plan temporaire");
        plan.setLevel("L1");
        plan = planRepository.save(plan);

        ScheduleEventDTO eventDTO = new ScheduleEventDTO();
        eventDTO.setTitle("Cours à supprimer");
        eventDTO.setRoomId(1L);
        eventDTO.setPlanId(plan.getId().toString());
        
        mockMvc.perform(post("/api/scheduling/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isOk());

        // 2. Delete plan
        mockMvc.perform(delete("/api/scheduling/plans/" + plan.getId()))
                .andExpect(status().isOk());

        // 3. Verify event is also gone
        mockMvc.perform(get("/api/scheduling/events?planId=" + plan.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
