import { useState } from 'react'
import DiaryPage from './pages/DiaryPage.jsx'
import AuthForm from './components/auth/AuthForm.jsx'
import { setAccessToken } from './api/client.js'

function App() {
  const [email, setEmail] = useState('')
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  function handleLogin(session) {
    setAccessToken(session.accessToken)
    setEmail(session.email)
    setIsLoggedIn(true)
  }

  function handleLogout() {
    setAccessToken(null)
    setIsLoggedIn(false)
  }

  if (!isLoggedIn) return <AuthForm onLogin={handleLogin} />

  return <DiaryPage key={email} email={email} onLogout={handleLogout} />
}

export default App
