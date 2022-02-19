import React, { useState } from 'react';
import { useFormik } from 'formik'
import * as yup from 'yup';
import { FormInput } from '../components/FormInput';
import { CustomMultiSelect } from '../components/CustomMultiSelect';
import Button from '@mui/material/Button';
import { Row, Col, Form } from "reactstrap";
import { insertStudentDetails } from '../api/student-api';
import Box from '@mui/material/Box';

const ariaLabel = { 'aria-label': 'description' };

const validationSchema = yup.object({
    name: yup
        .string('Enter your name')
        .required('Name is required'),
    email: yup
        .string('Enter your email')
        .email('Enter a valid email')
        .required('Email is required'),
    age: yup
        .number('Enter your age')
        .required('Age is required'),
    cgpa: yup
        .number('Enter your GPA')
        .required('GPA is required'),
    studentNumber: yup
        .number('Enter your Student Number')
        .required('Student Number is required')
});

const subjectList = [
    'CSE 586-Distributed Systems',
    'CSE 562-Database Systems',
    'CSE 529-Algorithms for Modern Computing',
    'CSE 546-Reinforcement Learning',
    'CSE 611-MS Project Development',
    'CSE 610-Special Topics',
    'CSE 676-Deep Learning',
    'CSE 603-Parallel and Distributed System'
];
export const StudentDetails = () => {
    const [subjects, setSubjects] = React.useState([]);
    const formik = useFormik({
        initialValues: {
            name: '',
            email: '',
            subjects: [],
            age: '',
            cgpa: '',
            studentNumber: ''
        },
        validationSchema: validationSchema,
        onSubmit: (values) => {

            let sub = {
                'subjects': subjects
            }
            const formData = Object.assign({}, values, sub);
            console.log(formData);
            insertStudentDetails(formData).then(response => {
                console.log(response);
            });

        },
    });

    const handleSubjectChange = (e) => {
        // console.log(e.target.value);
        setSubjects(e.target.value);
    }

    return (

        // <Row style={{width:100}}>
        <Box >

            <Col sm="12" md={{ size: 6, offset: 3 }}>
                <Form onSubmit={formik.handleSubmit}>
                    <FormInput
                        id="Name"
                        name="name"
                        label="Name"
                        value={formik.values.name}
                        onChange={formik.handleChange}
                        error={formik.touched.name && Boolean(formik.errors.name)}
                        helperText={formik.touched.name && formik.errors.name}
                        inputProps={ariaLabel}
                    />
                    <FormInput
                        id="studentNumber"
                        name="studentNumber"
                        label="Student Number"
                        value={formik.values.studentNumber}
                        onChange={formik.handleChange}
                        error={formik.touched.studentNumber && Boolean(formik.errors.studentNumber)}
                        helperText={formik.touched.studentNumber && formik.errors.studentNumber}
                        inputProps={ariaLabel}
                    />
                    <FormInput
                        id="email"
                        name="email"
                        label="Email"
                        value={formik.values.email}
                        onChange={formik.handleChange}
                        error={formik.touched.email && Boolean(formik.errors.email)}
                        helperText={formik.touched.email && formik.errors.email}
                        inputProps={ariaLabel}
                    />
                    <FormInput
                        id="cgpa"
                        name="cgpa"
                        label="GPA"
                        value={formik.values.cgpa}
                        onChange={formik.handleChange}
                        error={formik.touched.cgpa && Boolean(formik.errors.cgpa)}
                        helperText={formik.touched.cgpa && formik.errors.cgpa}
                        inputProps={ariaLabel}
                    />

                    <FormInput
                        id="age"
                        name="age"
                        label="Age"
                        value={formik.values.age}
                        onChange={formik.handleChange}
                        error={formik.touched.age && Boolean(formik.errors.age)}
                        helperText={formik.touched.age && formik.errors.age}
                        inputProps={ariaLabel}
                    />

                    <CustomMultiSelect
                        id="subjects"
                        name="subjects"
                        label="Subjects"
                        optionList={subjectList}
                        onChange={handleSubjectChange}
                        value={formik.values.subjects}
                    />
                    <Row>
                        <Box textAlign='center' >
                            <Button color="primary" variant="contained" type="submit">
                                Submit
                            </Button>
                        </Box>
                    </Row>
                </Form>
            </Col>
        </Box>

    )
}

