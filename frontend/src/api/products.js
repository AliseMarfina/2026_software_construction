// API продуктов (Маша).
//
// Общий клиент api/client.js делает Матвей: он добавляет заголовок
// Authorization: Bearer <accessToken> и разбирает ошибки. Пока его нет,
// функции создаются через фабрику и получают request снаружи — отдельную
// систему авторизации здесь не заводим.
//
// Ожидаемый контракт request (согласовать с Матвеем):
//   request(path, { method, body, signal }) -> Promise<распарсенный JSON>
//   при ошибке бросает Error с понятным message.

export const MIN_QUERY_LENGTH = 2

export function createProductsApi(request) {
  return {
    // GET /api/products/search?q=...
    searchProducts(query, { signal } = {}) {
      const q = query.trim()
      return request(`/api/products/search?q=${encodeURIComponent(q)}`, {
        method: 'GET',
        signal,
      })
    },

    // POST /api/products/custom
    // data: { name, kcalPer100g, proteinPer100g, fatPer100g, carbsPer100g }
    // Возвращает полный объект продукта: id, source, barcode, name, ...
    createCustomProduct(data) {
      return request('/api/products/custom', {
        method: 'POST',
        body: data,
      })
    },
  }
}
