import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link } from 'react-router-dom';
import {
  Box, Button, TextField, Typography, Alert,
  InputAdornment, IconButton, CircularProgress,
} from '@mui/material';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import { useLogin } from '../hooks/useLogin';
import { extractApiError } from '@/shared/utils/apiError';

const schema = z.object({
  email: z.string().email('Formato de email inválido'),
  password: z.string().min(1, 'La contraseña es obligatoria'),
});

type FormData = z.infer<typeof schema>;

const LoginForm: React.FC = () => {
  const [showPassword, setShowPassword] = useState(false);
  const { mutate: login, isPending, error } = useLogin();

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = (data: FormData) => login(data);

  const errorMessage = error ? extractApiError(error, 'Error al iniciar sesión') : null;

  return (
    <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate sx={{ mt: 1 }}>
      {errorMessage && (
        <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>
      )}

      <TextField
        {...register('email')}
        label="Correo electrónico"
        type="email"
        fullWidth
        margin="normal"
        autoComplete="email"
        autoFocus
        error={!!errors.email}
        helperText={errors.email?.message}
        disabled={isPending}
      />

      <TextField
        {...register('password')}
        label="Contraseña"
        type={showPassword ? 'text' : 'password'}
        fullWidth
        margin="normal"
        autoComplete="current-password"
        error={!!errors.password}
        helperText={errors.password?.message}
        disabled={isPending}
        InputProps={{
          endAdornment: (
            <InputAdornment position="end">
              <IconButton onClick={() => setShowPassword((s) => !s)} edge="end">
                {showPassword ? <VisibilityOff /> : <Visibility />}
              </IconButton>
            </InputAdornment>
          ),
        }}
      />

      <Button
        type="submit"
        fullWidth
        variant="contained"
        size="large"
        disabled={isPending}
        sx={{ mt: 3, mb: 2 }}
      >
        {isPending ? <CircularProgress size={24} color="inherit" /> : 'Ingresar'}
      </Button>

      <Typography variant="body2" align="center">
        ¿No tienes cuenta?{' '}
        <Link to="/register" style={{ color: 'inherit' }}>Regístrate aquí</Link>
      </Typography>
    </Box>
  );
};

export default LoginForm;
