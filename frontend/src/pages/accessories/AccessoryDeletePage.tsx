import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { deleteAccessory, fetchAccessoryById } from '../../api/accessoriesApi'
import { parseEntityId } from '../../utils/parseEntityId'
import { formatCarDisplay } from './carDisplay'

function formatPriceBrl(price: number): string {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(price)
}

export function AccessoryDeletePage() {
  const navigate = useNavigate()
  const [idInput, setIdInput] = useState('')
  const [preview, setPreview] = useState<{
    id: number
    name: string
    description: string
    price: number
    carLine: string
  } | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [deleting, setDeleting] = useState(false)

  function goBack() {
    navigate('..')
  }

  async function onLoadById() {
    setError(null)
    setPreview(null)
    const id = parseEntityId(idInput)
    if (id == null) {
      setError('Informe um ID valido.')
      return
    }
    setLoading(true)
    try {
      const a = await fetchAccessoryById(id)
      setPreview({
        id: a.id,
        name: a.name,
        description: a.description,
        price: a.price,
        carLine: formatCarDisplay(a.car.brand, a.car.model, a.car.version),
      })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar acessorio')
    } finally {
      setLoading(false)
    }
  }

  async function onConfirmDelete() {
    if (preview == null) return
    setError(null)
    setDeleting(true)
    try {
      await deleteAccessory(preview.id)
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao excluir acessorio')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div>
      <h2 className="dash-page__heading">Excluir acessorio</h2>
      <p className="dash-hint">Informe o ID da tabela, carregue para conferir e confirme a exclusao.</p>

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
              disabled={loading || deleting}
            />
          </label>
        </div>
        <div className="dash-form-actions">
          <button type="button" className="dash-btn-secondary" onClick={onLoadById} disabled={loading || deleting}>
            {loading ? 'Carregando...' : 'Carregar'}
          </button>
        </div>
      </div>

      {preview ? (
        <div className="dash-delete-preview">
          <p className="dash-delete-preview__title">Acessorio a excluir</p>
          <p className="dash-delete-preview__line">
            <strong>ID:</strong> {preview.id}
          </p>
          <p className="dash-delete-preview__line">
            <strong>Nome:</strong> {preview.name}
          </p>
          <p className="dash-delete-preview__line">
            <strong>Preco:</strong> {formatPriceBrl(preview.price)}
          </p>
          <p className="dash-delete-preview__line">
            <strong>Carro:</strong> {preview.carLine}
          </p>
          <p className="dash-delete-preview__desc">{preview.description}</p>
        </div>
      ) : null}

      <div className="dash-form-actions">
        <button
          type="button"
          className="dash-btn-danger"
          onClick={onConfirmDelete}
          disabled={preview == null || deleting}
        >
          {deleting ? 'Excluindo...' : 'Excluir permanentemente'}
        </button>
        <button type="button" className="dash-btn-secondary" onClick={goBack} disabled={deleting}>
          Cancelar
        </button>
      </div>
    </div>
  )
}
