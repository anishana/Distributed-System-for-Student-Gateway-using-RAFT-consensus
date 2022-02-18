package com.ds.management.services.impl;

import com.ds.management.models.Subject;
import com.ds.management.models.requests.SubjectRequest;
import com.ds.management.repository.SubjectRepository;
import com.ds.management.services.MapperService;
import com.ds.management.services.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectServiceImpl implements SubjectService {

    @Autowired
    private SubjectRepository repository;

    @Override
    public Subject getSubjectByCourseId(String courseId) {
        return repository.findSubjectByCourseId(courseId);
    }

    @Override
    public List<Subject> getSubjects() {
        return repository.findAll();
    }

    @Override
    public Subject createSubject(SubjectRequest request) {
        Subject subject= MapperService.mapSubjectRequestToSubject(request);
        repository.save(subject);
        return subject;
    }
}
