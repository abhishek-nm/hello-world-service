# Environment profiles: dev vs prod

The app uses Spring profiles to choose which config (YAML) to load. All infra URLs (Redis, RabbitMQ, Kafka, Postgres, Elasticsearch) are **env-driven**; the profile sets the defaults.

| Profile | Config file | Infra source | Use case |
|--------|-------------|--------------|----------|
| **dev** | `application-dev.yml` | **Kubernetes** (Kind/EKS) or Docker network | Defaults: `redis`, `rabbitmq`, `kafka`, `postgres`, `elasticsearch`. Override any via env. |
| **prod** | `application-prod.yml` | **AWS managed** (RDS, ElastiCache, Amazon MQ, MSK, OpenSearch) | No in-repo defaults; set `SPRING_DATASOURCE_URL`, `SPRING_DATA_REDIS_HOST`, `SPRING_RABBITMQ_HOST`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, etc. via env. |

Full env list: [ENV_INFRA_REFERENCE.md](ENV_INFRA_REFERENCE.md).

## How to run

- **Local / Docker (dev):**
  ```bash
  # From shared-infra-observability repo:
  ./run-infra.sh start
  ./run-observability.sh start
  # From this repo: run app (e.g. docker compose up or deploy to Kind/EKS)
  SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
  # or: docker compose up -d --build app
  ```

- **Production (e.g. ECS, EKS) – profile `prod`:**
  ```bash
  SPRING_PROFILES_ACTIVE=prod
  SPRING_DATASOURCE_URL=jdbc:postgresql://your-rds:5432/boilerplate
  SPRING_DATA_REDIS_HOST=your-elasticache.amazonaws.com
  # ... etc.
  ```
  For local test against prod URLs, set `SPRING_PROFILES_ACTIVE=prod` and the env vars (or a `.env` file).

- **Which profile is active?**  
  Set `SPRING_PROFILES_ACTIVE=dev` or `prod` (e.g. in deployment env or when running `./mvnw spring-boot:run`). Default is **dev** when not set.

- **Backward compatibility:** Profile `docker` still works: it includes `dev`, so existing setups using `SPRING_PROFILES_ACTIVE=docker` behave the same as `dev`.

## Summary

- **dev** → `application-dev.yml` → Docker/local infra hostnames.
- **prod** → `application-prod.yml` → Env-driven (AWS or any managed services).
