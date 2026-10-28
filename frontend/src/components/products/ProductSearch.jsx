import { useRef, useState } from 'react'
import { MIN_QUERY_LENGTH } from '../../api/products.js'
import './products.css'

const fmt = (n) => (n === null || n === undefined ? '—' : String(n))

// Поиск и выбор продукта.
// Props:
//   onSelect(product)  — получает полный объект продукта
//   searchProducts(query) -> Promise<Product[]> — из createProductsApi(request)
export default function ProductSearch({ onSelect, searchProducts }) {
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('idle') // idle | loading | done | error
  const [results, setResults] = useState([])
  const [error, setError] = useState('')
  const [validation, setValidation] = useState('')
  const requestId = useRef(0)

  async function handleSubmit(event) {
    event.preventDefault()
    const q = query.trim()
    if (q.length < MIN_QUERY_LENGTH) {
      setValidation(`Введите не менее ${MIN_QUERY_LENGTH} символов.`)
      return
    }
    setValidation('')
    setError('')
    setStatus('loading')
    const id = ++requestId.current // игнорируем устаревшие ответы
    try {
      const found = await searchProducts(q)
      if (id !== requestId.current) return
      setResults(Array.isArray(found) ? found : [])
      setStatus('done')
    } catch (err) {
      if (id !== requestId.current) return
      setError(err?.message || 'Не удалось выполнить поиск.')
      setStatus('error')
    }
  }

  return (
    <section className="products-card" aria-labelledby="products-search-title">
      <h2 id="products-search-title" className="products-title">
        Поиск продукта
      </h2>

      <form className="products-form" onSubmit={handleSubmit} noValidate>
        <label className="products-label" htmlFor="products-search-input">
          Название продукта
        </label>
        <div className="products-row">
          <input
            id="products-search-input"
            className="products-input"
            type="search"
            value={query}
            placeholder="Например, творог"
            onChange={(e) => {
              setQuery(e.target.value)
              setValidation('')
            }}
          />
          <button
            className="products-button"
            type="submit"
            disabled={status === 'loading'}
          >
            {status === 'loading' ? 'Поиск…' : 'Найти'}
          </button>
        </div>
        {validation && (
          <p className="products-error" role="alert">
            {validation}
          </p>
        )}
      </form>

      {status === 'loading' && <p className="products-muted">Загрузка…</p>}
      {status === 'error' && (
        <p className="products-error" role="alert">
          {error}
        </p>
      )}
      {status === 'done' && results.length === 0 && (
        <p className="products-muted">Ничего не найдено. Можно создать свой продукт.</p>
      )}

      {status === 'done' && results.length > 0 && (
        <ul className="products-list">
          {results.map((p) => (
            <li key={p.id} className="products-list-item">
              <div className="products-item-info">
                <span className="products-item-name">{p.name}</span>
                <span className="products-muted">
                  на 100 г: {fmt(p.kcalPer100g)} ккал · Б {fmt(p.proteinPer100g)} · Ж{' '}
                  {fmt(p.fatPer100g)} · У {fmt(p.carbsPer100g)}
                </span>
              </div>
              <button
                className="products-button products-button-secondary"
                type="button"
                onClick={() => onSelect(p)}
              >
                Выбрать
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
