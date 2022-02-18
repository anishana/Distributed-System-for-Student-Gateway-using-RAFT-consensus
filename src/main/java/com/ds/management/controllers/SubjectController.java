package com.ds.management.controllers;

import com.ds.management.models.Subject;
import com.ds.management.models.requests.SubjectRequest;
import com.ds.management.services.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/subject")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @GetMapping("/get")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        try{
            List<Subject> subjects= subjectService.getSubjects();
            return new ResponseEntity<>(subjects, HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Subject> getSubject(@PathVariable("id") String id) {
        try{
            Subject subject= subjectService.getSubjectByCourseId(id);
            return new ResponseEntity<>(subject, HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Subject> createSubject(SubjectRequest request) {
        try{
            Subject subject= subjectService.createSubject(request);
            return new ResponseEntity<>(subject, HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

