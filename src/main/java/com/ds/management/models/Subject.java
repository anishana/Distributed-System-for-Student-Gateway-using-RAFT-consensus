package com.ds.management.models;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("subject")
public class Subject {

    @Id
    private String id;
    @BsonProperty(value = "course_id")
    private String courseId;
    private String name;
    private String teacher;
    @BsonProperty(value = "dept_name")
    private String departmentName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "id='" + id + '\'' +
                ", courseId='" + courseId + '\'' +
                ", name='" + name + '\'' +
                ", teacher='" + teacher + '\'' +
                ", departmentName='" + departmentName + '\'' +
                '}';
    }
}
