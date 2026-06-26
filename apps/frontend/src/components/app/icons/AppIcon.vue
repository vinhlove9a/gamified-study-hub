<script setup lang="ts">
/**
 * Lightweight inline icon set for the user-facing app shell.
 * Icons are described as primitive shapes (path / circle / rect) instead of an
 * HTML string so we stay clear of `v-html` (flagged by vue3-recommended) while
 * keeping a single, tree-shakeable component. All icons inherit `currentColor`.
 */
type IconShape =
  | { t: 'path'; d: string; fill?: boolean }
  | { t: 'circle'; cx: number; cy: number; r: number; fill?: boolean }
  | { t: 'rect'; x: number; y: number; w: number; h: number; rx?: number };

interface Props {
  name: string;
}

const props = defineProps<Props>();

const icons: Record<string, IconShape[]> = {
  home: [
    { t: 'path', d: 'm3 9.5 9-6.5 9 6.5' },
    { t: 'path', d: 'M5 9.2V20a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V9.2' },
    { t: 'path', d: 'M9.5 21v-6.5h5V21' }
  ],
  'file-text': [
    { t: 'path', d: 'M6 3h8l5 5v12a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z' },
    { t: 'path', d: 'M14 3v5h5' },
    { t: 'path', d: 'M8.5 13h7' },
    { t: 'path', d: 'M8.5 16.5h7' },
    { t: 'path', d: 'M8.5 9.5h2' }
  ],
  folder: [
    {
      t: 'path',
      d: 'M3.5 7.5A1.5 1.5 0 0 1 5 6h3.8a1.5 1.5 0 0 1 1.1.5l1 1.1a1.5 1.5 0 0 0 1.1.5H19a1.5 1.5 0 0 1 1.5 1.5v7.4A1.5 1.5 0 0 1 19 18.5H5a1.5 1.5 0 0 1-1.5-1.5z'
    }
  ],
  compass: [
    { t: 'circle', cx: 12, cy: 12, r: 9 },
    { t: 'path', d: 'M15 9l-1.8 4.2L9 15l1.8-4.2z' }
  ],
  target: [
    { t: 'circle', cx: 12, cy: 12, r: 8.5 },
    { t: 'circle', cx: 12, cy: 12, r: 5 },
    { t: 'circle', cx: 12, cy: 12, r: 1.6, fill: true }
  ],
  route: [
    { t: 'circle', cx: 6, cy: 18, r: 2.2 },
    { t: 'circle', cx: 18, cy: 6, r: 2.2 },
    { t: 'path', d: 'M8 18h7.5a3 3 0 0 0 0-6h-7a3 3 0 0 1 0-6H16' }
  ],
  trophy: [
    { t: 'path', d: 'M7.5 4.5h9v3.5a4.5 4.5 0 0 1-9 0z' },
    { t: 'path', d: 'M7.5 5.5H5v1.5a3 3 0 0 0 3 3' },
    { t: 'path', d: 'M16.5 5.5H19v1.5a3 3 0 0 1-3 3' },
    { t: 'path', d: 'M12 12.5V16' },
    { t: 'path', d: 'M10 16h4l.5 3.5h-5z' }
  ],
  chart: [
    { t: 'path', d: 'M4.5 4v14a.5.5 0 0 0 .5.5h15' },
    { t: 'rect', x: 7, y: 12, w: 2.4, h: 6, rx: 0.5 },
    { t: 'rect', x: 11, y: 9, w: 2.4, h: 9, rx: 0.5 },
    { t: 'rect', x: 15, y: 6, w: 2.4, h: 12, rx: 0.5 }
  ],
  store: [
    { t: 'path', d: 'M4.5 9.5h15l-1-4.5h-13z' },
    { t: 'path', d: 'M5.5 9.5V19a.5.5 0 0 0 .5.5h12a.5.5 0 0 0 .5-.5V9.5' },
    { t: 'path', d: 'M9.5 19.5V15h5v4.5' }
  ],
  badge: [
    { t: 'circle', cx: 12, cy: 9, r: 4.5 },
    { t: 'path', d: 'M9.2 12.7 8 20l4-2.2L16 20l-1.2-7.3' }
  ],
  settings: [
    { t: 'circle', cx: 12, cy: 12, r: 3.2 },
    {
      t: 'path',
      d: 'M12 4v2.3M12 17.7V20M4 12h2.3M17.7 12H20M6 6l1.6 1.6M16.4 16.4 18 18M18 6l-1.6 1.6M7.6 16.4 6 18'
    }
  ],
  help: [
    { t: 'circle', cx: 12, cy: 12, r: 9 },
    { t: 'path', d: 'M9.7 9.3a2.4 2.4 0 0 1 4.3 1.2c0 1.6-2 1.9-2.2 3' },
    { t: 'circle', cx: 12, cy: 16.4, r: 0.6, fill: true }
  ],
  search: [
    { t: 'circle', cx: 11, cy: 11, r: 7 },
    { t: 'path', d: 'm20 20-3.8-3.8' }
  ],
  bell: [
    { t: 'path', d: 'M6 9a6 6 0 0 1 12 0c0 5 2 6 2 6H4s2-1 2-6z' },
    { t: 'path', d: 'M10 20a2 2 0 0 0 4 0' }
  ],
  plus: [{ t: 'path', d: 'M12 5.5v13M5.5 12h13' }],
  upload: [
    { t: 'path', d: 'M5 16.5V19a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5' },
    { t: 'path', d: 'M12 15V4' },
    { t: 'path', d: 'm8 8 4-4 4 4' }
  ],
  bookmark: [{ t: 'path', d: 'M6.5 4.5h11a.5.5 0 0 1 .5.5v15l-6-3.8L6 20V5a.5.5 0 0 1 .5-.5z' }],
  flame: [
    {
      t: 'path',
      d: 'M12 3s4.5 3.5 4.5 8.2a4.5 4.5 0 0 1-9 0c0-1.7.8-2.9 1.6-3.9C10.5 9 12 7.5 12 3z'
    }
  ],
  clock: [
    { t: 'circle', cx: 12, cy: 12, r: 9 },
    { t: 'path', d: 'M12 7.5V12l3 1.8' }
  ],
  'chevron-down': [{ t: 'path', d: 'm6 9.5 6 6 6-6' }],
  'chevron-right': [{ t: 'path', d: 'm9.5 6 6 6-6 6' }],
  menu: [{ t: 'path', d: 'M4 6.5h16M4 12h16M4 17.5h16' }],
  x: [{ t: 'path', d: 'M6.5 6.5 17.5 17.5M17.5 6.5 6.5 17.5' }],
  user: [
    { t: 'circle', cx: 12, cy: 8, r: 4 },
    { t: 'path', d: 'M5 20a7 7 0 0 1 14 0' }
  ],
  logout: [
    { t: 'path', d: 'M9 20H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h3' },
    { t: 'path', d: 'm16 16 4-4-4-4' },
    { t: 'path', d: 'M20 12H9' }
  ],
  sparkles: [
    { t: 'path', d: 'M12 3.5l1.7 4 4 1.7-4 1.7L12 15l-1.7-4-4-1.7 4-1.7z' },
    { t: 'path', d: 'M18.5 14l.9 2 2 .9-2 .9-.9 2-.9-2-2-.9 2-.9z' }
  ],
  check: [{ t: 'path', d: 'm5 12.5 4.5 4.5L19 7.5' }],
  'check-circle': [
    { t: 'circle', cx: 12, cy: 12, r: 9 },
    { t: 'path', d: 'm8.3 12.2 2.4 2.4 4.8-5.2' }
  ],
  star: [
    {
      t: 'path',
      d: 'M12 3.5l2.6 5.3 5.8.8-4.2 4.1 1 5.8L12 16.8l-5.2 2.7 1-5.8L2.6 9.6l5.8-.8z'
    }
  ],
  'book-open': [
    {
      t: 'path',
      d: 'M12 6.5C10.3 5.3 7.8 4.8 5.5 4.8V18c2.3 0 4.8.5 6.5 1.7 1.7-1.2 4.2-1.7 6.5-1.7V4.8c-2.3 0-4.8.5-6.5 1.7z'
    },
    { t: 'path', d: 'M12 6.5v13.2' }
  ],
  calendar: [
    { t: 'rect', x: 4, y: 5.5, w: 16, h: 15, rx: 2 },
    { t: 'path', d: 'M8 3.5v4M16 3.5v4M4 10.5h16' }
  ],
  zap: [{ t: 'path', d: 'M13 3 5 13.5h6L10 21l8-10.5h-6z' }],
  grid: [
    { t: 'rect', x: 4, y: 4, w: 7, h: 7, rx: 1.5 },
    { t: 'rect', x: 13, y: 4, w: 7, h: 7, rx: 1.5 },
    { t: 'rect', x: 4, y: 13, w: 7, h: 7, rx: 1.5 },
    { t: 'rect', x: 13, y: 13, w: 7, h: 7, rx: 1.5 }
  ],
  more: [
    { t: 'circle', cx: 6, cy: 12, r: 1.5, fill: true },
    { t: 'circle', cx: 12, cy: 12, r: 1.5, fill: true },
    { t: 'circle', cx: 18, cy: 12, r: 1.5, fill: true }
  ],
  inbox: [
    { t: 'path', d: 'M4 13 6.5 5.5h11L20 13' },
    { t: 'path', d: 'M4 13v5a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-5h-4.5a2.5 2.5 0 0 1-5 0z' }
  ],
  'arrow-right': [
    { t: 'path', d: 'M5 12h13' },
    { t: 'path', d: 'm13.5 6 6 6-6 6' }
  ],
  play: [{ t: 'path', d: 'M8 5.5v13l11-6.5z', fill: true }],
  gift: [
    { t: 'rect', x: 4, y: 9.5, w: 16, h: 10.5, rx: 1 },
    { t: 'path', d: 'M4 13.5h16' },
    { t: 'path', d: 'M12 9.5V20' },
    {
      t: 'path',
      d: 'M12 9.5C9.5 9.5 8 8.2 8.5 6.8 9 5.5 11 6 12 9.5c1-3.5 3-4 3.5-2.7.5 1.4-1 2.7-3.5 2.7z'
    }
  ],
  crown: [
    { t: 'path', d: 'M4 8l3.5 2.8L12 5l4.5 5.8L20 8l-1.3 9.5H5.3z' },
    { t: 'path', d: 'M5 20.5h14' }
  ],
  eye: [
    { t: 'path', d: 'M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z' },
    { t: 'circle', cx: 12, cy: 12, r: 2.6 }
  ],
  download: [
    { t: 'path', d: 'M12 4v11' },
    { t: 'path', d: 'm8 11 4 4 4-4' },
    { t: 'path', d: 'M5 19.5h14' }
  ],
  users: [
    { t: 'circle', cx: 9, cy: 9, r: 3.2 },
    { t: 'path', d: 'M3.5 19a5.5 5.5 0 0 1 11 0' },
    { t: 'path', d: 'M16 6.2a3 3 0 0 1 0 5.6' },
    { t: 'path', d: 'M16.8 14.2A5.5 5.5 0 0 1 20.5 19' }
  ],
  lock: [
    { t: 'rect', x: 5, y: 10.5, w: 14, h: 9.5, rx: 2 },
    { t: 'path', d: 'M8 10.5V8a4 4 0 0 1 8 0v2.5' }
  ],
  coin: [
    { t: 'circle', cx: 12, cy: 12, r: 8 },
    { t: 'path', d: 'M14.5 9.7a3.2 3.2 0 1 0 0 4.6' }
  ],
  'trending-up': [
    { t: 'path', d: 'M3 17l6-6 4 4 7-7' },
    { t: 'path', d: 'M17 7h4v4' }
  ],
  'trending-down': [
    { t: 'path', d: 'M3 7l6 6 4-4 7 7' },
    { t: 'path', d: 'M17 17h4v-4' }
  ],
  list: [
    { t: 'path', d: 'M8 6h13M8 12h13M8 18h13' },
    { t: 'path', d: 'M3.5 6h.01M3.5 12h.01M3.5 18h.01' }
  ],
  filter: [{ t: 'path', d: 'M4 5h16l-6 7v6l-4 2v-8z' }],
  edit: [
    { t: 'path', d: 'M4 20h4L18.5 9.5a2.1 2.1 0 0 0-3-3L5 17v3z' },
    { t: 'path', d: 'M13.5 7.5l3 3' }
  ],
  trash: [
    { t: 'path', d: 'M4 7h16' },
    { t: 'path', d: 'M9 7V5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2' },
    { t: 'path', d: 'M6 7l1 13a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1l1-13' },
    { t: 'path', d: 'M10 11v6M14 11v6' }
  ],
  shield: [
    { t: 'path', d: 'M12 3l7 3v5c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V6z' },
    { t: 'path', d: 'm9 12 2 2 4-4' }
  ],
  rocket: [
    {
      t: 'path',
      d: 'M4.5 16.5c-1.5 1.26-2 5-2 5s3.74-.5 5-2c.71-.84.7-2.13-.09-2.91a2.18 2.18 0 0 0-2.91-.09z'
    },
    {
      t: 'path',
      d: 'm12 15-3-3a22 22 0 0 1 2-3.95A12.88 12.88 0 0 1 22 2c0 2.72-.78 7.5-6 11a22.35 22.35 0 0 1-4 2z'
    },
    { t: 'path', d: 'M9 12H4s.55-3.03 2-4c1.62-1.08 5 0 5 0' },
    { t: 'path', d: 'M12 15v5s3.03-.55 4-2c1.08-1.62 0-5 0-5' }
  ],
  graduation: [
    { t: 'path', d: 'M3 9l9-4 9 4-9 4-9-4z' },
    { t: 'path', d: 'M7 11v4c0 1 2.5 2.5 5 2.5s5-1.5 5-2.5v-4' },
    { t: 'path', d: 'M21 9v4' }
  ],
  heart: [
    { t: 'path', d: 'M12 20S4 14.5 4 8.8A4 4 0 0 1 12 6a4 4 0 0 1 8 2.8C20 14.5 12 20 12 20z' }
  ],
  share: [
    { t: 'circle', cx: 6, cy: 12, r: 2.5 },
    { t: 'circle', cx: 18, cy: 6, r: 2.5 },
    { t: 'circle', cx: 18, cy: 18, r: 2.5 },
    { t: 'path', d: 'm8.2 10.8 7.6-3.6M8.2 13.2l7.6 3.6' }
  ],
  unlock: [
    { t: 'rect', x: 5, y: 10.5, w: 14, h: 9.5, rx: 2 },
    { t: 'path', d: 'M8 10.5V7a4 4 0 0 1 7.5-2' }
  ],
  flag: [
    { t: 'path', d: 'M6 21V4' },
    { t: 'path', d: 'M6 4h11l-2 3.5L17 11H6' }
  ],
  mail: [
    { t: 'rect', x: 3, y: 5.5, w: 18, h: 13, rx: 1.5 },
    { t: 'path', d: 'm4 7 8 6 8-6' }
  ],
  palette: [
    {
      t: 'path',
      d: 'M12 3a9 9 0 1 0 0 18c1.5 0 2-1 2-2 0-1.5 1-2 2-2h1a4 4 0 0 0 4-4c0-5-4-8-9-8z'
    },
    { t: 'circle', cx: 8.5, cy: 10, r: 1, fill: true },
    { t: 'circle', cx: 12, cy: 8, r: 1, fill: true },
    { t: 'circle', cx: 15.5, cy: 10, r: 1, fill: true }
  ],
  globe: [
    { t: 'circle', cx: 12, cy: 12, r: 9 },
    { t: 'path', d: 'M3.5 9h17M3.5 15h17' },
    { t: 'path', d: 'M12 3c2.5 2.5 3.5 6 3.5 9s-1 6.5-3.5 9c-2.5-2.5-3.5-6-3.5-9s1-6.5 3.5-9z' }
  ],
  moon: [{ t: 'path', d: 'M20 14.5A8 8 0 0 1 9.5 4 7 7 0 1 0 20 14.5z' }],
  sun: [
    { t: 'circle', cx: 12, cy: 12, r: 4.2 },
    {
      t: 'path',
      d: 'M12 2v2M12 20v2M4 12H2M22 12h-2M5 5l1.5 1.5M17.5 17.5 19 19M19 5l-1.5 1.5M6.5 17.5 5 19'
    }
  ],
  'chevron-left': [{ t: 'path', d: 'm14 6-6 6 6 6' }],
  gem: [
    { t: 'path', d: 'M6 4h12l3 5-9 11L3 9z' },
    { t: 'path', d: 'M3 9h18' },
    { t: 'path', d: 'M9 4l-1.5 5L12 20l4.5-11L15 4' }
  ],
  lightbulb: [
    { t: 'path', d: 'M9 18h6M10 21h4' },
    { t: 'path', d: 'M12 3a6 6 0 0 0-4 10.5c.7.7 1 1.5 1 2.5h6c0-1 .3-1.8 1-2.5A6 6 0 0 0 12 3z' }
  ],
  layers: [
    { t: 'path', d: 'M12 3 3 8l9 5 9-5-9-5z' },
    { t: 'path', d: 'M3 12l9 5 9-5' },
    { t: 'path', d: 'M3 16l9 5 9-5' }
  ],
  minus: [{ t: 'path', d: 'M5 12h14' }],
  'arrow-up': [
    { t: 'path', d: 'M12 5v14' },
    { t: 'path', d: 'm6 11 6-6 6 6' }
  ],
  'external-link': [
    { t: 'path', d: 'M14 4h6v6' },
    { t: 'path', d: 'M20 4 10 14' },
    { t: 'path', d: 'M19 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h6' }
  ],
  message: [
    {
      t: 'path',
      d: 'M5 5h14a1 1 0 0 1 1 1v9a1 1 0 0 1-1 1H9l-4 3v-3a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1z'
    }
  ],
  pause: [
    { t: 'rect', x: 7, y: 5, w: 3.2, h: 14, rx: 1 },
    { t: 'rect', x: 13.8, y: 5, w: 3.2, h: 14, rx: 1 }
  ],
  award: [
    { t: 'circle', cx: 12, cy: 9, r: 5 },
    { t: 'path', d: 'M9.2 13 8 21l4-2.3L16 21l-1.2-8' }
  ],
  dot: [{ t: 'circle', cx: 12, cy: 12, r: 3, fill: true }],
  bot: [
    { t: 'rect', x: 4, y: 8, w: 16, h: 11, rx: 3 },
    { t: 'path', d: 'M12 4.5V8' },
    { t: 'circle', cx: 12, cy: 4, r: 1, fill: true },
    { t: 'path', d: 'M2 13.5v2M22 13.5v2' },
    { t: 'circle', cx: 9, cy: 13, r: 1, fill: true },
    { t: 'circle', cx: 15, cy: 13, r: 1, fill: true },
    { t: 'path', d: 'M9.5 16.5h5' }
  ],
  command: [
    {
      t: 'path',
      d: 'M18 3a3 3 0 0 0-3 3v12a3 3 0 0 0 3 3 3 3 0 0 0 3-3 3 3 0 0 0-3-3H6a3 3 0 0 0-3 3 3 3 0 0 0 3 3 3 3 0 0 0 3-3V6a3 3 0 0 0-3-3 3 3 0 0 0-3 3 3 3 0 0 0 3 3h12a3 3 0 0 0 3-3 3 3 0 0 0-3-3z'
    }
  ],
  maximize: [
    { t: 'path', d: 'M8 3H5a2 2 0 0 0-2 2v3' },
    { t: 'path', d: 'M21 8V5a2 2 0 0 0-2-2h-3' },
    { t: 'path', d: 'M3 16v3a2 2 0 0 0 2 2h3' },
    { t: 'path', d: 'M16 21h3a2 2 0 0 0 2-2v-3' }
  ],
  minimize: [
    { t: 'path', d: 'M8 3v3a2 2 0 0 1-2 2H3' },
    { t: 'path', d: 'M21 8h-3a2 2 0 0 1-2-2V3' },
    { t: 'path', d: 'M3 16h3a2 2 0 0 1 2 2v3' },
    { t: 'path', d: 'M16 21v-3a2 2 0 0 1 2-2h3' }
  ],
  refresh: [
    { t: 'path', d: 'M3 12a9 9 0 0 1 15.3-6.4L21 8' },
    { t: 'path', d: 'M21 3v5h-5' },
    { t: 'path', d: 'M21 12a9 9 0 0 1-15.3 6.4L3 16' },
    { t: 'path', d: 'M3 21v-5h5' }
  ],
  send: [
    { t: 'path', d: 'M21 4 14 21l-3-7.5L3.5 10.5z' },
    { t: 'path', d: 'm21 4-10 9.5' }
  ],
  video: [
    { t: 'rect', x: 3, y: 6.5, w: 12, h: 11, rx: 2.5 },
    { t: 'path', d: 'M15 10l5.5-3.2v10.4L15 14z' }
  ],
  repeat: [
    { t: 'path', d: 'm17 2.5 4 4-4 4' },
    { t: 'path', d: 'M3 11.5V10a3 3 0 0 1 3-3h15' },
    { t: 'path', d: 'm7 21.5-4-4 4-4' },
    { t: 'path', d: 'M21 12.5V14a3 3 0 0 1-3 3H3' }
  ],
  wifi: [
    { t: 'path', d: 'M2.5 8.5a16 16 0 0 1 19 0' },
    { t: 'path', d: 'M5.5 12a11 11 0 0 1 13 0' },
    { t: 'path', d: 'M8.5 15.5a6 6 0 0 1 7 0' },
    { t: 'circle', cx: 12, cy: 19, r: 0.8, fill: true }
  ],
  'wifi-off': [
    { t: 'path', d: 'M8.5 15.5a6 6 0 0 1 7 0' },
    { t: 'path', d: 'M5.5 12a11 11 0 0 1 4-2.6' },
    { t: 'path', d: 'M14.6 9.6A11 11 0 0 1 18.5 12' },
    { t: 'path', d: 'M2.5 8.5a16 16 0 0 1 5.4-3.7M13 4.4a16 16 0 0 1 8.5 4.1' },
    { t: 'circle', cx: 12, cy: 19, r: 0.8, fill: true },
    { t: 'path', d: 'M3 3 21 21' }
  ],
  phone: [
    {
      t: 'path',
      d: 'M5 4.5h3.2l1.4 3.6-1.8 1.4a10.5 10.5 0 0 0 5.3 5.3l1.4-1.8 3.6 1.4V18a1.6 1.6 0 0 1-1.8 1.6A14 14 0 0 1 3.4 6.3 1.6 1.6 0 0 1 5 4.5z'
    }
  ],
  image: [
    { t: 'rect', x: 3.5, y: 4.5, w: 17, h: 15, rx: 2.5 },
    { t: 'circle', cx: 9, cy: 9.5, r: 1.6 },
    { t: 'path', d: 'm4 17 5-4.5 4 3 3-2.5 4 4' }
  ],
  smile: [
    { t: 'circle', cx: 12, cy: 12, r: 9 },
    { t: 'path', d: 'M8.5 14.5a4.5 4.5 0 0 0 7 0' },
    { t: 'circle', cx: 9, cy: 10, r: 0.7, fill: true },
    { t: 'circle', cx: 15, cy: 10, r: 0.7, fill: true }
  ],
  bug: [
    { t: 'rect', x: 7.5, y: 7.5, w: 9, h: 11, rx: 4.5 },
    { t: 'path', d: 'M9.5 7.5a2.5 2.5 0 0 1 5 0' },
    {
      t: 'path',
      d: 'M7.5 11.5H4M16.5 11.5H20M7.5 15.5H4M16.5 15.5H20M9 5.2 7.5 3.7M15 5.2l1.5-1.5'
    },
    { t: 'path', d: 'M12 11v6' }
  ],
  'thumbs-up': [
    { t: 'path', d: 'M7 10.5v9H4.6a1 1 0 0 1-1-1v-7a1 1 0 0 1 1-1z' },
    {
      t: 'path',
      d: 'M7 10.5 11 3a2 2 0 0 1 2 2v4h5.4a1.6 1.6 0 0 1 1.6 1.9l-1.3 7a1.6 1.6 0 0 1-1.6 1.1H7'
    }
  ],
  copy: [
    { t: 'rect', x: 8.5, y: 8.5, w: 11, h: 11, rx: 2 },
    {
      t: 'path',
      d: 'M5.5 15.5H5A1.5 1.5 0 0 1 3.5 14V5A1.5 1.5 0 0 1 5 3.5h9A1.5 1.5 0 0 1 15.5 5v.5'
    }
  ],
  link: [
    { t: 'path', d: 'm9.5 14.5 5-5' },
    { t: 'path', d: 'M8 12 5.5 14.5a3.5 3.5 0 0 0 5 5L13 17' },
    { t: 'path', d: 'M16 12l2.5-2.5a3.5 3.5 0 0 0-5-5L11 7' }
  ],
  info: [
    { t: 'circle', cx: 12, cy: 12, r: 9 },
    { t: 'path', d: 'M12 11v5' },
    { t: 'circle', cx: 12, cy: 7.8, r: 0.7, fill: true }
  ],
  alert: [
    { t: 'path', d: 'M12 4 21.5 20H2.5z' },
    { t: 'path', d: 'M12 10v4' },
    { t: 'circle', cx: 12, cy: 17, r: 0.7, fill: true }
  ],
  megaphone: [
    { t: 'path', d: 'M4.5 9.5 14 6v12l-9.5-3.5H4a.5.5 0 0 1-.5-.5v-4a.5.5 0 0 1 .5-.5z' },
    { t: 'path', d: 'M14 6.5c3 0 5.5 1.6 5.5 5.5S17 17.5 14 17.5' },
    { t: 'path', d: 'M7.5 15v3.5a1.5 1.5 0 0 0 3 0V16' }
  ],
  sliders: [
    { t: 'path', d: 'M4 8h9M19 8h1M4 16h1M11 16h9' },
    { t: 'circle', cx: 15, cy: 8, r: 2.2 },
    { t: 'circle', cx: 7, cy: 16, r: 2.2 }
  ],
  headset: [
    { t: 'path', d: 'M4 13v-1a8 8 0 0 1 16 0v1' },
    { t: 'rect', x: 3, y: 13, w: 4, h: 6, rx: 1.5 },
    { t: 'rect', x: 17, y: 13, w: 4, h: 6, rx: 1.5 },
    { t: 'path', d: 'M20 19v0.5a3 3 0 0 1-3 3h-3' }
  ],
  pin: [
    { t: 'path', d: 'M12 21s6-5.3 6-10a6 6 0 1 0-12 0c0 4.7 6 10 6 10z' },
    { t: 'circle', cx: 12, cy: 11, r: 2.3 }
  ],
  fire: [
    {
      t: 'path',
      d: 'M12 3s4.5 3.5 4.5 8.2a4.5 4.5 0 0 1-9 0c0-1.7.8-2.9 1.6-3.9C10.5 9 12 7.5 12 3z'
    },
    {
      t: 'path',
      d: 'M12 20a2.2 2.2 0 0 0 2.2-2.2c0-1.6-2.2-3-2.2-3s-2.2 1.4-2.2 3A2.2 2.2 0 0 0 12 20z',
      fill: true
    }
  ]
};

function shapes(): IconShape[] {
  return icons[props.name] ?? icons.dot;
}
</script>

<template>
  <svg
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.7"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
    focusable="false"
  >
    <template v-for="(shape, index) in shapes()" :key="index">
      <path
        v-if="shape.t === 'path'"
        :d="shape.d"
        :fill="shape.fill ? 'currentColor' : 'none'"
        :stroke="shape.fill ? 'none' : 'currentColor'"
      />
      <circle
        v-else-if="shape.t === 'circle'"
        :cx="shape.cx"
        :cy="shape.cy"
        :r="shape.r"
        :fill="shape.fill ? 'currentColor' : 'none'"
        :stroke="shape.fill ? 'none' : 'currentColor'"
      />
      <rect
        v-else
        :x="shape.x"
        :y="shape.y"
        :width="shape.w"
        :height="shape.h"
        :rx="shape.rx ?? 0"
      />
    </template>
  </svg>
</template>
