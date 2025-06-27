// src/components/common/Spinner.jsx
import React from 'react';

const Spinner = ({ size = 'md' }) => {
  let spinnerSizeClass = 'h-8 w-8';
  if (size === 'sm') spinnerSizeClass = 'h-5 w-5';
  if (size === 'lg') spinnerSizeClass = 'h-12 w-12';
  // Basic CSS spinner - ensure you have keyframes for 'spin'
  return (
    <div style={{
        border: '4px solid rgba(0, 0, 0, 0.1)',
        width: size === 'sm' ? '20px' : (size === 'lg' ? '48px' : '32px'),
        height: size === 'sm' ? '20px' : (size === 'lg' ? '48px' : '32px'),
        borderRadius: '50%',
        borderLeftColor: '#007bff', // Primary color
        animation: 'spin 1s linear infinite'
      }}
      role="status" aria-label="loading">
    </div>
  );
};
// Add to index.css: @keyframes spin { to { transform: rotate(360deg); } }
export default Spinner;