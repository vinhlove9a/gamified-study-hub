<script setup lang="ts">
import { appEnv } from '../config/env';
</script>

<template>
  <main class="min-h-screen bg-slate-100 text-slate-900">
    <section class="mx-auto flex min-h-screen w-full max-w-4xl flex-col items-center justify-center px-6 py-16">
      <div class="w-full rounded-2xl border border-slate-200 bg-white p-10 shadow-sm">
        <p class="mb-2 text-sm font-semibold uppercase tracking-widest text-brand-700">Gamified Study Hub</p>
        <h1 class="mb-4 text-3xl font-bold text-slate-900 md:text-4xl">Base project is running</h1>
        <p class="mb-6 text-slate-600">
          Frontend foundation is active. Domain features will be implemented in future phases.
        </p>

        <dl class="mb-8 rounded-xl bg-slate-50 p-4">
          <dt class="text-xs uppercase tracking-wide text-slate-500">Backend API Base URL</dt>
          <dd class="mt-1 break-all font-mono text-sm text-slate-800">{{ appEnv.apiBaseUrl }}</dd>
        </dl>

        <a
          href="https://github.com"
          target="_blank"
          rel="noreferrer"
          class="inline-flex items-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-700"
        >
          Documentation Placeholder
        </a>
      </div>
    </section>
  </main>
</template>
