package com.CampusHub.scheduling_Service.integration;

import com.CampusHub.scheduling_Service.client.SalleClient;
import com.CampusHub.scheduling_Service.client.UserClient;
import com.CampusHub.scheduling_Service.entity.SchedulePlan;
import com.CampusHub.scheduling_Service.repository.SchedulePlanRepository;
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
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @MockBean
    private SalleClient salleClient;

    @BeforeEach
    void setup() {
        planRepository.deleteAll();
    }

    @Test
    void shouldCreateAndListPlans() throws Exception {
        SchedulePlan plan = new SchedulePlan();
        plan.setName("Plan Test L1");
        plan.setLevel("L1");
        plan.setStatus(SchedulePlan.PlanStatus.DRAFT);
        plan.setAcademicYear("2024-2025");
        plan.setSemester(1);

        mockMvc.perform(post("/api/scheduling/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plan)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Plan Test L1")))
                .andExpect(jsonPath("$.status", is("DRAFT")));

        mockMvc.perform(get("/api/scheduling/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Plan Test L1")));
    }

    @Test
    void shouldActivatePlanAndArchiveOthers() throws Exception {
        // 1. Create first active plan
        SchedulePlan plan1 = new SchedulePlan();
        plan1.setName("Ancien Plan");
        plan1.setLevel("L1");
        plan1.setSemester(1);
        plan1.setStatus(SchedulePlan.PlanStatus.ACTIVE);
        plan1 = planRepository.save(plan1);

        // 2. Create a draft plan
        SchedulePlan plan2 = new SchedulePlan();
        plan2.setName("Nouveau Plan");
        plan2.setLevel("L1");
        plan2.setSemester(1);
        plan2.setStatus(SchedulePlan.PlanStatus.DRAFT);
        plan2 = planRepository.save(plan2);

        // 3. Activate plan 2
        mockMvc.perform(post("/api/scheduling/plans/" + plan2.getId() + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        // 4. Verify plan 1 is archived
        mockMvc.perform(get("/api/scheduling/plans/" + plan1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ARCHIVED")));
    }

    @Test
    void shouldImportPlanWithEvents() throws Exception {
        String jsonImport = """
            {
              "name": "Import Test",
              "academicYear": "2024-2025",
              "semester": 1,
              "level": "L1",
              "events": [
                {
                  "title": "Cours Importé",
                  "type": "lecture",
                  "day": 0,
                  "startTime": "08:00",
                  "endTime": "10:00",
                  "roomId": 1
                }
              ]
            }
            """;

        mockMvc.perform(post("/api/scheduling/plans/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonImport))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Import Test")));

        // Verify events are created
        mockMvc.perform(get("/api/scheduling/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[?(@.title == 'Cours Importé')]", notNullValue()));
    }
}
