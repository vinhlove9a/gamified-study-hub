<script setup lang="ts">
import { computed, ref } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';

type ModuleStatus = 'completed' | 'current' | 'locked';

interface LessonItem {
  id: string;
  title: string;
  duration: string;
  status: ModuleStatus;
}

interface LessonModule {
  id: string;
  title: string;
  desc: string;
  xp: number;
  reward: number;
  status: ModuleStatus;
  lessons: LessonItem[];
}

interface LearningPath {
  id: string;
  name: string;
  tagline: string;
  code: string;
  modules: LessonModule[];
}

const paths: LearningPath[] = [
  {
    id: 'math',
    code: 'MATH',
    name: 'Nền tảng Toán',
    tagline: 'Từ số học đến giải tích',
    modules: [
      {
        id: 'm1',
        title: 'Số học cơ bản',
        desc: 'Phép tính, phân số và số thập phân.',
        xp: 120,
        reward: 30,
        status: 'completed',
        lessons: [
          { id: 'm1-1', title: 'Thứ tự phép tính', duration: '08:00', status: 'completed' },
          { id: 'm1-2', title: 'Phân số và tỉ lệ', duration: '12:00', status: 'completed' },
          { id: 'm1-3', title: 'Số thập phân', duration: '10:00', status: 'completed' }
        ]
      },
      {
        id: 'm2',
        title: 'Đại số nhập môn',
        desc: 'Biến số, biểu thức và phương trình bậc nhất.',
        xp: 180,
        reward: 45,
        status: 'completed',
        lessons: [
          { id: 'm2-1', title: 'Biểu thức đại số', duration: '14:00', status: 'completed' },
          { id: 'm2-2', title: 'Rút gọn biểu thức', duration: '16:00', status: 'completed' },
          { id: 'm2-3', title: 'Phương trình bậc nhất', duration: '18:00', status: 'completed' }
        ]
      },
      {
        id: 'm3',
        title: 'Hàm số & Đồ thị',
        desc: 'Hàm bậc nhất, bậc hai và cách đọc đồ thị.',
        xp: 220,
        reward: 60,
        status: 'current',
        lessons: [
          { id: 'm3-1', title: 'Khái niệm hàm số', duration: '15:00', status: 'completed' },
          { id: 'm3-2', title: 'Đồ thị hàm bậc nhất', duration: '20:00', status: 'current' },
          { id: 'm3-3', title: 'Đồ thị hàm bậc hai', duration: '22:00', status: 'locked' }
        ]
      },
      {
        id: 'm4',
        title: 'Lượng giác',
        desc: 'Sin, cos, tan và công thức biến đổi.',
        xp: 260,
        reward: 70,
        status: 'locked',
        lessons: [
          { id: 'm4-1', title: 'Tỉ số lượng giác', duration: '18:00', status: 'locked' },
          { id: 'm4-2', title: 'Công thức cơ bản', duration: '24:00', status: 'locked' }
        ]
      }
    ]
  },
  {
    id: 'eng',
    code: 'ENG',
    name: 'Tiếng Anh giao tiếp',
    tagline: 'Tự tin nói chuyện mỗi ngày',
    modules: [
      {
        id: 'e1',
        title: 'Phát âm nền tảng',
        desc: 'IPA, trọng âm và ngữ điệu.',
        xp: 100,
        reward: 25,
        status: 'completed',
        lessons: [
          { id: 'e1-1', title: 'Âm nguyên âm', duration: '11:00', status: 'completed' },
          { id: 'e1-2', title: 'Trọng âm từ', duration: '13:00', status: 'completed' }
        ]
      },
      {
        id: 'e2',
        title: 'Chào hỏi & Giới thiệu',
        desc: 'Mẫu câu giao tiếp hằng ngày.',
        xp: 150,
        reward: 40,
        status: 'completed',
        lessons: [
          { id: 'e2-1', title: 'Tự giới thiệu', duration: '09:00', status: 'completed' },
          { id: 'e2-2', title: 'Hỏi thông tin cá nhân', duration: '14:00', status: 'completed' }
        ]
      },
      {
        id: 'e3',
        title: 'Hội thoại mua sắm',
        desc: 'Hỏi giá, thương lượng và thanh toán.',
        xp: 190,
        reward: 50,
        status: 'current',
        lessons: [
          { id: 'e3-1', title: 'Hỏi giá', duration: '10:00', status: 'current' },
          { id: 'e3-2', title: 'Thanh toán', duration: '12:00', status: 'locked' }
        ]
      }
    ]
  },
  {
    id: 'web',
    code: 'WEB',
    name: 'Lập trình Web',
    tagline: 'Xây dựng ứng dụng từ đầu',
    modules: [
      {
        id: 'w1',
        title: 'HTML & CSS',
        desc: 'Cấu trúc trang và tạo kiểu cơ bản.',
        xp: 140,
        reward: 35,
        status: 'completed',
        lessons: [
          { id: 'w1-1', title: 'Semantic HTML', duration: '16:00', status: 'completed' },
          { id: 'w1-2', title: 'Layout với CSS', duration: '20:00', status: 'completed' }
        ]
      },
      {
        id: 'w2',
        title: 'JavaScript nền tảng',
        desc: 'Biến, hàm, vòng lặp và DOM.',
        xp: 200,
        reward: 55,
        status: 'current',
        lessons: [
          { id: 'w2-1', title: 'Biến và kiểu dữ liệu', duration: '18:00', status: 'completed' },
          { id: 'w2-2', title: 'Hàm và phạm vi', duration: '22:00', status: 'current' },
          { id: 'w2-3', title: 'DOM events', duration: '24:00', status: 'locked' }
        ]
      }
    ]
  }
];

const activePathId = ref(paths[0].id);
const openModuleId = ref('m3');
const activePath = computed(() => paths.find((path) => path.id === activePathId.value) ?? paths[0]);
const completedModules = computed(() => activePath.value.modules.filter((module) => module.status === 'completed').length);
const progressPct = computed(() => Math.round((completedModules.value / activePath.value.modules.length) * 100));
const earnedXp = computed(() => activePath.value.modules.filter((module) => module.status === 'completed').reduce((sum, module) => sum + module.xp, 0));
const totalXp = computed(() => activePath.value.modules.reduce((sum, module) => sum + module.xp, 0));
const activeModule = computed(() => activePath.value.modules.find((module) => module.id === openModuleId.value) ?? activePath.value.modules[0]);

function selectPath(id: string): void {
  activePathId.value = id;
  openModuleId.value = activePath.value.modules.find((module) => module.status === 'current')?.id ?? activePath.value.modules[0].id;
}

function toggleModule(module: LessonModule): void {
  if (module.status === 'locked') return;
  openModuleId.value = openModuleId.value === module.id ? '' : module.id;
}

function statusLabel(status: ModuleStatus): string {
  if (status === 'completed') return 'Hoàn thành';
  if (status === 'current') return 'Đang học';
  return 'Khóa';
}
</script>

<template>
  <div class="space-y-4 text-zinc-100">
    <section class="border border-border bg-zinc-950">
      <div class="grid gap-4 border-b border-border px-4 py-3 lg:grid-cols-[1fr_360px] lg:items-center">
        <div>
          <p class="text-xs font-semibold uppercase text-zinc-500">Track detail</p>
          <h1 class="mt-1 text-xl font-semibold text-zinc-100">{{ activePath.name }}</h1>
          <p class="mt-1 text-sm text-zinc-500">{{ activePath.tagline }}</p>
        </div>
        <div>
          <div class="mb-2 flex items-center justify-between text-xs">
            <span class="font-semibold uppercase text-zinc-500">Tiến độ lộ trình</span>
            <span class="font-mono text-zinc-300">{{ progressPct }}%</span>
          </div>
          <div class="h-2 border border-zinc-800 bg-zinc-950">
            <div class="h-full bg-primary" :style="{ width: `${progressPct}%` }" />
          </div>
          <div class="mt-2 flex justify-between text-xs text-zinc-500">
            <span>{{ completedModules }}/{{ activePath.modules.length }} module</span>
            <span>{{ earnedXp.toLocaleString('vi-VN') }}/{{ totalXp.toLocaleString('vi-VN') }} XP</span>
          </div>
        </div>
      </div>

      <div class="flex overflow-x-auto border-b border-border">
        <button
          v-for="path in paths"
          :key="path.id"
          type="button"
          class="min-w-52 border-r border-border px-4 py-3 text-left text-sm hover:bg-zinc-900"
          :class="path.id === activePathId ? 'bg-zinc-900 text-primary' : 'text-zinc-400'"
          @click="selectPath(path.id)"
        >
          <span class="block font-mono text-[0.7rem] text-zinc-500">{{ path.code }}</span>
          <span class="block font-semibold">{{ path.name }}</span>
        </button>
      </div>
    </section>

    <div class="grid gap-4 xl:grid-cols-[1fr_360px]">
      <section class="border border-border bg-zinc-950">
        <div class="border-b border-border px-4 py-3">
          <h2 class="text-sm font-semibold uppercase text-zinc-200">Modules & lessons</h2>
          <p class="text-xs text-zinc-500">Accordion không che khuất nội dung học</p>
        </div>

        <div class="divide-y divide-border">
          <article v-for="(module, index) in activePath.modules" :key="module.id" class="bg-zinc-950">
            <button
              type="button"
              class="grid w-full gap-3 px-4 py-3 text-left hover:bg-zinc-900/70 md:grid-cols-[44px_1fr_120px_120px_32px] md:items-center"
              :class="module.status === 'locked' ? 'cursor-not-allowed text-zinc-600' : 'text-zinc-200'"
              :disabled="module.status === 'locked'"
              @click="toggleModule(module)"
            >
              <span class="font-mono text-xs text-zinc-500">M{{ index + 1 }}</span>
              <span>
                <span class="block font-semibold">{{ module.title }}</span>
                <span class="block text-xs text-zinc-500">{{ module.desc }}</span>
              </span>
              <span class="font-mono text-xs text-zinc-500">{{ module.lessons.length }} lessons</span>
              <span class="border border-zinc-800 px-2 py-1 text-center text-xs" :class="module.status === 'current' ? 'text-primary' : 'text-zinc-400'">
                {{ statusLabel(module.status) }}
              </span>
              <AppIcon :name="openModuleId === module.id ? 'chevron-down' : 'chevron-right'" class="h-4 w-4 justify-self-end text-zinc-500" />
            </button>

            <div v-if="openModuleId === module.id" class="border-t border-border bg-zinc-900/40">
              <div class="grid gap-4 px-4 py-4 lg:grid-cols-[1fr_220px]">
                <ol class="divide-y divide-zinc-800 border border-zinc-800 bg-zinc-950">
                  <li v-for="lesson in module.lessons" :key="lesson.id" class="grid gap-3 px-3 py-2.5 text-sm sm:grid-cols-[1fr_90px_96px] sm:items-center">
                    <span class="font-medium" :class="lesson.status === 'locked' ? 'text-zinc-600' : 'text-zinc-200'">{{ lesson.title }}</span>
                    <span class="font-mono text-xs text-zinc-500">{{ lesson.duration }}</span>
                    <span class="border border-zinc-800 px-2 py-1 text-center text-xs" :class="lesson.status === 'current' ? 'text-primary' : 'text-zinc-400'">
                      {{ statusLabel(lesson.status) }}
                    </span>
                  </li>
                </ol>

                <div class="border border-zinc-800 bg-zinc-950 p-3">
                  <p class="text-xs font-semibold uppercase text-zinc-500">Module summary</p>
                  <dl class="mt-3 space-y-2 text-sm">
                    <div class="flex justify-between gap-4">
                      <dt class="text-zinc-500">XP</dt>
                      <dd class="font-mono text-zinc-200">{{ module.xp }}</dd>
                    </div>
                    <div class="flex justify-between gap-4">
                      <dt class="text-zinc-500">Reward</dt>
                      <dd class="font-mono text-zinc-200">{{ module.reward }}</dd>
                    </div>
                    <div class="flex justify-between gap-4">
                      <dt class="text-zinc-500">Status</dt>
                      <dd class="text-zinc-200">{{ statusLabel(module.status) }}</dd>
                    </div>
                  </dl>
                  <button
                    type="button"
                    class="mt-4 inline-flex w-full items-center justify-center gap-2 border border-primary bg-primary px-3 py-2 text-sm font-semibold text-primary-foreground disabled:border-zinc-800 disabled:bg-zinc-900 disabled:text-zinc-600"
                    :disabled="module.status === 'completed'"
                  >
                    <AppIcon :name="module.status === 'completed' ? 'check' : 'play'" class="h-4 w-4" />
                    {{ module.status === 'completed' ? 'Đã hoàn thành' : 'Tiếp tục' }}
                  </button>
                </div>
              </div>
            </div>
          </article>
        </div>
      </section>

      <aside class="space-y-4">
        <section class="border border-border bg-zinc-950">
          <div class="border-b border-border px-4 py-3">
            <h2 class="text-sm font-semibold uppercase text-zinc-200">Module đang mở</h2>
          </div>
          <div class="space-y-3 px-4 py-3">
            <p class="text-base font-semibold text-zinc-100">{{ activeModule.title }}</p>
            <p class="text-sm text-zinc-500">{{ activeModule.desc }}</p>
            <div class="grid grid-cols-3 border border-zinc-800 text-center text-xs">
              <div class="border-r border-zinc-800 p-2">
                <p class="font-mono text-zinc-200">{{ activeModule.lessons.length }}</p>
                <p class="text-zinc-500">Bài</p>
              </div>
              <div class="border-r border-zinc-800 p-2">
                <p class="font-mono text-zinc-200">{{ activeModule.xp }}</p>
                <p class="text-zinc-500">XP</p>
              </div>
              <div class="p-2">
                <p class="font-mono text-zinc-200">{{ activeModule.reward }}</p>
                <p class="text-zinc-500">Thưởng</p>
              </div>
            </div>
          </div>
        </section>

        <section class="border border-border bg-zinc-950">
          <div class="border-b border-border px-4 py-3">
            <h2 class="text-sm font-semibold uppercase text-zinc-200">Mốc thưởng</h2>
          </div>
          <ul class="divide-y divide-border">
            <li
              v-for="milestone in [
                { at: 25, label: 'Huy hiệu Đồng', reward: 100 },
                { at: 50, label: 'Huy hiệu Bạc', reward: 250 },
                { at: 75, label: 'Huy hiệu Vàng', reward: 500 },
                { at: 100, label: 'Hoàn thành lộ trình', reward: 1000 }
              ]"
              :key="milestone.at"
              class="grid grid-cols-[64px_1fr_72px] gap-3 px-4 py-3 text-sm"
            >
              <span class="font-mono text-xs text-zinc-500">{{ milestone.at }}%</span>
              <span :class="progressPct >= milestone.at ? 'text-zinc-100' : 'text-zinc-600'">{{ milestone.label }}</span>
              <span class="text-right font-mono text-xs text-zinc-500">+{{ milestone.reward }}</span>
            </li>
          </ul>
        </section>
      </aside>
    </div>
  </div>
</template>
