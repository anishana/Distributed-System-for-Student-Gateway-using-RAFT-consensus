import React, { useEffect, useState } from 'react';
import * as yup from 'yup';
import { Row, Col, Form } from "reactstrap";
import { getStudentDetails } from '../api/student-api';
import { DataGrid } from '@mui/x-data-grid';
import FormControl from '@mui/material/FormControl';
// import {CustomSelect,FormikMultiSelect} from '../components/FormikMultiSelect'

export const ListStudents = () => {
    const [rows, setRows] = React.useState([]);
    useEffect(() => {
        console.log("Initial");
        // setRows('[{"id":"621056afeb331802fa5bcfc5","name":"Anish Joshi","studentNumber":"50419412","email":"anishana@buffalo.edu","age":27,"subjects":["CSE 586-Distributed Systems","CSE 562-Database Systems","CSE 529-Algorithms for Modern Computing","CSE 611-MS Project Development"],"cgpa":3.84},{"id":"62105a2fd9a6c23924f32865","name":"Neha Mishra","studentNumber":"50416280","email":"anishana@buffalo.edu","age":26,"subjects":["CSE 586-Distributed Systems","CSE 529-Algorithms for Modern Computing","CSE 603-Parallel and Distributed System"],"cgpa":3.6}]');
        getStudentDetails().then(response => {
            // console.log(response.data);
            setRows(response.data);
        });
    }, []);

    const columns = [
        // { field: 'id', headerName: 'ID', width: 90 },
        {
            field: 'name',
            headerName: 'Name',
            width: 300,
        },
        {
            field: 'email',
            headerName: 'Email',
            width: 300,
        },
        {
            field: 'age',
            headerName: 'Age',
            type: 'number',
            width: 70,
        },
        {
            field: 'studentNumber',
            headerName: 'Student Number',
            type: 'number',
            width: 200,
        },
        {
            field: 'cgpa',
            headerName: 'CGPA',
            type: 'number',
            width: 110,
        }
        ,
        {
            field: 'subjects',
            headerName: 'Subjects',
            width: 550,
        }
    ];

    return (

        <div style={{ height: '650px', width: '100%' }}>

            <DataGrid
                rows={rows}
                columns={columns}
                pageSize={5}
                rowsPerPageOptions={[5]}
            // checkboxSelection
            // disableSelectionOnClick
            // columnBuffer={8}
            />
        </div>

    )
}

