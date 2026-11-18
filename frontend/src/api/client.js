// Токен хранится в памяти; вход и выход управляют им через setAccessToken.
let accessToken = null

export function setAccessToken(token) {
  accessToken = token || null
}

export class ApiError extends Error {
  constructor(message, status = 0) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

// body — обычный JS-объект. Возвращает JSON либо null для пустого ответа.
export async function request(path, { method = 'GET', body, signal } = {}) {
  if (!path.startsWith('/api/')) {
    throw new ApiError('Адрес запроса должен начинаться с /api/.')
  }

  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (accessToken) headers.Authorization = `Bearer ${accessToken}`

  try {
    const response = await fetch(path, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      signal,
    })
    const text = await response.text()
    let data = null
    if (text) {
      try {
        data = JSON.parse(text)
      } catch {
        if (response.ok) {
          throw new ApiError('Сервер вернул ответ в неожиданном формате.', response.status)
        }
      }
    }

    if (!response.ok) {
      const fallback = response.status === 401
        ? 'Не удалось авторизоваться. Войдите в аккаунт повторно.'
        : `Не удалось выполнить запрос (код ${response.status}).`
      throw new ApiError(
        typeof data?.message === 'string' && data.message ? data.message : fallback,
        response.status,
      )
    }
    return data
  } catch (error) {
    if (error.name === 'AbortError' || error instanceof ApiError) throw error
    throw new ApiError('Не удалось связаться с сервером. Проверьте подключение и запуск бэкенда.')
  }
}
