import { createBrowserRouter, Navigate } from 'react-router-dom';
import ProtectedRoute from '@/shared/components/ProtectedRoute';
import AppLayout from '@/shared/components/AppLayout';
import LoginPage from '@/modules/auth/pages/LoginPage';
import RegisterPage from '@/modules/auth/pages/RegisterPage';
import IncomePage from '@/modules/income/pages/IncomePage';
import CommitmentPage from '@/modules/commitment/pages/CommitmentPage';
import { ExpensePage } from '@/modules/expense/pages/ExpensePage';
import { DashboardPage } from '@/modules/dashboard/pages/DashboardPage';
import { CreditPage } from '@/modules/credit/pages/CreditPage';
import { NotificationPage } from '@/modules/notification/pages/NotificationPage';
import { ImportPage } from '@/modules/importing/pages/ImportPage';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
  },
  {
    path: '/',
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { index: true, element: <Navigate to="/dashboard" replace /> },
          { path: 'dashboard',       element: <DashboardPage /> },
          { path: 'income',          element: <IncomePage /> },
          { path: 'commitments',     element: <CommitmentPage /> },
          { path: 'expenses',        element: <ExpensePage /> },
          { path: 'credits',         element: <CreditPage /> },
          { path: 'notifications',   element: <NotificationPage /> },
          { path: 'import',          element: <ImportPage /> },
        ],
      },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/login" replace />,
  },
]);
