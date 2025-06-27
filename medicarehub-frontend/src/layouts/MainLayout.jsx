// src/layouts/MainLayout.jsx
import React from 'react';
import Navbar from '../components/common/Navbar.jsx';
import Footer from '../components/common/Footer.jsx';

const MainLayout = ({ children }) => {
  return (
    <>
      <Navbar />
      <main className="container"> {/* main.container for specific padding */}
        {children}
      </main>
      <Footer />
    </>
  );
};
export default MainLayout;