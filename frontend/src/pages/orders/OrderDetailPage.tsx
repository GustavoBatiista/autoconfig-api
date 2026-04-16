import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { fetchOrderById, type OrderResponse } from '../../api/ordersApi'
import { orderStatusLabelPt } from '../../domain/orderStatus'
import { vehicleConditionLabelPt } from '../../domain/vehicleCondition'
import { parseEntityId } from '../../utils/parseEntityId'

const moneyBr = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

function vehicleLabel(car: OrderResponse['car']): string {
  const v = car.version?.trim()
  return [car.brand, car.model, v].filter(Boolean).join(' ')
}

function clientLabel(c: OrderResponse['client']): string {
  return `${c.name} ${c.lastName}`.trim()
}

function formatDateTime(iso: string | null | undefined, fallbackIso: string): string {
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

export function OrderDetailPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const idFromQuery = searchParams.get('id')

  const [idInput, setIdInput] = useState(() => idFromQuery ?? '')
  const [order, setOrder] = useState<OrderResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loadingOrder, setLoadingOrder] = useState(false)

  const loadOrderByParsedId = useCallback(async (id: number) => {
    setError(null)
    setLoadingOrder(true)
    try {
      const o = await fetchOrderById(id)
      setOrder(o)
      setIdInput(String(id))
    } catch (err) {
      setOrder(null)
      setError(err instanceof Error ? err.message : 'Erro ao carregar pedido')
    } finally {
      setLoadingOrder(false)
    }
  }, [])

  async function onLoadById() {
    const id = parseEntityId(idInput)
    if (id == null) {
      setError('Informe um ID valido.')
      return
    }
    await loadOrderByParsedId(id)
  }

  useEffect(() => {
    if (!idFromQuery) return
    const id = parseEntityId(idFromQuery)
    if (id == null) return
    void loadOrderByParsedId(id)
  }, [idFromQuery, loadOrderByParsedId])

  function goBack() {
    navigate('..')
  }

  const ve = order?.vehicleEntry

  return (
    <div>
      <h2 className="dash-page__heading">Detalhe do pedido</h2>

      {error ? <p className="dash-error">{error}</p> : null}

      <div className="dash-user-form dash-form-id-block">
        <div className="dash-user-form__grid dash-form-id-row">
          <label>
            ID do pedido
            <input
              value={idInput}
              onChange={(e) => setIdInput(e.target.value)}
              inputMode="numeric"
              placeholder="Ex.: 12"
              disabled={loadingOrder}
            />
          </label>
        </div>
        <div className="dash-form-actions">
          <button type="button" className="dash-btn-secondary" onClick={onLoadById} disabled={loadingOrder}>
            {loadingOrder ? 'Carregando...' : 'Carregar'}
          </button>
          <button type="button" className="dash-btn-secondary" onClick={goBack}>
            Voltar
          </button>
        </div>
      </div>

      {order != null ? (
        <div className="dash-user-form">
          <div className="dash-user-form__grid">
            <label>
              Cliente
              <input type="text" readOnly className="dash-input-readonly" value={clientLabel(order.client)} />
            </label>
            <label>
              Veículo
              <input type="text" readOnly className="dash-input-readonly" value={vehicleLabel(order.car)} />
            </label>
            <label>
              Status
              <input type="text" readOnly className="dash-input-readonly" value={orderStatusLabelPt(order.status)} />
            </label>
            <label>
              Total
              <input type="text" readOnly className="dash-input-readonly" value={moneyBr.format(order.totalPrice)} />
            </label>
            <label>
              Data do pedido
              <input type="text" readOnly className="dash-input-readonly" value={formatDateTime(null, order.orderDate)} />
            </label>
            <label>
              Criado em
              <input
                type="text"
                readOnly
                className="dash-input-readonly"
                value={formatDateTime(order.createdAt, order.orderDate)}
              />
            </label>
            <label>
              Vendedor (id)
              <input type="text" readOnly className="dash-input-readonly" value={String(order.sellerId)} />
            </label>
            <label>
              Veículo confirmado
              <input type="text" readOnly className="dash-input-readonly" value={order.vehicleArrived ? 'Sim' : 'Nao'} />
            </label>
            <label>
              Acessórios confirmados
              <input
                type="text"
                readOnly
                className="dash-input-readonly"
                value={order.accessoriesConfirmed ? 'Sim' : 'Nao'}
              />
            </label>
            <label>
              Instalação concluída
              <input
                type="text"
                readOnly
                className="dash-input-readonly"
                value={order.installationCompleted ? 'Sim' : 'Nao'}
              />
            </label>
          </div>

          <fieldset className="dash-checkboxes-fieldset">
            <legend className="dash-checkboxes-legend">Acessórios</legend>
            {order.accessories.length === 0 ? (
              <p className="dash-muted">Nenhum.</p>
            ) : (
              <ul className="dash-checkboxes-list">
                {order.accessories.map((a) => (
                  <li key={a.id}>
                    <span className="dash-checkbox-row__title">
                      #{a.id} {a.name}
                    </span>
                    <span className="dash-checkbox-row__meta">{moneyBr.format(a.price)}</span>
                  </li>
                ))}
              </ul>
            )}
          </fieldset>

          <fieldset className="dash-checkboxes-fieldset">
            <legend className="dash-checkboxes-legend">Dados do veículo recebido</legend>
            {ve == null ? (
              <p className="dash-muted">Ainda nao registrados.</p>
            ) : (
              <div className="dash-user-form__grid">
                <label>
                  Chassi
                  <input type="text" readOnly className="dash-input-readonly" value={ve.chassis} />
                </label>
                <label>
                  Data de chegada
                  <input type="text" readOnly className="dash-input-readonly" value={formatDateTime(null, ve.arrivalDate)} />
                </label>
                <label>
                  Estado do veículo
                  <input type="text" readOnly className="dash-input-readonly" value={vehicleConditionLabelPt(ve.condition)} />
                </label>
              </div>
            )}
          </fieldset>
        </div>
      ) : null}
    </div>
  )
}
