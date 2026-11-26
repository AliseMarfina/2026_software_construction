export const MIN_QUERY_LENGTH = 2

export function createProductsApi(request) {
  const cache = new Map()
  const pending = new Map()
  return {
    searchProducts(query, { signal } = {}) {
      const q = query.trim()
      const key = q.toLocaleLowerCase('ru-RU')
      const cached = cache.get(key)
      if (cached && Date.now() - cached.time < 60000) return Promise.resolve(cached.value)
      if (pending.has(key)) return pending.get(key)
      const result = request(`/api/products/search?q=${encodeURIComponent(q)}`, { method: 'GET', signal })
        .then(value => { cache.set(key, { value, time: Date.now() }); return value })
        .finally(() => pending.delete(key))
      pending.set(key, result)
      return result
    },

    createCustomProduct(data) {
      cache.clear()
      return request('/api/products/custom', {
        method: 'POST',
        body: data,
      })
    },
  }
}
