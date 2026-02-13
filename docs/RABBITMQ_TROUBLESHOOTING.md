# Why RabbitMQ returns errors (RABBITMQ_UNAVAILABLE / INTERNAL_ERROR)

The app connects to RabbitMQ using a **host and port** that depend on **how you run the app**.

---

## 1. How the app chooses the RabbitMQ host

| How you run the app | Active profile | RabbitMQ host:port |
|---------------------|----------------|---------------------|
| **Inside Docker** (e.g. `docker-compose up`) | `docker` | **rabbitmq:5672** (Docker service name) |
| **On your machine** (e.g. `./mvnw spring-boot:run`) | default | **localhost:5672** |

- **Docker profile** is set in the Dockerfile and in `docker-compose.yml` → reads `application-docker.yml` → `spring.rabbitmq.host: rabbitmq`.
- **Default profile** → reads `application.yml` → `spring.rabbitmq.host: localhost`.

If the app tries to connect to the wrong host (or RabbitMQ isn’t running), you get connection errors and the API returns **503** with `RABBITMQ_UNAVAILABLE` or (before the fix) **500** with `INTERNAL_ERROR`.

---

## 2. Typical causes

### A. App in Docker, but RabbitMQ not running or not in the same stack

- You run **only the app container** (e.g. `docker run -p 5001:5000 hello-world-service`) **without** starting the RabbitMQ container.
- The app uses hostname **`rabbitmq`**. That name only resolves when a RabbitMQ service is running on the **same Docker network** (e.g. same `docker-compose` stack).

**Fix:** Start the full stack so the app and RabbitMQ are on the same network:

```bash
docker-compose up -d
```

(Or at least: `docker-compose up -d rabbitmq` and then start the app with the same compose file so they share the `appnet` network.)

---

### B. App on host, RabbitMQ not running or not exposed on localhost

- You run the app with `./mvnw spring-boot:run` (no Docker). It tries **localhost:5672**.
- Nothing is listening on 5672 (RabbitMQ not started, or not port‑mapped to the host).

**Fix:** Start RabbitMQ and expose port 5672 to the host, e.g.:

```bash
docker-compose up -d rabbitmq
```

Then the app on the host can use `localhost:5672`.

---

### C. RabbitMQ not ready when the app starts

- Compose starts the app as soon as RabbitMQ’s **healthcheck** passes. Sometimes AMQP is still not fully ready and the first connection fails.

**Fix:** Retry the request; or restart the app after RabbitMQ has been up for a few seconds: `docker-compose restart app`.

---

### D. Wrong profile when running the app

- You run the app **in Docker** but override the profile to `default` (e.g. `-e SPRING_PROFILES_ACTIVE=default`). Then the app still tries **localhost:5672** from inside the container, and “localhost” is the container itself, not the host or the RabbitMQ container → connection fails.

**Fix:** When the app runs in Docker with compose, **do not** set `SPRING_PROFILES_ACTIVE=default`. Let it use the **docker** profile so it uses `rabbitmq:5672`.

---

## 3. How to verify RabbitMQ

1. **Containers and network**
   ```bash
   docker ps
   ```
   You should see both `hello-world-service` (or your app name) and `rabbitmq` (or similar). If only the app is running, start RabbitMQ (e.g. `docker-compose up -d rabbitmq`).

2. **Management UI**
   - Open **http://localhost:15672** in a browser.
   - Login: **guest** / **guest**.
   - If this works, RabbitMQ is up and reachable on the host.

3. **App logs**
   ```bash
   docker-compose logs app
   ```
   Look for AMQP connection errors (e.g. “Connection refused”, “UnknownHostException: rabbitmq”). That confirms the app is failing to reach RabbitMQ.

4. **Same Docker network**
   If the app runs in Docker, it must be on the same network as RabbitMQ (e.g. both in the same `docker-compose` with `networks: - appnet`). Don’t run the app with a plain `docker run` and expect it to see a `rabbitmq` hostname unless you use that same network.

---

## 4. Quick checklist

- [ ] RabbitMQ container is running (`docker ps`).
- [ ] If the app runs **in Docker**: it uses profile **docker** and host **rabbitmq** → start with `docker-compose up -d` (app + rabbitmq on same network).
- [ ] If the app runs **on host**: it uses **localhost:5672** → start RabbitMQ with `docker-compose up -d rabbitmq` (or similar) so 5672 is exposed on the host.
- [ ] Management UI http://localhost:15672 is reachable (guest/guest).
- [ ] No profile override that forces the app to use `localhost` when it’s actually running inside Docker.

After fixing the cause, call the API again; you should get **202** when the message is sent, or **503** with `RABBITMQ_UNAVAILABLE` and a clear message if RabbitMQ is still down or unreachable.
