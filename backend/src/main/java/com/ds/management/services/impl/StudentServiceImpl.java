package com.ds.management.services.impl;

import com.ds.management.models.Student;
import com.ds.management.models.requests.StudentRequest;
import com.ds.management.repository.StudentRepository;
import com.ds.management.services.MapperService;
import com.ds.management.services.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Service
public class StudentServiceImpl implements StudentService {

    private final static Logger LOGGER = Logger.getLogger(StudentServiceImpl.class.getName());

    @Value("serverUrl")
    private String serverUrl;

    @Value("studentCreateUrl")
    private String studentCreate;

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

    RequestCallback requestCallback(final Student updated) {
        return clientHttpRequest -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(clientHttpRequest.getBody(), updated);
            clientHttpRequest.getHeaders().add(
                    HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        };
    }

    @Override
    public Student createStudent(StudentRequest request) {
        Student student= MapperService.mapStudentRequestToStudent(request);
        String isMaster = env.getProperty("MASTER");
        if(isMaster.equalsIgnoreCase("yes")){
            LOGGER.info("is Master");
            AtomicInteger count= new AtomicInteger(1);
            RestTemplate restTemplate = new RestTemplate();
            ResponseExtractor<ClientHttpResponse> responseExtractor = response -> {
                System.out.println(response.toString());
                if(response.getStatusCode().value()==200){
                    count.addAndGet(1);
                    if(count.get()==3){
                        repository.save(student);
                    }
                }
                return response;
            };

            for(int i=1; i<3; i++){
                String resourceUrl= serverUrl+ Integer.toString(i)+ "/"+ studentCreate;
                Student response = new Student();
                restTemplate.execute(resourceUrl, HttpMethod.POST, requestCallback(response), responseExtractor);
            }
            repository.save(student);
        } else{
            LOGGER.info("is not Master");
            repository.save(student);
        }
        return student;
    }

    @Override
    public void deleteStudent(String id){
        repository.deleteById(id);
    }
}
