import { useState } from 'react'
import './products.css'

function parseGrams(raw) {
  const text = raw.trim().replace(',', '.')
  if (text === '') return { error: 'Введите граммовку.' }
  if (!/^\d+(\.\d{1,2})?$/.test(text)) {
    return { error: 'Введите число, максимум два знака после запятой.' }
  }
  const value = Number(text)
  if (value <= 0) return { error: 'Граммовка должна быть больше 0.' }
  if (value > 10000) return { error: 'Не более 10000 г.' }
  return { value }
}

export default function AddEntryForm({ product, onAdded, addEntry }) {
  const [grams, setGrams] = useState('')
  const [fieldError, setFieldError] = useState('')
  const [submitError, setSubmitError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (!product) {
    return (
      <section className="products-card">
        <h2 className="products-title">Добавить в дневник</h2>
        <p className="products-muted">Сначала выберите или создайте продукт.</p>
      </section>
    )
  }

  async function handleSubmit(event) {
    event.preventDefault()
    if (submitting) return

    const parsed = parseGrams(grams)
    if (parsed.error) {
      setFieldError(parsed.error)
      return
    }
    setFieldError('')
    setSubmitError('')
    setSubmitting(true)
    try {
      await addEntry({ productId: product.id, grams: parsed.value })
    } catch (err) {
      setSubmitError(err?.message || 'Не удалось добавить запись.')
      setSubmitting(false)
      return
    }
    setSubmitting(false)
    setGrams('')
    onAdded()
  }

  return (
    <section className="products-card" aria-labelledby="products-add-title">
      <h2 id="products-add-title" className="products-title">
        Добавить в дневник
      </h2>
      <p className="products-item-name">{product.name}</p>

      <form className="products-form" onSubmit={handleSubmit} noValidate>
        <label className="products-label" htmlFor="products-add-grams">
          Граммы
        </label>
        <input
          id="products-add-grams"
          className="products-input"
          type="text"
          inputMode="decimal"
          value={grams}
          onChange={(e) => {
            setGrams(e.target.value)
            setFieldError('')
            setSubmitError('')
          }}
          aria-invalid={Boolean(fieldError)}
        />
        {fieldError && <p className="products-error">{fieldError}</p>}
        {submitError && (
          <p className="products-error" role="alert">
            {submitError}
          </p>
        )}
        <button className="products-button" type="submit" disabled={submitting}>
          {submitting ? 'Добавление…' : 'Добавить'}
        </button>
      </form>
    </section>
  )
}
