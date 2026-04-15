import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchClientById, updateClient } from '../../api/clientsApi'
import { parseEntityId } from '../../utils/parseEntityId'

export function ClientEditPage() {
  const navigate = useNavigate()
  const [idInput, setIdInput] = useState('')
  const [name, setName] = useState('')
  const [lastName, setLastName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
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
      const c = await fetchClientById(id)
      setLoadedId(c.id)
      setName(c.name)
      setLastName(c.lastName)
      setPhoneNumber(c.phoneNumber)
    } catch (err) {
      setLoadedId(null)
      setError(err instanceof Error ? err.message : 'Erro ao carregar cliente')
    } finally {
      setLoading(false)
    }
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (loadedId == null) {
      setError('Carregue um cliente pelo ID antes de salvar.')
      return
    }
    setError(null)
    setSaving(true)
    try {
      const digits = phoneNumber.replace(/\D/g, '')
      if (digits.length !== 11) {
        setError('Telefone deve ter 11 digitos.')
        setSaving(false)
        return
      }
      await updateClient(loadedId, {
        name: name.trim(),
        lastName: lastName.trim(),
        phoneNumber: digits,
      })
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao atualizar cliente')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <h2 className="dash-page__heading">Alterar cliente</h2>
      <p className="dash-hint">Use o ID da lista de clientes, carregue os dados e edite o formulario.</p>

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
            Sobrenome
            <input
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required
              minLength={3}
              maxLength={50}
              disabled={loadedId == null || saving}
            />
          </label>
          <label>
            Telefone (11 digitos)
            <input
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              required
              inputMode="numeric"
              autoComplete="tel"
              placeholder="11999999999"
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
