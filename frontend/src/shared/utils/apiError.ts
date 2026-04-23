import { isAxiosError } from 'axios';

/**
 * Extrae el mensaje de error más específico disponible:
 * 1. Mensaje del cuerpo JSON que devuelve el backend  → {"message":"..."}
 * 2. Mensaje de red (sin respuesta del servidor)
 * 3. Fallback genérico con texto del caller
 */
export function extractApiError(error: unknown, fallback = 'Ha ocurrido un error inesperado'): string {
  if (!isAxiosError(error)) {
    return error instanceof Error ? error.message : fallback;
  }

  // El servidor respondió con un código de error (4xx / 5xx)
  if (error.response) {
    const data = error.response.data as { message?: string } | undefined;
    if (data?.message) return data.message;
    return `Error del servidor (${error.response.status})`;
  }

  // La petición salió pero no llegó respuesta (timeout, red caída)
  if (error.request) {
    return 'No se puede conectar con el servidor. Verifica tu conexión.';
  }

  return fallback;
}
