import { type FormEvent, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchAccessoriesPage, type AccessoryResponse } from '../../api/accessoriesApi'
import { fetchCarsPage, type CarResponse } from '../../api/carsApi'
import { fetchClientsPage, type ClientResponse } from '../../api/clientsApi'
import { createOrder } from '../../api/ordersApi'
import { ORDER_STATUS_FORM_OPTIONS } from '../../domain/orderStatus'

const moneyBr = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

function carLabel(c: CarResponse): string {
  const v = c.version?.trim()
  return `${c.brand} ${c.model} ${v}`.trim()
}

function clientLabel(c: ClientResponse): string {
  return `${c.name} ${c.lastName}`.trim()
}

export function OrderCreatePage() {
  const navigate = useNavigate()
  const [clients, setClients] = useState<ClientResponse[]>([])
  const [cars, setCars] = useState<CarResponse[]>([])
  const [accessories, setAccessories] = useState<AccessoryResponse[]>([])
  const [listsLoading, setListsLoading] = useState(true)
  const [listsError, setListsError] = useState<string | null>(null)

  const [clientId, setClientId] = useState<number | ''>('')
  const [carId, setCarId] = useState<number | ''>('')
  const [selectedAccessoryIds, setSelectedAccessoryIds] = useState<Set<number>>(new Set())
  const [status, setStatus] = useState<string>(ORDER_STATUS_FORM_OPTIONS[0].value)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      setListsLoading(true)
      setListsError(null)
      try {
        const [clientsPage, carsPage, accPage] = await Promise.all([
          fetchClientsPage(0, 200),
          fetchCarsPage(0, 200),
          fetchAccessoriesPage(0, 500),
        ])
        if (cancelled) return
        setClients(clientsPage.content)
        setCars(carsPage.content)
        setAccessories(accPage.content)
        if (clientsPage.content.length) setClientId((p) => (p === '' ? clientsPage.content[0].id : p))
        if (carsPage.content.length) setCarId((p) => (p === '' ? carsPage.content[0].id : p))
      } catch {
        if (!cancelled) setListsError('Nao foi possivel carregar listas para o formulario.')
      } finally {
        if (!cancelled) setListsLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  const accessoriesForCar = useMemo(() => {
    if (carId === '') return []
    return accessories.filter((a) => a.car.id === carId)
  }, [accessories, carId])

  useEffect(() => {
    const allowed = new Set(accessoriesForCar.map((a) => a.id))
    setSelectedAccessoryIds((prev) => {
      const next = new Set<number>()
      for (const id of prev) {
        if (allowed.has(id)) next.add(id)
      }
      return next
    })
  }, [carId, accessoriesForCar])

  const computedTotal = useMemo(() => {
    let sum = 0
    for (const a of accessoriesForCar) {
      if (selectedAccessoryIds.has(a.id)) sum += a.price
    }
    return sum
  }, [accessoriesForCar, selectedAccessoryIds])

  function toggleAccessory(id: number) {
    setSelectedAccessoryIds((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  function goBack() {
    navigate('..')
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    if (clientId === '' || carId === '') {
      setError('Selecione cliente e carro.')
      return
    }
    if (selectedAccessoryIds.size === 0) {
      setError('Selecione pelo menos um acessorio do carro.')
      return
    }

    setSaving(true)
    try {
      await createOrder({
        clientId,
        carId,
        accessoryIds: [...selectedAccessoryIds],
        status,
      })
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao criar pedido')
    } finally {
      setSaving(false)
    }
  }

  const canSubmit =
    !listsLoading &&
    !listsError &&
    clients.length > 0 &&
    cars.length > 0 &&
    accessoriesForCar.length > 0

  return (
    <div>
      <h2 className="dash-page__heading">Novo pedido</h2>

      <p className="dash-hint">
        A data do pedido e o registro de criação são definidos automaticamente no servidor. O preço total do pedido é a
        soma dos acessórios selecionados (calculada no servidor a partir dos valores cadastrados).
      </p>

      {listsError ? <p className="dash-error">{listsError}</p> : null}
      {error ? <p className="dash-error">{error}</p> : null}

      <form className="dash-user-form" onSubmit={onSubmit}>
        <div className="dash-user-form__grid">
          <label>
            Cliente
            <select
              value={clientId === '' ? '' : String(clientId)}
              onChange={(e) => setClientId(e.target.value ? Number(e.target.value) : '')}
              required
              disabled={listsLoading || clients.length === 0}
            >
              {clients.length === 0 ? (
                <option value="">{listsLoading ? 'Carregando...' : 'Nenhum cliente'}</option>
              ) : (
                clients.map((c) => (
                  <option key={c.id} value={c.id}>
                    #{c.id} {clientLabel(c)}
                  </option>
                ))
              )}
            </select>
          </label>
          <label>
            Carro
            <select
              value={carId === '' ? '' : String(carId)}
              onChange={(e) => setCarId(e.target.value ? Number(e.target.value) : '')}
              required
              disabled={listsLoading || cars.length === 0}
            >
              {cars.length === 0 ? (
                <option value="">{listsLoading ? 'Carregando...' : 'Nenhum carro'}</option>
              ) : (
                cars.map((c) => (
                  <option key={c.id} value={c.id}>
                    #{c.id} {carLabel(c)}
                  </option>
                ))
              )}
            </select>
          </label>
          <label>
            Preco total (soma dos acessórios)
            <input type="text" readOnly value={moneyBr.format(computedTotal)} className="dash-input-readonly" />
          </label>
          <label>
            Status
            <select value={status} onChange={(e) => setStatus(e.target.value)}>
              {ORDER_STATUS_FORM_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
        </div>

        <fieldset className="dash-checkboxes-fieldset">
          <legend className="dash-checkboxes-legend">Acessórios do carro selecionado</legend>
          {listsLoading || carId === '' ? (
            <p className="dash-muted">Carregando...</p>
          ) : accessoriesForCar.length === 0 ? (
            <p className="dash-muted">Nenhum acessório cadastrado para este carro.</p>
          ) : (
            <ul className="dash-checkboxes-list">
              {accessoriesForCar.map((a) => (
                <li key={a.id}>
                  <label className="dash-checkbox-row">
                    <input
                      type="checkbox"
                      checked={selectedAccessoryIds.has(a.id)}
                      onChange={() => toggleAccessory(a.id)}
                    />
                    <span>
                      <span className="dash-checkbox-row__title">
                        #{a.id} {a.name}
                      </span>
                      <span className="dash-checkbox-row__meta">{moneyBr.format(a.price)}</span>
                    </span>
                  </label>
                </li>
              ))}
            </ul>
          )}
        </fieldset>

        <div className="dash-form-actions">
          <button type="submit" className="dash-btn-primary" disabled={saving || !canSubmit}>
            {saving ? 'Salvando...' : 'Salvar'}
          </button>
          <button type="button" className="dash-btn-secondary" onClick={goBack} disabled={saving}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  )
}
