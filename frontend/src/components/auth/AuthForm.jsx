import { useState } from 'react'
import { login, register } from '../../api/auth.js'
import './auth.css'

function AuthForm({ onLogin }) {
  const [isRegister, setIsRegister] = useState(false)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')

  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()

    if (submitting) return
    setSubmitting(true)
    setMessage('')
    setError('')
    try {
      if (isRegister) {
        await register({ email, password })
        setIsRegister(false)
        setPassword('')
        setMessage('Аккаунт создан. Теперь войдите с вашим паролем.')
      } else {
        const session = await login({ email, password })
        if (typeof session?.accessToken !== 'string' || !session.accessToken) {
          throw new Error('Сервер не вернул токен входа. Попробуйте ещё раз.')
        }
        onLogin(session)
      }
    } catch (err) {
      setError(err.message || 'Не удалось выполнить запрос.')
    } finally {
      setSubmitting(false)
    }
  }

  function switchMode() {
    setError('')
    setIsRegister(!isRegister)
    setPassword('')
    setMessage('')
  }

  return (
    <main className="auth-page">
      <section className="auth-card" aria-labelledby="auth-title">
        <p className="app-name">Трекер калорий</p>

        <h1 id="auth-title">
          {isRegister ? 'Регистрация' : 'Вход'}
        </h1>

        <p className="description">
          Ведите дневник питания и следите за КБЖУ.
        </p>

        <form onSubmit={handleSubmit} aria-busy={submitting}>
          <label htmlFor="email">Электронная почта</label>
          <input
            id="email"
            name="email"
            type="email"
            maxLength={255}
            autoComplete="email"
            placeholder="name@example.com"
            value={email}
            onChange={(event) => {
              setEmail(event.target.value)
              setMessage('')
              setError('')
            }}
            disabled={submitting}
            required
          />

          <label htmlFor="password">Пароль</label>
          <input
            id="password"
            name="password"
            type="password"
            autoComplete={
              isRegister ? 'new-password' : 'current-password'
            }
            value={password}
            onChange={(event) => {
              setPassword(event.target.value)
              setMessage('')
              setError('')
            }}
            minLength={isRegister ? 8 : undefined}
            maxLength={isRegister ? 72 : undefined}
            aria-describedby={isRegister ? 'password-hint' : undefined}
            disabled={submitting}
            required
          />

          {isRegister && (
            <p id="password-hint" className="hint">
              От 8 до 72 символов.
            </p>
          )}

          <button className="primary-button" type="submit" disabled={submitting}>
            {submitting ? 'Подождите…' : isRegister ? 'Зарегистрироваться' : 'Войти'}
          </button>

          {error && <p className="auth-error" role="alert">{error}</p>}

          <p className="message" role="status">
            {message}
          </p>
        </form>

        <button
          className="switch-button"
          type="button"
          onClick={switchMode}
          disabled={submitting}
        >
          {isRegister
            ? 'Уже есть аккаунт? Войти'
            : 'Нет аккаунта? Зарегистрироваться'}
        </button>
      </section>
    </main>
  )
}

export default AuthForm
