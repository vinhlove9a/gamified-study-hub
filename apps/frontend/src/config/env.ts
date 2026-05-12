export interface AppEnv {
  apiBaseUrl: string;
}

const safeRead = (value: string | undefined, fallback: string): string => {
  if (!value || value.trim().length === 0) {
    return fallback;
  }

  return value;
};

export const appEnv: AppEnv = {
  apiBaseUrl: safeRead(import.meta.env.VITE_API_BASE_URL, 'http://localhost:8080/api')
};
