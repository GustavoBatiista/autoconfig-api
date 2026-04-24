import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { fetchMe, type MeResponse } from '../../api/authApi'
import {
  confirmOrderAccessories,
  confirmOrderInspection,
  confirmOrderInstallation,
  deleteOrder,
  fetchOrderById,
  type OrderResponse,
} from '../../api/ordersApi'
import {
  accessoriesWorkflowDisabled,
  accessoriesWorkflowTitle,
  canMutateOrderInUi,
  canShowOrderWorkflowArea,
  inspectionWorkflowDisabled,
  inspectionWorkflowTitle,
  installationWorkflowDisabled,
  installationWorkflowTitle,
  showAccessoriesWorkflowButton,
  showInstallationWorkflowButton,
  showInspectionWorkflowButton,
  showVehicleWorkflowButton,
  showsFourStepOrderToolbar,
  vehicleWorkflowDisabled,
  vehicleWorkflowTitle,
} from '../../domain/orderPermissions'
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

function sellerLabel(order: OrderResponse): string {
  const name = order.sellerName?.trim()
  if (name) return name
  return String(order.sellerId)
}

export function OrderDetailPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const idFromQuery = searchParams.get('id')

  const [me, setMe] = useState<MeResponse | null>(null)
  const [order, setOrder] = useState<OrderResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loadingOrder, setLoadingOrder] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [confirmingAccessories, setConfirmingAccessories] = useState(false)
  const [confirmingInspection, setConfirmingInspection] = useState(false)
  const [confirmingInstallation, setConfirmingInstallation] = useState(false)

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

  const loadOrderByParsedId = useCallback(async (id: number) => {
    setError(null)
    setLoadingOrder(true)
    try {
      const o = await fetchOrderById(id)
      setOrder(o)
    } catch (err) {
      setOrder(null)
      setError(err instanceof Error ? err.message : 'Erro ao carregar pedido')
    } finally {
      setLoadingOrder(false)
    }
  }, [])

  useEffect(() => {
    if (!idFromQuery) {
      setOrder(null)
      setError('Nenhum pedido selecionado.')
      return
    }
    const id = parseEntityId(idFromQuery)
    if (id == null) {
      setOrder(null)
      setError('ID do pedido invalido.')
      return
    }
    setOrder(null)
    void loadOrderByParsedId(id)
  }, [idFromQuery, loadOrderByParsedId])

  const ve = order?.vehicleEntry

  const actionBusy =
    deleting || confirmingAccessories || confirmingInspection || confirmingInstallation

  async function onConfirmAccessories() {
    if (order == null || me == null || confirmingAccessories || accessoriesWorkflowDisabled(me, order, actionBusy))
      return
    setError(null)
    setConfirmingAccessories(true)
    try {
      const updated = await confirmOrderAccessories(order.id)
      setOrder(updated)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao confirmar acessorios')
    } finally {
      setConfirmingAccessories(false)
    }
  }

  async function onConfirmInspection() {
    if (order == null || me == null || confirmingInspection || inspectionWorkflowDisabled(me, order, actionBusy))
      return
    setError(null)
    setConfirmingInspection(true)
    try {
      const updated = await confirmOrderInspection(order.id)
      setOrder(updated)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao confirmar inspecao')
    } finally {
      setConfirmingInspection(false)
    }
  }

  async function onConfirmInstallation() {
    if (order == null || me == null || confirmingInstallation || installationWorkflowDisabled(me, order, actionBusy))
      return
    setError(null)
    setConfirmingInstallation(true)
    try {
      const updated = await confirmOrderInstallation(order.id)
      setOrder(updated)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao confirmar instalacao')
    } finally {
      setConfirmingInstallation(false)
    }
  }

  async function onDeleteOrder() {
    if (order == null || deleting) return
    if (!window.confirm('Excluir este pedido permanentemente?')) return
    setError(null)
    setDeleting(true)
    try {
      await deleteOrder(order.id)
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao excluir pedido')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div>
      {order != null ? (
        <header className="dash-list-header">
          <h2 className="dash-page__heading dash-list-header__title">Detalhe do pedido</h2>
          {me && canMutateOrderInUi(me, order) ? (
            <div className="dash-list-header__actions">
              <button
                type="button"
                className="dash-btn-secondary"
                onClick={() => navigate({ pathname: '../edit', search: `?id=${order.id}` }, { relative: 'path' })}
                disabled={actionBusy}
              >
                Alterar
              </button>
              <button
                type="button"
                className="dash-btn-danger"
                onClick={onDeleteOrder}
                disabled={actionBusy}
              >
                {deleting ? 'Excluindo...' : 'Excluir'}
              </button>
            </div>
          ) : null}
        </header>
      ) : (
        <h2 className="dash-page__heading">Detalhe do pedido</h2>
      )}

      {loadingOrder ? <p className="dash-muted">Carregando...</p> : null}
      {error ? <p className="dash-error">{error}</p> : null}

      {order != null ? (
        <div className="dash-user-form">
          {me != null && canShowOrderWorkflowArea(me, order) ? (
            <div
              className={
                showsFourStepOrderToolbar(me, order)
                  ? 'dash-form-actions dash-order-detail__workflow dash-order-detail__workflow--four'
                  : 'dash-form-actions dash-order-detail__workflow'
              }
            >
              {showsFourStepOrderToolbar(me, order) ? (
                <>
                  <button
                    type="button"
                    className="dash-btn-secondary"
                    onClick={() =>
                      navigate({ pathname: '../vehicle-data', search: `?id=${order.id}` }, { relative: 'path' })
                    }
                    disabled={vehicleWorkflowDisabled(me, order, actionBusy)}
                    title={vehicleWorkflowTitle(me, order, actionBusy)}
                  >
                    Dados do veículo
                  </button>
                  <button
                    type="button"
                    className="dash-btn-secondary"
                    onClick={onConfirmAccessories}
                    disabled={accessoriesWorkflowDisabled(me, order, actionBusy)}
                    title={accessoriesWorkflowTitle(me, order, actionBusy)}
                  >
                    {confirmingAccessories ? 'Confirmando...' : 'Confirmar acessórios'}
                  </button>
                  <button
                    type="button"
                    className="dash-btn-secondary"
                    onClick={onConfirmInspection}
                    disabled={inspectionWorkflowDisabled(me, order, actionBusy)}
                    title={inspectionWorkflowTitle(me, order, actionBusy)}
                  >
                    {confirmingInspection ? 'Confirmando...' : 'Confirmar inspeção'}
                  </button>
                  <button
                    type="button"
                    className="dash-btn-secondary"
                    onClick={onConfirmInstallation}
                    disabled={installationWorkflowDisabled(me, order, actionBusy)}
                    title={installationWorkflowTitle(me, order, actionBusy)}
                  >
                    {confirmingInstallation ? 'Confirmando...' : 'Confirmar instalação'}
                  </button>
                </>
              ) : (
                <>
                  {showVehicleWorkflowButton(me, order) ? (
                    <button
                      type="button"
                      className="dash-btn-secondary"
                      onClick={() =>
                        navigate({ pathname: '../vehicle-data', search: `?id=${order.id}` }, { relative: 'path' })
                      }
                      disabled={vehicleWorkflowDisabled(me, order, actionBusy)}
                      title={vehicleWorkflowTitle(me, order, actionBusy)}
                    >
                      Dados do veículo
                    </button>
                  ) : null}
                  {showAccessoriesWorkflowButton(me, order) ? (
                    <button
                      type="button"
                      className="dash-btn-secondary"
                      onClick={onConfirmAccessories}
                      disabled={accessoriesWorkflowDisabled(me, order, actionBusy)}
                      title={accessoriesWorkflowTitle(me, order, actionBusy)}
                    >
                      {confirmingAccessories ? 'Confirmando...' : 'Confirmar acessórios'}
                    </button>
                  ) : null}
                  {showInspectionWorkflowButton(me, order) ? (
                    <button
                      type="button"
                      className="dash-btn-secondary"
                      onClick={onConfirmInspection}
                      disabled={inspectionWorkflowDisabled(me, order, actionBusy)}
                      title={inspectionWorkflowTitle(me, order, actionBusy)}
                    >
                      {confirmingInspection ? 'Confirmando...' : 'Confirmar inspeção'}
                    </button>
                  ) : null}
                  {showInstallationWorkflowButton(me, order) ? (
                    <button
                      type="button"
                      className="dash-btn-secondary"
                      onClick={onConfirmInstallation}
                      disabled={installationWorkflowDisabled(me, order, actionBusy)}
                      title={installationWorkflowTitle(me, order, actionBusy)}
                    >
                      {confirmingInstallation ? 'Confirmando...' : 'Confirmar instalação'}
                    </button>
                  ) : null}
                </>
              )}
            </div>
          ) : null}
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
              Última atualização
              <input
                type="text"
                readOnly
                className="dash-input-readonly"
                value={formatDateTime(order.updatedAt ?? order.createdAt, order.orderDate)}
              />
            </label>
            <label>
              Vendedor
              <input type="text" readOnly className="dash-input-readonly" value={sellerLabel(order)} />
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
              Inspeção concluída
              <input
                type="text"
                readOnly
                className="dash-input-readonly"
                value={order.inspectionCompleted ? 'Sim' : 'Nao'}
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
              <ul className="dash-accessory-list">
                {order.accessories.map((a) => (
                  <li key={a.id} className="dash-accessory-item">
                    <div className="dash-accessory-item__main">
                      <span className="dash-accessory-item__id">#{a.accessoryId}</span>
                      <span className="dash-accessory-item__name">{a.name}</span>
                    </div>
                    <span className="dash-accessory-item__price">{moneyBr.format(a.price)}</span>
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
