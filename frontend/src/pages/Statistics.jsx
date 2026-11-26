import { useEffect, useState } from 'react'
import { getStats } from '../api/diary.js'
const fmt = n => new Intl.NumberFormat('ru-RU', { maximumFractionDigits: 2 }).format(n ?? 0)
export default function Statistics({ today }) {
  const [from, setFrom] = useState(today)
  const [to, setTo] = useState(today)
  const [period, setPeriod] = useState({ from: today, to: today })
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  useEffect(() => {
    const controller = new AbortController()
    getStats(period.from, period.to, { signal: controller.signal })
      .then(value => { if (!controller.signal.aborted) { setData(value); setLoading(false) } })
      .catch(err => { if (!controller.signal.aborted) { setError(err.message); setLoading(false) } })
    return () => controller.abort()
  }, [period])
  function submit(event) {
    event.preventDefault()
    const days = (Date.parse(to) - Date.parse(from)) / 86400000 + 1
    if (days < 1 || days > 366) { setError('Выберите период от 1 до 366 дней.'); return }
    setError(''); setData(null); setLoading(true); setPeriod({ from, to })
  }
  const days = data?.days ?? []
  const populated = days.filter(day => !day.noData)
  const max = Math.max(100, ...populated.map(day => day.kcal))
  const width = Math.max(320, days.length * 28 + 32)
  return <>
    <section className="diary-card"><form className="period-form" onSubmit={submit}>
      <label>С<input type="date" required value={from} onChange={e => setFrom(e.target.value)} /></label>
      <label>По<input type="date" required value={to} onChange={e => setTo(e.target.value)} /></label>
      <button type="submit" disabled={loading}>Показать</button>
    </form></section>
    {error && <p role="alert">{error}</p>}
    {loading && <p role="status">Загрузка…</p>}
    {data && <><section className="diary-card"><h2>Калории по дням</h2>
      <div className="stats-chart"><svg width={width} height="230" role="img" aria-label="Калории по дням">
        {days.map((day, i) => { const h = day.noData ? 22 : Math.max(2, day.kcal / max * 180); return <g key={day.date}>
          <title>{day.date}: {day.noData ? 'нет данных' : `${fmt(day.kcal)} ккал · Б ${fmt(day.protein)} · Ж ${fmt(day.fat)} · У ${fmt(day.carbs)}`}</title>
          <rect x={30 + i * 28} y={195 - h} width="22" height={h} rx="3" fill={day.noData ? 'none' : 'var(--ac)'} stroke="var(--ac)" strokeDasharray={day.noData ? '3 3' : undefined} />
          {i % Math.ceil(days.length / 10 || 1) === 0 && <text x={30 + i * 28} y="216" fontSize="10" fill="var(--mu)">{day.date.slice(8)}.{day.date.slice(5, 7)}</text>}
        </g> })}
      </svg></div><p className="products-muted stats-legend"><span className="stats-empty-sample" aria-hidden="true" />Дни без записей</p></section>
      <section className="diary-card"><h2>Среднее за дни с данными</h2>
        {populated.length && data.average ? <dl className="diary-totals">{[['Ккал', 'kcal'], ['Белки, г', 'protein'], ['Жиры, г', 'fat'], ['Углеводы, г', 'carbs']].map(([label, key]) => <div key={key}><dt>{label}</dt><dd>{fmt(data.average[key])}</dd></div>)}</dl> : <p>За выбранный период нет данных.</p>}
        <p className="products-muted">Дней с данными: {populated.length} из {days.length}</p>
      </section></>}
  </>
}
