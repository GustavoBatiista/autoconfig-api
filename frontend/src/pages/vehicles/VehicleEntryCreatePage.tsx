import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createVehicleEntry } from '../../api/vehiclesApi'
import { VEHICLE_CONDITION_OPTIONS } from '../../domain/vehicleCondition'

export function VehicleEntryCreatePage() {
  const navigate = useNavigate()
  const [chassis, setChassis] = useState('')
  const [arrivalDate, setArrivalDate] = useState('')
  const [condition, setCondition] = useState<string>(VEHICLE_CONDITION_OPTIONS[0].value)
  const [orderId, setOrderId] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  function goBack() {
    navigate('..')
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    const ch = chassis.trim().toUpperCase()
    if (ch.length !== 17) {
      setError('Chassi deve ter exatamente 17 caracteres.')
      return
    }
    const oid = Number.parseInt(orderId, 10)
    if (Number.isNaN(oid) || oid < 1) {
      setError('ID do pedido invalido.')
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
      await createVehicleEntry({
        chassis: ch,
        arrivalDate: iso,
        condition,
        orderId: oid,
      })
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao registrar veiculo')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <h2 className="dash-page__heading">Nova entrada de veiculo</h2>

      {error ? <p className="dash-error">{error}</p> : null}

      <form className="dash-user-form" onSubmit={onSubmit}>
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
            />
          </label>
          <label>
            Data e hora de chegada
            <input
              type="datetime-local"
              value={arrivalDate}
              onChange={(e) => setArrivalDate(e.target.value)}
              required
            />
          </label>
          <label>
            Condicao
            <select value={condition} onChange={(e) => setCondition(e.target.value)}>
              {VEHICLE_CONDITION_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
          <label>
            ID do pedido
            <input
              type="number"
              min={1}
              value={orderId}
              onChange={(e) => setOrderId(e.target.value)}
              required
            />
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
    </div>
  )
}
