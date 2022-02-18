package com.ds.management.models.requests;

public class SubjectRequest {

    private String courseId;
    private String name;
    private String teacher;
    private String departmentName;

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
        return "SubjectRequest{" +
                "courseId='" + courseId + '\'' +
                ", name='" + name + '\'' +
                ", teacher='" + teacher + '\'' +
                ", departmentName='" + departmentName + '\'' +
                '}';
    }
}
