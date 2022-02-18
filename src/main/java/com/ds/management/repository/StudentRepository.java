package com.ds.management.repository;

import com.ds.management.models.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface StudentRepository extends MongoRepository<Student, String> {

    Student findStudentByEmail(String email);

    List<Student> findAll();

}
