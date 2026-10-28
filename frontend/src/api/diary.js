import { request } from './client.js'

export function getDay(date, { signal } = {}) {
  return request(`/api/diary/day?date=${encodeURIComponent(date)}`, { signal })
}

export function addEntry({ productId, grams }) {
  return request('/api/diary', { method: 'POST', body: { productId, grams } })
}

export function deleteEntry(id) {
  return request(`/api/diary/${encodeURIComponent(id)}`, { method: 'DELETE' })
}
