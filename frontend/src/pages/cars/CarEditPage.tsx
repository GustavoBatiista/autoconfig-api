import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchCarById, updateCar } from '../../api/carsApi'
import { parseEntityId } from '../../utils/parseEntityId'

export function CarEditPage() {
  const navigate = useNavigate()
  const [idInput, setIdInput] = useState('')
  const [brand, setBrand] = useState('')
  const [model, setModel] = useState('')
  const [version, setVersion] = useState('')
  const [loadedId, setLoadedId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)

  function goBack() {
    navigate('..')
  }

  async function onLoadById() {
    setError(null)
    const id = parseEntityId(idInput)
    if (id == null) {
      setError('Informe um ID valido.')
      return
    }
    setLoading(true)
    try {
      const c = await fetchCarById(id)
      setLoadedId(c.id)
      setBrand(c.brand)
      setModel(c.model)
      setVersion(c.version)
    } catch (err) {
      setLoadedId(null)
      setError(err instanceof Error ? err.message : 'Erro ao carregar carro')
    } finally {
      setLoading(false)
    }
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (loadedId == null) {
      setError('Carregue um carro pelo ID antes de salvar.')
      return
    }
    setError(null)
    setSaving(true)
    try {
      await updateCar(loadedId, {
        brand: brand.trim(),
        model: model.trim(),
        version: version.trim(),
      })
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao atualizar carro')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <h2 className="dash-page__heading">Alterar carro</h2>
      <p className="dash-hint">Use o ID da tabela de carros, carregue os dados e edite o formulario.</p>

      {error ? <p className="dash-error">{error}</p> : null}

      <div className="dash-user-form dash-form-id-block">
        <div className="dash-user-form__grid dash-form-id-row">
          <label>
            ID do carro
            <input
              value={idInput}
              onChange={(e) => setIdInput(e.target.value)}
              inputMode="numeric"
              placeholder="Ex.: 2"
              disabled={loading || saving}
            />
          </label>
        </div>
        <div className="dash-form-actions">
          <button type="button" className="dash-btn-secondary" onClick={onLoadById} disabled={loading || saving}>
            {loading ? 'Carregando...' : 'Carregar'}
          </button>
        </div>
      </div>

      <form className="dash-user-form" onSubmit={onSubmit}>
        <div className="dash-user-form__grid">
          <label>
            Marca
            <input
              value={brand}
              onChange={(e) => setBrand(e.target.value)}
              required
              minLength={3}
              maxLength={50}
              disabled={loadedId == null || saving}
            />
          </label>
          <label>
            Modelo
            <input
              value={model}
              onChange={(e) => setModel(e.target.value)}
              required
              minLength={3}
              maxLength={50}
              disabled={loadedId == null || saving}
            />
          </label>
          <label>
            Versao
            <input
              value={version}
              onChange={(e) => setVersion(e.target.value)}
              required
              minLength={3}
              maxLength={50}
              disabled={loadedId == null || saving}
            />
          </label>
        </div>
        <div className="dash-form-actions">
          <button type="submit" className="dash-btn-primary" disabled={saving || loadedId == null}>
            {saving ? 'Salvando...' : 'Salvar alteracoes'}
          </button>
          <button type="button" className="dash-btn-secondary" onClick={goBack} disabled={saving}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  )
}
