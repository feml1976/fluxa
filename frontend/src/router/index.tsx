import { createBrowserRouter, Navigate } from 'react-router-dom';
import ProtectedRoute from '@/shared/components/ProtectedRoute';
import LoginPage from '@/modules/auth/pages/LoginPage';
import IncomePage from '@/modules/income/pages/IncomePage';
import CommitmentPage from '@/modules/commitment/pages/CommitmentPage';
import { ExpensePage } from '@/modules/expense/pages/ExpensePage';
import { DashboardPage } from '@/modules/dashboard/pages/DashboardPage';
import { CreditPage } from '@/modules/credit/pages/CreditPage';

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
      { path: 'expenses',     element: <ExpensePage /> },
      { path: 'credits',      element: <CreditPage /> },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/login" replace />,
  },
]);
