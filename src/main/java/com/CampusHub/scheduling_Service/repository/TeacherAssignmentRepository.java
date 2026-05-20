package com.CampusHub.scheduling_Service.repository;

import com.CampusHub.scheduling_Service.entity.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {
    List<TeacherAssignment> findBySubjectCode(String subjectCode);
    List<TeacherAssignment> findByTeacherId(Long teacherId);
    Optional<TeacherAssignment> findFirstByTeacherId(Long teacherId);
}


