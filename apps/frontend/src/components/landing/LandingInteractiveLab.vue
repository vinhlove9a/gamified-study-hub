<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue';

type VertexKey = 'A' | 'B' | 'C';
type CoefficientKey = 'a' | 'b' | 'c' | 'd';

interface Point {
  x: number;
  y: number;
}

interface VertexPoint extends Point {
  label: VertexKey;
}

interface SideLabel {
  label: string;
  value: number;
  x: number;
  y: number;
}

interface AngleArc {
  key: VertexKey;
  d: string;
}

interface PlotPoint {
  x: number;
  y: number;
  px: number;
  py: number;
}

interface QuizItem {
  question: string;
  options: string[];
  correct: number;
  feedback: string;
}

interface StepItem {
  title: string;
  expression: string;
  note: string;
  final?: boolean;
}

const labRef = ref<HTMLElement | null>(null);
const triangleSvgRef = ref<SVGSVGElement | null>(null);
const activeVertex = ref<VertexKey | null>(null);
const answeredOptions = ref<Array<number | null>>([]);
const revealedSteps = ref(0);
const toastVisible = ref(false);
const toastMessage = ref('');
const stepRefs = ref<Array<HTMLElement | null>>([]);

const cleanupFns: Array<() => void> = [];
let toastTimer = 0;

const triangleWidth = 600;
const triangleHeight = 450;
const plotWidth = 600;
const plotHeight = 450;
const plotPadding = { left: 40, right: 20, top: 20, bottom: 40 };
const plotInnerWidth = plotWidth - plotPadding.left - plotPadding.right;
const plotInnerHeight = plotHeight - plotPadding.top - plotPadding.bottom;
const xMin = -5;
const xMax = 5;
const yMin = -5;
const yMax = 5;
const vertexKeys: VertexKey[] = ['A', 'B', 'C'];
const xTicks = Array.from({ length: xMax - xMin + 1 }, (_, index) => xMin + index);
const yTicks = Array.from({ length: yMax - yMin + 1 }, (_, index) => yMin + index);

const triangle = reactive<Record<VertexKey, VertexPoint>>({
  A: { x: 100, y: 100, label: 'A' },
  B: { x: 500, y: 130, label: 'B' },
  C: { x: 300, y: 400, label: 'C' }
});

const coeffs = reactive<Record<CoefficientKey, number>>({
  a: 0.3,
  b: -0.5,
  c: -1.5,
  d: 1
});

const coefficientControls: Array<{
  key: CoefficientKey;
  label: string;
  min: number;
  max: number;
  step: number;
}> = [
  { key: 'a', label: 'Nhịp tăng tốc', min: -1, max: 1, step: 0.05 },
  { key: 'b', label: 'Độ cong động lực', min: -2, max: 2, step: 0.05 },
  { key: 'c', label: 'Ma sát duy trì', min: -3, max: 3, step: 0.05 },
  { key: 'd', label: 'Điểm xuất phát', min: -3, max: 3, step: 0.05 }
];

const quizData: QuizItem[] = [
  {
    question: 'Học viên hoàn thành nhiệm vụ đúng hạn. Cơ chế nào nên kích hoạt ngay?',
    options: ['Ẩn tài liệu cũ', 'Cộng coin và cập nhật streak', 'Xóa nhiệm vụ', 'Đổi workspace'],
    correct: 1,
    feedback: 'Vòng lặp động lực cần phản hồi tức thì: cộng coin, giữ streak và mở đường tới phần thưởng tiếp theo.'
  },
  {
    question: 'Khi kho tài liệu quá lớn, tín hiệu nào giúp học viên tìm nhanh hơn?',
    options: ['Tên file dài hơn', 'Danh mục, tag và quyền truy cập rõ ràng', 'Ẩn thanh tìm kiếm', 'Chỉ dùng folder gốc'],
    correct: 1,
    feedback: 'Danh mục và tag biến kho file thành hệ thống có ngữ cảnh, dễ lọc theo mục tiêu học tập.'
  },
  {
    question: 'Coin học tập nên đại diện cho điều gì?',
    options: ['Nỗ lực được ghi nhận', 'Mật khẩu đăng nhập', 'Dung lượng lưu trữ', 'Màu giao diện'],
    correct: 0,
    feedback: 'Coin có ý nghĩa khi nó phản ánh hành động học thật: làm bài, điểm danh, đóng góp hoặc hoàn thành mục tiêu.'
  },
  {
    question: 'Tài liệu VIP phù hợp nhất khi được mở khóa bằng gì?',
    options: ['Số lần refresh trang', 'Coin kiếm được từ nhiệm vụ', 'Tên người dùng', 'Kích thước màn hình'],
    correct: 1,
    feedback: 'Mở khóa bằng coin nối nhiệm vụ với phần thưởng, giúp học viên thấy hành trình học có tiến triển.'
  }
];

const stepsData: StepItem[] = [
  {
    title: 'Tập hợp tài liệu',
    expression: 'Upload -> danh mục -> tag',
    note: 'Tài liệu được đưa về cùng một workspace, có metadata để tìm kiếm và phân quyền.'
  },
  {
    title: 'Biến mục tiêu thành nhiệm vụ',
    expression: 'Task = hành động + hạn + thưởng',
    note: 'Mỗi hoạt động học tập có điều kiện hoàn thành và số coin rõ ràng.'
  },
  {
    title: 'Ghi nhận nỗ lực',
    expression: 'Hoàn thành -> coin + streak',
    note: 'Phản hồi tức thì giúp học viên hiểu hành động nào đang tạo tiến bộ.'
  },
  {
    title: 'Mở khóa giá trị',
    expression: 'Coin -> tài liệu VIP',
    note: 'Tài liệu nâng cao trở thành phần thưởng cho nỗ lực học tập liên tục.',
    final: true
  }
];

answeredOptions.value = Array(quizData.length).fill(null);

const clamp = (value: number, min: number, max: number): number => Math.max(min, Math.min(max, value));
const fmt = (value: number, digits = 1): string => value.toFixed(digits);

const distance = (p1: Point, p2: Point): number => Math.hypot(p2.x - p1.x, p2.y - p1.y);

const angleAtVertex = (p: Point, q1: Point, q2: Point): number => {
  const v1x = q1.x - p.x;
  const v1y = q1.y - p.y;
  const v2x = q2.x - p.x;
  const v2y = q2.y - p.y;
  const dot = v1x * v2x + v1y * v2y;
  const m1 = Math.hypot(v1x, v1y);
  const m2 = Math.hypot(v2x, v2y);

  if (m1 < 0.001 || m2 < 0.001) return 0;

  return Math.acos(clamp(dot / (m1 * m2), -1, 1)) * 180 / Math.PI;
};

const midpoint = (p1: Point, p2: Point): Point => ({
  x: (p1.x + p2.x) / 2,
  y: (p1.y + p2.y) / 2
});

const describeArc = (cx: number, cy: number, radius: number, startAngle: number, endAngle: number): string => {
  const start = {
    x: cx + radius * Math.cos(startAngle),
    y: cy + radius * Math.sin(startAngle)
  };
  const end = {
    x: cx + radius * Math.cos(endAngle),
    y: cy + radius * Math.sin(endAngle)
  };
  const largeArc = Math.abs(endAngle - startAngle) > Math.PI ? 1 : 0;
  const sweep = endAngle > startAngle ? 1 : 0;

  return `M ${start.x.toFixed(2)} ${start.y.toFixed(2)} A ${radius} ${radius} 0 ${largeArc} ${sweep} ${end.x.toFixed(2)} ${end.y.toFixed(2)}`;
};

const describeSmallAngleArc = (vertex: Point, p1: Point, p2: Point): string => {
  let start = Math.atan2(p1.y - vertex.y, p1.x - vertex.x);
  let end = Math.atan2(p2.y - vertex.y, p2.x - vertex.x);
  let diff = (end - start + Math.PI * 2) % (Math.PI * 2);

  if (diff > Math.PI) {
    [start, end] = [end, start];
    diff = Math.PI * 2 - diff;
  }

  return describeArc(vertex.x, vertex.y, 28, start, start + diff);
};

const trianglePath = computed(() => {
  const { A, B, C } = triangle;
  return `M ${A.x} ${A.y} L ${B.x} ${B.y} L ${C.x} ${C.y} Z`;
});

const triangleMetrics = computed(() => {
  const { A, B, C } = triangle;
  const sideA = distance(B, C);
  const sideB = distance(A, C);
  const sideC = distance(A, B);
  const angleA = angleAtVertex(A, B, C);
  const angleB = angleAtVertex(B, A, C);
  const angleC = angleAtVertex(C, A, B);
  const perimeter = sideA + sideB + sideC;
  const semi = perimeter / 2;
  const area = Math.sqrt(Math.max(0, semi * (semi - sideA) * (semi - sideB) * (semi - sideC)));
  const centroid = {
    x: (A.x + B.x + C.x) / 3,
    y: (A.y + B.y + C.y) / 3
  };
  const maxAngle = Math.max(angleA, angleB, angleC);
  const same = (v1: number, v2: number) => Math.abs(v1 - v2) < 1.5;

  let bySide = 'quỹ đạo lệch';
  if (same(sideA, sideB) && same(sideB, sideC)) bySide = 'quỹ đạo cân bằng';
  else if (same(sideA, sideB) || same(sideB, sideC) || same(sideA, sideC)) bySide = 'quỹ đạo bán cân bằng';

  let byAngle = 'tín hiệu ổn định';
  if (Math.abs(maxAngle - 90) < 1.5) byAngle = 'điểm chuyển pha';
  else if (maxAngle > 90) byAngle = 'cần điều phối lại';

  return {
    sideA,
    sideB,
    sideC,
    angleA,
    angleB,
    angleC,
    perimeter,
    area,
    centroid,
    classification: `${bySide} - ${byAngle}`
  };
});

const sideLabels = computed<SideLabel[]>(() => {
  const { A, B, C } = triangle;
  const { centroid, sideA, sideB, sideC } = triangleMetrics.value;
  const sides = [
    { p1: B, p2: C, label: 'D', value: sideA },
    { p1: A, p2: C, label: 'E', value: sideB },
    { p1: A, p2: B, label: 'R', value: sideC }
  ];

  return sides.map((side) => {
    const mid = midpoint(side.p1, side.p2);
    const dx = side.p2.x - side.p1.x;
    const dy = side.p2.y - side.p1.y;
    const len = Math.max(1, Math.hypot(dx, dy));
    const nx = -dy / len;
    const ny = dx / len;
    const direction = ((mid.x - centroid.x) * nx + (mid.y - centroid.y) * ny) >= 0 ? 1 : -1;

    return {
      label: side.label,
      value: side.value,
      x: mid.x + nx * 22 * direction,
      y: mid.y + ny * 22 * direction
    };
  });
});

const angleArcs = computed<AngleArc[]>(() => {
  const { A, B, C } = triangle;

  return [
    { key: 'A', d: describeSmallAngleArc(A, B, C) },
    { key: 'B', d: describeSmallAngleArc(B, A, C) },
    { key: 'C', d: describeSmallAngleArc(C, A, B) }
  ];
});

const pointerToTrianglePoint = (event: PointerEvent): Point | null => {
  if (!triangleSvgRef.value) return null;

  const rect = triangleSvgRef.value.getBoundingClientRect();
  const scaleX = triangleWidth / rect.width;
  const scaleY = triangleHeight / rect.height;

  return {
    x: clamp((event.clientX - rect.left) * scaleX, 30, triangleWidth - 30),
    y: clamp((event.clientY - rect.top) * scaleY, 30, triangleHeight - 30)
  };
};

const startVertexDrag = (key: VertexKey, event: PointerEvent) => {
  event.preventDefault();
  activeVertex.value = key;
  const nextPoint = pointerToTrianglePoint(event);

  if (nextPoint) {
    triangle[key].x = nextPoint.x;
    triangle[key].y = nextPoint.y;
  }
};

const moveActiveVertex = (event: PointerEvent) => {
  if (!activeVertex.value) return;

  const nextPoint = pointerToTrianglePoint(event);
  if (!nextPoint) return;

  triangle[activeVertex.value].x = nextPoint.x;
  triangle[activeVertex.value].y = nextPoint.y;
};

const stopVertexDrag = () => {
  activeVertex.value = null;
};

const scaleX = (x: number): number => plotPadding.left + ((x - xMin) / (xMax - xMin)) * plotInnerWidth;
const scaleY = (y: number): number => plotPadding.top + (1 - (y - yMin) / (yMax - yMin)) * plotInnerHeight;

const evaluateCurve = (x: number): number => coeffs.a * x ** 3 + coeffs.b * x ** 2 + coeffs.c * x + coeffs.d;

const plotState = computed(() => {
  const points: PlotPoint[] = [];
  let path = '';
  let started = false;

  for (let px = 0; px <= plotInnerWidth; px += 1) {
    const x = xMin + (px / plotInnerWidth) * (xMax - xMin);
    const y = evaluateCurve(x);
    const plotPoint = { x, y, px: scaleX(x), py: scaleY(y) };
    points.push(plotPoint);

    if (plotPoint.py >= plotPadding.top - 50 && plotPoint.py <= plotPadding.top + plotInnerHeight + 50) {
      path += `${started ? 'L' : 'M'} ${plotPoint.px.toFixed(2)} ${plotPoint.py.toFixed(2)} `;
      started = true;
    } else {
      started = false;
    }
  }

  let fillPath = '';
  let inFill = false;
  points.forEach((point, index) => {
    if (point.y >= 0 && point.y <= yMax + 2) {
      if (!inFill) {
        fillPath += `M ${point.px.toFixed(2)} ${scaleY(0).toFixed(2)} L ${point.px.toFixed(2)} ${point.py.toFixed(2)} `;
        inFill = true;
      } else {
        fillPath += `L ${point.px.toFixed(2)} ${point.py.toFixed(2)} `;
      }
    } else if (inFill) {
      const previous = points[Math.max(0, index - 1)];
      fillPath += `L ${previous.px.toFixed(2)} ${scaleY(0).toFixed(2)} Z `;
      inFill = false;
    }
  });

  if (inFill) {
    const lastPoint = points[points.length - 1];
    fillPath += `L ${lastPoint.px.toFixed(2)} ${scaleY(0).toFixed(2)} Z`;
  }

  const roots: number[] = [];
  const sampleCount = 200;
  const step = (xMax - xMin) / sampleCount;
  let previousX = xMin;
  let previousY = evaluateCurve(previousX);

  for (let i = 1; i <= sampleCount; i += 1) {
    const x = xMin + i * step;
    const y = evaluateCurve(x);

    if (previousY === 0) roots.push(previousX);
    else if (previousY * y < 0) {
      roots.push(x - step * y / (y - previousY));
    }

    previousX = x;
    previousY = y;
  }

  const dA = 3 * coeffs.a;
  const dB = 2 * coeffs.b;
  const dC = coeffs.c;
  const discriminant = dB * dB - 4 * dA * dC;
  let criticals: number[] = [];

  if (Math.abs(dA) < 0.001) {
    if (Math.abs(dB) > 0.001) {
      criticals.push(-dC / dB);
    }
  } else if (discriminant > 0) {
    const sqrtDiscriminant = Math.sqrt(discriminant);
    criticals.push((-dB + sqrtDiscriminant) / (2 * dA));
    criticals.push((-dB - sqrtDiscriminant) / (2 * dA));
  }

  criticals = criticals.filter((x) => x >= xMin && x <= xMax);

  return {
    curvePath: path,
    fillPath,
    roots,
    criticals,
    yIntercept: coeffs.d,
    quadraticDiscriminant: coeffs.c * coeffs.c - 4 * coeffs.b * coeffs.d
  };
});

const quizDone = computed(() => answeredOptions.value.filter((answer) => answer !== null).length);
const quizCorrect = computed(() =>
  answeredOptions.value.reduce<number>((total, answer, index) => total + (answer === quizData[index].correct ? 1 : 0), 0)
);
const quizAccuracy = computed(() => (quizDone.value ? `${Math.round((quizCorrect.value / quizDone.value) * 100)}%` : '-'));

const answerQuiz = (questionIndex: number, optionIndex: number) => {
  if (answeredOptions.value[questionIndex] !== null) return;

  answeredOptions.value[questionIndex] = optionIndex;

  if (answeredOptions.value.every((answer) => answer !== null)) {
    window.setTimeout(() => {
      showToast(`Hoàn thành quiz - ${quizCorrect.value}/${quizData.length} câu đúng`);
    }, 600);
  }
};

const revealNextStep = () => {
  if (revealedSteps.value >= stepsData.length) return;

  revealedSteps.value += 1;

  void nextTick(() => {
    stepRefs.value[revealedSteps.value - 1]?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  });

  if (revealedSteps.value === stepsData.length) {
    window.setTimeout(() => showToast('Lộ trình đã mở đủ'), 400);
  }
};

const resetSteps = () => {
  revealedSteps.value = 0;
};

const showToast = (message: string) => {
  toastMessage.value = message;
  toastVisible.value = true;
  window.clearTimeout(toastTimer);
  toastTimer = window.setTimeout(() => {
    toastVisible.value = false;
  }, 3000);
};

const setStepRef = (element: unknown, index: number) => {
  stepRefs.value[index] = element instanceof HTMLElement ? element : null;
};

onMounted(() => {
  window.addEventListener('pointermove', moveActiveVertex);
  window.addEventListener('pointerup', stopVertexDrag);
  window.addEventListener('pointercancel', stopVertexDrag);
  cleanupFns.push(() => {
    window.removeEventListener('pointermove', moveActiveVertex);
    window.removeEventListener('pointerup', stopVertexDrag);
    window.removeEventListener('pointercancel', stopVertexDrag);
  });

  const root = labRef.value;
  if (!root) return;

  const revealTargets = Array.from(root.querySelectorAll<HTMLElement>('.lab-reveal'));
  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (shouldReduceMotion) {
    revealTargets.forEach((target) => target.classList.add('is-visible'));
    return;
  }

  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (!entry.isIntersecting) return;
      entry.target.classList.add('is-visible');
      observer.unobserve(entry.target);
    });
  }, { threshold: 0.12 });

  revealTargets.forEach((target) => observer.observe(target));
  cleanupFns.push(() => observer.disconnect());

  window.setTimeout(() => {
    revealTargets.forEach((target) => {
      if (target.getBoundingClientRect().top < window.innerHeight) {
        target.classList.add('is-visible');
      }
    });
  }, 100);
});

onUnmounted(() => {
  cleanupFns.forEach((cleanup) => cleanup());
  window.clearTimeout(toastTimer);
});
</script>

<template>
  <section id="interactive-lab" ref="labRef" class="study-lab-section panel" aria-labelledby="study-lab-title">
    <div class="study-lab-grid-bg" aria-hidden="true"></div>
    <div class="study-lab-container">
      <header class="study-lab-header lab-reveal">
        <div>
          <span class="study-lab-eyebrow">
            <span class="live-dot"></span>
            Demo tương tác
          </span>
          <h2 id="study-lab-title" class="study-lab-title">
            Chạm vào dữ liệu học tập,
            <span>thấy động lực phản hồi ngay.</span>
          </h2>
        </div>
        <p class="study-lab-copy">
          Một mô phỏng vận hành ngay trong landing: kéo node mục tiêu, chỉnh nhịp nhiệm vụ,
          kiểm tra cơ chế thưởng và mở từng bước triển khai cho cộng đồng học tập.
        </p>
      </header>

      <div class="lab-marquee lab-reveal" aria-label="Các chế độ demo">
        <div class="lab-marquee__track">
          <span>mission map</span>
          <span>reward signal</span>
          <span>progress radar</span>
          <span>coin loop</span>
          <span>VIP unlock</span>
          <span>workspace pulse</span>
          <span>mission map</span>
          <span>reward signal</span>
          <span>progress radar</span>
          <span>coin loop</span>
          <span>VIP unlock</span>
          <span>workspace pulse</span>
        </div>
      </div>

      <div class="lab-workspace lab-workspace--geometry lab-reveal">
        <div class="lab-board">
          <div class="lab-board__topline">
            <span><span class="live-dot"></span> Mission map</span>
            <span>kéo node mục tiêu</span>
          </div>
          <svg
            ref="triangleSvgRef"
            class="triangle-svg"
            viewBox="0 0 600 450"
            role="img"
            aria-label="Bản đồ nhiệm vụ có ba node kéo được và tín hiệu cập nhật trực tiếp"
          >
            <defs>
              <pattern id="studyTriGrid" width="30" height="30" patternUnits="userSpaceOnUse">
                <path d="M 30 0 L 0 0 0 30" fill="none" stroke="rgba(240,235,225,0.055)" stroke-width="1" />
              </pattern>
              <linearGradient id="studyTriFill" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#22e0d6" stop-opacity="0.18" />
                <stop offset="100%" stop-color="#f8d66d" stop-opacity="0.05" />
              </linearGradient>
            </defs>

            <rect width="600" height="450" fill="url(#studyTriGrid)" />
            <path :d="trianglePath" fill="url(#studyTriFill)" class="triangle-path" />

            <g>
              <text
                v-for="side in sideLabels"
                :key="side.label"
                :x="side.x"
                :y="side.y"
                class="side-label"
                text-anchor="middle"
                dominant-baseline="central"
              >
                {{ side.label }} = {{ fmt(side.value) }}
              </text>
            </g>

            <g>
              <path v-for="arc in angleArcs" :key="arc.key" :d="arc.d" class="angle-arc" />
            </g>

            <circle :cx="triangleMetrics.centroid.x" :cy="triangleMetrics.centroid.y" r="4" class="centroid-dot" />
            <text
              :x="triangleMetrics.centroid.x + 12"
              :y="triangleMetrics.centroid.y + 4"
              class="centroid-label"
              text-anchor="middle"
            >
              G
            </text>

            <g
              v-for="key in vertexKeys"
              :key="key"
              class="vertex-handle"
              :class="{ dragging: activeVertex === key }"
              :transform="`translate(${triangle[key].x}, ${triangle[key].y})`"
              role="button"
              tabindex="0"
              :aria-label="`Node mục tiêu ${key}`"
              @pointerdown="startVertexDrag(key, $event)"
            >
              <circle r="18" class="vertex-ring" />
              <circle r="6" class="vertex-dot" />
              <text class="vertex-label" text-anchor="middle" dominant-baseline="central" y="1">{{ key }}</text>
            </g>
          </svg>
        </div>

        <aside class="lab-readouts" aria-label="Tín hiệu vận hành nhiệm vụ">
          <div class="lab-board__topline">
            <span><span class="live-dot"></span> Live signals</span>
            <span>engagement score</span>
          </div>
          <div class="metric-grid">
            <div class="metric-cell">
              <span class="metric-label">độ khó</span>
              <strong>{{ fmt(triangleMetrics.sideA) }}<small>pt</small></strong>
            </div>
            <div class="metric-cell">
              <span class="metric-label">nỗ lực</span>
              <strong>{{ fmt(triangleMetrics.sideB) }}<small>pt</small></strong>
            </div>
            <div class="metric-cell">
              <span class="metric-label">giá trị</span>
              <strong>{{ fmt(triangleMetrics.sideC) }}<small>pt</small></strong>
            </div>
            <div class="metric-cell">
              <span class="metric-label">tổng lực</span>
              <strong>{{ fmt(triangleMetrics.perimeter) }}<small>pt</small></strong>
            </div>
            <div class="metric-cell">
              <span class="metric-label">node A</span>
              <strong>{{ fmt(triangleMetrics.angleA / 1.8) }}<small>%</small></strong>
            </div>
            <div class="metric-cell">
              <span class="metric-label">node B</span>
              <strong>{{ fmt(triangleMetrics.angleB / 1.8) }}<small>%</small></strong>
            </div>
            <div class="metric-cell">
              <span class="metric-label">node C</span>
              <strong>{{ fmt(triangleMetrics.angleC / 1.8) }}<small>%</small></strong>
            </div>
            <div class="metric-cell metric-cell--accent">
              <span class="metric-label">động lượng</span>
              <strong>{{ fmt(triangleMetrics.area / 100) }}<small>x</small></strong>
            </div>
          </div>
          <div class="classification-box">
            <span>trạng thái vòng lặp</span>
            <strong>{{ triangleMetrics.classification }}</strong>
          </div>
        </aside>
      </div>

      <div class="lab-workspace lab-workspace--plot lab-reveal">
        <div class="lab-board">
          <div class="lab-board__topline">
            <span><span class="live-dot"></span> Engagement curve</span>
            <span>tuần học -5 -> 5</span>
          </div>

          <svg class="plot-svg" viewBox="0 0 600 450" role="img" aria-label="Đồ thị động lực học tập thay đổi theo thanh trượt">
            <defs>
              <linearGradient id="studyCurveGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#ef5da8" />
                <stop offset="50%" stop-color="#f8d66d" />
                <stop offset="100%" stop-color="#22e0d6" />
              </linearGradient>
              <filter id="studyCurveGlow">
                <feGaussianBlur stdDeviation="2.5" result="blur" />
                <feMerge>
                  <feMergeNode in="blur" />
                  <feMergeNode in="SourceGraphic" />
                </feMerge>
              </filter>
            </defs>

            <g>
              <line
                v-for="x in xTicks"
                :key="`x-${x}`"
                :x1="scaleX(x)"
                :x2="scaleX(x)"
                :y1="plotPadding.top"
                :y2="plotPadding.top + plotInnerHeight"
                :class="['plot-grid-line', { 'plot-grid-line--axis': x === 0 }]"
              />
              <line
                v-for="y in yTicks"
                :key="`y-${y}`"
                :x1="plotPadding.left"
                :x2="plotPadding.left + plotInnerWidth"
                :y1="scaleY(y)"
                :y2="scaleY(y)"
                :class="['plot-grid-line', { 'plot-grid-line--axis': y === 0 }]"
              />
            </g>

            <g class="plot-axis-labels">
              <text v-for="x in xTicks.filter((tick) => tick !== 0)" :key="`xl-${x}`" :x="scaleX(x)" :y="plotPadding.top + plotInnerHeight + 18" text-anchor="middle">{{ x }}</text>
              <text v-for="y in yTicks.filter((tick) => tick !== 0)" :key="`yl-${y}`" :x="plotPadding.left - 8" :y="scaleY(y) + 3" text-anchor="end">{{ y }}</text>
              <text :x="scaleX(0) - 8" :y="scaleY(0) + 16" text-anchor="end">0</text>
            </g>

            <path :d="plotState.fillPath" class="curve-fill" />
            <path :d="plotState.curvePath" class="curve-path" />

            <g>
              <circle :cx="scaleX(0)" :cy="scaleY(plotState.yIntercept)" r="5" class="plot-point plot-point--intercept" />
              <circle
                v-for="root in plotState.roots"
                :key="`root-${root}`"
                :cx="scaleX(root)"
                :cy="scaleY(0)"
                r="5"
                class="plot-point plot-point--root"
              />
              <circle
                v-for="critical in plotState.criticals"
                :key="`critical-${critical}`"
                :cx="scaleX(critical)"
                :cy="scaleY(evaluateCurve(critical))"
                r="5"
                class="plot-point plot-point--critical"
              />
            </g>
          </svg>
        </div>

        <aside class="lab-controls">
          <div class="lab-board__topline">
            <span><span class="live-dot"></span> Mission parameters</span>
            <span>điều chỉnh tức thì</span>
          </div>
          <div class="equation-display" aria-label="Tín hiệu nhiệm vụ hiện tại">
            <span class="num">{{ coeffs.a.toFixed(2) }}</span><span class="var">x3</span>
            <span class="op">{{ coeffs.b >= 0 ? '+' : '-' }}</span>
            <span class="num">{{ Math.abs(coeffs.b).toFixed(2) }}</span><span class="var">x2</span>
            <span class="op">{{ coeffs.c >= 0 ? '+' : '-' }}</span>
            <span class="num">{{ Math.abs(coeffs.c).toFixed(2) }}</span><span class="var">x</span>
            <span class="op">{{ coeffs.d >= 0 ? '+' : '-' }}</span>
            <span class="num">{{ Math.abs(coeffs.d).toFixed(2) }}</span>
          </div>

          <label v-for="control in coefficientControls" :key="control.key" class="slider-row">
            <span class="slider-header">
              <span>{{ control.label }} <strong>{{ control.key }}</strong></span>
              <output>{{ coeffs[control.key].toFixed(2) }}</output>
            </span>
            <input
              v-model.number="coeffs[control.key]"
              type="range"
              :min="control.min"
              :max="control.max"
              :step="control.step"
            />
          </label>

          <div class="feature-list">
            <div><span>điểm khởi động</span><strong>{{ plotState.yIntercept.toFixed(2) }}</strong></div>
            <div><span>độ phân tách</span><strong>{{ plotState.quadraticDiscriminant.toFixed(2) }}</strong></div>
            <div><span>điểm cân bằng</span><strong>{{ plotState.roots.length ? plotState.roots.map((root) => root.toFixed(2)).join(', ') : 'chưa có' }}</strong></div>
            <div><span>điểm đổi chiều</span><strong>{{ plotState.criticals.length ? plotState.criticals.map((critical) => critical.toFixed(2)).join(', ') : 'chưa có' }}</strong></div>
          </div>
        </aside>
      </div>

      <div class="quiz-panel lab-reveal">
        <div class="quiz-progress">
          <div>
            <span>đã phản hồi</span>
            <strong>{{ quizDone }}<small>/{{ quizData.length }}</small></strong>
          </div>
          <div>
            <span>đúng cơ chế</span>
            <strong class="good">{{ quizCorrect }}</strong>
          </div>
          <div>
            <span>độ khớp</span>
            <strong>{{ quizAccuracy }}</strong>
          </div>
        </div>

        <div class="quiz-grid">
          <article v-for="(item, qIndex) in quizData" :key="item.question" class="quiz-card">
            <div class="quiz-question-num">TÍN HIỆU {{ String(qIndex + 1).padStart(2, '0') }}</div>
            <h3>{{ item.question }}</h3>
            <div class="quiz-options">
              <button
                v-for="(option, optionIndex) in item.options"
                :key="option"
                type="button"
                class="quiz-option"
                :class="{
                  answered: answeredOptions[qIndex] !== null,
                  correct: answeredOptions[qIndex] === optionIndex && optionIndex === item.correct,
                  incorrect: answeredOptions[qIndex] === optionIndex && optionIndex !== item.correct,
                  'reveal-correct': answeredOptions[qIndex] !== null && answeredOptions[qIndex] !== item.correct && optionIndex === item.correct
                }"
                :disabled="answeredOptions[qIndex] !== null"
                @click="answerQuiz(qIndex, optionIndex)"
              >
                <span class="letter">{{ String.fromCharCode(65 + optionIndex) }}</span>
                <span>{{ option }}</span>
              </button>
            </div>
            <p
              v-if="answeredOptions[qIndex] !== null"
              :class="['quiz-feedback', answeredOptions[qIndex] === item.correct ? 'correct' : 'incorrect']"
            >
              <strong>{{ answeredOptions[qIndex] === item.correct ? 'Đúng.' : 'Chưa đúng.' }}</strong>
              {{ item.feedback }}
            </p>
          </article>
        </div>
      </div>

      <div class="solution-panel lab-reveal">
        <div class="problem-card">
          <span>Deployment flow</span>
          <h3>Từ kho tài liệu đến vòng lặp học tập có thưởng</h3>
          <p>Nhấn từng bước để thấy cách hệ thống biến nội dung tĩnh thành hành trình học có phản hồi.</p>
        </div>

        <div class="steps-list">
          <article
            v-for="(step, index) in stepsData"
            :key="step.title"
            :ref="(element) => setStepRef(element, index)"
            :class="['step-item', { revealed: index < revealedSteps, final: step.final }]"
          >
            <div class="step-num">{{ index + 1 }}</div>
            <h4>{{ step.title }}</h4>
            <div class="step-expression">{{ step.expression }}</div>
            <p>{{ step.note }}</p>
          </article>
        </div>

        <div class="reveal-controls">
          <button type="button" class="reveal-btn" :disabled="revealedSteps >= stepsData.length" @click="revealNextStep">
            {{ revealedSteps >= stepsData.length ? 'Hoàn tất' : `Mở bước ${revealedSteps + 1}` }}
          </button>
          <button type="button" class="reset-btn" @click="resetSteps">Reset</button>
        </div>
      </div>
    </div>

    <div :class="['lab-toast', { show: toastVisible }]" role="status" aria-live="polite">
      <span class="toast-dot"></span>
      <span>{{ toastMessage }}</span>
    </div>
  </section>
</template>

<style scoped>
.study-lab-section {
  position: relative;
  overflow: hidden;
  min-height: 100vh;
  background:
    radial-gradient(circle at 12% 10%, oklch(0.78 0.18 195 / 13%), transparent 32%),
    radial-gradient(circle at 86% 18%, oklch(0.72 0.22 330 / 11%), transparent 34%),
    linear-gradient(145deg, oklch(0.075 0.02 280 / 72%), oklch(0.115 0.035 286 / 68%) 56%, oklch(0.085 0.018 245 / 72%));
  color: oklch(0.96 0.01 240);
  padding: clamp(4rem, 8vw, 7rem) 1.5rem;
  isolation: isolate;
}

.study-lab-grid-bg {
  position: absolute;
  inset: 0;
  z-index: -1;
  background-image:
    linear-gradient(oklch(0.96 0.01 240 / 3.5%) 1px, transparent 1px),
    linear-gradient(90deg, oklch(0.96 0.01 240 / 3.5%) 1px, transparent 1px);
  background-size: 60px 60px;
  mask-image: linear-gradient(to bottom, transparent, black 12%, black 88%, transparent);
}

.study-lab-container {
  width: min(1400px, 100%);
  margin: 0 auto;
}

.study-lab-header {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(260px, 0.8fr);
  gap: 2rem;
  align-items: end;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid oklch(0.7 0.1 280 / 16%);
}

.study-lab-eyebrow,
.lab-board__topline,
.metric-label,
.classification-box span,
.quiz-question-num,
.quiz-progress span,
.problem-card span {
  font-family: var(--font-sans);
  font-size: 0.68rem;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.study-lab-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 0.65rem;
  color: oklch(0.82 0.16 195);
  margin-bottom: 1rem;
}

.live-dot {
  width: 0.45rem;
  height: 0.45rem;
  border-radius: 999px;
  display: inline-block;
  background: oklch(0.82 0.16 195);
  box-shadow: 0 0 12px oklch(0.82 0.16 195 / 70%);
  position: relative;
}

.live-dot::after {
  content: '';
  position: absolute;
  inset: -0.28rem;
  border: 1px solid oklch(0.82 0.16 195 / 60%);
  border-radius: inherit;
  animation: livePulse 2s infinite;
}

.study-lab-title {
  max-width: 920px;
  font-family: var(--font-sans);
  font-size: clamp(2.2rem, 5vw, 4.6rem);
  line-height: 1.02;
  font-weight: 800;
  margin: 0;
  letter-spacing: 0;
}

.study-lab-title span {
  display: block;
  color: oklch(0.82 0.16 195);
}

.study-lab-copy {
  color: oklch(0.82 0.04 240 / 78%);
  line-height: 1.75;
  margin: 0;
}

.lab-marquee {
  overflow: hidden;
  margin: 2rem 0;
  padding: 1rem 0;
  border-top: 1px solid oklch(0.7 0.1 280 / 14%);
  border-bottom: 1px solid oklch(0.7 0.1 280 / 14%);
}

.lab-marquee__track {
  display: flex;
  width: max-content;
  gap: 3rem;
  white-space: nowrap;
  animation: labMarqueeMove 46s linear infinite;
  color: oklch(0.82 0.04 240 / 62%);
  font-weight: 700;
  font-size: 1.1rem;
}

.lab-marquee__track span {
  display: inline-flex;
  gap: 3rem;
  align-items: center;
}

.lab-marquee__track span::after {
  content: '';
  width: 0.38rem;
  height: 0.38rem;
  border-radius: 999px;
  background: oklch(0.82 0.16 195);
}

.lab-workspace {
  display: grid;
  gap: 1.5rem;
  margin-top: 2rem;
}

.lab-workspace--geometry,
.lab-workspace--plot {
  grid-template-columns: minmax(0, 1.35fr) minmax(300px, 0.65fr);
}

.lab-board,
.lab-readouts,
.lab-controls,
.quiz-panel,
.solution-panel,
.problem-card,
.quiz-card {
  border: 1px solid oklch(0.7 0.1 280 / 16%);
  background: linear-gradient(145deg, oklch(0.15 0.032 280 / 76%), oklch(0.105 0.024 280 / 72%));
  box-shadow: 0 18px 60px oklch(0 0 0 / 34%);
  backdrop-filter: blur(18px);
  border-radius: 8px;
}

.lab-board,
.lab-readouts,
.lab-controls,
.quiz-panel,
.solution-panel {
  padding: clamp(1rem, 2vw, 1.6rem);
}

.lab-board__topline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  color: oklch(0.76 0.04 240 / 72%);
}

.lab-board__topline span {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.triangle-svg,
.plot-svg {
  display: block;
  width: 100%;
  aspect-ratio: 4 / 3;
  min-height: 260px;
  border-radius: 6px;
  background: oklch(0.08 0.018 275 / 74%);
}

.triangle-svg {
  cursor: crosshair;
  touch-action: none;
}

.triangle-path {
  stroke: oklch(0.82 0.16 195);
  stroke-width: 2;
  stroke-linejoin: round;
  filter: drop-shadow(0 0 12px oklch(0.82 0.16 195 / 34%));
}

.side-label,
.centroid-label {
  font-family: var(--font-sans);
  font-size: 13px;
  fill: oklch(0.84 0.04 240 / 72%);
}

.angle-arc {
  fill: none;
  stroke: oklch(0.84 0.16 85);
  stroke-width: 1.5;
  opacity: 0.72;
}

.centroid-dot {
  fill: oklch(0.72 0.22 330);
}

.centroid-label {
  fill: oklch(0.72 0.22 330);
  font-size: 10px;
}

.vertex-handle {
  cursor: grab;
  transition: filter 0.15s ease;
}

.vertex-handle:hover,
.vertex-handle:focus-visible,
.vertex-handle.dragging {
  filter: drop-shadow(0 0 12px oklch(0.82 0.16 195 / 70%));
}

.vertex-handle.dragging {
  cursor: grabbing;
}

.vertex-ring {
  fill: oklch(0.82 0.16 195 / 8%);
  stroke: oklch(0.82 0.16 195);
  stroke-width: 1.5;
}

.vertex-dot {
  fill: oklch(0.82 0.16 195);
}

.vertex-label {
  fill: oklch(0.08 0.02 280);
  font-family: var(--font-sans);
  font-size: 18px;
  font-weight: 800;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
}

.metric-cell,
.classification-box,
.feature-list div {
  border: 1px solid oklch(0.7 0.1 280 / 14%);
  background: oklch(0.13 0.026 280 / 68%);
  border-radius: 6px;
  padding: 0.85rem;
}

.metric-cell {
  min-height: 84px;
}

.metric-cell--accent strong {
  color: oklch(0.82 0.16 195);
}

.metric-label {
  display: block;
  color: oklch(0.72 0.04 240 / 72%);
  margin-bottom: 0.45rem;
}

.metric-cell strong,
.classification-box strong,
.quiz-progress strong {
  display: block;
  font-size: clamp(1.2rem, 2vw, 1.65rem);
  line-height: 1;
}

.metric-cell small,
.quiz-progress small {
  font-size: 0.72rem;
  color: oklch(0.72 0.04 240 / 70%);
  margin-left: 0.2rem;
}

.classification-box {
  margin-top: 0.85rem;
  background: linear-gradient(135deg, oklch(0.82 0.16 195 / 12%), oklch(0.84 0.16 85 / 8%));
  border-color: oklch(0.82 0.16 195 / 26%);
}

.classification-box span {
  display: block;
  color: oklch(0.82 0.16 195);
  margin-bottom: 0.45rem;
}

.classification-box strong {
  font-size: 1.2rem;
}

.plot-grid-line {
  stroke: oklch(0.96 0.01 240 / 7%);
  stroke-width: 1;
}

.plot-grid-line--axis {
  stroke: oklch(0.96 0.01 240 / 26%);
  stroke-width: 1.5;
}

.plot-axis-labels text {
  fill: oklch(0.72 0.04 240 / 64%);
  font-family: var(--font-sans);
  font-size: 10px;
}

.curve-fill {
  fill: oklch(0.82 0.16 195 / 7%);
}

.curve-path {
  fill: none;
  stroke: url(#studyCurveGrad);
  stroke-width: 2.5;
  stroke-linejoin: round;
  stroke-linecap: round;
  filter: url(#studyCurveGlow);
}

.plot-point {
  stroke: oklch(0.08 0.02 280);
  stroke-width: 2;
}

.plot-point--intercept {
  fill: oklch(0.84 0.16 85);
}

.plot-point--root {
  fill: oklch(0.82 0.16 195);
}

.plot-point--critical {
  fill: oklch(0.72 0.22 330);
}

.equation-display {
  border: 1px solid oklch(0.7 0.1 280 / 14%);
  background: oklch(0.08 0.018 275 / 64%);
  border-radius: 6px;
  padding: 1rem;
  text-align: center;
  font-size: 1.1rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.equation-display .num {
  color: oklch(0.82 0.16 195);
}

.equation-display .var {
  color: oklch(0.96 0.01 240);
  margin-left: 0.08rem;
}

.equation-display .op {
  color: oklch(0.76 0.04 240 / 74%);
  margin: 0 0.35rem;
}

.slider-row {
  display: block;
  margin-bottom: 1rem;
}

.slider-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 1rem;
  margin-bottom: 0.5rem;
  color: oklch(0.88 0.04 240);
  font-weight: 700;
}

.slider-header strong,
.slider-header output {
  color: oklch(0.82 0.16 195);
}

.slider-row input[type='range'] {
  width: 100%;
  height: 4px;
  appearance: none;
  border-radius: 999px;
  background: oklch(0.96 0.01 240 / 12%);
  outline: none;
}

.slider-row input[type='range']::-webkit-slider-thumb {
  width: 18px;
  height: 18px;
  appearance: none;
  border: 3px solid oklch(0.12 0.03 280);
  border-radius: 50%;
  background: oklch(0.82 0.16 195);
  box-shadow: 0 0 0 1px oklch(0.82 0.16 195), 0 0 18px oklch(0.82 0.16 195 / 55%);
  cursor: pointer;
  transition: transform 0.15s ease;
}

.slider-row input[type='range']::-webkit-slider-thumb:hover {
  transform: scale(1.16);
}

.slider-row input[type='range']::-moz-range-thumb {
  width: 14px;
  height: 14px;
  border: 3px solid oklch(0.12 0.03 280);
  border-radius: 50%;
  background: oklch(0.82 0.16 195);
  box-shadow: 0 0 0 1px oklch(0.82 0.16 195), 0 0 18px oklch(0.82 0.16 195 / 55%);
  cursor: pointer;
}

.feature-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.6rem;
  margin-top: 1rem;
}

.feature-list div {
  display: flex;
  justify-content: space-between;
  gap: 0.7rem;
  font-size: 0.78rem;
}

.feature-list span {
  color: oklch(0.72 0.04 240 / 70%);
}

.feature-list strong {
  color: oklch(0.96 0.01 240);
  text-align: right;
}

.quiz-panel,
.solution-panel {
  margin-top: 1.5rem;
}

.quiz-progress {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1rem;
  border: 1px solid oklch(0.7 0.1 280 / 14%);
  background: oklch(0.08 0.018 275 / 48%);
  border-radius: 8px;
  padding: 1rem;
}

.quiz-progress div:nth-child(2) {
  text-align: center;
}

.quiz-progress div:nth-child(3) {
  text-align: right;
}

.quiz-progress span {
  display: block;
  color: oklch(0.72 0.04 240 / 68%);
  margin-bottom: 0.35rem;
}

.quiz-progress .good {
  color: oklch(0.75 0.16 165);
}

.quiz-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.quiz-card {
  padding: 1.1rem;
}

.quiz-question-num {
  color: oklch(0.82 0.16 195);
  margin-bottom: 0.5rem;
}

.quiz-card h3 {
  margin: 0 0 1rem;
  font-size: 1rem;
  line-height: 1.45;
}

.quiz-options {
  display: grid;
  gap: 0.65rem;
}

.quiz-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  border: 1px solid oklch(0.7 0.1 280 / 20%);
  background: oklch(0.13 0.026 280 / 70%);
  color: oklch(0.96 0.01 240);
  border-radius: 7px;
  padding: 0.8rem 0.9rem;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.quiz-option:hover:not(:disabled) {
  border-color: oklch(0.82 0.16 195);
  background: oklch(0.16 0.032 280 / 80%);
  transform: translateX(4px);
}

.quiz-option:disabled {
  cursor: default;
}

.letter {
  display: inline-flex;
  width: 28px;
  height: 28px;
  flex: 0 0 28px;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: oklch(0.08 0.02 280);
  color: oklch(0.82 0.16 195);
  font-size: 0.78rem;
  font-weight: 800;
}

.quiz-option.correct {
  animation: correctPulse 1.4s forwards;
}

.quiz-option.incorrect {
  animation: incorrectPulse 1.4s forwards;
}

.quiz-option.reveal-correct {
  animation: revealCorrect 0.6s forwards;
}

.quiz-feedback {
  margin: 0.9rem 0 0;
  padding: 0.75rem 0.85rem;
  border-radius: 7px;
  font-size: 0.9rem;
  line-height: 1.55;
  animation: fadeIn 0.35s ease;
}

.quiz-feedback.correct {
  border: 1px solid oklch(0.75 0.16 165 / 35%);
  background: oklch(0.75 0.16 165 / 10%);
  color: oklch(0.83 0.13 165);
}

.quiz-feedback.incorrect {
  border: 1px solid oklch(0.66 0.22 25 / 35%);
  background: oklch(0.66 0.22 25 / 10%);
  color: oklch(0.75 0.2 25);
}

.solution-panel {
  display: grid;
  grid-template-columns: minmax(260px, 0.45fr) minmax(0, 0.55fr);
  gap: 1.25rem;
}

.problem-card {
  padding: 1.5rem;
  align-self: start;
  position: relative;
  overflow: hidden;
}

.problem-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, oklch(0.82 0.16 195), oklch(0.84 0.16 85), oklch(0.72 0.22 330));
}

.problem-card span {
  color: oklch(0.82 0.16 195);
}

.problem-card h3 {
  font-size: clamp(1.35rem, 2vw, 1.9rem);
  line-height: 1.2;
  margin: 0.8rem 0;
}

.problem-card p {
  margin: 0;
  color: oklch(0.82 0.04 240 / 74%);
  line-height: 1.7;
}

.steps-list {
  position: relative;
}

.steps-list::before {
  content: '';
  position: absolute;
  left: 19px;
  top: 18px;
  bottom: 18px;
  width: 1px;
  background: oklch(0.7 0.1 280 / 18%);
}

.step-item {
  position: relative;
  overflow: hidden;
  max-height: 0;
  padding: 0 0 0 3.5rem;
  opacity: 0;
  transform: translateX(-12px);
  transition: opacity 0.55s ease, transform 0.55s ease, max-height 0.55s ease, padding 0.55s ease;
}

.step-item.revealed {
  max-height: 360px;
  padding-bottom: 1.35rem;
  opacity: 1;
  transform: translateX(0);
}

.step-num {
  position: absolute;
  left: 0;
  top: 0;
  z-index: 1;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid oklch(0.82 0.16 195);
  border-radius: 999px;
  background: oklch(0.12 0.03 280);
  color: oklch(0.82 0.16 195);
  font-weight: 800;
}

.step-item.final .step-num {
  background: oklch(0.82 0.16 195);
  color: oklch(0.08 0.02 280);
}

.step-item h4 {
  margin: 0 0 0.5rem;
  font-size: 1.05rem;
}

.step-expression {
  display: inline-block;
  border: 1px solid oklch(0.7 0.1 280 / 18%);
  background: oklch(0.08 0.018 275 / 62%);
  border-radius: 6px;
  padding: 0.7rem 0.85rem;
  color: oklch(0.82 0.16 195);
  font-weight: 800;
  margin-bottom: 0.55rem;
}

.step-item p {
  margin: 0;
  color: oklch(0.82 0.04 240 / 72%);
  line-height: 1.6;
}

.reveal-controls {
  grid-column: 2;
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.reveal-btn,
.reset-btn {
  border-radius: 7px;
  padding: 0.78rem 1.15rem;
  border: 1px solid transparent;
  font-weight: 800;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.reveal-btn {
  background: linear-gradient(135deg, oklch(0.82 0.16 195), oklch(0.84 0.16 85));
  color: oklch(0.08 0.02 280);
  box-shadow: 0 10px 32px oklch(0.82 0.16 195 / 24%);
}

.reveal-btn:hover:not(:disabled),
.reset-btn:hover {
  transform: translateY(-2px);
}

.reveal-btn:disabled {
  cursor: not-allowed;
  opacity: 0.62;
  box-shadow: none;
}

.reset-btn {
  border-color: oklch(0.7 0.1 280 / 24%);
  background: transparent;
  color: oklch(0.86 0.04 240);
}

.reset-btn:hover {
  border-color: oklch(0.82 0.16 195 / 55%);
}

.lab-toast {
  position: fixed;
  left: 50%;
  bottom: 2rem;
  z-index: 1200;
  display: flex;
  align-items: center;
  gap: 0.65rem;
  max-width: calc(100vw - 2rem);
  transform: translateX(-50%) translateY(110px);
  border: 1px solid oklch(0.82 0.16 195 / 55%);
  border-radius: 999px;
  background: oklch(0.11 0.026 280 / 92%);
  color: oklch(0.96 0.01 240);
  box-shadow: 0 14px 44px oklch(0 0 0 / 42%), 0 0 28px oklch(0.82 0.16 195 / 16%);
  backdrop-filter: blur(16px);
  padding: 0.85rem 1.2rem;
  font-weight: 800;
  transition: transform 0.38s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.lab-toast.show {
  transform: translateX(-50%) translateY(0);
}

.toast-dot {
  width: 0.55rem;
  height: 0.55rem;
  border-radius: 999px;
  background: oklch(0.82 0.16 195);
  box-shadow: 0 0 14px oklch(0.82 0.16 195 / 60%);
}

.lab-reveal {
  opacity: 0;
  transform: translateY(40px);
  transition: opacity 0.9s ease, transform 0.9s ease;
}

.lab-reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}

@keyframes livePulse {
  0% {
    opacity: 1;
    transform: scale(0.8);
  }

  100% {
    opacity: 0;
    transform: scale(2);
  }
}

@keyframes labMarqueeMove {
  from {
    transform: translateX(0);
  }

  to {
    transform: translateX(-50%);
  }
}

@keyframes correctPulse {
  0% {
    background: oklch(0.13 0.026 280 / 70%);
    border-color: oklch(0.7 0.1 280 / 20%);
    transform: scale(1);
    box-shadow: 0 0 0 oklch(0.75 0.16 165 / 0%);
  }

  20% {
    background: oklch(0.75 0.16 165);
    border-color: oklch(0.75 0.16 165);
    color: oklch(0.08 0.02 280);
    transform: scale(1.03);
    box-shadow: 0 0 60px oklch(0.75 0.16 165 / 55%);
  }

  100% {
    background: oklch(0.75 0.16 165 / 18%);
    border-color: oklch(0.75 0.16 165);
    transform: scale(1);
  }
}

@keyframes incorrectPulse {
  0% {
    background: oklch(0.13 0.026 280 / 70%);
    border-color: oklch(0.7 0.1 280 / 20%);
    transform: scale(1);
  }

  20% {
    background: oklch(0.66 0.22 25);
    border-color: oklch(0.66 0.22 25);
    transform: scale(0.98);
    box-shadow: 0 0 60px oklch(0.66 0.22 25 / 50%);
  }

  100% {
    background: oklch(0.66 0.22 25 / 18%);
    border-color: oklch(0.66 0.22 25);
    transform: scale(1);
  }
}

@keyframes revealCorrect {
  0% {
    background: oklch(0.13 0.026 280 / 70%);
    border-color: oklch(0.7 0.1 280 / 20%);
  }

  100% {
    background: oklch(0.75 0.16 165 / 18%);
    border-color: oklch(0.75 0.16 165);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1100px) {
  .study-lab-header,
  .lab-workspace--geometry,
  .lab-workspace--plot,
  .solution-panel {
    grid-template-columns: 1fr;
  }

  .reveal-controls {
    grid-column: auto;
  }
}

@media (max-width: 760px) {
  .study-lab-section {
    padding: 4rem 1rem;
  }

  .study-lab-header {
    gap: 1rem;
  }

  .metric-grid,
  .feature-list,
  .quiz-grid,
  .quiz-progress {
    grid-template-columns: 1fr;
  }

  .quiz-progress div,
  .quiz-progress div:nth-child(2),
  .quiz-progress div:nth-child(3) {
    text-align: left;
  }

  .lab-board__topline {
    align-items: flex-start;
    flex-direction: column;
  }

  .solution-panel {
    padding: 1rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .live-dot::after,
  .lab-marquee__track {
    animation: none;
  }

  .lab-reveal,
  .step-item,
  .quiz-option,
  .lab-toast {
    transition-duration: 0.001ms;
  }
}
</style>
