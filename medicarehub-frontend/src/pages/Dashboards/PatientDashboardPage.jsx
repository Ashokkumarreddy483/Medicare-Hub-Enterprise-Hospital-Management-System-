// src/pages/Dashboards/PatientDashboardPage.jsx
import React from 'react';

const PatientDashboardPage = () => {
  const getUserData = () => {
    const data = localStorage.getItem('userData');
    try {
      return data ? JSON.parse(data) : null;
    } catch (e) {
      return null;
    }
  };

  const userData = getUserData();

  return (
    <div className="dashboard-page" style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <h1>Patient Dashboard</h1>
      {userData && <p>Welcome, {userData.firstName || userData.username}!</p>}

      <div style={{ marginTop: '30px', border: '1px solid #ccc', borderRadius: '8px', padding: '20px', backgroundColor: '#f9f9f9' }}>
        <h2>Why Was the EEG Test Recommended?</h2>
        <p>EEG (Electroencephalogram) is a test that measures electrical activity in the brain. A doctor may refer a patient for an EEG if they suspect conditions such as:</p>
        <ul>
          <li><strong>Seizures or Epilepsy</strong> – To detect abnormal electrical activity.</li>
          <li><strong>Unexplained Fainting or Blackouts</strong> – To rule out neurological causes.</li>
          <li><strong>Sleep Disorders</strong> – Such as narcolepsy or sleep apnea.</li>
          <li><strong>Head Injuries</strong> – To check for disturbances in brain function.</li>
          <li><strong>Brain Infections (e.g., Encephalitis)</strong> – To monitor brain response.</li>
          <li><strong>Dementia or Memory Problems</strong> – To assess cognitive activity.</li>
        </ul>

        <h3>Why This Patient?</h3>
        <p>Sometimes, the EEG doctor may prefer or prioritize a patient based on:</p>
        <ul>
          <li>Clear symptoms indicating need for EEG.</li>
          <li>Involvement in a clinical trial.</li>
          <li>Patient's cooperation or availability.</li>
        </ul>
      </div>
    </div>
  );
};

export default PatientDashboardPage;
