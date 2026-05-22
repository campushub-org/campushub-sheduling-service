package com.CampusHub.scheduling_Service.repository;

import com.CampusHub.scheduling_Service.entity.SchedulePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchedulePlanRepository extends JpaRepository<SchedulePlan, UUID> {
    
    List<SchedulePlan> findByLevelAndAcademicYear(String level, String academicYear);
    
    Optional<SchedulePlan> findByLevelAndStatusAndIsDefaultTrue(String level, SchedulePlan.PlanStatus status);
    
    List<SchedulePlan> findByStatus(SchedulePlan.PlanStatus status);

    Optional<SchedulePlan> findByLevelAndAcademicYearAndSemesterAndStatus(String level, String academicYear, int semester, SchedulePlan.PlanStatus status);
}
