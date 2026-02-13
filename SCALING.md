# Scaling to 100M+ Users and Horizontal Distribution

This doc summarises what this boilerplate does for scale and what you should add for production at 100M+ users.

---

## What’s already in place (horizontal / scale-friendly)

| Area | What’s done | Why it helps |
|------|----------------|--------------|
| **Stateless app** | No in-memory session; all shared state in DB/Redis/Kafka/RabbitMQ | Any instance can serve any request; scale by adding instances behind a load balancer |
| **RestTemplate** | Connect/read timeouts (5s / 10s) in `RestTemplateConfig` | One slow downstream doesn’t block threads forever |
| **Circuit breaker** | Resilience4j on downstream call (`HelloController`), config in `application.yml` | Fails fast when downstream is down; avoids thread exhaustion and cascading failure |
| **Redis cache** | Configurable `app.cache.key-prefix` and `app.cache.ttl-seconds` | Safe to share one Redis across services (prefix per service); TTL avoids unbounded growth |
| **DB connection pool** | HikariCP with `maximum-pool-size: 20`, `minimum-idle: 5` | Bounded connections per instance; tune per env (e.g. 20–50) |
| **Redis connection pool** | Lettuce pool (`max-active: 16`, `max-idle: 8`) when Redis enabled | Bounded Redis connections per instance |
| **JPA batch fetch** | `default_batch_fetch_size: 32` | Reduces N+1 and round-trips for collections |
| **Kafka** | Producer acks=1, retries=3; consumer `concurrency: 3` | Throughput and at-least-once; scale consumers by increasing concurrency or instances |
| **Feature flags** | Postgres/Redis/Kafka/RabbitMQ toggled by config | Run with minimal deps in dev; enable only what each env needs |

---

## What you should add for 100M and horizontal scale

### Application layer

- **Rate limiting**  
  Per user/IP/API to protect downstream and DB (e.g. Bucket4j, Redis-backed, or API gateway).
- **Request timeouts**  
  RestTemplate is already timed; add timeouts for any other HTTP clients and for Kafka/Rabbit consumer processing where appropriate.
- **Idempotency**  
  For writes and event publishing, use idempotency keys (e.g. in DB or Redis) so retries don’t duplicate side effects.

### Database (Postgres)

- **Read replicas**  
  Use separate read datasources and `@Transactional(readOnly = true)` for read-heavy paths so writes go to primary, reads to replicas.
- **Migrations**  
  In production use Flyway/Liquibase; set `spring.jpa.hibernate.ddl-auto=validate` (or `none`) and never `update` on prod.
- **Pool sizing**  
  Rule of thumb: `pool size ≈ (core_count * 2) + effective_spindle_count`; tune with DB max_connections and instance count. Keep `maximum-pool-size` in `application.yml` per instance.

### Redis

- **Cluster / HA**  
  For high availability and scale, use Redis Cluster or at least Sentinel; configure `spring.data.redis` accordingly.
- **Cache stampede**  
  For very hot keys, add probabilistic early expiry, locking, or “request coalescing” so many threads don’t hit the backend at once when the key expires.

### Kafka

- **Partition key**  
  Use `KafkaProducerService.send(topic, key, value)` with a stable key (e.g. entity id) so ordering is per key and partitions are used evenly.
- **Consumer scaling**  
  Increase `spring.kafka.consumer.concurrency` or run more instances (same group-id) so partitions are spread across consumers.
- **DLQ and retries**  
  For failed messages use retries + dead-letter topic and alerting; avoid infinite retry loops.

### RabbitMQ

- **Prefetch and concurrency**  
  Tune `spring.rabbitmq.listener.simple.prefetch` and `concurrency` so each instance does useful work without starving other consumers.
- **DLQ**  
  Configure dead-letter exchange/queue for failed messages.

### Observability and ops

- **Distributed tracing**  
  Add Micrometer Tracing (e.g. with OpenTelemetry or Brave) and propagate trace ids across HTTP and messaging so you can follow a request across services.
- **Structured logging**  
  JSON logs with trace id, user id (if present), and correlation id; ship to a central store (e.g. ELK, Datadog).
- **Health**  
  Keep `/actuator/health` and use it for k8s liveness/readiness; add DB/Redis/Kafka health indicators so the platform can replace unhealthy instances.
- **Secrets**  
  Use a secret manager (e.g. Vault, cloud provider secrets) and inject DB/Redis/Kafka credentials via env or config; avoid committing secrets.

### Infrastructure

- **Horizontal scaling**  
  Run N stateless instances behind a load balancer; scale N by CPU/throughput/latency (e.g. HPA in Kubernetes).
- **DB and brokers**  
  Scale Postgres (primary + replicas), Redis (cluster), Kafka (brokers and partitions), RabbitMQ (cluster) according to load; document target RPS and P99 per service.

---

## Quick checklist (100M / horizontal)

- [ ] App is stateless (no local session).
- [ ] All outbound calls have timeouts and, where appropriate, circuit breakers.
- [ ] DB: connection pool tuned; read replicas for reads; migrations only (no ddl-auto update in prod).
- [ ] Redis: key prefix if shared; TTL on caches; cluster/HA in prod.
- [ ] Kafka: partition key for ordering/balance; consumer concurrency and DLQ.
- [ ] RabbitMQ: prefetch/concurrency and DLQ.
- [ ] Rate limiting and idempotency where needed.
- [ ] Tracing, structured logs, health checks, and secrets from a secret manager.

This repo gives you the patterns and config hooks; apply the items above in your environment and capacity plan.
