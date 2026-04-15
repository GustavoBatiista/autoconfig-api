import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { deleteClient, fetchClientById } from '../../api/clientsApi'
import { parseEntityId } from '../../utils/parseEntityId'

export function ClientDeletePage() {
  const navigate = useNavigate()
  const [idInput, setIdInput] = useState('')
  const [preview, setPreview] = useState<{ id: number; name: string; lastName: string; phoneNumber: string } | null>(
    null,
  )
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
      const c = await fetchClientById(id)
      setPreview({ id: c.id, name: c.name, lastName: c.lastName, phoneNumber: c.phoneNumber })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar cliente')
    } finally {
      setLoading(false)
    }
  }

  async function onConfirmDelete() {
    if (preview == null) return
    setError(null)
    setDeleting(true)
    try {
      await deleteClient(preview.id)
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao excluir cliente')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div>
      <h2 className="dash-page__heading">Excluir cliente</h2>
      <p className="dash-hint">Informe o ID da lista, carregue para conferir os dados e confirme a exclusao.</p>

      {error ? <p className="dash-error">{error}</p> : null}

      <div className="dash-user-form dash-form-id-block">
        <div className="dash-user-form__grid dash-form-id-row">
          <label>
            ID do cliente
            <input
              value={idInput}
              onChange={(e) => setIdInput(e.target.value)}
              inputMode="numeric"
              placeholder="Ex.: 3"
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
          <p className="dash-delete-preview__title">Cliente a excluir</p>
          <p className="dash-delete-preview__line">
            <strong>ID:</strong> {preview.id}
          </p>
          <p className="dash-delete-preview__line">
            {preview.name} {preview.lastName} — {preview.phoneNumber}
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
