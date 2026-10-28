export const MIN_QUERY_LENGTH = 2

export function createProductsApi(request) {
  return {
    searchProducts(query, { signal } = {}) {
      const q = query.trim()
      return request(`/api/products/search?q=${encodeURIComponent(q)}`, {
        method: 'GET',
        signal,
      })
    },

    createCustomProduct(data) {
      return request('/api/products/custom', {
        method: 'POST',
        body: data,
      })
    },
  }
}
