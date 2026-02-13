# Global observability stack (Elasticsearch, Kibana, APM Server)

One **Elasticsearch**, one **Kibana**, and one **APM Server** run as Docker containers. Use this as a **shared** endpoint for **logs** and **APM** for **multiple services** (e.g. 2 or more apps created from this boilerplate).

## What runs

| Service       | Image | Port | Purpose |
|---------------|-------|------|---------|
| Elasticsearch | docker.elastic.co/elasticsearch/elasticsearch:7.17.21 | 9200 | Stores logs, APM traces, metrics |
| Kibana        | docker.elastic.co/kibana/kibana:7.17.21               | 5601 | UI: Discover, APM (traces, metrics); no Fleet integration required |
| APM Server    | docker.elastic.co/apm/apm-server:7.17.21               | 8200 | Receives data from all APM agents; writes to Elasticsearch |
| Filebeat      | —    | —    | On Kind/EKS: use the Filebeat DaemonSet in [deploy/filebeat/](../deploy/filebeat/) to ship all container logs to ES. |

## Start the stack

From the **shared-infra-observability** repo root:

```bash
From shared-infra-observability repo: ./run-observability.sh start
```

- **Kibana (logs & APM UI):** http://localhost:5601  
- **Elasticsearch:** http://localhost:9200  
- **APM Server (agent intake):** http://localhost:8200  

## Point your services at the same APM

Each of your apps (e.g. 2 services from this boilerplate) must send APM data to **this** APM Server.

### Option A: Apps run on the host (e.g. `./mvnw spring-boot:run`)

Set before starting each app:

```bash
export ELASTIC_APM_SERVER_URL=http://localhost:8200
# optional: ELASTIC_APM_SERVICE_NAME=my-service-one
./mvnw spring-boot:run
```

Use a different `ELASTIC_APM_SERVICE_NAME` per app so you can filter in Kibana (e.g. `service-one`, `service-two`).

### Option B: Apps run in Docker on the same host

Use the host’s APM endpoint from inside the container:

```bash
# Linux
docker run -e ELASTIC_APM_SERVER_URL=http://host.docker.internal:8200 -e ELASTIC_APM_SERVICE_NAME=service-one -p 5001:5000 hello-world-service

# Or in your app’s docker-compose:
environment:
  ELASTIC_APM_SERVER_URL: http://host.docker.internal:8200
  ELASTIC_APM_SERVICE_NAME: service-one
```

(`host.docker.internal` works on Docker Desktop; on Linux you may need `--add-host=host.docker.internal:host-gateway` or the host IP.)

### Option C: Apps and observability on the same Docker network

1. Start the observability stack (it creates a network named `observability`).
2. In each **app’s** compose, use that network as **external** and set `ELASTIC_APM_SERVER_URL=http://apm-server:8200`.

Start the stack:

```bash
From shared-infra-observability repo: ./run-observability.sh start
```

Then in each **app’s** compose, attach to the same network and point to the APM server by name:

```yaml
services:
  app:
    image: hello-world-service
    environment:
      ELASTIC_APM_SERVER_URL: http://apm-server:8200
      ELASTIC_APM_SERVICE_NAME: my-service-one
    networks:
      - observability

networks:
  observability:
    external: true
```

So: **one Elasticsearch, one Kibana, one APM Server**; **each app** sets `ELASTIC_APM_SERVER_URL` (and optionally `ELASTIC_APM_SERVICE_NAME`) to this shared APM endpoint.

## Where to see logs and APM in Kibana

| What | URL (Kibana base = http://localhost:5601) |
|------|-------------------------------------------|
| **Logs** | http://localhost:5601/app/logs |
| **Discover (logs index)** | http://localhost:5601/app/discover |
| **APM – services** | http://localhost:5601/app/apm/services |
| **APM – traces** | http://localhost:5601/app/apm/traces |
| **APM – service metrics** | http://localhost:5601/app/apm/services/<service-name>/metrics |

Logs appear in Kibana only if they are **shipped to Elasticsearch** (e.g. Filebeat, Fluentd, or your log pipeline). The apps only add `trace.id` to their log pattern; something must collect and send those logs to this Elasticsearch so they show under **Logs** / **Discover**.

## Separate Docker for APM

APM Server runs **only** in this observability compose (separate from your app composes). Your app containers do **not** run APM Server; they run the **APM Java agent** inside the JVM and send data to the **single** APM Server at port 8200. So:

- **One** Docker setup = `docker-compose.observability.yml` (Elasticsearch + Kibana + APM Server).
- **Each** app = its own image/compose, with `ELASTIC_APM_SERVER_URL` pointing to that single APM Server.

## Stop the stack

```bash
From shared-infra-observability repo: ./run-observability.sh stop
```
