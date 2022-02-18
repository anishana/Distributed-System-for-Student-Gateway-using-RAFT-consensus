package com.ds.management.services.impl;

import com.ds.management.models.Student;
import com.ds.management.models.requests.StudentRequest;
import com.ds.management.repository.StudentRepository;
import com.ds.management.services.MapperService;
import com.ds.management.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository repository;

    @Override
    public Student getStudentByEmail(String email) {
        return repository.findStudentByEmail(email);
    }

    @Override
    public List<Student> getStudents() {
        return repository.findAll();
    }

    @Override
    public Student createStudent(StudentRequest request) {
        Student student= MapperService.mapStudentRequestToStudent(request);
        repository.save(student);
        return student;
    }
}
