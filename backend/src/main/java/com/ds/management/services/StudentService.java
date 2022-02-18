package com.ds.management.services;

import com.ds.management.models.Student;
import com.ds.management.models.requests.StudentRequest;

import java.util.List;

public interface StudentService {

    public Student getStudentByEmail(String email);

    public List<Student> getStudents();

    public Student createStudent(StudentRequest request);

    public void deleteStudent(String id);

}
