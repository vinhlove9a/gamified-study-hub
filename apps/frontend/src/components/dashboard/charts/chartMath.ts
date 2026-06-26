/**
 * Pure-SVG charting helpers — the Vue port has no React/Recharts dependency, so
 * these reproduce the pieces Recharts gave us for free: monotone smoothing, area
 * fills and "nice" axis ticks.
 */

export interface Point {
  x: number;
  y: number;
}

/**
 * Monotone cubic interpolation (Fritsch–Carlson) — the same curve Recharts draws
 * for `type="monotone"`. Tangents are clamped so the spline never overshoots the
 * data, which is what keeps these telemetry lines from wobbling between points.
 */
export function monotonePath(pts: Point[]): string {
  const n = pts.length;
  if (n === 0) return '';
  if (n === 1) return `M${pts[0].x},${pts[0].y}`;
  if (n === 2) return `M${pts[0].x},${pts[0].y}L${pts[1].x},${pts[1].y}`;

  const dx: number[] = [];
  const dy: number[] = [];
  const slope: number[] = [];
  for (let i = 0; i < n - 1; i++) {
    dx[i] = pts[i + 1].x - pts[i].x;
    dy[i] = pts[i + 1].y - pts[i].y;
    slope[i] = dy[i] / (dx[i] || 1);
  }

  const tangent: number[] = new Array(n);
  tangent[0] = slope[0];
  tangent[n - 1] = slope[n - 2];
  for (let i = 1; i < n - 1; i++) {
    if (slope[i - 1] * slope[i] <= 0) {
      tangent[i] = 0;
    } else {
      const common = dx[i - 1] + dx[i];
      tangent[i] = (3 * common) / ((common + dx[i]) / slope[i - 1] + (common + dx[i - 1]) / slope[i]);
    }
  }

  let d = `M${pts[0].x},${pts[0].y}`;
  for (let i = 0; i < n - 1; i++) {
    const h = dx[i] / 3;
    const c1x = pts[i].x + h;
    const c1y = pts[i].y + h * tangent[i];
    const c2x = pts[i + 1].x - h;
    const c2y = pts[i + 1].y - h * tangent[i + 1];
    d += `C${c1x.toFixed(2)},${c1y.toFixed(2)} ${c2x.toFixed(2)},${c2y.toFixed(2)} ${pts[i + 1].x.toFixed(2)},${pts[i + 1].y.toFixed(2)}`;
  }
  return d;
}

/** Closes a monotone line down to a baseline `y` to form a fillable area. */
export function areaPath(pts: Point[], baselineY: number): string {
  if (pts.length === 0) return '';
  const line = monotonePath(pts);
  const last = pts[pts.length - 1];
  const first = pts[0];
  return `${line} L${last.x.toFixed(2)},${baselineY} L${first.x.toFixed(2)},${baselineY} Z`;
}

function niceNum(range: number, round: boolean): number {
  const exponent = Math.floor(Math.log10(range));
  const fraction = range / 10 ** exponent;
  let niceFraction: number;
  if (round) {
    if (fraction < 1.5) niceFraction = 1;
    else if (fraction < 3) niceFraction = 2;
    else if (fraction < 7) niceFraction = 5;
    else niceFraction = 10;
  } else if (fraction <= 1) niceFraction = 1;
  else if (fraction <= 2) niceFraction = 2;
  else if (fraction <= 5) niceFraction = 5;
  else niceFraction = 10;
  return niceFraction * 10 ** exponent;
}

/** Axis domain + evenly spaced "nice" tick values, mirroring Recharts' default axis. */
export function niceTicks(min: number, max: number, tickCount = 5): { ticks: number[]; min: number; max: number } {
  if (min === max) {
    return { ticks: [min], min: min - 1, max: max + 1 };
  }
  const range = niceNum(max - min, false);
  const spacing = niceNum(range / (tickCount - 1), true);
  const niceMin = Math.floor(min / spacing) * spacing;
  const niceMax = Math.ceil(max / spacing) * spacing;
  const ticks: number[] = [];
  for (let v = niceMin; v <= niceMax + spacing * 0.5; v += spacing) {
    ticks.push(Number(v.toFixed(6)));
  }
  return { ticks, min: niceMin, max: niceMax };
}

/** Maps a value in [domainMin, domainMax] to a pixel y (inverted: high value → small y). */
export function makeYScale(domainMin: number, domainMax: number, top: number, bottom: number) {
  const span = domainMax - domainMin || 1;
  return (v: number) => bottom - ((v - domainMin) / span) * (bottom - top);
}
