# Organisation Service Boilerplate

Spring Boot boilerplate for org services: feature-flagged Postgres, Redis, Elasticsearch, and monitoring. Scale horizontally; fallbacks when integrations are disabled.

## Features

| Feature | Config | When enabled | When disabled |
|---------|--------|--------------|---------------|
| **Postgres** | `app.features.postgres.enabled=true` | Use PostgreSQL (set `spring.datasource.url`) | H2 in-memory |
| **Redis** | `app.features.redis.enabled=true` | Cache (e.g. sample by id) via Redis | No cache (DB only) |
| **Elasticsearch** | `app.features.elasticsearch.enabled=true` | Placeholder for ES search (see code) | DB search (LIKE) |
| **Monitoring** | `app.features.monitoring.enabled=true` | Actuator + Prometheus metrics | Health/info only |
| **Kafka** | `app.features.kafka.enabled=true` | Event listeners (add `spring-kafka`, see `listener/`) | No Kafka |
| **RabbitMQ** | `app.features.rabbitmq.enabled=true` | Event listeners (add `spring-boot-starter-amqp`, see `listener/`) | No RabbitMQ |
| **State machine** | `app.features.statemachine.enabled=true` | Demo workflow API (see `statemachine/`) | No state machine API |
| **Elastic APM** | `app.features.apm.enabled=true` + `ELASTIC_APM_SERVER_URL` | Traces, metrics, and log correlation to Elastic APM | No APM agent attached |

## Infra and observability

**Infra** (Postgres, Redis, RabbitMQ, Kafka) and **observability** (Elasticsearch, Kibana, APM) are in a separate repo: **[loylty-ai/shared-infra-observability](https://github.com/loylty-ai/shared-infra-observability)**. Start them from there, then run this app (locally, Docker, or K8s).

## Run locally

Start infra and observability from the **shared-infra-observability** repo, then:

```bash
./mvnw spring-boot:run
```

Server: **http://localhost:9100**

### One-shot local start (Docker)

If you have both repos cloned (e.g. **shared-infra-observability** as a sibling of this repo), you can start infra + observability and run this app with one script:

```bash
./start-local.sh
```

- **Prereq:** Clone [shared-infra-observability](https://github.com/loylty-ai/shared-infra-observability) next to this repo, or set `export SHARED_INFRA_OBSERVABILITY_REPO=/path/to/shared-infra-observability`.
- **App URL:** http://localhost:9100 (configurable via `HOST_PORT`, `CONTAINER_PORT`, `IMAGE_NAME` at the top of `start-local.sh`).
- **Example curl** (when `HOST_PORT=9100`):  
  `curl -X POST http://localhost:9100/api/v1/events/rabbit -H "Content-Type: application/json" -H "X-Client-Id: web" -H "X-Client-Version: 1.0.0" -H "X-Idempotency-Key: idem-$(date +%s)" -d '{"message": "test"}'`

## Docker

**Step-by-step:** See [docs/RUN_WITH_DOCKER.md](docs/RUN_WITH_DOCKER.md) for full instructions and troubleshooting.

**App only** — connects to Redis, RabbitMQ, Kafka, Postgres by name on the **infra** network; APM/ES via host. Start [shared-infra-observability](https://github.com/loylty-ai/shared-infra-observability) first (so network `infra` exists), then:

```bash
docker compose up -d --build
```

App: **http://localhost:9100**. Resolves `redis`, `rabbitmq`, `kafka`, `postgres` from the infra stack; APM at `host.docker.internal:8200`. If your infra compose uses a different network name, set it under `networks` in `docker-compose.yml`.

**Option B – Run only the app image (you provide brokers/DB elsewhere):**

```bash
docker build -t hello-world-service .
docker run -p 5000:5000 hello-world-service
```

**Option C – App on existing infra network (Redis, RabbitMQ, Kafka, etc. on network `infra`):**

```bash
docker build -t hello-world-service .
docker run --rm -p 9100:5000 --network infra --add-host=host.docker.internal:host-gateway hello-world-service
```

App: **http://localhost:9100**. Profile and APM URL are set in the Dockerfile; override with `-e` if needed. See [docs/RUN_WITH_DOCKER_RUN.md](docs/RUN_WITH_DOCKER_RUN.md).

## Kubernetes (Kind and EKS)

**One codebase** for **Mac Kind** and **AWS EKS**: app is deployed via Argo CD from your GitOps repo; logs go to Elasticsearch/Kibana via the Filebeat DaemonSet in `deploy/filebeat/`. Exact steps and which files to use per environment: **[docs/RUN_ON_KIND_AND_EKS.md](docs/RUN_ON_KIND_AND_EKS.md)**.

- **Kind:** GitOps `apps/filebeat/` uses `daemonset.yaml` (ES at `host.docker.internal`).
- **EKS:** GitOps `apps/filebeat/` uses `daemonset-aws.yaml` (set your OpenSearch/ES endpoint).

## APIs

All REST APIs are versioned under **`/api/v1`** and use the **universal request–response contract** (see [docs/UNIVERSAL_API_CONTRACT.md](docs/UNIVERSAL_API_CONTRACT.md)): mandatory headers (`X-Request-Id`, `X-Client-Id`, `X-Client-Version`, and `X-Idempotency-Key` for POST/PUT/PATCH), and every response in the shape `{ requestId, status, data, errors, meta }`.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Hello + downstream call (no contract) |
| GET | `/api/v1/samples` | List samples |
| GET | `/api/v1/samples/{id}` | Get sample (cached if Redis on) |
| POST | `/api/v1/samples` | Create sample (body: `{"name":"x","description":"y"}`) |
| GET | `/api/v1/samples/search?q=` | Search (ES if on, else DB) |
| GET | `/api/v1/status/features` | Which features are enabled |
| POST | `/api/v1/events/rabbit` | Send message to RabbitMQ (body: `{"message":"..."}`); 503 if disabled |
| POST | `/api/v1/events/kafka` | Send message to Kafka (body: `{"message":"..."}`); 503 if disabled |
| POST | `/api/v1/statemachine/demo` | Create workflow instance (starts in DRAFT); returns `{ "id", "state" }` in `data` |
| GET | `/api/v1/statemachine/demo/{id}` | Get current state |
| POST | `/api/v1/statemachine/demo/{id}/event` | Send event (body: `{"event":"SUBMIT"}`); events: SUBMIT, START_REVIEW, APPROVE, REJECT |
| GET | `/api/v1/statemachine/demo` | List all demo instances (for testing) |

## State machine example

When `app.features.statemachine.enabled=true` (default), a demo workflow is available:

- **States**: DRAFT → SUBMITTED → IN_REVIEW → APPROVED or REJECTED
- **Events**: SUBMIT, START_REVIEW, APPROVE, REJECT

Example: `POST /api/v1/statemachine/demo` → create; then `POST /api/v1/statemachine/demo/1/event` with `{"event":"SUBMIT"}` to move to SUBMITTED. The implementation is in `statemachine/` (enums, transition rules, in-memory store). For production persist state (e.g. JPA); for complex workflows consider [Spring State Machine](https://spring.io/projects/spring-statemachine).

## RabbitMQ, Kafka, Redis, Elasticsearch

**Infra URLs are env- and profile-driven:** **dev** = Kubernetes (Kind/EKS) or Docker (defaults: redis, rabbitmq, kafka, postgres, elasticsearch). **prod** = AWS managed (RDS, ElastiCache, Amazon MQ, MSK, OpenSearch); set all via env. See [docs/ENV_INFRA_REFERENCE.md](docs/ENV_INFRA_REFERENCE.md).

When you run the app **inside Docker** with the infra profiles (e.g. `docker compose --profile with-rabbitmq ... up`), the **`docker`** profile (includes **dev**) is active and the app uses dev defaults (service names). Override via env (`rabbitmq`, `kafka`, `redis`, `elasticsearch`). Set `app.features.*.enabled` to match which profiles you start so the app doesn’t try to connect to missing infra.

When you run the app **on the host** (e.g. `./mvnw spring-boot:run`), start only the brokers you need:

```bash
docker compose --profile with-rabbitmq --profile with-kafka --profile with-redis --profile with-elasticsearch up -d
```

- **RabbitMQ**: port 5672 (AMQP), management UI http://localhost:15672 (guest/guest).
- **Kafka**: port 9092.
- **Redis**: port 6379.
- **Elasticsearch**: port 9200.

Then in `application.yml` (or use the same hostnames if your hosts file maps them) set:

```yaml
app:
  features:
    rabbitmq:
      enabled: true
    kafka:
      enabled: true
    redis:
      enabled: true
    elasticsearch:
      enabled: true
```

Restart the app. Use `POST /api/v1/events/rabbit` or `POST /api/v1/events/kafka` with body `{"message":"hello"}` to send; the consumer logs the message. In code, inject `RabbitProducerService` or `KafkaProducerService` to publish from your services.

## Boilerplate structure

- **API paths**: All paths are defined in `ApiPaths` (`api/ApiPaths.java`). Change `BASE` or add new constants when adding resources.
- **Sample resource**: The **Sample** API (entity, repository, service, controller, DTO) is the reference—neutral name, not domain-specific. Replace or copy it for your domain.
- **Adding a new API**: See **[BOILERPLATE.md](BOILERPLATE.md)** for step-by-step instructions (entity → repository → DTO → service → controller).

## Enabling Postgres

1. Set `app.features.postgres.enabled: true`.
2. Override datasource (e.g. in `application-postgres.yml` or env):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: myuser
    password: mypass
  jpa:
    hibernate:
      ddl-auto: update
```

## Enabling Redis

1. Set `app.features.redis.enabled: true`.
2. Ensure Redis is reachable (default `localhost:6379` or set `spring.data.redis.host` / `port`).

## Enabling Elasticsearch

1. Set `app.features.elasticsearch.enabled: true`.
2. Current implementation uses DB search; replace with real ES client/index when ready (see `ElasticsearchSearchService`).

## Elastic APM (traces, metrics, log correlation)

1. Set `app.features.apm.enabled: true` and set **`ELASTIC_APM_SERVER_URL`** (e.g. `http://apm-server:8200`) so the agent attaches at startup.
2. Optionally set `ELASTIC_APM_SERVICE_NAME`, `ELASTIC_APM_ENVIRONMENT`, `ELASTIC_APM_SERVICE_VERSION`.
3. Logs include `trace.id` in the pattern when the agent is active so you can correlate logs with traces in Kibana. **Where to view:** Kibana → **Logs** (`/app/logs`), **Discover** (`/app/discover`), **APM** (`/app/apm`). See [docs/ELASTIC_APM.md](docs/ELASTIC_APM.md) for exact URLs and log correlation.

**Shared observability and infra** live in **[loylty-ai/shared-infra-observability](https://github.com/loylty-ai/shared-infra-observability)** (run `./run-infra.sh start` and `./run-observability.sh start`; Kibana: http://localhost:5601, APM: http://localhost:5601/app/apm). For **logs to Kibana** on Kind or EKS, use the Filebeat DaemonSet in [deploy/filebeat/](deploy/filebeat/). See [docs/INFRA_STACK.md](docs/INFRA_STACK.md).

## Event listeners (Kafka / RabbitMQ)

Kafka and RabbitMQ are **enabled by default** when using the shared infra. If you use the all-in-one compose instead:

1. Run brokers: `docker-compose up -d` (RabbitMQ on 5672, Kafka on 9092).
2. Ensure `app.features.kafka.enabled` and `app.features.rabbitmq.enabled` match the profiles you start (defaults use infra).
3. **RabbitMQ**: Queue `sample.queue` is declared; `RabbitProducerService` sends, `RabbitConsumerListener` logs. Add more queues in `RabbitMQConfig` and listeners in `listener/`.
4. **Kafka**: `KafkaProducerService` sends to `sample-topic` (generic `send(topic, key, value)`); `KafkaConsumerListener` logs. Add more `@KafkaListener` methods for other topics.

## Tests

```bash
./mvnw test
```

- Unit: `SampleServiceTest`, `SampleControllerTest`, `HelloControllerTest`
- Integration: `SampleControllerIntegrationTest` (full context, H2, no Redis/ES)

## Configuration reference

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 9100 (local), 5000 (Docker) | Server port |
| `app.features.postgres.enabled` | false | Use PostgreSQL |
| `app.features.redis.enabled` | false | Use Redis cache |
| `app.features.elasticsearch.enabled` | false | Reserve for ES search |
| `app.features.monitoring.enabled` | true | Actuator + Prometheus |
| `app.features.kafka.enabled` | false | Kafka event listeners (see `listener/`) |
| `app.features.rabbitmq.enabled` | false | RabbitMQ event listeners (see `listener/`) |
| `app.features.statemachine.enabled` | true | State machine demo (see `statemachine/`) |
| `app.features.apm.enabled` | false | Flag for APM; agent attaches when `ELASTIC_APM_SERVER_URL` is set |
| `downstream.url` | http://hello-world-1:5000/internal | Downstream service (e.g. K8s DNS) |

## Deployment: self-hosted vs managed (e.g. AWS)

You can deploy with **self-hosted** infrastructure (Docker, your own VMs) or **AWS managed services** (RDS, ElastiCache, Amazon MQ, MSK). Same codebase; switch via **config and feature flags** only.

- **Local (Mac) vs AWS Kubernetes**: See **[docs/LOCAL_MAC_VS_AWS_KUBERNETES.md](docs/LOCAL_MAC_VS_AWS_KUBERNETES.md)** for what to use on Mac now and what to use when you run on EKS (logs, APM, infra, health probes).
- **Self-hosted**: Use Docker Compose (or your own Redis, RabbitMQ, Postgres, Kafka). See [docs/RUN_WITH_DOCKER.md](docs/RUN_WITH_DOCKER.md).
- **AWS managed**: Use RDS (Postgres), ElastiCache (Redis), Amazon MQ (RabbitMQ), MSK (Kafka). Set connection details via env or `application-aws.yml`. See **[docs/DEPLOYMENT_OPTIONS.md](docs/DEPLOYMENT_OPTIONS.md)** and **`application-aws.yml.example`** (copy to `application-aws.yml` and fill or override with env).

## Engineering notes

- **Horizontal scaling**: Stateless app; use external Postgres/Redis/ES. RestTemplate timeouts and circuit breaker on downstream calls; HikariCP and Lettuce pools tuned in config.
- **Feature flags**: All integrations are optional; service runs with H2 and no cache/search backend by default.
- **Monitoring**: Expose `/actuator/prometheus` and scrape with Prometheus when `app.features.monitoring.enabled=true`.
- **100M+ scale**: See **[SCALING.md](SCALING.md)** for what’s in place and what to add (read replicas, rate limiting, tracing, DLQ, etc.).
