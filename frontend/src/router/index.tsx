import { createBrowserRouter, Navigate } from 'react-router-dom';
import ProtectedRoute from '@/shared/components/ProtectedRoute';
import LoginPage from '@/modules/auth/pages/LoginPage';

// Placeholder del Dashboard hasta Sprint 3
const DashboardPage = () => (
  <div style={{ padding: 32 }}>
    <h2>Dashboard — Próximamente</h2>
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
      { path: 'dashboard', element: <DashboardPage /> },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/login" replace />,
  },
]);
