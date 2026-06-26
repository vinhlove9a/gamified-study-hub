<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';

/**
 * Deep-space backdrop for the cosmic dashboard / app shell.
 *
 * Layers (back → front):
 *   1. Drifting nebula clouds
 *   2. Slow cosmic dust motes (mouse-parallax)
 *   3. Multi-colour twinkling starfield (3 parallax depths)
 *   4. Occasional pulsar flashes (bright star + expanding ring)
 *   5. Frequent shooting stars from many angles, in several colours
 *   6. Periodic meteor showers (a burst of aligned streaks)
 *   7. Rare slow comets with a long glowing coma + tail
 *
 * Honours `prefers-reduced-motion`: renders a single static frame and stops.
 */

interface Star {
  x: number;
  y: number;
  z: number; // depth → parallax + size
  size: number;
  twinkle: number;
  twinkleSpeed: number;
  hue: number; // 0 = neutral white, else coloured
  pulsarTimer: number; // frames until this star may flash
}

interface ShootingStar {
  x: number;
  y: number;
  vx: number;
  vy: number;
  life: number;
  maxLife: number;
  len: number;
  width: number;
  hue: number;
  sat: number;
}

interface Comet {
  x: number;
  y: number;
  vx: number;
  vy: number;
  life: number;
  maxLife: number;
  hue: number;
}

interface Dust {
  x: number;
  y: number;
  z: number;
  size: number;
  drift: number;
  phase: number;
}

interface Pulsar {
  x: number;
  y: number;
  life: number;
  maxLife: number;
  hue: number;
}

interface Nebula {
  x: number;
  y: number;
  r: number;
  hue: number;
  alpha: number;
  drift: number;
  phase: number;
}

// Accent hues used across the whole effect set (cyan, magenta, violet, gold, mint).
const ACCENTS = [195, 330, 280, 45, 150];

const canvasRef = ref<HTMLCanvasElement | null>(null);
let rafId = 0;
let cleanup: (() => void) | null = null;

onMounted(() => {
  const canvas = canvasRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  const reduceMotion =
    typeof window.matchMedia === 'function' &&
    window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  let width = (canvas.width = window.innerWidth);
  let height = (canvas.height = window.innerHeight);

  const rand = (min: number, max: number) => Math.random() * (max - min) + min;
  const pick = <T,>(arr: T[]): T => arr[Math.floor(Math.random() * arr.length)];

  // ── Stars (multi-depth, multi-colour) ────────────────────────────────────
  const starCount = Math.min(520, Math.floor((width * height) / 2600));
  const stars: Star[] = [];
  for (let i = 0; i < starCount; i++) {
    // ~22% of stars get a subtle colour tint, the rest stay near-white.
    const coloured = Math.random() < 0.22;
    stars.push({
      x: Math.random() * width,
      y: Math.random() * height,
      z: rand(0.5, 3.5),
      size: rand(0.3, 1.9),
      twinkle: rand(0, Math.PI * 2),
      twinkleSpeed: rand(0.005, 0.03),
      hue: coloured ? pick(ACCENTS) : -1,
      pulsarTimer: rand(300, 2600)
    });
  }

  // ── Cosmic dust motes (foreground parallax) ──────────────────────────────
  const dustCount = Math.min(90, Math.floor((width * height) / 24000));
  const dust: Dust[] = [];
  for (let i = 0; i < dustCount; i++) {
    dust.push({
      x: Math.random() * width,
      y: Math.random() * height,
      z: rand(1, 4),
      size: rand(0.4, 1.4),
      drift: rand(0.05, 0.25),
      phase: rand(0, Math.PI * 2)
    });
  }

  // ── Nebula clouds (slow drift) ────────────────────────────────────────────
  const nebulae: Nebula[] = [
    { x: width * 0.2, y: height * 0.24, r: 320, hue: 290, alpha: 0.2, drift: 14, phase: 0 },
    { x: width * 0.84, y: height * 0.72, r: 380, hue: 200, alpha: 0.18, drift: 18, phase: 1.6 },
    { x: width * 0.55, y: height * 0.42, r: 230, hue: 330, alpha: 0.14, drift: 22, phase: 3.1 },
    { x: width * 0.1, y: height * 0.85, r: 260, hue: 150, alpha: 0.1, drift: 16, phase: 4.4 }
  ];

  const shootingStars: ShootingStar[] = [];
  const comets: Comet[] = [];
  const pulsars: Pulsar[] = [];

  let mouseX = width / 2;
  let mouseY = height / 2;
  let targetMouseX = mouseX;
  let targetMouseY = mouseY;
  let frame = 0;
  let showerTimer = rand(600, 1100); // frames until next meteor shower
  let cometTimer = rand(1400, 2600); // frames until next comet

  const onResize = () => {
    width = canvas.width = window.innerWidth;
    height = canvas.height = window.innerHeight;
    stars.forEach((s) => {
      if (s.x > width) s.x = Math.random() * width;
      if (s.y > height) s.y = Math.random() * height;
    });
  };

  const onMouse = (e: MouseEvent) => {
    targetMouseX = e.clientX;
    targetMouseY = e.clientY;
  };

  // Spawn one shooting star. Defaults bias toward the classic top-right → down-left
  // streak, but direction can be fully overridden (used by meteor showers).
  const spawnShootingStar = (opts?: Partial<ShootingStar> & { angle?: number; speed?: number }) => {
    const angle = opts?.angle ?? rand(Math.PI * 0.62, Math.PI * 0.95); // mostly down-left
    const speed = opts?.speed ?? rand(7, 12);
    shootingStars.push({
      x: opts?.x ?? rand(width * 0.15, width * 1.05),
      y: opts?.y ?? rand(-height * 0.05, height * 0.55),
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      life: 0,
      maxLife: opts?.maxLife ?? rand(55, 95),
      len: opts?.len ?? rand(60, 150),
      width: opts?.width ?? rand(1, 2.2),
      hue: opts?.hue ?? (Math.random() < 0.55 ? -1 : pick(ACCENTS)),
      sat: opts?.sat ?? 90
    });
  };

  const spawnMeteorShower = () => {
    const angle = rand(Math.PI * 0.66, Math.PI * 0.9);
    const speed = rand(9, 13);
    const count = Math.floor(rand(5, 10));
    const hue = Math.random() < 0.5 ? -1 : pick(ACCENTS);
    for (let i = 0; i < count; i++) {
      spawnShootingStar({
        angle: angle + rand(-0.04, 0.04),
        speed,
        x: rand(width * 0.3, width * 1.15),
        y: rand(-height * 0.15, height * 0.2),
        len: rand(80, 170),
        maxLife: rand(60, 100),
        hue,
        sat: 95
      });
    }
  };

  const spawnComet = () => {
    const fromLeft = Math.random() < 0.5;
    const speed = rand(2.2, 3.4);
    const angle = fromLeft ? rand(0.18, 0.42) : Math.PI - rand(0.18, 0.42);
    comets.push({
      x: fromLeft ? -80 : width + 80,
      y: rand(height * 0.05, height * 0.4),
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      life: 0,
      maxLife: rand(420, 620),
      hue: pick(ACCENTS)
    });
  };

  // ── Drawing helpers ───────────────────────────────────────────────────────
  const drawNebulae = (t: number) => {
    nebulae.forEach((n) => {
      const ox = Math.sin(t * 0.00006 + n.phase) * n.drift;
      const oy = Math.cos(t * 0.00005 + n.phase) * n.drift;
      const cx = n.x + ox;
      const cy = n.y + oy;
      const grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, n.r);
      grad.addColorStop(0, `hsla(${n.hue}, 70%, 55%, ${n.alpha})`);
      grad.addColorStop(0.5, `hsla(${n.hue}, 70%, 45%, ${n.alpha * 0.4})`);
      grad.addColorStop(1, `hsla(${n.hue}, 70%, 35%, 0)`);
      ctx.fillStyle = grad;
      ctx.fillRect(cx - n.r, cy - n.r, n.r * 2, n.r * 2);
    });
  };

  const drawStars = (px: number, py: number) => {
    stars.forEach((s) => {
      s.twinkle += s.twinkleSpeed;
      const brightness = 0.4 + Math.abs(Math.sin(s.twinkle)) * 0.6;
      const dx = s.x - px * s.z * 8;
      const dy = s.y - py * s.z * 8;
      const col = s.hue < 0 ? '220, 230, 255' : null;

      // Glow halo for the brighter stars.
      if (s.size > 1.15) {
        const grad = ctx.createRadialGradient(dx, dy, 0, dx, dy, s.size * 4.5);
        if (col) {
          grad.addColorStop(0, `rgba(190, 220, 255, ${brightness * 0.5})`);
          grad.addColorStop(1, 'rgba(190, 220, 255, 0)');
        } else {
          grad.addColorStop(0, `hsla(${s.hue}, 90%, 75%, ${brightness * 0.5})`);
          grad.addColorStop(1, `hsla(${s.hue}, 90%, 70%, 0)`);
        }
        ctx.fillStyle = grad;
        ctx.fillRect(dx - s.size * 4.5, dy - s.size * 4.5, s.size * 9, s.size * 9);
      }

      ctx.fillStyle = col
        ? `rgba(220, 230, 255, ${brightness})`
        : `hsla(${s.hue}, 85%, 80%, ${brightness})`;
      ctx.beginPath();
      ctx.arc(dx, dy, s.size, 0, Math.PI * 2);
      ctx.fill();
    });
  };

  const drawDust = (px: number, py: number, t: number) => {
    ctx.fillStyle = 'rgba(180, 200, 255, 0.5)';
    dust.forEach((d) => {
      const flicker = 0.25 + Math.abs(Math.sin(t * 0.001 + d.phase)) * 0.55;
      const dx = d.x - px * d.z * 14 + Math.sin(t * 0.0003 + d.phase) * d.drift * 6;
      const dy = d.y - py * d.z * 14;
      ctx.globalAlpha = flicker;
      ctx.beginPath();
      ctx.arc(dx, dy, d.size, 0, Math.PI * 2);
      ctx.fill();
    });
    ctx.globalAlpha = 1;
  };

  const streakColor = (hue: number, sat: number, alpha: number) =>
    hue < 0 ? `rgba(190, 225, 255, ${alpha})` : `hsla(${hue}, ${sat}%, 72%, ${alpha})`;

  const drawShootingStars = () => {
    for (let i = shootingStars.length - 1; i >= 0; i--) {
      const ss = shootingStars[i];
      ss.life++;
      ss.x += ss.vx;
      ss.y += ss.vy;

      const progress = ss.life / ss.maxLife;
      const alpha = Math.sin(progress * Math.PI);
      const mag = Math.hypot(ss.vx, ss.vy) || 1;
      const tx = ss.x - (ss.vx / mag) * ss.len;
      const ty = ss.y - (ss.vy / mag) * ss.len;

      const grad = ctx.createLinearGradient(ss.x, ss.y, tx, ty);
      grad.addColorStop(0, streakColor(ss.hue, ss.sat, alpha * 0.95));
      grad.addColorStop(1, streakColor(ss.hue, ss.sat, 0));
      ctx.strokeStyle = grad;
      ctx.lineWidth = ss.width;
      ctx.lineCap = 'round';
      ctx.beginPath();
      ctx.moveTo(ss.x, ss.y);
      ctx.lineTo(tx, ty);
      ctx.stroke();

      // Bright head with a soft bloom.
      const head = ctx.createRadialGradient(ss.x, ss.y, 0, ss.x, ss.y, ss.width * 4);
      head.addColorStop(0, streakColor(ss.hue, ss.sat, alpha));
      head.addColorStop(1, streakColor(ss.hue, ss.sat, 0));
      ctx.fillStyle = head;
      ctx.beginPath();
      ctx.arc(ss.x, ss.y, ss.width * 4, 0, Math.PI * 2);
      ctx.fill();

      if (ss.life >= ss.maxLife || ss.x < -ss.len || ss.y > height + ss.len || ss.x > width + ss.len) {
        shootingStars.splice(i, 1);
      }
    }
  };

  const drawComets = () => {
    for (let i = comets.length - 1; i >= 0; i--) {
      const c = comets[i];
      c.life++;
      c.x += c.vx;
      c.y += c.vy;
      const progress = c.life / c.maxLife;
      const alpha = Math.sin(progress * Math.PI);
      const mag = Math.hypot(c.vx, c.vy) || 1;
      const tailLen = 180;
      const tx = c.x - (c.vx / mag) * tailLen;
      const ty = c.y - (c.vy / mag) * tailLen;

      // Tail
      const grad = ctx.createLinearGradient(c.x, c.y, tx, ty);
      grad.addColorStop(0, `hsla(${c.hue}, 95%, 78%, ${alpha * 0.85})`);
      grad.addColorStop(0.4, `hsla(${c.hue}, 90%, 65%, ${alpha * 0.3})`);
      grad.addColorStop(1, `hsla(${c.hue}, 90%, 60%, 0)`);
      ctx.strokeStyle = grad;
      ctx.lineWidth = 3.5;
      ctx.lineCap = 'round';
      ctx.beginPath();
      ctx.moveTo(c.x, c.y);
      ctx.lineTo(tx, ty);
      ctx.stroke();

      // Glowing coma (head)
      const coma = ctx.createRadialGradient(c.x, c.y, 0, c.x, c.y, 14);
      coma.addColorStop(0, `hsla(${c.hue}, 100%, 92%, ${alpha})`);
      coma.addColorStop(0.4, `hsla(${c.hue}, 95%, 75%, ${alpha * 0.6})`);
      coma.addColorStop(1, `hsla(${c.hue}, 90%, 65%, 0)`);
      ctx.fillStyle = coma;
      ctx.beginPath();
      ctx.arc(c.x, c.y, 14, 0, Math.PI * 2);
      ctx.fill();

      if (c.life >= c.maxLife) comets.splice(i, 1);
    }
  };

  const drawPulsars = () => {
    for (let i = pulsars.length - 1; i >= 0; i--) {
      const p = pulsars[i];
      p.life++;
      const progress = p.life / p.maxLife;
      const alpha = 1 - progress;
      const ringR = progress * 26;

      ctx.strokeStyle = `hsla(${p.hue}, 90%, 78%, ${alpha * 0.7})`;
      ctx.lineWidth = 1.4;
      ctx.beginPath();
      ctx.arc(p.x, p.y, ringR, 0, Math.PI * 2);
      ctx.stroke();

      const core = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, 6);
      core.addColorStop(0, `hsla(${p.hue}, 100%, 92%, ${alpha})`);
      core.addColorStop(1, `hsla(${p.hue}, 95%, 75%, 0)`);
      ctx.fillStyle = core;
      ctx.beginPath();
      ctx.arc(p.x, p.y, 6, 0, Math.PI * 2);
      ctx.fill();

      if (p.life >= p.maxLife) pulsars.splice(i, 1);
    }
  };

  const renderFrame = (t: number) => {
    ctx.clearRect(0, 0, width, height);
    const px = (mouseX - width / 2) * 0.005;
    const py = (mouseY - height / 2) * 0.005;
    drawNebulae(t);
    drawDust(px, py, t);
    drawStars(px, py);
    drawPulsars();
    drawShootingStars();
    drawComets();
  };

  if (reduceMotion) {
    // Single calm frame — no animation, no spawners.
    renderFrame(0);
    cleanup = () => {
      window.removeEventListener('resize', onResize);
    };
    window.addEventListener('resize', onResize);
    return;
  }

  window.addEventListener('resize', onResize);
  window.addEventListener('mousemove', onMouse);

  const render = () => {
    frame++;
    mouseX += (targetMouseX - mouseX) * 0.05;
    mouseY += (targetMouseY - mouseY) * 0.05;

    renderFrame(frame);

    // Spawn a steady drizzle of shooting stars (much more frequent than before).
    if (Math.random() < 0.05 && shootingStars.length < 14) {
      spawnShootingStar();
    }

    // Meteor showers on a timer.
    if (--showerTimer <= 0) {
      spawnMeteorShower();
      showerTimer = rand(650, 1300);
    }

    // Rare comets.
    if (--cometTimer <= 0 && comets.length === 0) {
      spawnComet();
      cometTimer = rand(1600, 3000);
    }

    // Pulsar flashes from random bright stars.
    for (const s of stars) {
      if (s.size <= 1.2) continue;
      s.pulsarTimer -= 1;
      if (s.pulsarTimer <= 0) {
        pulsars.push({ x: s.x, y: s.y, life: 0, maxLife: 36, hue: s.hue < 0 ? pick(ACCENTS) : s.hue });
        s.pulsarTimer = rand(900, 3200);
      }
    }

    rafId = requestAnimationFrame(render);
  };

  render();

  cleanup = () => {
    window.removeEventListener('resize', onResize);
    window.removeEventListener('mousemove', onMouse);
    cancelAnimationFrame(rafId);
  };
});

onBeforeUnmount(() => cleanup?.());
</script>

<template>
  <canvas ref="canvasRef" class="starfield-canvas fixed inset-0 -z-10 pointer-events-none" aria-hidden="true" />
</template>
