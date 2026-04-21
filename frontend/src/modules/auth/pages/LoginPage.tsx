import { Box, Container, Paper, Typography, Divider } from '@mui/material';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import LoginForm from '../components/LoginForm';

const LoginPage: React.FC = () => (
  <Box
    sx={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      bgcolor: 'background.default',
    }}
  >
    <Container maxWidth="xs">
      <Paper elevation={3} sx={{ p: 4, borderRadius: 2 }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
          <AccountBalanceWalletIcon sx={{ fontSize: 48, color: 'primary.main', mb: 1 }} />
          <Typography component="h1" variant="h4" fontWeight="bold" color="primary">
            FLUXA
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Gestión Financiera Personal
          </Typography>
        </Box>

        <Divider sx={{ mb: 3 }} />

        <Typography component="h2" variant="h6" gutterBottom>
          Iniciar sesión
        </Typography>

        <LoginForm />
      </Paper>
    </Container>
  </Box>
);

export default LoginPage;
