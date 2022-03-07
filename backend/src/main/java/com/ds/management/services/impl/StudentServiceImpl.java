package com.ds.management.services.impl;

import com.ds.management.models.Student;
import com.ds.management.models.requests.StudentRequest;
import com.ds.management.repository.StudentRepository;
import com.ds.management.services.MapperService;
import com.ds.management.services.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StudentServiceImpl implements StudentService {

    private final static Logger LOGGER = LoggerFactory.getLogger(StudentServiceImpl.class);
    @Value("${server.url}")
    private String serverUrl;

    @Value("${student.create.url}")
    private String studentCreate;

    @Value("${student.delete.url}")
    private String studentDelete;

    @Value("${is.Master}")
    private String isMaster;

    @Autowired
    private StudentRepository repository;

    @Override
    public Student getStudentByEmail(String email) {
        return repository.findStudentByEmail(email);
    }

    @Override
    public List<Student> getStudents() {
        if(isMaster.equalsIgnoreCase("yes")){
            LOGGER.info("is Master");
        } else {
            LOGGER.info("is not Master");
        }
        return repository.findAll();
    }

    RequestCallback requestCallback(StudentRequest updated) {
        return clientHttpRequest -> {
            ObjectMapper mapper = new ObjectMapper();
            LOGGER.info("updated: "+updated.toString());
            mapper.writeValue(clientHttpRequest.getBody(), updated);
            clientHttpRequest.getHeaders().add(
                    HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        };
    }

    @Override
    public Student createStudent(StudentRequest request) {
        try{
            Student student= MapperService.mapStudentRequestToStudent(request);
            if(isMaster.equalsIgnoreCase("yes")){
                LOGGER.info("createStudent.is Master");
                AtomicInteger count= new AtomicInteger(1);
                RestTemplate restTemplate = new RestTemplate();
                ResponseExtractor<ClientHttpResponse> responseExtractor = response -> {
                    LOGGER.info("createStudent"+response.toString());
                    if(response.getStatusCode().value()==200){
                        count.addAndGet(1);
                        if(count.get()==3){
                            repository.save(student);
                        }
                    }
                    return response;
                };

                for(int i=1; i<3; i++){
                    String resourceUrl= serverUrl+ Integer.toString(i)+ ":8080/"+ studentCreate;
                    LOGGER.info("createStudent.resourceUrl "+resourceUrl);
                    restTemplate.execute(resourceUrl, HttpMethod.POST, requestCallback(request), responseExtractor);
                }
            } else{
                LOGGER.info("createStudent.is not Master");
                repository.save(student);
            }
            return student;
        } catch (Exception e) {
            LOGGER.error("Exception: ",e);
            throw e;
        }

    }

    @Override
    public void deleteStudent(String email){

        if(isMaster.equalsIgnoreCase("yes")){
            LOGGER.info("deleteStudent.is Master");
            AtomicInteger count= new AtomicInteger(1);
            RestTemplate restTemplate = new RestTemplate();
            ResponseExtractor<ClientHttpResponse> responseExtractor = response -> {
                LOGGER.info("deleteStudent"+response.toString());
                if(response.getStatusCode().value()==200){
                    count.addAndGet(1);
                    if(count.get()==3){
                        repository.deleteByEmail(email);
                    }
                }
                return response;
            };

            for(int i=1; i<3; i++){
                String resourceUrl= serverUrl+ Integer.toString(i)+ ":8080/"+ studentDelete+"/"+email;
                LOGGER.info("deleteStudent.resourceUrl "+resourceUrl);
                restTemplate.execute(resourceUrl, HttpMethod.DELETE, null, responseExtractor);
            }
        } else{
            LOGGER.info("deleteStudent.is not Master");
            repository.deleteByEmail(email);
        }

    }
}
