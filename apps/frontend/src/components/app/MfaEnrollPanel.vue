<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { authApi, type MfaEnrollResponse } from '@/features/auth/authApi';
import { ApiError } from '@/lib/api/apiError';

type Step = 'loading' | 'idle' | 'enrolling' | 'enabled';

const step = ref<Step>('loading');
const enroll = ref<MfaEnrollResponse | null>(null);
const code = ref('');
const recoveryCodes = ref<string[]>([]);
const message = ref('');
const busy = ref(false);

onMounted(refreshStatus);

async function refreshStatus() {
  try {
    const status = await authApi.mfaStatus();
    step.value = status.enabled ? 'enabled' : 'idle';
  } catch {
    step.value = 'idle';
  }
}

async function startEnroll() {
  message.value = '';
  busy.value = true;
  try {
    enroll.value = await authApi.mfaEnroll();
    step.value = 'enrolling';
  } catch (e) {
    message.value = e instanceof ApiError ? e.message : 'Không thể bắt đầu cài đặt 2FA.';
  } finally {
    busy.value = false;
  }
}

async function confirmEnroll() {
  message.value = '';
  busy.value = true;
  try {
    const res = await authApi.mfaVerifyEnroll({ code: code.value.trim() });
    recoveryCodes.value = res.recoveryCodes;
    step.value = 'enabled';
    code.value = '';
  } catch (e) {
    message.value = e instanceof ApiError ? e.message : 'Mã không hợp lệ.';
  } finally {
    busy.value = false;
  }
}

async function disable() {
  message.value = '';
  if (!code.value.trim()) {
    message.value = 'Nhập mã hiện tại để tắt 2FA.';
    return;
  }
  busy.value = true;
  try {
    await authApi.mfaDisable({ code: code.value.trim() });
    recoveryCodes.value = [];
    code.value = '';
    step.value = 'idle';
    message.value = 'Đã tắt 2FA.';
  } catch (e) {
    message.value = e instanceof ApiError ? e.message : 'Không thể tắt 2FA.';
  } finally {
    busy.value = false;
  }
}
</script>

<template>
  <div class="mfa">
    <p v-if="step === 'loading'" class="muted">Đang tải…</p>

    <template v-else-if="step === 'idle'">
      <p class="muted">Bảo vệ tài khoản bằng mã một lần (TOTP) từ Google Authenticator, Authy…</p>
      <button class="btn" :disabled="busy" @click="startEnroll">Bật 2FA</button>
    </template>

    <template v-else-if="step === 'enrolling'">
      <p class="muted">Quét mã QR bằng ứng dụng xác thực, rồi nhập mã 6 chữ số để xác nhận.</p>
      <img v-if="enroll" :src="enroll.qrDataUri" alt="QR code 2FA" class="qr" />
      <p v-if="enroll" class="secret">Khóa thủ công: <code>{{ enroll.secret }}</code></p>
      <input v-model="code" inputmode="numeric" maxlength="6" placeholder="123456" class="input" />
      <div class="row">
        <button class="btn" :disabled="busy" @click="confirmEnroll">Xác nhận</button>
        <button class="btn ghost" :disabled="busy" @click="step = 'idle'">Hủy</button>
      </div>
    </template>

    <template v-else-if="step === 'enabled'">
      <p class="ok">✓ 2FA đang bật cho tài khoản của bạn.</p>
      <div v-if="recoveryCodes.length" class="recovery">
        <p class="muted">Lưu các mã khôi phục này ở nơi an toàn (chỉ hiển thị một lần):</p>
        <ul>
          <li v-for="rc in recoveryCodes" :key="rc"><code>{{ rc }}</code></li>
        </ul>
      </div>
      <p class="muted">Để tắt 2FA, nhập mã hiện tại (hoặc mã khôi phục):</p>
      <input v-model="code" inputmode="numeric" maxlength="14" placeholder="Mã xác thực" class="input" />
      <button class="btn danger" :disabled="busy" @click="disable">Tắt 2FA</button>
    </template>

    <p v-if="message" class="msg">{{ message }}</p>
  </div>
</template>

<style scoped>
.mfa { display: flex; flex-direction: column; gap: 0.75rem; }
.muted { font-size: 0.85rem; color: var(--muted-foreground, #9aa3c0); margin: 0; }
.ok { color: #34d399; font-weight: 600; margin: 0; }
.qr { width: 180px; height: 180px; border-radius: 0.5rem; background: #fff; padding: 6px; }
.secret { font-size: 0.8rem; color: #9aa3c0; margin: 0; }
.input { padding: 0.6rem 0.8rem; border-radius: 0.5rem; border: 1px solid rgba(139,92,246,0.4); background: rgba(255,255,255,0.05); color: inherit; max-width: 16rem; letter-spacing: 0.15rem; }
.row { display: flex; gap: 0.6rem; }
.btn { padding: 0.55rem 1.1rem; border: none; border-radius: 0.5rem; background: linear-gradient(135deg,#8b5cf6,#6d28d9); color: #fff; font-weight: 600; cursor: pointer; width: fit-content; }
.btn.ghost { background: transparent; border: 1px solid rgba(255,255,255,0.2); }
.btn.danger { background: linear-gradient(135deg,#f43f5e,#be123c); }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
.recovery ul { display: grid; grid-template-columns: repeat(2, max-content); gap: 0.35rem 1.5rem; margin: 0.4rem 0; padding-left: 1rem; }
.recovery code { font-size: 0.9rem; }
.msg { font-size: 0.85rem; color: #fca5a5; margin: 0; }
</style>
