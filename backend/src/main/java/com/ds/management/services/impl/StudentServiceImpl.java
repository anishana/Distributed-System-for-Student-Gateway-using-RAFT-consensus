package com.ds.management.services.impl;

import com.ds.management.configuration.MongoConfig;
import com.ds.management.models.Student;
import com.ds.management.models.requests.StudentRequest;
import com.ds.management.repository.StudentRepository;
import com.ds.management.services.MapperService;
import com.ds.management.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class StudentServiceImpl implements StudentService {

    private final static Logger LOGGER = Logger.getLogger(StudentServiceImpl.class.getName());

    @Autowired
    private StudentRepository repository;

    @Autowired
    private Environment env;

    @Override
    public Student getStudentByEmail(String email) {
        return repository.findStudentByEmail(email);
    }

    @Override
    public List<Student> getStudents() {
        String isMaster = env.getProperty("MASTER");
        if(isMaster.equalsIgnoreCase("yes")){
            LOGGER.info("is Master");
        } else {
            LOGGER.info("is not Master");
        }
        return repository.findAll();
    }

    @Override
    public Student createStudent(StudentRequest request) {
        Student student= MapperService.mapStudentRequestToStudent(request);

        repository.save(student);
        String isMaster = env.getProperty("MASTER");
        if(isMaster.equalsIgnoreCase("yes")){
            LOGGER.info("is Master");
        } else {
            LOGGER.info("is not Master");
        }


        return student;
    }

    @Override
    public void deleteStudent(String id){
        repository.deleteById(id);
    }
}
