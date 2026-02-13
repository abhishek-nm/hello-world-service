# Shared infra stack (Redis, RabbitMQ, Kafka, PostgreSQL)

Same idea as the **observability stack**: one Redis, one RabbitMQ, one Kafka, one PostgreSQL run as a **separate** Docker Compose. Run once and point **all your apps** (e.g. multiple services from this boilerplate) at it.

## What runs

| Service   | Image              | Port(s)        | Purpose        |
|-----------|--------------------|----------------|----------------|
| Redis     | redis:7-alpine     | 6379           | Cache          |
| RabbitMQ  | rabbitmq:3-management | 5672, 15672 | AMQP + management UI |
| Kafka     | apache/kafka:latest | 9092         | Event streaming |
| PostgreSQL| postgres:16-alpine | 5432           | Database (RDS-style) |

## Start the stack

From the project root:

```bash
From shared-infra-observability repo: ./run-infra.sh start
```

- **Redis:** `localhost:6379`
- **RabbitMQ:** `localhost:5672` (AMQP), management http://localhost:15672 (guest/guest)
- **Kafka:** `localhost:9092`
- **PostgreSQL:** `localhost:5432` (user `postgres`, password `postgres`, database `boilerplate`)

## Point your app at this stack

**This service is configured by default to use this infra.** No extra config needed if you run the stack and then start the app.

### Apps on the host (e.g. `./mvnw spring-boot:run`)

1. Start the infra stack: `From shared-infra-observability repo: ./run-infra.sh start`
2. Run the app. Default `application.yml` already has Postgres, Redis, RabbitMQ, and Kafka enabled and pointed at `localhost` and the ports above.

To run without infra (e.g. H2 only), set `app.features.postgres.enabled=false`, `app.features.redis.enabled=false`, etc., and override `spring.datasource.url` to `jdbc:h2:mem:...` if needed.

### Apps in Docker (same network)

1. Start the infra stack (it uses network `infra`).
2. Run the app with profile `docker` and attach to the `infra` network. `application-docker.yml` already points to hostnames `postgres`, `redis`, `rabbitmq`, `kafka`:

```yaml
services:
  app:
    build: .
    environment:
      SPRING_PROFILES_ACTIVE: docker
    networks:
      - infra

networks:
  infra:
    external: true
```

### App on infra + APM (observability stack)

To run the app in Docker on the infra network **and** send traces to Elastic APM:

1. Start observability stack (Kibana, Elasticsearch, APM Server):  
   From shared-infra-observability repo: ./run-observability.sh start
2. Start infra stack:  
   `From shared-infra-observability repo: ./run-infra.sh start`
3. Build and run the app (APM Server is reached via host):

```bash
docker build -t hello-world-service .
docker run --rm -p 9100:5000 --network infra \
  --add-host=host.docker.internal:host-gateway \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e ELASTIC_APM_SERVER_URL=http://host.docker.internal:8200 \
  -e ELASTIC_APM_SERVICE_NAME=hello-world-service \
  hello-world-service
```

From this repo: deploy the app via Argo CD (Kind/EKS) or `docker compose up`; for logs to Kibana on Kubernetes use the Filebeat DaemonSet in [deploy/filebeat/](../deploy/filebeat/) (see [RUN_ON_KIND_AND_EKS.md](RUN_ON_KIND_AND_EKS.md)).

- **App:** http://localhost:9100  
- **APM in Kibana:** http://localhost:5601/app/apm → select service `hello-world-service`

## Relation to other composes

| Compose file                      | Contents                          | Use case                    |
|-----------------------------------|-----------------------------------|-----------------------------|
| shared-infra-observability repo | Redis, RabbitMQ, Kafka, Postgres, ES, Kibana, APM | Shared infra + observability |
| `docker-compose.yml`             | App + optional same services     | All-in-one dev (app + infra)|

You can run **infra** and **observability** separately and run your app(s) on the host or in their own compose with `external: true` to the `infra` and `observability` networks.

## Use only the infra components this app needs

The **infra stack runs all four services** (Redis, RabbitMQ, Kafka, Postgres). Other services can use any of them. **This app** only connects to the components you enable in config — no env vars needed.

**Edit `application-docker.yml`:** set `app.features.<component>.enabled: true` only for the infra this service uses; set `false` for the rest. That file is the single source of truth when running with profile `docker`. Example: this service uses Postgres, Redis, and RabbitMQ but not Kafka — so in `application-docker.yml`, `kafka.enabled` is `false`. Kafka still runs in infra for other apps; this app will not connect to it.

You can run the app normally:

```bash
docker run --rm -p 9100:5000 --network infra \
  --add-host=host.docker.internal:host-gateway \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e ELASTIC_APM_SERVER_URL=http://host.docker.internal:8200 \
  -e ELASTIC_APM_SERVICE_NAME=hello-world-service \
  hello-world-service
```

To change which infra this service uses, edit `application-docker.yml` and rebuild the image; no need to pass `APP_FEATURES_*` env vars.

## Stop the stack

```bash
From shared-infra-observability repo: ./run-infra.sh stop
```
