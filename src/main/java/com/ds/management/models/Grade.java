package com.ds.management.models;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("grades")
public class Grade {

    @Id
    private ObjectId id;
    @BsonProperty("student_id")
    private String studentId;
    @BsonProperty("subject_id")
    private String subjectId;
    private String marks;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getMarks() {
        return marks;
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "id=" + id +
                ", studentId='" + studentId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", marks='" + marks + '\'' +
                '}';
    }
}
