import './App.css';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom'
import { StudentDetails } from './pages/student_details.js';

function App() {
  return (
    <Router>
      <div>
        <Routes>
          <Route path="/" exact />
        </Routes>
      </div>
    </Router>
  )
}

export default App;
