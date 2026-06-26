import { onBeforeUnmount, onMounted, ref, type Ref } from 'vue';

/**
 * Tracks an element's content-box size via ResizeObserver — the replacement for
 * Recharts' <ResponsiveContainer>. Charts render at real pixel dimensions so
 * strokes and axis text stay crisp (no viewBox stretching).
 */
export function useElementSize(target: Ref<HTMLElement | null>) {
  const width = ref(0);
  const height = ref(0);
  let observer: ResizeObserver | null = null;

  onMounted(() => {
    const el = target.value;
    if (!el) return;
    observer = new ResizeObserver((entries) => {
      const box = entries[0]?.contentRect;
      if (!box) return;
      width.value = box.width;
      height.value = box.height;
    });
    observer.observe(el);
    width.value = el.clientWidth;
    height.value = el.clientHeight;
  });

  onBeforeUnmount(() => observer?.disconnect());

  return { width, height };
}
