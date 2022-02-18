import React from 'react';
import ReactDOM, {render} from 'react-dom';
// import './index.css';
import { BrowserRouter, Router, Route, Routes } from 'react-router-dom'
// import App from './App';
import reportWebVitals from './reportWebVitals';
// import 'bootstrap/dist/css/bootstrap.min.css';
import { StudentDetails } from './pages/student_details.js';
import { Drawer } from '@mui/material';
import { NavBar } from './components/NavBar.js';

const rootElement = document.getElementById("root");
render(
  <>
  <NavBar/>
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<StudentDetails/>} />
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
