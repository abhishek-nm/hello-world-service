# Running with AWS managed services

The app is **config-driven**: all infra and observability endpoints come from configuration (or environment variables). So the **same codebase** works with:

- **Local Docker (dev)** – use profile `dev`; start shared infra and observability from the shared-infra-observability repo
- **Production / AWS (prod)** – use profile `prod` and set env vars for RDS, ElastiCache, Amazon MQ, MSK, APM

Use **`SPRING_PROFILES_ACTIVE=prod`** in production so the app loads `application-prod.yml` (env-driven). No code changes; only profile and env differ. See [ENV_PROFILES.md](ENV_PROFILES.md) for dev vs prod.

## How it works

| Component   | Local (Docker)              | AWS managed service        | Config / env override |
|------------|-----------------------------|----------------------------|-------------------------|
| **Postgres** | `postgres:5432` (infra)     | RDS endpoint              | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` |
| **Redis**    | `redis:6379` (infra)       | ElastiCache endpoint      | `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT` |
| **RabbitMQ** | `rabbitmq:5672` (infra)    | Amazon MQ (RabbitMQ) URL  | `SPRING_RABBITMQ_HOST`, `SPRING_RABBITMQ_PORT`, `SPRING_RABBITMQ_USERNAME`, `SPRING_RABBITMQ_PASSWORD` |
| **Kafka**    | `kafka:9092` (infra)       | MSK bootstrap servers     | `SPRING_KAFKA_BOOTSTRAP_SERVERS` |
| **APM**      | `http://host.docker.internal:8200` | Elastic APM / OpenSearch APM / X-Ray | `ELASTIC_APM_SERVER_URL`, `ELASTIC_APM_SERVICE_NAME` |

Spring Boot maps env vars to properties: `SPRING_DATASOURCE_URL` → `spring.datasource.url`, etc. Set these in your ECS task definition, Kubernetes deployment, or Lambda config.

## Example: ECS / EKS with RDS, ElastiCache, Amazon MQ

```bash
# Example env vars (set in ECS task def, K8s manifest, or .env)
SPRING_PROFILES_ACTIVE=prod

# RDS
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db.region.rds.amazonaws.com:5432/boilerplate
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your-secret

# ElastiCache (Redis)
SPRING_DATA_REDIS_HOST=your-cache.region.cache.amazonaws.com
SPRING_DATA_REDIS_PORT=6379

# Amazon MQ (RabbitMQ)
SPRING_RABBITMQ_HOST=b-xxx.mq.region.amazonaws.com
SPRING_RABBITMQ_PORT=5671
SPRING_RABBITMQ_USERNAME=admin
SPRING_RABBITMQ_PASSWORD=your-secret

# MSK (if you use Kafka)
SPRING_KAFKA_BOOTSTRAP_SERVERS=b-1.xxx.kafka.region.amazonaws.com:9092,b-2.xxx.kafka.region.amazonaws.com:9092

# APM (Elastic APM Server, or omit to disable)
ELASTIC_APM_SERVER_URL=https://your-apm-server.example.com
ELASTIC_APM_SERVICE_NAME=hello-world-service
```

Use TLS ports where required (e.g. Amazon MQ 5671, MSK 9094 for TLS).

## Features (enable/disable)

You can still turn components off via config:

- `app.features.kafka.enabled=false` if you don’t use Kafka (same as today).
- Feature flags in `application-dev.yml` / `application-prod.yml` control which clients are wired (e.g. `app.features.kafka.enabled`).

So: **yes, this setup supports switching to AWS managed services** by pointing the same app at RDS, ElastiCache, Amazon MQ, MSK, and your chosen APM via environment variables.
