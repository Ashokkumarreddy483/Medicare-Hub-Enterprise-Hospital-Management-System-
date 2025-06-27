// src/main.jsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App.jsx'; // Ensure .jsx extension
import { AuthProvider } from './contexts/AuthContext.jsx'; // Ensure .jsx extension
import './index.css'; // Global styles

const rootElement = document.getElementById('root');

if (!rootElement) {
  throw new Error("Failed to find the root element with ID 'root'.");
}

const root = ReactDOM.createRoot(rootElement);
root.render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
);