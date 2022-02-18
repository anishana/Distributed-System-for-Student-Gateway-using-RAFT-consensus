import axios from 'axios'

const api = axios.create({
    baseURL: 'http://localhost:8080',
})


export const insertStudentDetails = payload => api.post(`/student/create`, payload);
export const getStudentDetails = payload => api.post(`/student/get`);
export const getStudentDetailsByEmail= email => api.post(`/students/${email}`);


const apis = {
    insertStudentDetails,
    getStudentDetails,
    getStudentDetailsByEmail
}

export default apis;