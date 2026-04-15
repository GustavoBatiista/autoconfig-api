import { type FormEvent, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchAccessoryById, updateAccessory } from '../../api/accessoriesApi'
import { fetchCarsPage, type CarResponse } from '../../api/carsApi'
import { parseEntityId } from '../../utils/parseEntityId'
import { formatCarDisplay } from './carDisplay'

export function AccessoryEditPage() {
  const navigate = useNavigate()
  const [idInput, setIdInput] = useState('')
  const [cars, setCars] = useState<CarResponse[]>([])
  const [carsLoading, setCarsLoading] = useState(true)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [price, setPrice] = useState('')
  const [carId, setCarId] = useState<number | ''>('')
  const [loadedId, setLoadedId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      try {
        const page = await fetchCarsPage(0, 200)
        if (!cancelled) setCars(page.content)
      } catch {
        if (!cancelled) setError('Nao foi possivel carregar a lista de carros.')
      } finally {
        if (!cancelled) setCarsLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

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
      const a = await fetchAccessoryById(id)
      setLoadedId(a.id)
      setName(a.name)
      setDescription(a.description)
      setPrice(String(a.price))
      setCarId(a.car.id)
    } catch (err) {
      setLoadedId(null)
      setError(err instanceof Error ? err.message : 'Erro ao carregar acessorio')
    } finally {
      setLoading(false)
    }
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (loadedId == null) {
      setError('Carregue um acessorio pelo ID antes de salvar.')
      return
    }
    setError(null)
    const p = Number.parseFloat(price.replace(',', '.'))
    if (Number.isNaN(p) || p <= 0) {
      setError('Preco invalido.')
      return
    }
    if (carId === '') {
      setError('Selecione um carro.')
      return
    }
    setSaving(true)
    try {
      await updateAccessory(loadedId, {
        name: name.trim(),
        description: description.trim(),
        price: p,
        carId,
      })
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao atualizar acessorio')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <h2 className="dash-page__heading">Alterar acessorio</h2>
      <p className="dash-hint">Use o ID da tabela de acessorios, carregue os dados e edite o formulario.</p>

      {error ? <p className="dash-error">{error}</p> : null}

      <div className="dash-user-form dash-form-id-block">
        <div className="dash-user-form__grid dash-form-id-row">
          <label>
            ID do acessorio
            <input
              value={idInput}
              onChange={(e) => setIdInput(e.target.value)}
              inputMode="numeric"
              placeholder="Ex.: 1"
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
            Nome
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              minLength={3}
              maxLength={50}
              disabled={loadedId == null || saving}
            />
          </label>
          <label>
            Descricao
            <input
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
              minLength={3}
              maxLength={50}
              disabled={loadedId == null || saving}
            />
          </label>
          <label>
            Preco
            <input
              type="text"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              required
              placeholder="99.90"
              disabled={loadedId == null || saving}
            />
          </label>
          <label>
            Carro
            <select
              value={carId === '' ? '' : String(carId)}
              onChange={(e) => setCarId(e.target.value ? Number(e.target.value) : '')}
              required
              disabled={loadedId == null || saving || carsLoading || cars.length === 0}
            >
              {cars.length === 0 ? (
                <option value="">{carsLoading ? 'Carregando...' : 'Nenhum carro'}</option>
              ) : (
                cars.map((c) => (
                  <option key={c.id} value={c.id}>
                    #{c.id} {formatCarDisplay(c.brand, c.model, c.version)}
                  </option>
                ))
              )}
            </select>
          </label>
        </div>
        <div className="dash-form-actions">
          <button
            type="submit"
            className="dash-btn-primary"
            disabled={saving || loadedId == null || cars.length === 0}
          >
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
