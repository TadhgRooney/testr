import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'

function App() {

  return (
    <div className="bg-light min-vh-100">
      <nav className="navbar navbar-dark bg-dark mb-4">
        <div className="container-fluid">
        <span className='navbar-brand mb-0 h1'Testr Dashboard></span>
        </div>
      </nav>

      <div className="container pb-4">
        <DiagnosticsDashboard/>
      </div>
    </div>
  );
}

export default App
