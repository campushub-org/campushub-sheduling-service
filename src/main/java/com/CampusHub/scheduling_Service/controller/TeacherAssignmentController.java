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

    @PostMapping
    public TeacherAssignment createAssignment(@RequestBody TeacherAssignment assignment) {
        return repository.save(assignment);
    }

    @PutMapping("/{id}")
    public TeacherAssignment updateAssignment(@PathVariable Long id, @RequestBody TeacherAssignment assignment) {
        assignment.setId(id);
        return repository.save(assignment);
    }

    @DeleteMapping("/{id}")
    public void deleteAssignment(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
