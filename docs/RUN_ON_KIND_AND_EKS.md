# One codebase: Mac Kind and AWS EKS

This repo is the **single codebase** you run on **Mac Kind** and **AWS EKS**. Same app, same GitOps layout; only environment-specific bits (Filebeat ES endpoint, optional app values) differ.

---

## What this repo contains (for Kind + EKS)

| Purpose | Location | Kind | EKS |
|--------|----------|------|-----|
| **App image** | `Dockerfile` | Built by CI or locally; image in registry | Built by Circle CI; pushed to Docker Hub |
| **App deployment** | GitOps repo `apps/hello-world/` (Helm) | Argo CD syncs from GitOps | Argo CD syncs from GitOps |
| **Logs → Kibana** | `deploy/filebeat/` | Use `rbac.yaml` + `configmap.yaml` + **`daemonset.yaml`** | Use `rbac.yaml` + `configmap.yaml` + **`daemonset-aws.yaml`** (set ES endpoint) |
| **Argo CD app for Filebeat** | `deploy/filebeat/argocd-application.yaml` | Point path to `apps/filebeat` (with Kind daemonset) | Point path to `apps/filebeat` (with EKS daemonset) |

---

## Mac Kind

### Prerequisites

- Kind cluster created, `kubectl` and Argo CD installed and targeting that cluster.
- Elasticsearch and Kibana running (e.g. on your Mac on 9200 / 5601).

### 1. App

- GitOps repo has `apps/hello-world/` (Helm chart) with image tag from CI or set manually.
- Argo CD Application for `hello-world` points at `apps/hello-world`, destination namespace as you use (e.g. `default`).
- Sync the app; it runs on Kind.

### 2. Logs to Kibana (Filebeat)

- In your **GitOps repo**, under **`apps/filebeat/`**, put:
  - `rbac.yaml`
  - `configmap.yaml`
  - **`daemonset.yaml`** (uses `host.docker.internal:9200` for ES)
- Do **not** put `daemonset-aws.yaml` in that path (so only the Kind daemonset is applied).
- Argo CD Application for **filebeat**: path **`apps/filebeat`**, destination namespace **`kube-system`**.
- Sync; Filebeat DaemonSet runs and sends all container logs to your Mac’s Elasticsearch/Kibana.

### 3. Verify

- App: `kubectl get pods` in the app namespace; call the service (NodePort/LoadBalancer or port-forward).
- Logs: Kibana → index pattern `filebeat-*` → Discover; filter e.g. by `kubernetes.namespace` or `kubernetes.labels.app`.

---

## AWS EKS

### Prerequisites

- EKS cluster, `kubectl` and Argo CD configured for that cluster.
- Amazon OpenSearch (or Elasticsearch) and Kibana/Dashboards available; note the endpoint.

### 1. App

- Same as Kind: GitOps repo `apps/hello-world/` (Helm), image tag from Circle CI (or your pipeline).
- Argo CD Application for `hello-world` points at `apps/hello-world` for the EKS cluster.
- Sync; app runs on EKS.

### 2. Logs to Kibana (Filebeat)

- In your **GitOps repo**, under **`apps/filebeat/`**, put:
  - `rbac.yaml`
  - `configmap.yaml`
  - **`daemonset-aws.yaml`** (edit: replace `REPLACE_WITH_OPENSEARCH_OR_ES_ENDPOINT` with your OpenSearch/ES host; add Secret for auth if needed)
- Do **not** put `daemonset.yaml` (Kind) in that path for EKS (so only the EKS daemonset is applied).
- Argo CD Application for **filebeat**: path **`apps/filebeat`**, destination namespace **`kube-system`**.
- Sync; Filebeat DaemonSet runs and sends all container logs to your OpenSearch/Kibana.

### 3. Verify

- App: `kubectl get pods` in the app namespace; use LoadBalancer/Ingress or port-forward.
- Logs: OpenSearch Dashboards (or Kibana) → index pattern `filebeat-*` → Discover; filter by namespace or labels.

---

## One-codebase summary

| Item | Mac Kind | AWS EKS |
|------|----------|---------|
| **App** | Same GitOps `apps/hello-world/`, Argo CD | Same GitOps `apps/hello-world/`, Argo CD |
| **Filebeat** | `deploy/filebeat/` → GitOps `apps/filebeat/` with **daemonset.yaml** | Same dir → GitOps `apps/filebeat/` with **daemonset-aws.yaml** (endpoint set) |
| **ES/Kibana** | On Mac; Filebeat uses `host.docker.internal:9200` | OpenSearch/ES in AWS; Filebeat uses your endpoint (e.g. HTTPS:443) |

Everything you need for both environments is in **this repo** and your **GitOps repo**; the only differences are which daemonset file you put in `apps/filebeat/` and the Elasticsearch endpoint (Kind: host.docker.internal, EKS: your OpenSearch/ES URL).
