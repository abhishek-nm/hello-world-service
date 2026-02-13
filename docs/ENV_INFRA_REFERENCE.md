# Infra URLs and credentials: dev vs prod (env / YAML)

All infra endpoints (Redis, RabbitMQ, Kafka, Postgres, Elasticsearch) are **env-driven**. The **profile** chooses the defaults:

- **dev** → Infra on **Kubernetes** (Kind/EKS) or Docker network. Defaults use K8s service names.
- **prod** → **AWS managed services** (RDS, ElastiCache, Amazon MQ, MSK, OpenSearch). All values from env; no in-repo defaults for secrets/URLs.

---

## Profile summary

| Profile | Infra location      | Config file            | Defaults |
|---------|---------------------|------------------------|----------|
| **dev** | Kubernetes / Docker | `application-dev.yml`  | redis, rabbitmq, kafka, postgres, elasticsearch (K8s/Docker hostnames) |
| **prod**| AWS managed         | `application-prod.yml` | None; set all via env (RDS, ElastiCache, Amazon MQ, MSK, OpenSearch) |

Set profile with **`SPRING_PROFILES_ACTIVE=dev`** or **`SPRING_PROFILES_ACTIVE=prod`** (env or CLI).

---

## Common env vars (both profiles)

| Env var | Dev default | Prod | Description |
|---------|-------------|------|-------------|
| `SPRING_PROFILES_ACTIVE` | — | — | `dev` or `prod` |
| `SERVER_PORT` | `5000` | `5000` | App port |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/boilerplate` | **required** | Postgres JDBC URL (RDS in prod) |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | **required** | DB user |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | **required** | DB password |
| `SPRING_DATA_REDIS_HOST` | `redis` | **required** | Redis host (ElastiCache in prod) |
| `SPRING_DATA_REDIS_PORT` | `6379` | `6379` | Redis port |
| `SPRING_DATA_REDIS_PASSWORD` | (empty) | optional | Redis auth (ElastiCache AUTH) |
| `SPRING_RABBITMQ_HOST` | `rabbitmq` | **required** | RabbitMQ host (Amazon MQ in prod) |
| `SPRING_RABBITMQ_PORT` | `5672` | `5671` (TLS) | RabbitMQ port |
| `SPRING_RABBITMQ_USERNAME` | `guest` | **required** | RabbitMQ user |
| `SPRING_RABBITMQ_PASSWORD` | `guest` | **required** | RabbitMQ password |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` | **required** | Kafka bootstrap (MSK in prod) |
| `SPRING_ELASTICSEARCH_URIS` | `http://elasticsearch:9200` | set if ES enabled | Elasticsearch/OpenSearch URL (OpenSearch in prod) |
| `APP_FEATURES_POSTGRES_ENABLED` | `true` | `true` | Use Postgres |
| `APP_FEATURES_REDIS_ENABLED` | `true` | `true` | Use Redis |
| `APP_FEATURES_RABBITMQ_ENABLED` | `true` | `true` | Use RabbitMQ |
| `APP_FEATURES_KAFKA_ENABLED` | `false` | `false` | Use Kafka |
| `APP_FEATURES_ELASTICSEARCH_ENABLED` | `false` | `false` | Use Elasticsearch/OpenSearch |

---

## Dev (Kubernetes or Docker)

- **Where:** App runs on **Kind**, **EKS**, or Docker on the **infra** network.
- **Defaults:** Hostnames `redis`, `rabbitmq`, `kafka`, `postgres`, `elasticsearch` (K8s services or Docker service names). Same names work on K8s and Docker.
- **Override:** Set any of the env vars above to point to a different host/URL (e.g. different namespace or port).

Example (override Redis host):

```bash
export SPRING_DATA_REDIS_HOST=redis.my-namespace.svc.cluster.local
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Or with `docker run`:

```bash
docker run --rm -p 9100:5000 --network infra \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e APP_FEATURES_KAFKA_ENABLED=true \
  hello-world-service
```

---

## Prod (AWS managed services)

- **Where:** App runs on **EKS**, ECS, or similar; infra = **RDS**, **ElastiCache**, **Amazon MQ**, **MSK**, **OpenSearch**.
- **Required env:** Set at least:
  - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
  - `SPRING_DATA_REDIS_HOST`
  - `SPRING_RABBITMQ_HOST`, `SPRING_RABBITMQ_USERNAME`, `SPRING_RABBITMQ_PASSWORD`
  - `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- **Optional:** `SPRING_ELASTICSEARCH_URIS` when using OpenSearch; `SPRING_DATA_REDIS_PASSWORD` for ElastiCache AUTH; `SPRING_RABBITMQ_PORT` (often 5671 for TLS).

Example (values from Secrets Manager or ConfigMap):

```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:postgresql://mydb.xxx.us-east-1.rds.amazonaws.com:5432/appdb
export SPRING_DATASOURCE_USERNAME=appuser
export SPRING_DATASOURCE_PASSWORD=...
export SPRING_DATA_REDIS_HOST=my-cache.xxx.cache.amazonaws.com
export SPRING_RABBITMQ_HOST=b-xxx.mq.us-east-1.amazonaws.com
export SPRING_RABBITMQ_PORT=5671
export SPRING_RABBITMQ_USERNAME=admin
export SPRING_RABBITMQ_PASSWORD=...
export SPRING_KAFKA_BOOTSTRAP_SERVERS=b-1.xxx.kafka.us-east-1.amazonaws.com:9092,b-2....
# If using OpenSearch:
export APP_FEATURES_ELASTICSEARCH_ENABLED=true
export SPRING_ELASTICSEARCH_URIS=https://my-domain.xxx.es.amazonaws.com:443
```

---

## APM (both profiles)

| Env var | Description |
|---------|-------------|
| `ELASTIC_APM_SERVER_URL` | APM Server URL (e.g. Elastic Cloud or host.docker.internal:8200 in dev) |
| `ELASTIC_APM_SERVICE_NAME` | Service name in APM (default: hello-world-service) |
| `ELASTIC_APM_ENVIRONMENT` | e.g. dev, prod |
