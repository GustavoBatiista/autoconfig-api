import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { deleteCar, fetchCarById } from '../../api/carsApi'
import { parseEntityId } from '../../utils/parseEntityId'

export function CarDeletePage() {
  const navigate = useNavigate()
  const [idInput, setIdInput] = useState('')
  const [preview, setPreview] = useState<{
    id: number
    brand: string
    model: string
    version: string
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
      const c = await fetchCarById(id)
      setPreview({ id: c.id, brand: c.brand, model: c.model, version: c.version })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar carro')
    } finally {
      setLoading(false)
    }
  }

  async function onConfirmDelete() {
    if (preview == null) return
    setError(null)
    setDeleting(true)
    try {
      await deleteCar(preview.id)
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao excluir carro')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div>
      <h2 className="dash-page__heading">Excluir carro</h2>
      <p className="dash-hint">Informe o ID da tabela, carregue para conferir e confirme a exclusao.</p>

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
          <p className="dash-delete-preview__title">Carro a excluir</p>
          <p className="dash-delete-preview__line">
            <strong>ID:</strong> {preview.id}
          </p>
          <p className="dash-delete-preview__line">
            {preview.brand} {preview.model} {preview.version}
          </p>
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
