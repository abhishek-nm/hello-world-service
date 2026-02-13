# Running the repo with Docker

## Prerequisites

- **Docker** and **Docker Compose** installed ([Get Docker](https://docs.docker.com/get-docker/))
- No need to install Java, Maven, RabbitMQ, Kafka, Redis, or Elasticsearch on your machine

---

## Optional infra (profiles)

RabbitMQ, Kafka, Redis, and Elasticsearch are **optional** and behind Compose **profiles**. If your service has `app.features.rabbitmq.enabled=false` (etc.), **do not** start that profile — the image won’t be pulled and the container won’t run.

| Profile              | Service      |
|----------------------|-------------|
| `with-rabbitmq`      | RabbitMQ    |
| `with-kafka`         | Kafka       |
| `with-redis`         | Redis       |
| `with-elasticsearch` | Elasticsearch |

- **App only** (no infra; disable unused features in config):  
  `docker compose up -d --build`
- **App + RabbitMQ only:**  
  `docker compose --profile with-rabbitmq up -d --build`
- **App + selected infra:**  
  `docker compose --profile with-rabbitmq --profile with-redis up -d --build`
- **App + all infra:**  
  `docker compose --profile with-rabbitmq --profile with-kafka --profile with-redis --profile with-elasticsearch up -d --build`

When you run with only some profiles, **set feature flags to match** so the app doesn’t try to connect to missing services. Override via env, e.g.:

```bash
docker compose up -d --build \
  -e APP_FEATURES_RABBITMQ_ENABLED=false \
  -e APP_FEATURES_KAFKA_ENABLED=false
```

Or in your service’s `application-docker.yml`: set only the features you use to `true`.

---

## Option 1: App + all services

From the project root:

```bash
docker compose --profile with-rabbitmq --profile with-kafka --profile with-redis --profile with-elasticsearch up -d --build
```

This will:

1. Build the app image (first time can take a few minutes).
2. Start RabbitMQ, Kafka, Redis, Elasticsearch (their images are pulled if needed).
3. Start the app on port **5001** (see compose) with profile `docker` (connects to the above by hostname).

### Check that it’s running

- **App:** http://localhost:5001  
  - Root: http://localhost:5001/  
  - API (with required headers):  
    `curl -s http://localhost:5001/api/v1/status/features -H "X-Client-Id: web" -H "X-Client-Version: 1.0.0"`
- **RabbitMQ management:** http://localhost:15672 (guest / guest)
- **Elasticsearch:** http://localhost:9200 (e.g. `curl http://localhost:9200/_cluster/health`)

### View logs

```bash
# All services
docker-compose logs -f

# Only the app
docker-compose logs -f app
```

### Stop

```bash
docker-compose down
```

---

## Option 2: App only (no infra containers)

Use this when your service does **not** use RabbitMQ, Kafka, Redis, or Elasticsearch (or you run them elsewhere). No infra images are pulled or started.

```bash
docker compose up -d --build
```

Set feature flags so the app doesn’t try to connect: in `application.yml` (or overlay) set `app.features.rabbitmq.enabled`, `app.features.kafka.enabled`, etc. to `false`, or override via env (see “Optional infra” above).

---

## Option 3: Build and run only the app image (no compose)

Use this if you already have RabbitMQ, Kafka, Redis, etc. elsewhere (or don’t need them).

```bash
# Build
docker build -t hello-world-service .

# Run (app listens on 5000 inside container; map to host 5000)
docker run -p 5000:5000 hello-world-service
```

The app uses the `docker` profile and will try to connect to hosts `rabbitmq`, `kafka`, `redis`, `elasticsearch`. If those don’t resolve (e.g. you’re not in docker-compose), override with env or run without the docker profile:

```bash
docker run -p 5000:5000 -e SPRING_PROFILES_ACTIVE=default hello-world-service
```

---

## Option 4: Only backing services (app on host)

Run the app locally and use Docker only for brokers/stores:

```bash
# Start only RabbitMQ, Kafka, Redis, Elasticsearch
docker-compose up -d rabbitmq kafka redis elasticsearch
```

Then start the app on your machine:

```bash
./mvnw spring-boot:run
```

Set in `application.yml` (or a local profile) the feature flags and hostnames (e.g. `localhost`) for the services you use.

---

## Troubleshooting

| Issue | What to do |
|--------|------------|
| **Port already in use** | Change the host port in `docker-compose.yml`, e.g. `"5001:5000"` for the app. |
| **App exits or “unhealthy”** | Check `docker-compose logs app`. The app waits for RabbitMQ, Kafka, and Redis to be healthy; first start can take 1–2 minutes. |
| **Build fails (e.g. “mvn not found”)** | The Dockerfile uses the Maven Wrapper (`mvnw`); ensure `mvnw` and `.mvn/` are in the repo and not in `.dockerignore`. |
| **API returns 400 “Missing required header”** | For `/api/v1/*` send headers: `X-Request-Id`, `X-Client-Id`, `X-Client-Version`, and for POST/PUT/PATCH `X-Idempotency-Key`. Or set `app.api.contract.strict-headers: false` in a profile. |
| **Elasticsearch healthcheck fails** | Some environments need more memory or time. You can comment out the `elasticsearch` healthcheck or increase `retries` in `docker-compose.yml`. |
| **Kafka image "not found"** | This project uses the official **apache/kafka** image. Ensure you can pull `apache/kafka:latest` (Bitnami Kafka images were moved/deprecated). |

---

## Quick test after `docker-compose up -d`

```bash
# Root (no contract)
curl -s http://localhost:5000/

# Features (with minimal headers; test profile or strict-headers=false would allow no headers)
curl -s http://localhost:5000/api/v1/status/features \
  -H "X-Client-Id: web" -H "X-Client-Version: 1.0.0"
```

If the app is up, you should get JSON from the features endpoint (with envelope `requestId`, `status`, `data`, `errors`, `meta`).
