import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchClientsPage, type ClientResponse } from '../../api/clientsApi'
import { DashListHeader } from '../../components/dashboard/DashListHeader'

export function ClientsPage() {
  const navigate = useNavigate()
  const [clients, setClients] = useState<ClientResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      setLoading(true)
      setError(null)
      try {
        const page = await fetchClientsPage(0, 100)
        if (!cancelled) setClients(page.content)
      } catch (e) {
        if (!cancelled) setError(e instanceof Error ? e.message : 'Erro ao carregar clientes')
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <div>
      <DashListHeader
        title="Clientes"
        actions={
          <button type="button" className="dash-btn-primary" onClick={() => navigate('new')}>
            Criar
          </button>
        }
      />

      {error ? <p className="dash-error">{error}</p> : null}

      <div className="dash-table-wrap">
        <table className="dash-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>Sobrenome</th>
              <th>Telefone</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={4} className="dash-table__empty">
                  Carregando…
                </td>
              </tr>
            ) : clients.length === 0 ? (
              <tr>
                <td colSpan={4} className="dash-table__empty">
                  Nenhum cliente encontrado.
                </td>
              </tr>
            ) : (
              clients.map((c) => (
                <tr key={c.id}>
                  <td>{c.id}</td>
                  <td>{c.name}</td>
                  <td>{c.lastName}</td>
                  <td>{c.phoneNumber}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
