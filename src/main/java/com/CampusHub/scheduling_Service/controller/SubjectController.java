package com.CampusHub.scheduling_Service.controller;

import com.CampusHub.scheduling_Service.entity.Subject;
import com.CampusHub.scheduling_Service.service.SubjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public List<Subject> getSubjects(
            @RequestParam(required = false) Integer niveau,
            @RequestParam(required = false) Integer semester) {
        if (niveau != null && semester != null) {
            return subjectService.getSubjectsByNiveauAndSemester(niveau, semester);
        } else if (niveau != null) {
            return subjectService.getSubjectsByNiveau(niveau);
        }
        return subjectService.getAllSubjects();
    }

    @GetMapping("/{code}")
    public Subject getSubjectByCode(@PathVariable String code) {
        return subjectService.getSubjectByCode(code);
    }
}
