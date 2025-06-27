// src/components/common/Footer.jsx
import React from 'react';

const Footer = () => {
  const currentYear = new Date().getFullYear();
  return (
    <footer className="footer"> {/* .footer class from index.css */}
      <div className="container">
        <p>© {currentYear} MediCareHub. All rights reserved.</p>
      </div>
    </footer>
  );
};
export default Footer;