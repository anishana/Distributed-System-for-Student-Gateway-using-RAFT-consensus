package com.ds.management.repository;

import com.ds.management.models.Subject;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SubjectRepository extends MongoRepository<Subject, String> {

    Subject findSubjectByCourseId(String courseId);

    List<Subject> findSubjectByDepartmentName(String department);

    List<Subject> findSubjectByTeacher(String teacher);

    List<Subject> findAll();

}
