import React, { useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  AppBar, Avatar, Box, Chip, Divider, Drawer, IconButton,
  List, ListItemButton, ListItemIcon, ListItemText,
  Stack, Toolbar, Tooltip, Typography, useMediaQuery, useTheme,
} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import AssignmentIcon from '@mui/icons-material/Assignment';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import MenuIcon from '@mui/icons-material/Menu';
import LogoutIcon from '@mui/icons-material/Logout';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import { useAuthStore } from '../store/authStore';
import { useDashboardSummary } from '../../modules/dashboard/hooks/useDashboard';

const DRAWER_WIDTH = 240;

const NAV_ITEMS = [
  { label: 'Dashboard',     path: '/dashboard',    icon: <DashboardIcon /> },
  { label: 'Ingresos',      path: '/income',       icon: <TrendingUpIcon /> },
  { label: 'Compromisos',   path: '/commitments',  icon: <AssignmentIcon /> },
  { label: 'Gastos',        path: '/expenses',     icon: <ShoppingCartIcon /> },
  { label: 'Créditos',      path: '/credits',      icon: <CreditCardIcon /> },
];

const HEALTH_COLORS = {
  GREEN:  { bg: '#2e7d32', label: 'Saludable' },
  YELLOW: { bg: '#ed6c02', label: 'Precaución' },
  RED:    { bg: '#d32f2f', label: 'Riesgo' },
};

const AppLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mobileOpen, setMobileOpen] = useState(false);

  const { user, logout } = useAuthStore();
  const now = new Date();
  const { data: summary } = useDashboardSummary(now.getMonth() + 1, now.getFullYear());

  const health = summary?.healthStatus
    ? HEALTH_COLORS[summary.healthStatus]
    : null;

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const drawerContent = (
    <Box display="flex" flexDirection="column" height="100%">
      {/* Logo */}
      <Toolbar sx={{ gap: 1 }}>
        <AccountBalanceWalletIcon color="primary" />
        <Typography variant="h6" fontWeight={800} color="primary">
          FLUXA
        </Typography>
      </Toolbar>
      <Divider />

      {/* Navegación */}
      <List sx={{ flex: 1, pt: 1 }}>
        {NAV_ITEMS.map(item => {
          const active = location.pathname.startsWith(item.path);
          return (
            <ListItemButton
              key={item.path}
              selected={active}
              onClick={() => { navigate(item.path); if (isMobile) setMobileOpen(false); }}
              sx={{
                mx: 1, borderRadius: 2, mb: 0.5,
                '&.Mui-selected': {
                  bgcolor: 'primary.main',
                  color: 'white',
                  '& .MuiListItemIcon-root': { color: 'white' },
                  '&:hover': { bgcolor: 'primary.dark' },
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 36 }}>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          );
        })}
      </List>

      <Divider />

      {/* Indicador de salud */}
      {health && (
        <Box px={2} py={1.5}>
          <Typography variant="caption" color="text.secondary" display="block" mb={0.5}>
            Salud Financiera
          </Typography>
          <Chip
            label={health.label}
            size="small"
            sx={{ bgcolor: health.bg, color: 'white', fontWeight: 600, width: '100%' }}
          />
        </Box>
      )}

      {/* Usuario */}
      <Box px={2} py={1.5}>
        <Stack direction="row" alignItems="center" spacing={1}>
          <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main', fontSize: 14 }}>
            {user?.firstName?.[0]?.toUpperCase() ?? 'U'}
          </Avatar>
          <Box flex={1} minWidth={0}>
            <Typography variant="body2" fontWeight={600} noWrap>
              {user?.firstName} {user?.lastName}
            </Typography>
            <Typography variant="caption" color="text.secondary" noWrap>
              {user?.email}
            </Typography>
          </Box>
          <Tooltip title="Cerrar sesión">
            <IconButton size="small" onClick={handleLogout}>
              <LogoutIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Stack>
      </Box>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      {/* AppBar móvil */}
      {isMobile && (
        <AppBar position="fixed" sx={{ zIndex: theme.zIndex.drawer + 1 }}>
          <Toolbar>
            <IconButton color="inherit" edge="start" onClick={() => setMobileOpen(!mobileOpen)} sx={{ mr: 2 }}>
              <MenuIcon />
            </IconButton>
            <AccountBalanceWalletIcon sx={{ mr: 1 }} />
            <Typography variant="h6" fontWeight={800}>FLUXA</Typography>
          </Toolbar>
        </AppBar>
      )}

      {/* Drawer desktop */}
      {!isMobile && (
        <Drawer
          variant="permanent"
          sx={{
            width: DRAWER_WIDTH,
            flexShrink: 0,
            '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' },
          }}
        >
          {drawerContent}
        </Drawer>
      )}

      {/* Drawer móvil */}
      {isMobile && (
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          sx={{
            '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' },
          }}
        >
          {drawerContent}
        </Drawer>
      )}

      {/* Contenido principal */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          minWidth: 0,
          mt: isMobile ? 8 : 0,
          bgcolor: 'grey.50',
          minHeight: '100vh',
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
};

export default AppLayout;
