# Pod / container health and system metrics

The app exposes **Kubernetes-style** liveness and readiness, plus **system and JVM metrics** over Spring Boot Actuator (and Prometheus).

## Health endpoints

| Endpoint | Purpose |
|----------|---------|
| **GET /actuator/health** | Overall health (up/down + optional details). |
| **GET /actuator/health/liveness** | **Liveness**: is the process alive? Orchestrator may restart the container if this fails. |
| **GET /actuator/health/readiness** | **Readiness**: is the app ready to receive traffic? Orchestrator may remove from load balancing if this fails. |

All return **HTTP 200** when healthy and **503** when not. No auth required for these endpoints (Actuator exposure is configured in `application.yml`).

## Docker

The image includes a **HEALTHCHECK** that uses the readiness endpoint:

- **Interval:** 30s  
- **Timeout:** 5s  
- **Start period:** 60s (no failure counted during startup)  
- **Retries:** 3  

So `docker run` and Docker Compose will mark the container as healthy only when `/actuator/health/readiness` returns 200. Port in the image is **5000** (Docker profile).

**Compose:** The app service in `docker-compose.yml` uses the same readiness check (with a 10s interval and 90s start period).

## Kubernetes

Use **liveness** and **readiness** probes so the kubelet can restart unhealthy pods and stop sending traffic to not-ready pods.

Example (adjust `port` if your service listens on another port, e.g. 9100):

```yaml
spec:
  containers:
    - name: hello-world-service
      image: hello-world-service:latest
      ports:
        - containerPort: 5000
      livenessProbe:
        httpGet:
          path: /actuator/health/liveness
          port: 5000
        initialDelaySeconds: 60
        periodSeconds: 30
        timeoutSeconds: 5
        failureThreshold: 3
      readinessProbe:
        httpGet:
          path: /actuator/health/readiness
          port: 5000
        initialDelaySeconds: 30
        periodSeconds: 10
        timeoutSeconds: 5
        failureThreshold: 3
```

- **Liveness**: Kubelet restarts the container if the probe fails repeatedly.
- **Readiness**: Pod is removed from Service endpoints until the probe succeeds again (e.g. during startup or when DB is temporarily down).

## Quick checks

```bash
# When app is running (e.g. on host port 9100 or container 5000)
curl -s http://localhost:9100/actuator/health | jq .
curl -s http://localhost:9100/actuator/health/liveness
curl -s http://localhost:9100/actuator/health/readiness
```

## System metrics

System and JVM metrics are exposed for Prometheus and for ad-hoc inspection:

| Endpoint | Purpose |
|----------|---------|
| **GET /actuator/prometheus** | All metrics in Prometheus text format (for scraping). Includes JVM memory, threads, GC, process and system CPU, HTTP, etc. |
| **GET /actuator/metrics** | List of metric names (JSON). |
| **GET /actuator/metrics/{name}** | Single metric (e.g. `jvm.memory.used`, `system.cpu.usage`, `process.cpu.usage`). |

**Examples:**

```bash
# Prometheus scrape target (all metrics)
curl -s http://localhost:9100/actuator/prometheus

# List metric names
curl -s http://localhost:9100/actuator/metrics | jq '.names'

# JVM memory used
curl -s http://localhost:9100/actuator/metrics/jvm.memory.used | jq .
# System CPU usage (0–1)
curl -s http://localhost:9100/actuator/metrics/system.cpu.usage | jq .
# Process CPU usage
curl -s http://localhost:9100/actuator/metrics/process.cpu.usage | jq .
```

These are the same metrics the Elastic APM agent and any Prometheus server can collect. Use port **5000** when the app runs in Docker without port mapping.
