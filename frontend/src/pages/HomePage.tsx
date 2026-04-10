import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchMe, logout, type MeResponse } from '../api/authApi'

export function HomePage() {
  const navigate = useNavigate()
  const [me, setMe] = useState<MeResponse | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      try {
        const data = await fetchMe()
        if (!cancelled) setMe(data)
      } catch (e) {
        if (!cancelled) {
          setError(e instanceof Error ? e.message : 'Erro ao carregar usuário')
        }
      }
    })()

    return () => {
      cancelled = true
    }
  }, [navigate])

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="shell">
      <header className="shell-header">
        <h1>Autoconfig</h1>
        <button type="button" className="btn-secondary" onClick={handleLogout}>
          Sair
        </button>
      </header>

      {error ? <p className="auth-error">{error}</p> : null}

      {me ? (
        <section className="panel">
          <p>
            <strong>Email:</strong> {me.email}
          </p>
          <p>
            <strong>Papel:</strong> {me.role}
          </p>
          <p>
            <strong>Id:</strong> {me.id}
          </p>
          <p className="hint">
            Próximo passo: listar clientes, pedidos, etc., chamando a API com o mesmo token.
          </p>
        </section>
      ) : !error ? (
        <p>Carregando…</p>
      ) : null}
    </div>
  )
}
