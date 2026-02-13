# Local (Mac) vs AWS Kubernetes

This service is set up to run **on your Mac** now and **on AWS Kubernetes (EKS)** later. Same image and config pattern; only the runtime and some wiring change.

---

## Current: Mac (local dev)

| Area | What you use |
|------|----------------|
| **App** | Docker: `docker run` on `infra` network, or `./mvnw spring-boot:run` with infra on localhost. |
| **Infra** | From shared-infra-observability repo: `./run-infra.sh start` (Postgres, Redis, RabbitMQ, Kafka). |
| **Observability** | From shared-infra-observability repo: `./run-observability.sh start`. Kibana: http://localhost:5601, APM Server: http://localhost:8200. |
| **APM** | Set `ELASTIC_APM_SERVER_URL=http://localhost:8200` (host) or `http://host.docker.internal:8200` (app in Docker). Traces and metrics show in Kibana APM. |
| **Logs in Kibana** | On **Kind/EKS**: use the Filebeat DaemonSet in [deploy/filebeat/](../deploy/filebeat/) (see [RUN_ON_KIND_AND_EKS.md](RUN_ON_KIND_AND_EKS.md)). On local Docker without K8s, use the observability stack’s log pipeline if available. |
| **Health** | Liveness/readiness at `/actuator/health/liveness` and `/actuator/health/readiness`. Docker image has `HEALTHCHECK`. |

---

## Future: AWS Kubernetes (EKS)

| Area | What to use |
|------|-------------|
| **App** | Deploy the same image as a Deployment. Expose with a Service (and Ingress/ALB if needed). |
| **Infra** | Prefer **AWS managed**: RDS (Postgres), ElastiCache (Redis), Amazon MQ (RabbitMQ), MSK (Kafka). Point the app at them via env (or ConfigMap/Secrets). See [docs/DEPLOYMENT_OPTIONS.md](DEPLOYMENT_OPTIONS.md) and `application-aws.yml.example`. |
| **Config** | Use env vars, ConfigMaps, or Secrets for `SPRING_DATASOURCE_URL`, `SPRING_DATA_REDIS_HOST`, `SPRING_RABBITMQ_HOST`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `APP_FEATURES_*`, etc. No code change; same feature flags. |
| **APM** | Set `ELASTIC_APM_SERVER_URL` to your Elastic Cloud URL or an APM Server in the cluster. Same Java agent; only the URL changes. |
| **Logs in Kibana** | **Yes.** Use the **Filebeat DaemonSet** in [deploy/filebeat/](../deploy/filebeat/): it reads all container logs from each node and sends to Elasticsearch/OpenSearch. See [RUN_ON_KIND_AND_EKS.md](RUN_ON_KIND_AND_EKS.md) and [deploy/filebeat/GITOPS_ARGOCD.md](../deploy/filebeat/GITOPS_ARGOCD.md). |
| **Health** | Use the same liveness/readiness endpoints in your Deployment. Example in [docs/POD_HEALTH.md](POD_HEALTH.md) (e.g. `httpGet` on `/actuator/health/liveness` and `/actuator/health/readiness` on the app port). |
| **Secrets** | Store DB and broker credentials in Kubernetes Secrets (or AWS Secrets Manager + CSI driver); inject as env or mounted files. |

---

## Summary

- **Mac (Kind)**: App and Filebeat DaemonSet via Argo CD; logs to Kibana via deploy/filebeat.  
- **AWS Kubernetes (EKS)**: Same app and image; use RDS/ElastiCache/Amazon MQ/MSK, env-based config, APM URL for your Elastic/APM setup, Filebeat DaemonSet (deploy/filebeat) for logs, and the existing health endpoints for pod probes.

No application code changes are required when moving from Mac to EKS; only deployment and configuration (env, Secrets, log pipeline) change.

---

## Logs to Kibana on AWS Kubernetes (Linux)

The same approach as on Mac works on Linux and EKS: the app writes to **`/app/logs`**; a log shipper reads that path and sends to Elasticsearch.

- **Option 1 – Sidecar (same pattern as Mac):** In your Deployment, use a shared volume (e.g. `emptyDir`) mounted at `/app/logs` for the app container, and run a **Filebeat or Fluent Bit sidecar** in the same pod that mounts the same volume, reads `/app/logs/*.log`, and sends to your Elasticsearch or OpenSearch endpoint. No app change; same image and config.
- **Option 2 – DaemonSet:** Run Fluent Bit or Filebeat as a DaemonSet that tails container logs from the node (e.g. `/var/log/pods`). Then the app can rely on stdout only; the DaemonSet collects it. You can still keep `logging.file.path` for a local copy if you want.

For Option 1, ensure the app runs with the same profile or env that sets `logging.file.path=/app/logs` (your Docker/K8s profile already does this when the image uses the docker profile).
