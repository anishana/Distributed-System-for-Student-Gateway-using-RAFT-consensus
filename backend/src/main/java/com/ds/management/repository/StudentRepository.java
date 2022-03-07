package com.ds.management.repository;

import com.ds.management.models.Student;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StudentRepository extends MongoRepository<Student, String> {

    Student findStudentByEmail(String email);

    List<Student> findAll();

    void deleteById(String id);

    void deleteByStudentNumber(Integer studentNumber);

    void deleteByEmail(String email);

}
