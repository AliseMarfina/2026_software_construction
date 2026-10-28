import { request } from './client.js'

export function register({ email, password }) {
  return request('/api/auth/register', {
    method: 'POST',
    body: { email: email.trim(), password },
  })
}

export function login({ email, password }) {
  return request('/api/auth/login', {
    method: 'POST',
    body: { email: email.trim(), password },
  })
}
