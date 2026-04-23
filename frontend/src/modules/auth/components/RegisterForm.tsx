import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link } from 'react-router-dom';
import {
  Box, Button, TextField, Alert,
  InputAdornment, IconButton, CircularProgress, Typography,
} from '@mui/material';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import { useRegister } from '../hooks/useRegister';

const schema = z.object({
  firstName: z.string().min(2, 'Mínimo 2 caracteres'),
  lastName:  z.string().min(2, 'Mínimo 2 caracteres'),
  email:     z.string().email('Formato de email inválido'),
  password:  z.string()
    .min(8, 'Mínimo 8 caracteres')
    .regex(/[A-Z]/, 'Debe contener al menos una mayúscula')
    .regex(/[0-9]/, 'Debe contener al menos un número')
    .regex(/[!@#$%^&*]/, 'Debe contener al menos un carácter especial (!@#$%^&*)'),
});

type FormData = z.infer<typeof schema>;

const RegisterForm: React.FC = () => {
  const [showPassword, setShowPassword] = useState(false);
  const { mutate: register, isPending, error } = useRegister();

  const { register: field, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = (data: FormData) => register(data);

  const errorMessage = error
    ? (error as { response?: { data?: { message?: string } } }).response?.data?.message
      ?? 'Error al registrar la cuenta'
    : null;

  return (
    <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate sx={{ mt: 1 }}>
      {errorMessage && (
        <Alert severity="error" sx={{ mb: 2 }}>{errorMessage}</Alert>
      )}

      <TextField
        {...field('firstName')}
        label="Nombre"
        fullWidth
        margin="normal"
        autoFocus
        error={!!errors.firstName}
        helperText={errors.firstName?.message}
        disabled={isPending}
      />

      <TextField
        {...field('lastName')}
        label="Apellido"
        fullWidth
        margin="normal"
        error={!!errors.lastName}
        helperText={errors.lastName?.message}
        disabled={isPending}
      />

      <TextField
        {...field('email')}
        label="Correo electrónico"
        type="email"
        fullWidth
        margin="normal"
        autoComplete="email"
        error={!!errors.email}
        helperText={errors.email?.message}
        disabled={isPending}
      />

      <TextField
        {...field('password')}
        label="Contraseña"
        type={showPassword ? 'text' : 'password'}
        fullWidth
        margin="normal"
        autoComplete="new-password"
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
        {isPending ? <CircularProgress size={24} color="inherit" /> : 'Crear cuenta'}
      </Button>

      <Typography variant="body2" align="center">
        ¿Ya tienes cuenta?{' '}
        <Link to="/login" style={{ color: 'inherit' }}>Inicia sesión aquí</Link>
      </Typography>
    </Box>
  );
};

export default RegisterForm;
