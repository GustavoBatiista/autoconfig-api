import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { deleteOrder, fetchOrderById } from '../../api/ordersApi'
import {
  orderStatusShortLabelPt,
} from '../../domain/orderStatus'
import { parseEntityId } from '../../utils/parseEntityId'

const moneyBr = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

export function OrderDeletePage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const idFromQuery = searchParams.get('id')

  const [idInput, setIdInput] = useState(() => idFromQuery ?? '')
  const [preview, setPreview] = useState<Awaited<ReturnType<typeof fetchOrderById>> | null>(null)
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
      const o = await fetchOrderById(id)
      setPreview(o)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar pedido')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!idFromQuery) return
    const id = parseEntityId(idFromQuery)
    if (id == null) return
    let cancelled = false
    setLoading(true)
    setError(null)
    void fetchOrderById(id)
      .then((o) => {
        if (!cancelled) {
          setIdInput(String(id))
          setPreview(o)
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Erro ao carregar pedido')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [idFromQuery])

  async function onConfirmDelete() {
    if (preview == null) return
    setError(null)
    setDeleting(true)
    try {
      await deleteOrder(preview.id)
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao excluir pedido')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div>
      <h2 className="dash-page__heading">Excluir pedido</h2>
      <p className="dash-hint">
        Informe o ID do pedido, carregue para conferir e confirme. Vendedores so podem excluir pedidos que criaram (o
        servidor valida).
      </p>

      {error ? <p className="dash-error">{error}</p> : null}

      <div className="dash-user-form dash-form-id-block">
        <div className="dash-user-form__grid dash-form-id-row">
          <label>
            ID do pedido
            <input
              value={idInput}
              onChange={(e) => setIdInput(e.target.value)}
              inputMode="numeric"
              placeholder="Ex.: 12"
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
          <p className="dash-delete-preview__title">Pedido a excluir</p>
          <p className="dash-delete-preview__line">
            <strong>ID:</strong> {preview.id}
          </p>
          <p className="dash-delete-preview__line">
            <strong>Cliente:</strong> {preview.client.name} {preview.client.lastName}
          </p>
          <p className="dash-delete-preview__line">
            <strong>Veículo:</strong> {preview.car.brand} {preview.car.model} {preview.car.version}
          </p>
          <p className="dash-delete-preview__line">
            <strong>Total:</strong> {moneyBr.format(preview.totalPrice)}
          </p>
          <p className="dash-delete-preview__line">
            <strong>Status:</strong> {orderStatusShortLabelPt(preview.status)}
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
