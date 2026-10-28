import { useEffect, useState } from 'react'
import { request } from '../api/client.js'
import { createProductsApi } from '../api/products.js'
import { getDay, addEntry, deleteEntry } from '../api/diary.js'
import ProductSearch from '../components/products/ProductSearch.jsx'
import CustomProductForm from '../components/products/CustomProductForm.jsx'
import AddEntryForm from '../components/products/AddEntryForm.jsx'
import './diary.css'

const products = createProductsApi(request)
const format = new Intl.NumberFormat('ru-RU', { maximumFractionDigits: 2 })
function today() {
  // Совпадает с app.timezone бэкенда по умолчанию.
  return new Intl.DateTimeFormat('en-CA', { timeZone: 'Europe/Moscow', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date())
}

function DayEntries({ date, onChanged }) {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [deleting, setDeleting] = useState(null)
  const [deleteError, setDeleteError] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    getDay(date, { signal: controller.signal })
      .then(result => { if (!controller.signal.aborted) setData(result) })
      .catch(err => { if (!controller.signal.aborted) setError(err.message) })
    return () => controller.abort()
  }, [date])

  async function remove(id) {
    if (deleting !== null) return
    setDeleting(id)
    setDeleteError('')
    try {
      await deleteEntry(id)
      onChanged()
    } catch (err) {
      setDeleteError(err.message)
      setDeleting(null)
    }
  }

  if (error) return <div role="alert"><p>{error}</p><button onClick={onChanged}>Повторить загрузку</button></div>
  if (!data) return <p role="status">Загрузка дневника…</p>

  return <>
    <dl className="diary-totals">
      {[["Калории, ккал", data.totalKcal], ["Белки, г", data.totalProtein], ["Жиры, г", data.totalFat], ["Углеводы, г", data.totalCarbs]].map(([label, value]) =>
        <div key={label}><dt>{label}</dt><dd>{format.format(value)}</dd></div>
      )}
    </dl>
    {deleteError && <p role="alert">{deleteError}</p>}
    {data.entries.length === 0 ? <p>За этот день записей пока нет.</p> :
      <ul className="diary-entries">{data.entries.map(entry =>
        <li key={entry.id}>
          <div><strong>{entry.productName}</strong><p>{format.format(entry.grams)} г · {format.format(entry.kcal)} ккал · Б {format.format(entry.protein)} · Ж {format.format(entry.fat)} · У {format.format(entry.carbs)}</p></div>
          <button disabled={deleting !== null} onClick={() => remove(entry.id)} aria-label={`Удалить ${entry.productName}`}>
            {deleting === entry.id ? 'Удаление…' : 'Удалить'}
          </button>
        </li>
      )}</ul>
    }
  </>
}

export default function DiaryPage({ onLogout }) {
  const [date, setDate] = useState(today)
  const [revision, setRevision] = useState(0)
  const [selectedProduct, setSelectedProduct] = useState(null)
  const [notice, setNotice] = useState('')
  function refresh() { setRevision(value => value + 1) }
  function onAdded() {
    setDate(today())
    refresh()
    setNotice('Запись добавлена. Дневник обновляется.')
  }

  return <main className="diary-page">
    <header className="diary-header"><h1>Дневник питания</h1><button onClick={onLogout}>Выйти</button></header>
    <section className="diary-card" aria-labelledby="day-title">
      <h2 id="day-title">Сводка за день</h2>
      <label htmlFor="diary-date">Дата </label>
      <input id="diary-date" type="date" value={date} onChange={event => { if (event.target.value) { setDate(event.target.value); setNotice('') } }} />
      <p role="status">{notice}</p>
      <DayEntries key={`${date}:${revision}`} date={date} onChanged={refresh} />
    </section>
    <div className="diary-columns">
      <div>
        <ProductSearch onSelect={setSelectedProduct} searchProducts={products.searchProducts} />
        <CustomProductForm onCreated={setSelectedProduct} createProduct={products.createCustomProduct} />
      </div>
      <div>
        <p>Новые записи добавляются за текущий день сервера. После добавления откроется сегодняшний дневник.</p>
        <AddEntryForm key={selectedProduct?.id ?? 'empty'} product={selectedProduct} addEntry={addEntry} onAdded={onAdded} />
      </div>
    </div>
  </main>
}
