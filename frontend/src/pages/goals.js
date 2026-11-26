export const defaultGoals = { kcal: 1400, proteinPct: 30, fatPct: 30, carbsPct: 40 }
export function readGoals(email) {
  try {
    const value = JSON.parse(localStorage.getItem(`ct_goals_${email}`))
    return validGoals(value) ? value : defaultGoals
  } catch { return defaultGoals }
}
export function validGoals(g) {
  return g && Number.isFinite(g.kcal) && g.kcal > 0 && g.kcal <= 10000 &&
    ['proteinPct', 'fatPct', 'carbsPct'].every(k => Number.isFinite(g[k]) && g[k] >= 0 && g[k] <= 100) &&
    Math.abs(g.proteinPct + g.fatPct + g.carbsPct - 100) < 0.00001
}
export function macroGoals(g) { return { kcal: g.kcal, protein: g.kcal * g.proteinPct / 400, fat: g.kcal * g.fatPct / 900, carbs: g.kcal * g.carbsPct / 400 } }
