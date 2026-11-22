/**
 * Safe mathematical expression evaluator for Universal Search.
 * Supports +, -, *, /, decimals, and Arabic/unicode multiply/divide symbols.
 */
export function evaluateSimpleMath(raw: string): string | null {
  const expr = raw
    .replace(/×/g, '*')
    .replace(/[xX]/g, '*')
    .replace(/÷/g, '/')
    .replace(/\s+/g, '');

  if (!expr || !/^[\d+\-*/.]+$/.test(expr)) return null;
  if (!/[+\-*/]/.test(expr)) return null;

  try {
    const tokens = expr.match(/\d+(\.\d+)?|[+\-*/]/g);
    if (!tokens || tokens.length < 3) return null;

    const list: (number | string)[] = tokens.map((t) =>
      /^[+\-*/]$/.test(t) ? t : Number(t)
    );
    if (typeof list[0] !== 'number' || Number.isNaN(list[0])) return null;

    // Multiply / Divide pass
    for (let i = 1; i < list.length - 1; ) {
      const op = list[i];
      if (op === '*' || op === '/') {
        const a = list[i - 1] as number;
        const b = list[i + 1] as number;
        if (typeof b !== 'number' || Number.isNaN(b)) return null;
        if (op === '/' && b === 0) return null;
        const res = op === '*' ? a * b : a / b;
        list.splice(i - 1, 3, res);
      } else {
        i += 2;
      }
    }

    // Add / Subtract pass
    let acc = list[0] as number;
    for (let i = 1; i < list.length - 1; i += 2) {
      const op = list[i];
      const b = list[i + 1] as number;
      if (typeof b !== 'number' || Number.isNaN(b)) return null;
      acc = op === '+' ? acc + b : acc - b;
    }

    if (!Number.isFinite(acc)) return null;
    if (Number.isInteger(acc)) return acc.toString();
    return Number(acc.toFixed(4)).toString();
  } catch {
    return null;
  }
}
