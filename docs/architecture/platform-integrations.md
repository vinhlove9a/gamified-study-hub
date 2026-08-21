# Platform Integrations

Optional infrastructure wired into the backend ahead of the features that will
use it: **MongoDB** (NoSQL), **Elasticsearch** (search), **Kafka** (messaging),
and a full **observability** stack (metrics, logs, traces). Everything is
**disabled by default** and **gated by config**, so the app boots and the current
deployment stays light until a piece is actually turned on.

## Design principles

- **Off by default, one flag to enable.** Each integration is guarded by an
  `APP_*_ENABLED` flag (`app.integrations.*` in `application.yml`). With a flag
  off, the app never contacts that service.
- **Boots without the service running.** Clients are lazy, health probes for
  Mongo/ES are disabled unless enabled, Kafka listener containers don't
  auto-start, and no Kafka topics / ES indexes are created at startup. Verified
  by `GamifiedStudyHubBackendApplicationTests.contextLoads` with nothing running.
- **Local first.** Heavy services run only via Docker Compose **profiles** on a
  dev machine. They are intentionally **not** in `docker-compose.prod.yml` — the
  1GB EC2 box cannot host them.

## Enabling locally

Two steps: start the stack, then flip the flag(s) in `.env` and restart the backend.

```bash
# Start a stack (profiles: nosql | search | messaging | observability | full)
docker compose --profile full up -d      # or: make infra-up
docker compose --profile observability up -d   # or: make obs-up
```

```dotenv
# .env — turn on what you started
APP_MONGO_ENABLED=true
APP_ES_ENABLED=true
APP_KAFKA_ENABLED=true
APP_TRACING_SAMPLING=1.0   # trace every request locally
```

Then restart the backend (`make dev-rebuild`, or re-run `make backend-run`).

## Services & ports (local dev)

| Service        | Compose profile        | Host port | Notes |
|----------------|------------------------|-----------|-------|
| MongoDB        | `nosql`, `full`        | 27017     | no auth locally |
| Elasticsearch  | `search`, `full`       | 9200      | single-node, security off, 512m heap |
| Kafka          | `messaging`, `full`    | 9094      | KRaft; in-container clients use `kafka:9092` |
| Kafka UI       | `messaging`, `full`    | 8085      | http://localhost:8085 |
| Prometheus     | `observability`, `full`| 9090      | scrapes `backend:8080/actuator/prometheus` |
| Grafana        | `observability`, `full`| 3000      | anonymous admin; datasources auto-provisioned |
| Loki           | `observability`, `full`| 3100      | logs (via Promtail → Docker socket) |
| Tempo          | `observability`, `full`| 3200      | traces |
| OTel Collector | `observability`, `full`| 4317/4318 | trace ingest → Tempo |

> `make backend-run` (backend on the host, not in Docker) talks to Kafka on
> `localhost:9094`. Set `KAFKA_BOOTSTRAP_SERVERS=localhost:9094` in that case.

## Reference feature: `activity`

The `com.gamifiedstudyhub.backend.activity` package is a working vertical slice
that uses all three data integrations together (enable with
`APP_FEATURE_ACTIVITY_ENABLED=true`, which requires Mongo + ES + Kafka on):

```
POST /api/v1/activities        → write MongoDB (source of truth) + publish Kafka
   (ActivityEventConsumer)      → index into Elasticsearch
GET  /api/v1/activities         → read from MongoDB
GET  /api/v1/activities/search  → full-text search from Elasticsearch
```

Use it as the copy-paste template for real features.

## Using them in code

- **MongoDB** — `ActivityDocument` + `ActivityMongoRepository` (Spring Data
  `MongoRepository`). Add `@Indexed` fields only when the integration is on (they
  trigger a startup connection otherwise).
- **Elasticsearch** — `ActivitySearchDocument` (`createIndex = false`) +
  `ActivitySearchRepository`. Create the index explicitly when enabled — see
  `ActivitySearchIndexInitializer` (`indexOps(Type.class).createWithMapping()`).
- **Kafka** — publish via `platform/messaging/KafkaEventPublisher`; consume with
  `@KafkaListener` (see `ActivityEventConsumer`). Topics live in `KafkaTopics` and
  are declared as `NewTopic` beans in `KafkaConfig` (created only when enabled).
  Keep one consumer group per logical consumer — two `@KafkaListener`s on the same
  topic **and** group split the partitions and each sees only some messages.
- **Observability** — metrics via Micrometer at `/actuator/prometheus`; add
  `@Observed` to a method (aspect enabled in `ObservabilityConfig`) to get a
  timer + a span in one shot (see `ActivityService`). Traces export over OTLP;
  logs are JSON in `prod` (`logback-spring.xml`) with `traceId`/`spanId`.

## Security note (metrics endpoint)

`SecurityConfig` permits only the read-only actuator endpoints needed for
scraping — `health`, `info`, `prometheus` (via `EndpointRequest.to(...)`);
everything else stays behind auth. These paths are **not** routed by the public
reverse proxy, so only in-cluster Prometheus reaches them. In prod, keep them off
the public proxy and restrict at the network layer.

## Production

Keep every flag `false` on the current box. When moving to a larger server, back
each integration with a **managed / dedicated** service (MongoDB Atlas, Elastic
Cloud, MSK/Redpanda, Grafana Cloud or a dedicated observability host), set the
connection env vars, flip the flags, and re-enable the health probes. Do not run
these stores on the application container.
