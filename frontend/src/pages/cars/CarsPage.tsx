import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { fetchCarsPage, type CarResponse } from '../../api/carsApi'
import { DashListHeader } from '../../components/dashboard/DashListHeader'

export function CarsPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const canCreate = location.pathname.startsWith('/manager')
  const [cars, setCars] = useState<CarResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      setLoading(true)
      setError(null)
      try {
        const page = await fetchCarsPage(0, 100)
        if (!cancelled) setCars(page.content)
      } catch (e) {
        if (!cancelled) setError(e instanceof Error ? e.message : 'Erro ao carregar carros')
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
        title="Carros"
        actions={
          canCreate ? (
            <button type="button" className="dash-btn-primary" onClick={() => navigate('new')}>
              Criar
            </button>
          ) : undefined
        }
      />

      {error ? <p className="dash-error">{error}</p> : null}

      <div className="dash-table-wrap">
        <table className="dash-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Marca</th>
              <th>Modelo</th>
              <th>Versão</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={4} className="dash-table__empty">
                  Carregando…
                </td>
              </tr>
            ) : cars.length === 0 ? (
              <tr>
                <td colSpan={4} className="dash-table__empty">
                  Nenhum carro encontrado.
                </td>
              </tr>
            ) : (
              cars.map((c) => (
                <tr key={c.id}>
                  <td>{c.id}</td>
                  <td>{c.brand}</td>
                  <td>{c.model}</td>
                  <td>{c.version}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
