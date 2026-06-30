<script setup lang="ts">
import { computed } from 'vue';
import { useAuthSession } from '@/features/auth/authSession';
import AppIcon from '@/components/app/icons/AppIcon.vue';

const { currentUser } = useAuthSession();

const firstName = computed(() => {
  const full = currentUser.value?.fullName?.trim();
  if (full) return full.split(/\s+/).slice(-1)[0];
  return currentUser.value?.email?.split('@')[0] ?? 'ban';
});

const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 11) return 'Chao buoi sang';
  if (hour < 14) return 'Chao buoi trua';
  if (hour < 18) return 'Chao buoi chieu';
  return 'Chao buoi toi';
});

const dailyGoal = { done: 35, target: 60 };
const dailyPct = Math.round((dailyGoal.done / dailyGoal.target) * 100);
const level = { current: 7, xp: 1240, next: 2000 };
const xpPct = Math.round((level.xp / level.next) * 100);

const summaryRows = [
  { label: 'XP hien tai', value: level.xp.toLocaleString('vi-VN'), detail: `Cap ${level.current} - ${xpPct}% toi cap 8` },
  { label: 'Chuoi ngay hoc', value: '12 ngay', detail: 'Ky luc: 21 ngay' },
  { label: 'Muc tieu hom nay', value: `${dailyGoal.done}/${dailyGoal.target} phut`, detail: `${dailyPct}% hoan thanh` },
  { label: 'Xep hang nhom', value: '#3', detail: 'Trong nhom ban be' }
];

const activeTracks = [
  { code: 'MATH-03', title: 'Giai tich 1 - Gioi han & Lien tuc', owner: 'Toan hoc', progress: 62, xp: 420, next: 'Bai 3.4', eta: '12 phut' },
  { code: 'ENG-B2', title: 'Ngu phap tieng Anh B2', owner: 'Ngoai ngu', progress: 38, xp: 260, next: 'Unit 7', eta: '18 phut' },
  { code: 'HIS-02', title: 'Lich su Viet Nam hien dai', owner: 'Lich su', progress: 80, xp: 510, next: 'Phan 2', eta: '9 phut' },
  { code: 'WEB-01', title: 'JavaScript nen tang', owner: 'Lap trinh', progress: 44, xp: 310, next: 'Ham va Scope', eta: '22 phut' }
];

const missions = [
  { title: 'Doc 3 tai lieu moi', metric: '2/3', reward: 50, status: 'Dang lam' },
  { title: 'Hoan thanh 1 bai quiz', metric: '1/1', reward: 30, status: 'Xong' },
  { title: 'Hoc lien tuc 30 phut', metric: '18/30', reward: 40, status: 'Dang lam' },
  { title: 'Mo khoa bo suu tap VIP', metric: '0/1', reward: 100, status: 'Khoa' }
];

const weekly = [
  { label: 'T2', value: 45 },
  { label: 'T3', value: 30 },
  { label: 'T4', value: 60 },
  { label: 'T5', value: 20 },
  { label: 'T6', value: 50 },
  { label: 'T7', value: 70 },
  { label: 'CN', value: 35 }
];
const weekTotal = weekly.reduce((sum, d) => sum + d.value, 0);
const weeklyMax = Math.max(...weekly.map((d) => d.value));

const recentActivity = [
  { time: '10 phut truoc', text: 'Hoan thanh nhiem vu Hoan thanh 1 bai quiz' },
  { time: '1 gio truoc', text: 'Nhan 50 coin tu phan thuong hang ngay' },
  { time: '3 gio truoc', text: 'Doc xong Lich su Viet Nam hien dai' },
  { time: 'Hom qua', text: 'Thang hang len Bac tren bang xep hang' }
];
</script>

<template>
  <div class="space-y-4 text-zinc-100">
    <section class="border border-border bg-zinc-950">
      <div class="flex flex-col gap-3 border-b border-border px-4 py-3 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-xs font-semibold uppercase text-zinc-500">Learning dashboard</p>
          <h1 class="mt-1 text-xl font-semibold text-zinc-100">{{ greeting }}, {{ firstName }}</h1>
        </div>
        <button type="button" class="inline-flex items-center justify-center gap-2 border border-primary bg-primary px-3 py-2 text-sm font-semibold text-primary-foreground">
          <AppIcon name="play" class="h-4 w-4" />
          Tiep tuc hoc
        </button>
      </div>

      <div class="grid border-b border-border md:grid-cols-4">
        <div v-for="row in summaryRows" :key="row.label" class="border-b border-border px-4 py-3 md:border-b-0 md:border-r last:md:border-r-0">
          <p class="text-[0.7rem] font-semibold uppercase text-zinc-500">{{ row.label }}</p>
          <p class="mt-1 text-lg font-semibold tabular-nums text-zinc-100">{{ row.value }}</p>
          <p class="mt-0.5 text-xs text-zinc-500">{{ row.detail }}</p>
        </div>
      </div>

      <div class="px-4 py-3">
        <div class="mb-2 flex items-center justify-between text-xs">
          <span class="font-semibold uppercase text-zinc-500">Tien do muc tieu ngay</span>
          <span class="font-mono text-zinc-300">{{ dailyPct }}%</span>
        </div>
        <div class="h-2 border border-zinc-800 bg-zinc-950">
          <div class="h-full bg-primary" :style="{ width: `${dailyPct}%` }" />
        </div>
      </div>
    </section>

    <section class="border border-border bg-zinc-950">
      <div class="flex items-center justify-between border-b border-border px-4 py-3">
        <div>
          <h2 class="text-sm font-semibold uppercase text-zinc-200">Lo trinh dang hoc</h2>
          <p class="text-xs text-zinc-500">Bang uu tien hoc tap trong ngay</p>
        </div>
        <span class="text-xs text-zinc-500">{{ activeTracks.length }} lo trinh</span>
      </div>
      <div class="overflow-x-auto">
        <table class="min-w-full text-left text-sm">
          <thead class="border-b border-border bg-zinc-900/60 text-[0.7rem] uppercase text-zinc-500">
            <tr>
              <th class="px-4 py-2 font-semibold">Ma</th>
              <th class="px-4 py-2 font-semibold">Noi dung</th>
              <th class="px-4 py-2 font-semibold">Tien do</th>
              <th class="px-4 py-2 font-semibold">XP</th>
              <th class="px-4 py-2 font-semibold">Tiep theo</th>
              <th class="px-4 py-2 text-right font-semibold">Thao tac</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-border">
            <tr v-for="track in activeTracks" :key="track.code" class="bg-zinc-950 hover:bg-zinc-900/70">
              <td class="px-4 py-3 font-mono text-xs text-zinc-500">{{ track.code }}</td>
              <td class="px-4 py-3">
                <p class="font-medium text-zinc-100">{{ track.title }}</p>
                <p class="text-xs text-zinc-500">{{ track.owner }} - con {{ track.eta }}</p>
              </td>
              <td class="px-4 py-3">
                <div class="flex min-w-32 items-center gap-2">
                  <div class="h-2 flex-1 border border-zinc-800 bg-zinc-950">
                    <div class="h-full bg-primary" :style="{ width: `${track.progress}%` }" />
                  </div>
                  <span class="w-10 text-right font-mono text-xs text-zinc-400">{{ track.progress }}%</span>
                </div>
              </td>
              <td class="px-4 py-3 font-mono text-zinc-300">{{ track.xp }}</td>
              <td class="px-4 py-3 text-zinc-400">{{ track.next }}</td>
              <td class="px-4 py-3 text-right">
                <button type="button" class="border border-zinc-700 px-2.5 py-1 text-xs font-semibold text-zinc-200 hover:border-primary hover:text-primary">
                  Mo
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <div class="grid gap-4 xl:grid-cols-[1.5fr_1fr]">
      <section class="border border-border bg-zinc-950">
        <div class="flex items-center justify-between border-b border-border px-4 py-3">
          <div>
            <h2 class="text-sm font-semibold uppercase text-zinc-200">Nhiem vu hom nay</h2>
            <p class="text-xs text-zinc-500">Hoan thanh de giu nhip hoc</p>
          </div>
          <span class="text-xs text-zinc-500">{{ missions.filter((m) => m.status === 'Xong').length }}/{{ missions.length }} xong</span>
        </div>
        <ul class="divide-y divide-border">
          <li v-for="mission in missions" :key="mission.title" class="grid gap-3 px-4 py-3 text-sm sm:grid-cols-[1fr_80px_80px_96px] sm:items-center">
            <span class="font-medium text-zinc-200">{{ mission.title }}</span>
            <span class="font-mono text-xs text-zinc-500">{{ mission.metric }}</span>
            <span class="font-mono text-xs text-zinc-400">+{{ mission.reward }} XP</span>
            <span class="border border-zinc-800 px-2 py-1 text-center text-xs text-zinc-400">{{ mission.status }}</span>
          </li>
        </ul>
      </section>

      <section class="border border-border bg-zinc-950">
        <div class="border-b border-border px-4 py-3">
          <h2 class="text-sm font-semibold uppercase text-zinc-200">Thoi gian hoc tuan nay</h2>
          <p class="text-xs text-zinc-500">Tong {{ weekTotal }} phut</p>
        </div>
        <div class="flex h-44 items-end gap-2 px-4 py-4">
          <div v-for="day in weekly" :key="day.label" class="flex flex-1 flex-col items-center gap-2">
            <span class="font-mono text-[0.7rem] text-zinc-500">{{ day.value }}</span>
            <div class="flex h-28 w-full items-end border border-zinc-800 bg-zinc-950">
              <div class="w-full bg-zinc-700" :class="day.label === 'CN' ? 'bg-primary' : ''" :style="{ height: `${Math.max(6, (day.value / weeklyMax) * 100)}%` }" />
            </div>
            <span class="text-[0.7rem] text-zinc-500">{{ day.label }}</span>
          </div>
        </div>
      </section>
    </div>

    <section class="border border-border bg-zinc-950">
      <div class="border-b border-border px-4 py-3">
        <h2 class="text-sm font-semibold uppercase text-zinc-200">Hoat dong gan day</h2>
      </div>
      <ul class="divide-y divide-border">
        <li v-for="item in recentActivity" :key="item.time" class="grid gap-2 px-4 py-2.5 text-sm sm:grid-cols-[120px_1fr]">
          <span class="font-mono text-xs text-zinc-500">{{ item.time }}</span>
          <span class="text-zinc-300">{{ item.text }}</span>
        </li>
      </ul>
    </section>
  </div>
</template>
