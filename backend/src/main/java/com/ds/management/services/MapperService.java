package com.ds.management.services;

import com.ds.management.models.Student;
import com.ds.management.models.Subject;
import com.ds.management.models.requests.StudentRequest;
import com.ds.management.models.requests.SubjectRequest;

public class MapperService {

    public static Student mapStudentRequestToStudent(StudentRequest request){
        Student student= new Student();
        student.setName(request.getName());
        student.setStudentNumber(request.getStudentNumber());
        student.setAge(request.getAge());
        student.setEmail(request.getEmail());
        student.setCgpa(request.getCgpa());
        student.setSubjects(request.getSubjects());
        return student;
    }

    public static Subject mapSubjectRequestToSubject(SubjectRequest request){
        Subject subject= new Subject();
        subject.setCourseId(request.getCourseId());
        subject.setDepartmentName(request.getDepartmentName());
        subject.setName(request.getName());
        subject.setTeacher(request.getTeacher());
        return subject;
    }

}
