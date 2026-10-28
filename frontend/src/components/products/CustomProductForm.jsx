import { useState } from 'react'
import './products.css'

// Ограничения совпадают с CreateCustomProductRequest на бэкенде.
const FIELDS = [
  { key: 'kcal', label: 'Калории, ккал на 100 г', max: 900 },
  { key: 'protein', label: 'Белки, г на 100 г', max: 100 },
  { key: 'fat', label: 'Жиры, г на 100 г', max: 100 },
  { key: 'carbs', label: 'Углеводы, г на 100 г', max: 100 },
]

// Принимает число от 0 до max с точностью до двух знаков; допускает запятую.
function parseNumber(raw, max) {
  const text = raw.trim().replace(',', '.')
  if (text === '') return { error: 'Обязательное поле.' }
  if (!/^\d+(\.\d{1,2})?$/.test(text)) {
    return { error: 'Введите число не менее 0, максимум два знака после запятой.' }
  }
  const value = Number(text)
  if (value > max) return { error: `Не более ${max}.` }
  return { value }
}

const EMPTY = { name: '', kcal: '', protein: '', fat: '', carbs: '' }

// Создание своего продукта.
// Props:
//   onCreated(product) — объект, возвращённый сервером (в дневник не добавляется)
//   createProduct(data) -> Promise<Product> — из createProductsApi(request)
export default function CustomProductForm({ onCreated, createProduct }) {
  const [values, setValues] = useState(EMPTY)
  const [errors, setErrors] = useState({})
  const [submitError, setSubmitError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  function setField(key, value) {
    setValues((v) => ({ ...v, [key]: value }))
    setErrors((e) => ({ ...e, [key]: undefined }))
    setSubmitError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    if (submitting) return

    const nextErrors = {}
    const name = values.name.trim()
    if (!name) nextErrors.name = 'Введите название.'
    else if (name.length > 255) nextErrors.name = 'Не более 255 символов.'

    const parsed = {}
    for (const { key, max } of FIELDS) {
      const r = parseNumber(values[key], max)
      if (r.error) nextErrors[key] = r.error
      else parsed[key] = r.value
    }
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return

    setSubmitting(true)
    setSubmitError('')
    try {
      const product = await createProduct({
        name,
        kcalPer100g: parsed.kcal,
        proteinPer100g: parsed.protein,
        fatPer100g: parsed.fat,
        carbsPer100g: parsed.carbs,
      })
      setValues(EMPTY)
      onCreated(product)
    } catch (err) {
      setSubmitError(err?.message || 'Не удалось создать продукт.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="products-card" aria-labelledby="products-custom-title">
      <h2 id="products-custom-title" className="products-title">
        Свой продукт
      </h2>

      <form className="products-form" onSubmit={handleSubmit} noValidate>
        <label className="products-label" htmlFor="products-custom-name">
          Название
        </label>
        <input
          id="products-custom-name"
          className="products-input"
          type="text"
          value={values.name}
          onChange={(e) => setField('name', e.target.value)}
          aria-invalid={Boolean(errors.name)}
        />
        {errors.name && <p className="products-error">{errors.name}</p>}

        {FIELDS.map(({ key, label }) => (
          <div key={key} className="products-field">
            <label className="products-label" htmlFor={`products-custom-${key}`}>
              {label}
            </label>
            <input
              id={`products-custom-${key}`}
              className="products-input"
              type="text"
              inputMode="decimal"
              value={values[key]}
              onChange={(e) => setField(key, e.target.value)}
              aria-invalid={Boolean(errors[key])}
            />
            {errors[key] && <p className="products-error">{errors[key]}</p>}
          </div>
        ))}

        {submitError && (
          <p className="products-error" role="alert">
            {submitError}
          </p>
        )}

        <button className="products-button" type="submit" disabled={submitting}>
          {submitting ? 'Создание…' : 'Создать продукт'}
        </button>
      </form>
    </section>
  )
}
