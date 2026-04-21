export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserDto;
}

export interface UserDto {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'ADMIN' | 'USER';
  groupId: number | null;
  isActive: boolean;
}
