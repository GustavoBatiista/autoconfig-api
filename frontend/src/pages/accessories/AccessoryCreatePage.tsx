import { type FormEvent, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createAccessory } from '../../api/accessoriesApi'
import { fetchCarsPage, type CarResponse } from '../../api/carsApi'

function carLabel(c: CarResponse): string {
  const v = c.version?.trim()
  return `${c.brand} ${c.model} ${v}`.trim()
}

export function AccessoryCreatePage() {
  const navigate = useNavigate()
  const [cars, setCars] = useState<CarResponse[]>([])
  const [carsLoading, setCarsLoading] = useState(true)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [price, setPrice] = useState('')
  const [carId, setCarId] = useState<number | ''>('')
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      try {
        const page = await fetchCarsPage(0, 200)
        if (!cancelled) {
          setCars(page.content)
          if (page.content.length > 0) {
            setCarId((prev) => (prev === '' ? page.content[0].id : prev))
          }
        }
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

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
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
      await createAccessory({
        name: name.trim(),
        description: description.trim(),
        price: p,
        carId,
      })
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao criar acessorio')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <h2 className="dash-page__heading">Novo acessorio</h2>

      {error ? <p className="dash-error">{error}</p> : null}

      <form className="dash-user-form" onSubmit={onSubmit}>
        <div className="dash-user-form__grid">
          <label>
            Nome
            <input value={name} onChange={(e) => setName(e.target.value)} required minLength={3} maxLength={50} />
          </label>
          <label>
            Descricao
            <input
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
              minLength={3}
              maxLength={50}
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
            />
          </label>
          <label>
            Carro
            <select
              value={carId === '' ? '' : String(carId)}
              onChange={(e) => setCarId(e.target.value ? Number(e.target.value) : '')}
              required
              disabled={carsLoading || cars.length === 0}
            >
              {cars.length === 0 ? (
                <option value="">{carsLoading ? 'Carregando...' : 'Nenhum carro'}</option>
              ) : (
                cars.map((c) => (
                  <option key={c.id} value={c.id}>
                    #{c.id} {carLabel(c)}
                  </option>
                ))
              )}
            </select>
          </label>
        </div>
        <div className="dash-form-actions">
          <button type="submit" className="dash-btn-primary" disabled={saving || cars.length === 0}>
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
