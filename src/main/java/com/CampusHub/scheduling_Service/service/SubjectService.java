package com.CampusHub.scheduling_Service.service;

import com.CampusHub.scheduling_Service.entity.Subject;
import com.CampusHub.scheduling_Service.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public List<Subject> getSubjectsByNiveauAndSemester(int niveau, int semester) {
        return subjectRepository.findByNiveauAndSemester(niveau, semester);
    }

    public List<Subject> getSubjectsByNiveau(int niveau) {
        return subjectRepository.findByNiveau(niveau);
    }

    public Subject getSubjectByCode(String code) {
        return subjectRepository.findById(code).orElse(null);
    }

    public Subject saveSubject(Subject subject) {
        return subjectRepository.save(subject);
    }
}
