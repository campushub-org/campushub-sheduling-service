package com.CampusHub.scheduling_Service.repository;

import com.CampusHub.scheduling_Service.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, String> {
    List<Subject> findByNiveauAndSemester(int niveau, int semester);
}
