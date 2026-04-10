import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createClient } from '../../api/clientsApi'

export function ClientCreatePage() {
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [lastName, setLastName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
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
      const digits = phoneNumber.replace(/\D/g, '')
      if (digits.length !== 11) {
        setError('Telefone deve ter 11 digitos.')
        setSaving(false)
        return
      }
      await createClient({
        name: name.trim(),
        lastName: lastName.trim(),
        phoneNumber: digits,
      })
      navigate('..')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao criar cliente')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <h2 className="dash-page__heading">Novo cliente</h2>

      {error ? <p className="dash-error">{error}</p> : null}

      <form className="dash-user-form" onSubmit={onSubmit}>
        <div className="dash-user-form__grid">
          <label>
            Nome
            <input value={name} onChange={(e) => setName(e.target.value)} required minLength={3} maxLength={50} />
          </label>
          <label>
            Sobrenome
            <input
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required
              minLength={3}
              maxLength={50}
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
