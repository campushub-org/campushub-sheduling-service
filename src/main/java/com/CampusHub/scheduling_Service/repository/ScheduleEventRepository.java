package com.CampusHub.scheduling_Service.repository;

import com.CampusHub.scheduling_Service.entity.ScheduleEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleEventRepository extends JpaRepository<ScheduleEvent, UUID> {
    List<ScheduleEvent> findByRoomId(Long roomId);
    List<ScheduleEvent> findByGroupId(Long groupId);
    List<ScheduleEvent> findBySubjectCode(String subjectCode);
}
