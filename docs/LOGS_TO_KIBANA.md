# Shipping application logs to Kibana

On **Kubernetes (Kind and EKS)**, logs from all services (including this app) are shipped to Elasticsearch/Kibana via the **Filebeat DaemonSet** in this repo.

---

## Kind and EKS: Filebeat DaemonSet

1. **Add Filebeat to your GitOps repo** under `apps/filebeat/`:
   - Copy `deploy/filebeat/rbac.yaml`, `configmap.yaml`, and either `daemonset.yaml` (Kind) or `daemonset-aws.yaml` (EKS) from this repo.
   - For EKS, edit `daemonset-aws.yaml` and set your OpenSearch/ES endpoint.

2. **Register the app in Argo CD**: create an Application with path `apps/filebeat`, destination namespace `kube-system`.

3. **View logs in Kibana**: create an index pattern for `filebeat-*`, then in Discover filter by `kubernetes.namespace`, `kubernetes.labels.app`, or `kubernetes.pod.name` to see this service’s logs.

Full steps: [RUN_ON_KIND_AND_EKS.md](RUN_ON_KIND_AND_EKS.md) and [deploy/filebeat/GITOPS_ARGOCD.md](../deploy/filebeat/GITOPS_ARGOCD.md).

---

## Logs and APM trace correlation

The app’s log pattern includes **`[trace.id=...]`** when the Elastic APM agent is attached. In Kibana you can:

- In **APM**: open a trace and use “View logs” to see log lines with the same `trace.id`.
- In **Discover** (filebeat-*): search for `trace.id:<id>` or filter by service/namespace.

Ensure Filebeat is sending to the same Elasticsearch as APM so correlation works.
