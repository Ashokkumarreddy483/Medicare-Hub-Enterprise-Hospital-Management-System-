// src/components/common/Pagination.jsx
import React from 'react';

const Pagination = ({ currentPage, totalPages, onPageChange, totalElements, pageSize }) => {
  // ... (full pagination logic as provided before) ...
  if (!totalElements || totalPages <= 1) return null;
  const pageNumbers = [];
  const maxPagesToShow = 3;
  const sidePages = 1;

  pageNumbers.push(0);
  if (currentPage > sidePages + Math.floor(maxPagesToShow / 2) && totalPages > maxPagesToShow + (2*sidePages)) {
    pageNumbers.push('...');
  }
  let startPage = Math.max(sidePages, currentPage - Math.floor((maxPagesToShow -1) / 2));
  let endPage = Math.min(totalPages - 1 - sidePages, currentPage + Math.floor(maxPagesToShow / 2));
  if (endPage - startPage + 1 < maxPagesToShow && totalPages >= maxPagesToShow + (2*sidePages) ) {
    if (currentPage < totalPages / 2) {
        endPage = Math.min(totalPages - 1 - sidePages, startPage + maxPagesToShow - 1);
    } else {
        startPage = Math.max(sidePages, endPage - maxPagesToShow + 1);
    }
  }
  for (let i = startPage; i <= endPage; i++) {
    if (!pageNumbers.includes(i)) pageNumbers.push(i);
  }
  if (currentPage < totalPages - 1 - sidePages - Math.floor(maxPagesToShow / 2) && totalPages > maxPagesToShow + (2*sidePages)) {
    if (!pageNumbers.includes('...')) pageNumbers.push('...');
  }
  if (totalPages > 1 && !pageNumbers.includes(totalPages - 1)) pageNumbers.push(totalPages - 1);
  const finalPageNumbers = pageNumbers.filter((item, index) => item !== '...' || pageNumbers[index-1] !== '...');

  const renderPageNumbers = finalPageNumbers.map((number, index) => { /* ... as before ... */
    if (number === '...') return <span key={`ellipsis-${index}`} className="ellipsis">...</span>;
    return (<button key={number} onClick={() => onPageChange(number)}
        className={`${currentPage === number ? 'active-page' : ''}`}> {number + 1} </button>);
  });

  return (
    <nav className="pagination-nav" aria-label="Pagination">
      <div className="pagination-info">
        Showing <span className="font-semibold">{(currentPage * pageSize) + 1}</span>
        {' '}to <span className="font-semibold">{Math.min((currentPage + 1) * pageSize, totalElements)}</span>
        {' '}of <span className="font-semibold">{totalElements}</span> results
      </div>
      <div className="pagination-buttons">
        <button onClick={() => onPageChange(0)} disabled={currentPage === 0} aria-label="First Page">« First</button>
        <button onClick={() => onPageChange(currentPage - 1)} disabled={currentPage === 0} aria-label="Previous Page">Previous</button>
        {renderPageNumbers}
        <button onClick={() => onPageChange(currentPage + 1)} disabled={currentPage === totalPages - 1} aria-label="Next Page">Next</button>
        <button onClick={() => onPageChange(totalPages - 1)} disabled={currentPage === totalPages - 1} aria-label="Last Page">Last »</button>
      </div>
    </nav>
  );
};
export default Pagination;