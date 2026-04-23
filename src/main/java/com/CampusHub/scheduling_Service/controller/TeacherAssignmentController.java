package com.CampusHub.scheduling_Service.controller;

import com.CampusHub.scheduling_Service.entity.TeacherAssignment;
import com.CampusHub.scheduling_Service.repository.TeacherAssignmentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduling/assignments")
public class TeacherAssignmentController {

    private final TeacherAssignmentRepository repository;

    public TeacherAssignmentController(TeacherAssignmentRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TeacherAssignment> getAllAssignments() {
        return repository.findAll();
    }

    @GetMapping("/subject/{subjectCode}")
    public List<TeacherAssignment> getAssignmentsBySubject(@PathVariable String subjectCode) {
        return repository.findBySubjectCode(subjectCode);
    }

    @GetMapping("/teacher/{teacherId}")
    public List<TeacherAssignment> getAssignmentsByTeacher(@PathVariable Long teacherId) {
        return repository.findByTeacherId(teacherId);
    }
}
