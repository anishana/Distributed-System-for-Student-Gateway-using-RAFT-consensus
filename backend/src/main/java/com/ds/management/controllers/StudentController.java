package com.ds.management.controllers;

import com.ds.management.models.Student;
import com.ds.management.models.requests.StudentRequest;
import com.ds.management.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/ping")
    public ResponseEntity<List<String>> ping() {
        try{
            return new ResponseEntity<List<String>>(Arrays.asList("Hello World 1"), HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<List<Student>> getAllStudents() {
        try{
            List<Student> students= studentService.getStudents();
            return new ResponseEntity<>(students, HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/{email}")
    public ResponseEntity<Student> getStudentByEmail(@PathVariable("email") String email) {
        try{
            Student student= studentService.getStudentByEmail(email);
            return new ResponseEntity<>(student, HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Student> createStudent(@RequestBody StudentRequest  request) {
        try{
            Student student= studentService.createStudent(request);
            return new ResponseEntity<>(student, HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<String> createStudent(@PathVariable("email") String email) {
        try{
            studentService.deleteStudent(email);
            return new ResponseEntity<String>(email, HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

