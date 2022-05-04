package com.ds.Controller_Java.model;

import lombok.Data;

import java.util.List;

@Data
public class StudentRequest {
    private String name;
    private String studentNumber;
    private String email;
    private Integer age;
    private Double cgpa;
    private List<String> subjects;
}
