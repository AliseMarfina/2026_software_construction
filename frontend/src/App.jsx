import { useState } from 'react'
import './App.css'

function App() {
  const [isRegister, setIsRegister] = useState(false)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')

  function handleSubmit(event) {
    event.preventDefault()

    setMessage(
      'Форма заполнена. Отправка на сервер пока не подключена.'
    )
  }

  function switchMode() {
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

        <form onSubmit={handleSubmit}>
          <label htmlFor="email">Электронная почта</label>
          <input
            id="email"
            name="email"
            type="email"
            autoComplete="email"
            placeholder="name@example.com"
            value={email}
            onChange={(event) => {
              setEmail(event.target.value)
              setMessage('')
            }}
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
            }}
            minLength={isRegister ? 8 : undefined}
            aria-describedby={isRegister ? 'password-hint' : undefined}
            required
          />

          {isRegister && (
            <p id="password-hint" className="hint">
              Не менее 8 символов.
            </p>
          )}

          <button className="primary-button" type="submit">
            {isRegister ? 'Зарегистрироваться' : 'Войти'}
          </button>

          <p className="message" role="status">
            {message}
          </p>
        </form>

        <button
          className="switch-button"
          type="button"
          onClick={switchMode}
        >
          {isRegister
            ? 'Уже есть аккаунт? Войти'
            : 'Нет аккаунта? Зарегистрироваться'}
        </button>
      </section>
    </main>
  )
}

export default App