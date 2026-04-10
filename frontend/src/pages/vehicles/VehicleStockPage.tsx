import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { fetchVehicleEntries, type VehicleEntryResponse } from '../../api/vehiclesApi'
import { vehicleConditionLabelPt } from '../../domain/vehicleCondition'
import { DashListHeader } from '../../components/dashboard/DashListHeader'

function formatDateTime(iso: string): string {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function VehicleStockPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const canCreate = location.pathname.startsWith('/manager')
  const [entries, setEntries] = useState<VehicleEntryResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      setLoading(true)
      setError(null)
      try {
        const list = await fetchVehicleEntries()
        if (!cancelled) setEntries(list)
      } catch (e) {
        if (!cancelled) setError(e instanceof Error ? e.message : 'Erro ao carregar estoque de veículos')
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
        title="Estoque de veículos"
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
              <th>Chassi</th>
              <th>Chegada</th>
              <th>Condição</th>
              <th>Pedido</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={5} className="dash-table__empty">
                  Carregando…
                </td>
              </tr>
            ) : entries.length === 0 ? (
              <tr>
                <td colSpan={5} className="dash-table__empty">
                  Nenhuma entrada de veículo encontrada.
                </td>
              </tr>
            ) : (
              entries.map((v) => (
                <tr key={v.id}>
                  <td>{v.id}</td>
                  <td>{v.chassis}</td>
                  <td>{formatDateTime(v.arrivalDate)}</td>
                  <td>{vehicleConditionLabelPt(v.condition)}</td>
                  <td>{v.order?.id ?? '-'}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
