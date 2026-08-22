# Pre-wired "modern stack"

A set of production-grade capabilities wired into the backend but **OFF by default**,
so the 1 GB production box stays lean. Each is activated by a single flag (and, where
relevant, its provider config) — no scaffolding needed when a feature finally needs it.
This mirrors the config-gated approach in [platform-integrations.md](platform-integrations.md).

> Design rule: a disabled capability must add **no runtime cost and never break startup**.
> Config classes are `@ConditionalOnProperty(... havingValue = "true")`; beans that dial a
> network at construction (Redisson, S3, Secrets Manager) are created only when switched on.

## Summary

| Capability | Flag (default) | Activated component |
|---|---|---|
| Object storage (Local / S3 / R2 / MinIO) | `app.storage.provider=local` | `platform.storage.StorageService` |
| Upload size caps | `app.*upload* = 25MB/30MB` | `spring.servlet.multipart` |
| Spring Cache (Caffeine / Redis) | `app.cache.enabled=false` | `platform.cache.CacheConfig` |
| Redisson distributed lock | `app.redisson.enabled=false` | `platform.lock.RedissonConfig`, `DistributedLock` |
| Scheduled jobs + ShedLock | `app.scheduling.enabled=false` | `platform.scheduling.SchedulingConfig` |
| Idempotency-Key replay | `app.idempotency.enabled=false` | `platform.idempotency.IdempotencyKeyFilter` |
| Resilience4j (retry/CB/bulkhead) | passive (annotate to use) | `resilience4j.*` in `application.yml` |
| WebSocket / STOMP | `app.websocket.enabled=false` | `platform.realtime.WebSocketConfig` |
| AWS Secrets Manager | `spring.cloud.aws.secretsmanager.enabled=false` | spring-cloud-aws config import |
| MapStruct | processor on classpath | `common.mapper.CentralMapperConfig` |
| Spring Modulith | opt-in test (`-Pintegration`) | `ModularityTests` |
| Testcontainers | `@Tag("integration")` | `-Pintegration` test run |
| RFC 7807 ProblemDetails | `spring.mvc.problemdetails.enabled=false` | Spring MVC (kept off; custom envelope active) |

## Notes per capability

**Object storage.** `LocalStorageService` (default) needs no credentials — good for dev.
Set `app.storage.provider=s3` plus `app.storage.s3.*` (bucket/region/keys, and `endpoint`
for R2/MinIO, `path-style-access=true` for those two) to use `S3StorageService`, which also
issues presigned download URLs. Consumers depend only on the `StorageService` interface.

**Caching.** `@Cacheable`/`@CacheEvict` do nothing until `app.cache.enabled=true`. Default
provider is in-process Caffeine (10 min TTL); switch to `redis` for correctness across
multiple instances.

**Redisson / DistributedLock.** `DistributedLock.runLocked(key, wait, lease, action)` runs a
critical section under a cluster-wide lock — the basis for anti-double-spend on the coin
wallet. The `RedissonClient` dials Redis eagerly, so it exists only when enabled.

**Scheduling.** `@EnableScheduling` + ShedLock activate together; annotate a job with
`@Scheduled(cron=...)` + `@SchedulerLock(name=...)` so it runs on exactly one instance.
Intended for token cleanup, daily-reward resets, and triggering the DB backup script.

**Idempotency.** A retried `POST/PUT/PATCH/DELETE` carrying the same `Idempotency-Key`
header replays the original response (cached in Redis, per authenticated user) instead of
re-running the side effect; a concurrent duplicate gets `409` while the first is in flight.
Essential for coin spends / payments.

**Resilience4j.** Named instances (`email`, `storage`) are defined in `application.yml` but
only take effect on annotated methods, e.g. `@Retry(name="email")`, `@CircuitBreaker(name="storage")`.

**WebSocket.** Enabling registers a SockJS/STOMP endpoint at `/ws` with a simple in-memory
broker (`/topic`, `/queue`, `/app`, `/user`). **Also add `/ws/**` to the permit rules in
`SecurityConfig`** — the authenticated `/**` rule otherwise blocks the handshake.

**Secrets Manager.** Off so the SDK does not try to resolve an AWS region at boot. To use:
`APP_SECRETS_MANAGER_ENABLED=true`, provide a region (`spring.cloud.aws.region.static` or
`AWS_REGION`), and add `spring.config.import=aws-secretsmanager:/gsh/prod`.

**MapStruct.** Processor is on the compile classpath (auto-discovered alongside the existing
setup). New mappers opt into shared settings with `@Mapper(config = CentralMapperConfig.class)`;
existing hand-written mappers are untouched.

**Spring Modulith / Testcontainers.** Both live behind `@Tag("integration")`, excluded from
the default `mvn test` (offline + fast). Run `mvn -Pintegration test` to verify module
boundaries and generate module docs, and to run Docker-backed integration tests.

**ProblemDetails.** Deliberately kept OFF: the API returns the custom
`{success,message,data}` / `{code,message,fieldErrors}` envelope the SPA parses
(`GlobalExceptionHandler`). Flip only alongside a coordinated frontend contract change.
