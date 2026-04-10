import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createCar } from '../../api/carsApi'

export function CarCreatePage() {
  const navigate = useNavigate()
  const [brand, setBrand] = useState('')
  const [model, setModel] = useState('')
  const [version, setVersion] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  function goBack() {
    navigate('..')
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setSaving(true)
    try {
      await createCar({
        brand: brand.trim(),
        model: model.trim(),
        version: version.trim(),
      })
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao criar carro')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <h2 className="dash-page__heading">Novo carro</h2>

      {error ? <p className="dash-error">{error}</p> : null}

      <form className="dash-user-form" onSubmit={onSubmit}>
        <div className="dash-user-form__grid">
          <label>
            Marca
            <input value={brand} onChange={(e) => setBrand(e.target.value)} required minLength={3} maxLength={50} />
          </label>
          <label>
            Modelo
            <input value={model} onChange={(e) => setModel(e.target.value)} required minLength={3} maxLength={50} />
          </label>
          <label>
            Versao
            <input
              value={version}
              onChange={(e) => setVersion(e.target.value)}
              required
              minLength={3}
              maxLength={50}
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
