import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { fetchAccessoriesPage, type AccessoryResponse } from '../../api/accessoriesApi'
import { DashListHeader } from '../../components/dashboard/DashListHeader'

function carLabel(car: AccessoryResponse['car']): string {
  const v = car.version?.trim()
  return [car.brand, car.model, v].filter(Boolean).join(' ')
}

function formatPrice(price: number): string {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(price)
}

export function AccessoriesPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const canCreate = location.pathname.startsWith('/manager')
  const [rows, setRows] = useState<AccessoryResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      setLoading(true)
      setError(null)
      try {
        const page = await fetchAccessoriesPage(0, 100)
        if (!cancelled) setRows(page.content)
      } catch (e) {
        if (!cancelled) setError(e instanceof Error ? e.message : 'Erro ao carregar acessórios')
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
        title="Acessórios"
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
              <th>Nome</th>
              <th>Descrição</th>
              <th>Preço</th>
              <th>Carro</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={5} className="dash-table__empty">
                  Carregando…
                </td>
              </tr>
            ) : rows.length === 0 ? (
              <tr>
                <td colSpan={5} className="dash-table__empty">
                  Nenhum acessório encontrado.
                </td>
              </tr>
            ) : (
              rows.map((a) => (
                <tr key={a.id}>
                  <td>{a.id}</td>
                  <td>{a.name}</td>
                  <td>{a.description}</td>
                  <td>{formatPrice(a.price)}</td>
                  <td>{carLabel(a.car)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
