/**
 * Persistência do JWT no sessionStorage (escopo do separador).
 * Separado do cliente HTTP para deixar claro: aqui é só armazenamento do token.
 */
const ACCESS_TOKEN_KEY = 'autoconfig_token'

export function getAccessToken(): string | null {
  return sessionStorage.getItem(ACCESS_TOKEN_KEY)
}

/** Passa `null` para remover o token (logout). */
export function setAccessToken(token: string | null): void {
  if (token) {
    sessionStorage.setItem(ACCESS_TOKEN_KEY, token)
  } else {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY)
  }
}
