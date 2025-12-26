/**
 * Utilidad para extraer mensajes de error de respuestas del servidor
 * Maneja múltiples formatos de error posibles
 */
export function extractErrorMessage(error: any): string {
  try {
    if (error?.error) {
      // Si es un string directo
      if (typeof error.error === 'string' && error.error.trim().length > 0) {
        return error.error;
      }
      // Si es un objeto con propiedad 'error'
      if (error.error.error) {
        return String(error.error.error);
      }
      // Si es un objeto con propiedad 'message'
      if (error.error.message) {
        return String(error.error.message);
      }
    }
    // Si hay propiedad 'message' en el error mismo
    if (error?.message) {
      return String(error.message);
    }
  } catch (_) {
    // Si falla el parsing, continúa
  }
  // Mensaje genérico por defecto
  return 'Ocurrió un error. Intenta nuevamente.';
}
