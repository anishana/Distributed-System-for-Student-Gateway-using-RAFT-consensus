import React,{ useState } from 'react';
import { Field, useFormik } from 'formik'
import * as yup from 'yup';
import { FormInput } from '../components/FormInput';
import { CustomMultiSelect } from '../components/CustomMultiSelect';
import Button from '@material-ui/core/Button';
import { Row, Col, Form } from "reactstrap";
import { Drawer } from '@mui/material';
import FormControl from '@mui/material/FormControl';
import { TextField } from '@material-ui/core';
import {insertStudentDetails} from '../api/student-api';
// import {CustomSelect,FormikMultiSelect} from '../components/FormikMultiSelect'


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
});

const subjectList = [
    'CSE 586-Distributed Systems',
    'CSE 562-Database Systems',
    'CSE 529-Algorithms for Modern Computing',
    'CSE 611-MS Project Development'
];
export const StudentDetails = () => {
    const [subjects, setSubjects] = React.useState([]);
    const formik = useFormik({
        initialValues: {
            name: '',
            email: '',
            subjects: [],
            age: '',
        },
        // validationSchema: validationSchema,
        onSubmit: (values) => {

            let sub = {
                'subjects':subjects
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
        <div>

            <Row className="mt-4">
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
                        <FormControl sx={{ m: 3, width: 100 }}>
                            <Button color="primary" variant="contained" type="submit">
                                Submit
                            </Button>
                        </FormControl>

                    </Form>
                </Col>
            </Row>
        </div>

    )
}

