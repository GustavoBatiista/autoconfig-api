import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchMe, type MeResponse } from '../../api/authApi'
import { fetchOrdersPage, type OrderAccessoryDto, type OrderResponse } from '../../api/ordersApi'
import { DashListHeader } from '../../components/dashboard/DashListHeader'
import { canConfirmVehicleInUi, canMutateOrderInUi } from '../../domain/orderPermissions'
import {
  orderStatusBadgeClass,
  orderStatusBucket,
  orderStatusShortLabelPt,
} from '../../domain/orderStatus'

const STATS_PAGE_SIZE = 500

function formatCreatedAt(iso: string | null | undefined, fallbackIso: string): string {
  const raw = iso ?? fallbackIso
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return raw
  return d.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function vehicleLabel(car: OrderResponse['car']): string {
  const v = car.version?.trim()
  return [car.brand, car.model, v].filter(Boolean).join(' ')
}

function clientLabel(c: OrderResponse['client']): string {
  return `${c.name} ${c.lastName}`.trim()
}

function accessoriesSummary(items: OrderAccessoryDto[]): string {
  if (!items.length) return '—'
  return items.map((a) => a.name).join(', ')
}

export function OrdersPage() {
  const navigate = useNavigate()
  const [me, setMe] = useState<MeResponse | null>(null)
  const [orders, setOrders] = useState<OrderResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    void fetchMe()
      .then((m) => {
        if (!cancelled) setMe(m)
      })
      .catch(() => {
        if (!cancelled) setMe(null)
      })
    return () => {
      cancelled = true
    }
  }, [])

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

      <div className="dash-order-cards" role="list" aria-label="Lista de pedidos">
        {loading ? (
          <div className="dash-order-cards__empty">Carregando...</div>
        ) : orders.length === 0 ? (
          <div className="dash-order-cards__empty">Nenhum pedido encontrado.</div>
        ) : (
          orders.map((o) => (
            <article
              key={o.id}
              className="dash-order-card dash-order-card--clickable"
              role="listitem"
              onClick={() => navigate({ pathname: 'orders/detail', search: `?id=${o.id}` })}
            >
              <div className="dash-order-card__id">Pedido #{o.id}</div>
              <div className="dash-order-card__row">
                <span className="dash-order-card__label">Cliente</span>
                <span className="dash-order-card__value">{clientLabel(o.client)}</span>
              </div>
              <div className="dash-order-card__row">
                <span className="dash-order-card__label">Veículo</span>
                <span className="dash-order-card__value">{vehicleLabel(o.car)}</span>
              </div>
              <div className="dash-order-card__row">
                <span className="dash-order-card__label">Acessórios</span>
                <span className="dash-order-card__value">{accessoriesSummary(o.accessories)}</span>
              </div>
              <div className="dash-order-card__row dash-order-card__row--status">
                <span className="dash-order-card__label">Status</span>
                <span className={orderStatusBadgeClass(o.status)}>{orderStatusShortLabelPt(o.status)}</span>
              </div>
              <div className="dash-order-card__row">
                <span className="dash-order-card__label">Criado em</span>
                <time className="dash-order-card__value" dateTime={o.createdAt ?? o.orderDate}>
                  {formatCreatedAt(o.createdAt, o.orderDate)}
                </time>
              </div>
              {me && (canConfirmVehicleInUi(me, o) || canMutateOrderInUi(me, o)) ? (
                <div className="dash-order-card__actions" onClick={(e) => e.stopPropagation()}>
                  {canConfirmVehicleInUi(me, o) ? (
                    <button
                      type="button"
                      className="dash-btn-secondary"
                      onClick={() => navigate({ pathname: 'orders/vehicle-data', search: `?id=${o.id}` })}
                    >
                      Incluir dados
                    </button>
                  ) : null}
                  {canMutateOrderInUi(me, o) ? (
                    <>
                      <button
                        type="button"
                        className="dash-btn-secondary"
                        onClick={() => navigate({ pathname: 'orders/edit', search: `?id=${o.id}` })}
                      >
                        Alterar
                      </button>
                      <button
                        type="button"
                        className="dash-btn-danger"
                        onClick={() => navigate({ pathname: 'orders/delete', search: `?id=${o.id}` })}
                      >
                        Excluir
                      </button>
                    </>
                  ) : null}
                </div>
              ) : null}
            </article>
          ))
        )}
      </div>
    </div>
  )
}
