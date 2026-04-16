import { type FormEvent, useCallback, useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { confirmOrderVehicle, fetchOrderById, type OrderResponse } from '../../api/ordersApi'
import { orderStatusLabelPt } from '../../domain/orderStatus'
import { VEHICLE_CONDITION_OPTIONS } from '../../domain/vehicleCondition'
import { parseEntityId } from '../../utils/parseEntityId'

const moneyBr = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

function vehicleLabel(car: OrderResponse['car']): string {
  const v = car.version?.trim()
  return [car.brand, car.model, v].filter(Boolean).join(' ')
}

function clientLabel(c: OrderResponse['client']): string {
  return `${c.name} ${c.lastName}`.trim()
}

function accessoriesSummary(order: OrderResponse): string {
  if (!order.accessories.length) return '—'
  return order.accessories.map((a) => a.name).join(', ')
}

export function OrderVehicleDataPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const idFromQuery = searchParams.get('id')

  const [idInput, setIdInput] = useState(() => idFromQuery ?? '')
  const [order, setOrder] = useState<OrderResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loadingOrder, setLoadingOrder] = useState(false)
  const [chassis, setChassis] = useState('')
  const [arrivalDate, setArrivalDate] = useState('')
  const [condition, setCondition] = useState<string>(VEHICLE_CONDITION_OPTIONS[0].value)
  const [saving, setSaving] = useState(false)

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

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (order == null) return
    setError(null)
    const ch = chassis.trim().toUpperCase()
    if (ch.length !== 17) {
      setError('Chassi deve ter exatamente 17 caracteres.')
      return
    }
    if (!arrivalDate) {
      setError('Informe data e hora de chegada.')
      return
    }
    let iso = arrivalDate
    if (arrivalDate.length === 16) {
      iso = `${arrivalDate}:00`
    }
    setSaving(true)
    try {
      await confirmOrderVehicle(order.id, {
        chassis: ch,
        arrivalDate: iso,
        condition,
      })
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao salvar dados do veiculo')
    } finally {
      setSaving(false)
    }
  }

  const readOnlyBlock =
    order != null ? (
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
          Acessórios
          <input type="text" readOnly className="dash-input-readonly" value={accessoriesSummary(order)} />
        </label>
        <label>
          Status
          <input type="text" readOnly className="dash-input-readonly" value={orderStatusLabelPt(order.status)} />
        </label>
        <label>
          Total
          <input type="text" readOnly className="dash-input-readonly" value={moneyBr.format(order.totalPrice)} />
        </label>
      </div>
    ) : null

  return (
    <div>
      <h2 className="dash-page__heading">Incluir dados do veículo</h2>

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
              disabled={loadingOrder || saving}
            />
          </label>
        </div>
        <div className="dash-form-actions">
          <button type="button" className="dash-btn-secondary" onClick={onLoadById} disabled={loadingOrder || saving}>
            {loadingOrder ? 'Carregando...' : 'Carregar'}
          </button>
        </div>
      </div>

      {order != null && order.vehicleArrived ? (
        <div className="dash-user-form">
          {readOnlyBlock}
          <p className="dash-muted">A chegada do veículo já foi registrada para este pedido.</p>
          <div className="dash-form-actions">
            <button type="button" className="dash-btn-secondary" onClick={goBack}>
              Voltar
            </button>
          </div>
        </div>
      ) : null}

      {order != null && !order.vehicleArrived ? (
        <form className="dash-user-form" onSubmit={onSubmit}>
          {readOnlyBlock}
          <div className="dash-user-form__grid">
            <label>
              Chassi (17 caracteres)
              <input
                value={chassis}
                onChange={(e) => setChassis(e.target.value)}
                required
                minLength={17}
                maxLength={17}
                autoComplete="off"
                disabled={saving}
              />
            </label>
            <label>
              Data e hora de chegada
              <input
                type="datetime-local"
                value={arrivalDate}
                onChange={(e) => setArrivalDate(e.target.value)}
                required
                disabled={saving}
              />
            </label>
            <label>
              Estado do veículo
              <select value={condition} onChange={(e) => setCondition(e.target.value)} disabled={saving}>
                {VEHICLE_CONDITION_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            </label>
          </div>
          <div className="dash-form-actions">
            <button type="submit" className="dash-btn-primary" disabled={saving}>
              {saving ? 'Salvando...' : 'Salvar'}
            </button>
            <button type="button" className="dash-btn-secondary" onClick={goBack} disabled={saving}>
              Cancelar
            </button>
          </div>
        </form>
      ) : null}
    </div>
  )
}
