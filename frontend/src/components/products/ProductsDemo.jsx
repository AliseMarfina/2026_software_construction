import { useState } from 'react'
import ProductSearch from './ProductSearch.jsx'
import CustomProductForm from './CustomProductForm.jsx'
import AddEntryForm from './AddEntryForm.jsx'
import './products.css'

const delay = (ms) => new Promise((r) => setTimeout(r, ms))

const MOCK = [
  { id: 1, source: 'openfoodfacts', barcode: '111', name: 'Творог 5%', kcalPer100g: 121, proteinPer100g: 17, fatPer100g: 5, carbsPer100g: 2 },
  { id: 2, source: 'openfoodfacts', barcode: '222', name: 'Творожная масса', kcalPer100g: 250, proteinPer100g: 8, fatPer100g: 12, carbsPer100g: 28 },
]

const mockSearch = async (q) => {
  await delay(400)
  if (q.toLowerCase() === 'ошибка') throw new Error('Тестовая ошибка поиска')
  return MOCK.filter((p) => p.name.toLowerCase().includes(q.toLowerCase()))
}
const mockCreate = async (data) => {
  await delay(400)
  return { id: 100, source: 'custom', barcode: null, ...data }
}
const mockAddEntry = async ({ grams }) => {
  await delay(400)
  if (grams === 13) throw new Error('Тестовая ошибка добавления (13 г)')
}

export default function ProductsDemo() {
  const [selected, setSelected] = useState(null)
  const [added, setAdded] = useState(0)

  return (
    <div>
      <p className="products-demo-banner">
        Демо на тестовых данных. Запись НЕ сохраняется на сервере. Подсказки: поиск «тво» —
        результаты, «ошибка» — ошибка, «zzz» — пусто; 13 г — ошибка добавления.
      </p>
      <ProductSearch onSelect={setSelected} searchProducts={mockSearch} />
      <CustomProductForm onCreated={setSelected} createProduct={mockCreate} />
      <AddEntryForm
        product={selected}
        onAdded={() => setAdded((n) => n + 1)}
        addEntry={mockAddEntry}
      />
      <p className="products-muted">Вызовов onAdded: {added}</p>
    </div>
  )
}
