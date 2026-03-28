/** Base URL da API Spring Boot (sem barra final). */
export function getApiBaseUrl(): string {
  const raw = import.meta.env.VITE_API_BASE_URL
  if (!raw || String(raw).trim() === '') {
    return 'http://localhost:8080'
  }
  return String(raw).replace(/\/$/, '')
}
