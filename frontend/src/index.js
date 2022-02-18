import React from 'react';
import ReactDOM, { render } from 'react-dom';
import { BrowserRouter, Router, Route, Routes } from 'react-router-dom'
import reportWebVitals from './reportWebVitals';
import { StudentDetails } from './pages/student_details';
import { ListStudents } from './pages/list_students'
import { Drawer } from '@mui/material';
import { NavBar } from './components/NavBar.js';

const rootElement = document.getElementById("root");
render(
  <>
    <BrowserRouter>
      <NavBar />
      <Routes>
        <Route path="/" element={<StudentDetails />} />
        <Route path="/list" element={<ListStudents />} />
      </Routes>
    </BrowserRouter>
  </>,
  rootElement
);

// ReactDOM.render(
//   <App>
// );

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
