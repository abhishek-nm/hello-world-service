# Run the app with `docker run` (infra network)

Use this when Redis, RabbitMQ, Kafka, Postgres, etc. are already running (e.g. on the **`infra`** network from your infra/observability compose). The app joins that network and uses hostnames **redis**, **rabbitmq**, **kafka**, **postgres**, **elasticsearch**. Profile and APM URL are set in the **Dockerfile** (no need to pass them).

---

## `docker run` (no script)

```bash
docker build -t hello-world-service .

docker run --rm -p 9100:5000 --network infra \
  --add-host=host.docker.internal:host-gateway \
  hello-world-service
```

App: **http://localhost:9100**

- **`--network infra`** – network where your infra/observability containers run (`docker network create infra` if needed).
- **`--add-host=host.docker.internal:host-gateway`** – so the app can reach the host (e.g. APM Server on Mac).
- **Profile and APM** – default in image: `SPRING_PROFILES_ACTIVE=docker`, `ELASTIC_APM_SERVER_URL=http://host.docker.internal:8200`, `ELASTIC_APM_SERVICE_NAME=hello-world-service`. Override with `-e` only if needed.

Optional env to enable/disable features or override defaults:

| Env | Effect |
|-----|--------|
| `APP_FEATURES_REDIS_ENABLED=true` | Use Redis cache (default true in dev) |
| `APP_FEATURES_RABBITMQ_ENABLED=true` | Use RabbitMQ (default true in dev) |
| `APP_FEATURES_KAFKA_ENABLED=true` | Use Kafka (default false in dev; set if Kafka is on infra) |
| `APP_FEATURES_ELASTICSEARCH_ENABLED=true` | Use Elasticsearch search (default false in dev) |
| `APP_FEATURES_POSTGRES_ENABLED=true` | Use Postgres (dev uses it by default) |

Example with Kafka and Elasticsearch enabled:

```bash
docker run --rm -p 9100:5000 --network infra \
  --add-host=host.docker.internal:host-gateway \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e ELASTIC_APM_SERVER_URL=http://host.docker.internal:8200 \
  -e ELASTIC_APM_SERVICE_NAME=hello-world-service \
  -e APP_FEATURES_KAFKA_ENABLED=true \
  -e APP_FEATURES_ELASTICSEARCH_ENABLED=true \
  hello-world-service
```

---

## 3. Prerequisites

- **Network:** Containers for Redis, RabbitMQ, Kafka, Postgres (and optionally Elasticsearch) must be on the same network (e.g. **infra**). Create it with `docker network create infra` and run your infra stack on that network.
- **Hostnames:** The app expects **redis**, **rabbitmq**, **kafka**, **postgres**, **elasticsearch** as hostnames (see `application-dev.yml`). Use the same service/container names or add aliases when you start the infra.
- **APM (optional):** If APM Server runs on the host, `ELASTIC_APM_SERVER_URL=http://host.docker.internal:8200` is correct on Mac/Windows. If APM runs in a container on **infra**, use e.g. `http://apm-server:8200`.
