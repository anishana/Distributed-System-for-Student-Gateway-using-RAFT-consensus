package com.ds.management.services;

import com.ds.management.models.Subject;
import com.ds.management.models.requests.SubjectRequest;
import java.util.List;

public interface SubjectService {

    public Subject getSubjectByCourseId(String email);

    public List<Subject> getSubjects();

    public Subject createSubject(SubjectRequest request);

}
