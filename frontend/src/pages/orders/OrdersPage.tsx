import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchOrdersPage, type OrderResponse } from '../../api/ordersApi'
import { DashListHeader } from '../../components/dashboard/DashListHeader'
import {
  orderStatusBadgeClass,
  orderStatusBucket,
  orderStatusShortLabelPt,
} from '../../domain/orderStatus'

const STATS_PAGE_SIZE = 500

function formatDate(iso: string): string {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function vehicleLabel(car: OrderResponse['car']): string {
  const v = car.version?.trim()
  return [car.brand, car.model, v].filter(Boolean).join(' ')
}

function clientLabel(c: OrderResponse['client']): string {
  return `${c.name} ${c.lastName}`.trim()
}

export function OrdersPage() {
  const navigate = useNavigate()
  const [orders, setOrders] = useState<OrderResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      setLoading(true)
      setError(null)
      try {
        const page = await fetchOrdersPage(0, STATS_PAGE_SIZE)
        if (!cancelled) setOrders(page.content)
      } catch (e) {
        if (!cancelled) setError(e instanceof Error ? e.message : 'Erro ao carregar pedidos')
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  const counts = useMemo(() => {
    let pending = 0
    let processing = 0
    let done = 0
    for (const o of orders) {
      const b = orderStatusBucket(o.status)
      if (b === 'pending') pending += 1
      else if (b === 'processing') processing += 1
      else done += 1
    }
    return { pending, processing, done }
  }, [orders])

  return (
    <div>
      <DashListHeader
        title="Pedidos"
        actions={
          <button type="button" className="dash-btn-primary" onClick={() => navigate('orders/new')}>
            Criar pedido
          </button>
        }
      />

      {error ? <p className="dash-error">{error}</p> : null}

      <div className="dash-stat-grid">
        <div className="dash-stat dash-stat--pending">
          <span className="dash-stat__icon" aria-hidden>
            🕐
          </span>
          <div>
            <div className="dash-stat__value">{loading ? '-' : counts.pending}</div>
            <div className="dash-stat__label">Pendentes</div>
          </div>
        </div>
        <div className="dash-stat dash-stat--processing">
          <span className="dash-stat__icon" aria-hidden>
            🔧
          </span>
          <div>
            <div className="dash-stat__value">{loading ? '-' : counts.processing}</div>
            <div className="dash-stat__label">Em processamento</div>
          </div>
        </div>
        <div className="dash-stat dash-stat--done">
          <span className="dash-stat__icon" aria-hidden>
            ✓
          </span>
          <div>
            <div className="dash-stat__value">{loading ? '-' : counts.done}</div>
            <div className="dash-stat__label">Concluídos</div>
          </div>
        </div>
      </div>

      <p className="dash-hint">
        Contagens com base nos pedidos carregados (até {STATS_PAGE_SIZE}). Para totais globais em escala, um endpoint de
        agregação no backend seria o próximo passo.
      </p>

      <div className="dash-table-wrap">
        <table className="dash-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Cliente</th>
              <th>Veículo</th>
              <th>Status</th>
              <th>Data</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={5} className="dash-table__empty">
                  Carregando...
                </td>
              </tr>
            ) : orders.length === 0 ? (
              <tr>
                <td colSpan={5} className="dash-table__empty">
                  Nenhum pedido encontrado.
                </td>
              </tr>
            ) : (
              orders.map((o) => (
                <tr key={o.id}>
                  <td>{o.id}</td>
                  <td>{clientLabel(o.client)}</td>
                  <td>{vehicleLabel(o.car)}</td>
                  <td>
                    <span className={orderStatusBadgeClass(o.status)}>{orderStatusShortLabelPt(o.status)}</span>
                  </td>
                  <td>{formatDate(o.orderDate)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
