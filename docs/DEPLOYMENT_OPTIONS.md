# Deployment options: self-hosted vs managed services

The same codebase can run with **self-hosted** infrastructure (e.g. Docker, EC2) or **AWS managed services**. You choose via **configuration and feature flags**, not code changes.

---

## Two modes

| Mode | Use case | How |
|------|----------|-----|
| **Self-hosted** | Local dev, Docker, or your own VMs/containers | Run Redis, RabbitMQ, Postgres, Kafka (and optionally ES, APM) yourself. Point config at their hostnames/ports (e.g. `localhost`, Docker service names). |
| **Managed** | AWS (or other cloud) production | Use RDS, ElastiCache, Amazon MQ, MSK, etc. Set connection details via env, Parameter Store, or Secrets Manager. Enable the same feature flags and point Spring at the managed endpoints. |

---

## Per-component mapping

| Component | Self-hosted (e.g. Docker / EC2) | AWS managed option | Config you override |
|-----------|----------------------------------|--------------------|----------------------|
| **Database** | H2 (default) or your Postgres container/VM | **Amazon RDS (PostgreSQL)** | `spring.datasource.url`, `username`, `password`; `app.features.postgres.enabled=true` |
| **Cache** | Redis container / VM | **Amazon ElastiCache (Redis)** | `spring.data.redis.host`, `port`; optional `password`; `app.features.redis.enabled=true` |
| **AMQP (RabbitMQ)** | RabbitMQ container / VM | **Amazon MQ for RabbitMQ** | `spring.rabbitmq.host`, `port`, `username`, `password` (and TLS if required); `app.features.rabbitmq.enabled=true` |
| **Kafka** | Kafka container / VM | **Amazon MSK** | `spring.kafka.bootstrap-servers` (MSK bootstrap strings); `app.features.kafka.enabled=true` |
| **Search** | Elasticsearch container / VM | **Amazon OpenSearch** or Elastic Cloud | `spring.elasticsearch.uris`; `app.features.elasticsearch.enabled=true` |
| **APM** | Self-hosted APM Server / Elastic Stack | **Elastic Cloud** or your APM Server | `ELASTIC_APM_SERVER_URL` (and optional `ELASTIC_APM_SERVICE_NAME`, etc.) |

You can **mix**: e.g. RDS + ElastiCache (managed) and self-hosted Kafka for dev. Turn each integration on or off with feature flags and connection config.

---

## How the app chooses

- **Feature flags** (`app.features.*.enabled`): Turn Redis, RabbitMQ, Kafka, Postgres, Elasticsearch, APM on or off. When off, the app uses fallbacks (no cache, H2, no messaging, etc.).
- **Connection config** (Spring properties): Host, port, URL, credentials. Same properties work for self-hosted or managed; only the values change (e.g. `localhost:6379` vs ElastiCache endpoint).

No code change is required to switch from self-hosted to managed; use a different profile or env-specific config (e.g. `application-aws.yml` or env vars).

---

## AWS example (managed services)

1. Create in AWS: RDS (Postgres), ElastiCache (Redis), Amazon MQ (RabbitMQ) or MSK (Kafka), and optionally OpenSearch or Elastic Cloud for search/APM.
2. Run the app with profile `aws` (or set env vars) so it loads the right config.
3. Prefer **env vars or AWS Secrets Manager / Parameter Store** for URLs and secrets; avoid committing production URLs/passwords.

Example env vars (or use an `application-aws.yml` that reads from placeholders and override via env):

```bash
# Database (RDS)
SPRING_DATASOURCE_URL=jdbc:postgresql://your-rds-endpoint:5432/mydb
SPRING_DATASOURCE_USERNAME=myuser
SPRING_DATASOURCE_PASSWORD=...   # or from Secrets Manager

# Redis (ElastiCache)
SPRING_DATA_REDIS_HOST=your-elasticache-endpoint.cache.amazonaws.com
SPRING_DATA_REDIS_PORT=6379

# RabbitMQ (Amazon MQ)
SPRING_RABBITMQ_HOST=b-xxx.mq.region.amazonaws.com
SPRING_RABBITMQ_PORT=5671
SPRING_RABBITMQ_USERNAME=...
SPRING_RABBITMQ_PASSWORD=...

# Kafka (MSK)
SPRING_KAFKA_BOOTSTRAP_SERVERS=b-1.xxx.kafka.region.amazonaws.com:9092,b-2.xxx.kafka.region.amazonaws.com:9092

# Feature flags (enable what you use)
APP_FEATURES_POSTGRES_ENABLED=true
APP_FEATURES_REDIS_ENABLED=true
APP_FEATURES_RABBITMQ_ENABLED=true
APP_FEATURES_KAFKA_ENABLED=true
```

See **`application-aws.yml.example`** for a profile template you can copy and fill (or override with env).

---

## Summary

- **End goal: deploy to AWS** → Use RDS, ElastiCache, Amazon MQ, MSK (and optionally OpenSearch/Elastic) and point this app at them via config.
- **Option to use managed services** → Same app; switch by changing profile and connection properties (env or `application-aws.yml`), not by changing code.
- **Local / dev** → Keep using Docker or self-hosted instances and existing `application.yml` / `application-docker.yml`.
