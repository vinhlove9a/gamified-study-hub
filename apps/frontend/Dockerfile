FROM node:20-alpine AS builder
WORKDIR /app

COPY package.json package-lock.json* ./
RUN if [ -f package-lock.json ]; then npm ci; else npm install; fi

COPY . .
# Empty by default → same-origin relative /api/v1 requests proxied by nginx.
# Compose overrides this via the build arg; see docker-compose.yml.
ARG VITE_API_BASE_URL=
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
# Raise the V8 heap limit so vue-tsc + vite build don't OOM on small hosts
# (a 1GB EC2 instance defaults Node to a ~0.5GB old-space and aborts mid-build).
ENV NODE_OPTIONS=--max-old-space-size=1536
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
