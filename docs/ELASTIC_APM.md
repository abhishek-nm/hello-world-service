# Elastic APM: traces, metrics, and log correlation

This service can send **traces** and **metrics** to Elastic APM and correlate **logs** with traces via `trace.id` in the log pattern.

## Setup: See APM in Kibana (step-by-step)

### 1. Start the observability stack

From the project root:

```bash
From shared-infra-observability repo: ./run-observability.sh start
```

Wait until Kibana is ready (about 1–2 minutes). Check:

- **Kibana:** http://localhost:5601 (open in browser; wait for "Ready" if prompted)
- **APM Server:** http://localhost:8200 (agent intake; no UI)
- **Elasticsearch:** http://localhost:9200

### 2. Install the APM integration in Kibana (required for 8.x)

APM Server 8.x requires the **APM integration** (index templates) to be installed in Elasticsearch. Without it, you’ll see errors like `index template matching [metrics-apm.app] not found` and no data in Kibana.

1. Open **http://localhost:5601**
2. In the main menu (hamburger ≡), go to **Integrations** (or **Add integrations** / **Fleet** → **Integrations**)
3. Search for **“Elastic APM”** or **“APM”**
4. Open the **Elastic APM** integration and click **Add Elastic APM** (or **Install**)
5. On the setup page, click **Save and continue**, then **Add Elastic Agent later** (you don’t need Fleet Agent for app-instrumented APM)
6. Wait a few seconds; APM Server will then be able to write data to Elasticsearch

After this, restart APM Server so it passes its precondition check:  
`docker restart observability-apm-server`

### 3. Start your app with APM enabled

**Option A – App on host** (`./mvnw spring-boot:run`)

```bash
export ELASTIC_APM_SERVER_URL=http://localhost:8200
export ELASTIC_APM_SERVICE_NAME=hello-world-service
./mvnw spring-boot:run
```

**Option B – App in Docker** (infra network + APM)

```bash
From shared-infra-observability repo: ./run-infra.sh start
docker build -t hello-world-service .
docker run --rm -p 9100:5000 --network infra \
  --add-host=host.docker.internal:host-gateway \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e ELASTIC_APM_SERVER_URL=http://host.docker.internal:8200 \
  -e ELASTIC_APM_SERVICE_NAME=hello-world-service \
  hello-world-service
```

From this repo: deploy the app (e.g. via Argo CD on Kind/EKS or `docker compose up`).

### 4. Generate some traffic

The agent only sends data when there are requests. Call your API at least once:

```bash
curl -s http://localhost:9100/api/v1/events
curl -X POST http://localhost:9100/api/v1/events/rabbit \
  -H "Content-Type: application/json" \
  -H "X-Client-Id: web" -H "X-Client-Version: 1.0.0" \
  -H "X-Idempotency-Key: $(uuidgen 2>/dev/null || echo idem-1)" \
  -d '{"message": "test"}'
```

(Use port **5000** if your app is not mapped to 9100.)

### 5. Open APM in Kibana

1. Open **http://localhost:5601**
2. In the left menu, go to **Analytics → APM** (or open **http://localhost:5601/app/apm**).
3. You should see service **hello-world-service**. Click it.
4. Check **Transactions** or **Traces**; expand the time range to **Last 24 hours** if you don’t see data.

If the service does not appear, see **Troubleshooting** below.

## Enable APM

1. **Set the APM server URL** (required for the agent to attach):
   ```bash
   export ELASTIC_APM_SERVER_URL=http://your-apm-server:8200
   ```
   Or in Docker:
   ```yaml
   environment:
     ELASTIC_APM_SERVER_URL: http://apm-server:8200
   ```

2. **Set the feature flag** (optional, for consistency and `/api/v1/status/features`):
   ```yaml
   app:
     features:
       apm:
         enabled: true
   ```

3. Start the app. The agent attaches before Spring starts and sends:
   - **Traces**: HTTP requests, DB calls, outgoing HTTP, Kafka/RabbitMQ when used.
   - **Metrics**: JVM and application metrics.
   - **Log correlation**: The agent injects `trace.id` (and `transaction.id`, `error.id`) into the SLF4J MDC. The log pattern in `logback-spring.xml` includes `[trace.id=%X{trace.id}]` so you can correlate log lines with traces in Kibana.

## Configuration (env or system properties)

| Env variable | Description |
|--------------|-------------|
| `ELASTIC_APM_SERVER_URL` | APM Server URL (e.g. `http://apm-server:8200`). **Required** for the agent to attach. |
| `ELASTIC_APM_SERVICE_NAME` | Service name in APM (default from `elasticapm.properties`: `hello-world-service`). |
| `ELASTIC_APM_ENVIRONMENT` | Environment (e.g. `production`, `staging`). |
| `ELASTIC_APM_SERVICE_VERSION` | Service version. |
| `ELASTIC_APM_ENABLED` | Set to `true` to attach even if `ELASTIC_APM_SERVER_URL` is not set (e.g. when using default URL). |

Defaults are in `src/main/resources/elasticapm.properties`; env vars (prefix `ELASTIC_APM_`) override them.

## Where to view logs and traces (Kibana URLs)

Use **Kibana** (the Elastic Stack UI). Replace `https://your-kibana-host` with your actual Kibana base URL (e.g. `https://your-deployment.kb.us-central1.gcp.cloud.es.io` for Elastic Cloud, or `http://localhost:5601` for local Kibana).

| What | Kibana URL |
|------|------------|
| **Logs** (stream / search) | `https://your-kibana-host/app/logs` |
| **Logs (Discover)** | `https://your-kibana-host/app/discover` (choose your log data view or index pattern, e.g. `logs-*`) |
| **APM – services / traces** | `https://your-kibana-host/app/apm` → select service `hello-world-service` (or your `ELASTIC_APM_SERVICE_NAME`) |
| **APM – single trace** | `https://your-kibana-host/app/apm/traces` → open a trace → use “View logs” / “Logs” to see correlated logs by `trace.id` |
| **APM – metrics** | `https://your-kibana-host/app/apm/services/<service-name>/metrics` |

To get logs into Kibana on **Kind or EKS**, use the Filebeat DaemonSet in [deploy/filebeat/](../deploy/filebeat/) (see [RUN_ON_KIND_AND_EKS.md](RUN_ON_KIND_AND_EKS.md)). The app prints logs with `trace.id` so you can correlate with APM traces in Kibana.

## Log correlation

When the agent is attached, each log line can include `[trace.id=<id>]`. In Elastic Stack you can:

- View a trace in APM and jump to correlated logs (by `trace.id`).
- View logs and jump to the corresponding trace.

Ensure your log shipping (Filebeat, Fluentd, or similar) sends logs to the same Elasticsearch/Logs app so that correlation by `trace.id` works in Kibana.

## Disable APM

- Do **not** set `ELASTIC_APM_SERVER_URL` (and do not set `ELASTIC_APM_ENABLED=true`).
- Set `app.features.apm.enabled: false` (default).

The agent will not attach; no APM dependency is used at runtime beyond the attach check.

The observability stack (Elasticsearch, Kibana, APM Server 7.17.x) does **not** require Kibana Integrations or Fleet.

## Troubleshooting: APM not showing in Kibana

| Check | What to do |
|-------|------------|
| **Observability stack running?** | `docker ps` and look for `observability-elasticsearch`, `observability-kibana`, `observability-apm-server`. If not: `From shared-infra-observability repo: ./run-observability.sh start` and wait 1–2 min. |
| **App started with APM URL?** | The agent attaches only at JVM startup. Restart the app **after** setting `ELASTIC_APM_SERVER_URL` (e.g. `export ELASTIC_APM_SERVER_URL=http://localhost:8200` then start the app). For Docker, ensure the run command includes `-e ELASTIC_APM_SERVER_URL=...`. |
| **App in Docker can reach APM?** | From the app container, APM Server must be reachable. Use `http://host.docker.internal:8200` (Mac/Windows). On Linux add `--add-host=host.docker.internal:host-gateway` to `docker run`. |
| **Any traffic?** | APM sends data when there are requests. Call at least one endpoint (e.g. `curl http://localhost:9100/api/v1/events`) and wait 10–30 seconds. |
| **Kibana time range** | In APM, set the time picker (top right) to **Last 24 hours** or **Last 15 minutes** so new data is included. |
| **Correct Kibana URL** | APM is under **Analytics → APM** in the left menu, or open **http://localhost:5601/app/apm** directly. |
| **APM Server logs** | `docker logs observability-apm-server` — look for errors connecting to Elasticsearch or receiving from agents. |
| **“index template matching [metrics-apm.*] not found”** | This stack uses APM Server 7.x (loads its own index templates). Restart: `docker restart observability-apm-server`. |
| **Integrations page keeps loading / “error loading integrations”** | Kibana may be unable to reach the Elastic Package Registry (network). This repo uses the 7.x observability stack only (no Integrations/Fleet). Use `From shared-infra-observability repo: ./run-observability.sh start`. |
| **Elasticsearch has APM indices?** | `curl -s http://localhost:9200/_cat/indices?v | grep -i apm` — you should see indices like `apm-*` after the app sends data. |
