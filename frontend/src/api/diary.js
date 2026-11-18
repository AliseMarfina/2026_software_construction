import { request } from './client.js'

export function getDay(date, { signal } = {}) {
  return request(`/api/diary/day?date=${encodeURIComponent(date)}`, { signal })
}

export function addEntry({ productId, grams, mealType, eatenAt = new Date().toISOString() }) {
  return request('/api/diary', { method: 'POST', body: { productId, grams, mealType, eatenAt } })
}

export function deleteEntry(id) {
  return request(`/api/diary/${encodeURIComponent(id)}`, { method: 'DELETE' })
}
