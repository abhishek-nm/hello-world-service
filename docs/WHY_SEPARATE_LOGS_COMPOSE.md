# Logs to Kibana (Kind and EKS)

This repo uses a **Filebeat DaemonSet** for shipping logs to Elasticsearch/Kibana when running on **Kubernetes** (Mac Kind or AWS EKS).

- **Location:** [deploy/filebeat/](../deploy/filebeat/)
- **How it works:** One Filebeat pod per node reads all container logs from the node and sends them to your Elasticsearch. No per-service configuration; deploy once per cluster.
- **Setup:** Copy the Filebeat YAML into your GitOps repo under `apps/filebeat/` and point an Argo CD Application at it. Use `daemonset.yaml` for Kind, `daemonset-aws.yaml` for EKS (set your OpenSearch/ES endpoint).

See [RUN_ON_KIND_AND_EKS.md](RUN_ON_KIND_AND_EKS.md) and [deploy/filebeat/GITOPS_ARGOCD.md](../deploy/filebeat/GITOPS_ARGOCD.md) for exact steps.
