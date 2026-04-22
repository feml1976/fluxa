import { createBrowserRouter, Navigate } from 'react-router-dom';
import ProtectedRoute from '@/shared/components/ProtectedRoute';
import LoginPage from '@/modules/auth/pages/LoginPage';
import IncomePage from '@/modules/income/pages/IncomePage';
import CommitmentPage from '@/modules/commitment/pages/CommitmentPage';

const DashboardPage = () => (
  <div style={{ padding: 32 }}>
    <h2>Dashboard — Sprint 3</h2>
  </div>
);

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/',
    element: <ProtectedRoute />,
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: 'dashboard',    element: <DashboardPage /> },
      { path: 'income',       element: <IncomePage /> },
      { path: 'commitments',  element: <CommitmentPage /> },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/login" replace />,
  },
]);
