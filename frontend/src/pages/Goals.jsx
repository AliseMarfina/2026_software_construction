import { useState } from 'react'
import { validGoals, macroGoals } from './goals.js'
export default function Goals({ email, goals, onSaved }) {
  const [draft, setDraft] = useState(goals)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  function submit(e) {
    e.preventDefault()
    if (!validGoals(draft)) { setError('Калории: от 1 до 10000. Сумма процентов БЖУ должна быть равна 100.'); return }
    try { localStorage.setItem(`ct_goals_${email}`, JSON.stringify(draft)); onSaved(draft); setError(''); setMessage('Цели сохранены.') }
    catch { setError('Браузер не разрешил сохранить цели.'); setMessage('') }
  }
  const targets = macroGoals(goals)
  return <><section className="diary-card"><h2>Дневная цель</h2>
    <form onSubmit={submit}>
      {[['kcal', 'Калорийность, ккал в день', 10000], ['proteinPct', 'Белки, %', 100], ['fatPct', 'Жиры, %', 100], ['carbsPct', 'Углеводы, %', 100]].map(([key, label, max]) => <label key={key}>{label}<input type="number" min={key === 'kcal' ? 1 : 0} max={max} required value={draft[key]} onChange={e => { setDraft({ ...draft, [key]: e.target.value === '' ? '' : Number(e.target.value) }); setMessage('') }} /></label>)}
      <p className="products-muted">Сумма процентов должна составлять 100%.</p>
      <div className="row"><button type="button" onClick={() => setDraft({ ...draft, proteinPct: 30, fatPct: 30, carbsPct: 40 })}>30 / 30 / 40</button><button className="products-button" type="submit">Сохранить цели</button></div>
      {error && <p role="alert">{error}</p>}<p role="status">{message}</p>
    </form>
  </section><section className="diary-card"><h2>Ваши целевые показатели</h2><dl className="diary-totals">{[['Ккал', 'kcal'], ['Белки, г', 'protein'], ['Жиры, г', 'fat'], ['Углеводы, г', 'carbs']].map(([label, key]) => <div key={key}><dt>{label}</dt><dd>{Math.round(targets[key] * 10) / 10}</dd></div>)}</dl></section></>
}
