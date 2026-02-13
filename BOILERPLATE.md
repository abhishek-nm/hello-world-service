# Boilerplate: Adding a New API

This doc describes how to add a new resource (e.g. Orders, Users) on top of this service. The sample **Sample** API (neutral name) is the reference.

## Layer overview

```
Controller (REST) → Service (business logic) → Repository (DB) + CacheService + SearchService
       ↓                      ↓
   DTOs (request/response)   Entity (JPA)
```

- **Paths**: Edit `ApiPaths` to add your base path (e.g. `ORDERS = BASE + "/orders"`).
- **Entity**: Add a JPA entity under `entity/`. See `Sample` for fields, `@Table`, `@Column`.
- **Repository**: Add an interface under `repository/` extending `JpaRepository<YourEntity, Long>`.
- **DTOs**: Add request/response classes under `dto/` (e.g. `OrderRequest`, `OrderResponse`).
- **Service**: Add a `@Service` under `service/` that uses your repository and optionally `CacheService` / `SearchService`.
- **Controller**: Add a `@RestController` under `controller/` with `@RequestMapping(ApiPaths.YOUR_PATH)`.

## Step-by-step: add an "Order" resource

1. **Entity**  
   Create `entity/Order.java` with JPA annotations and table name `orders`.

2. **Repository**  
   Create `repository/OrderRepository.java`:
   ```java
   public interface OrderRepository extends JpaRepository<Order, Long> { }
   ```

3. **DTOs**  
   Create `dto/OrderRequest.java` (and optionally `OrderResponse.java`) with validation annotations.

4. **ApiPaths**  
   In `api/ApiPaths.java` add:
   ```java
   public static final String ORDERS = BASE + "/orders";
   ```

5. **Service**  
   Create `service/OrderService.java`: inject `OrderRepository`, optionally `CacheService`/`SearchService`, and add methods like `findAll()`, `findById()`, `create()`.

6. **Controller**  
   Create `controller/OrderController.java`:
   - `@RequestMapping(ApiPaths.ORDERS)`
   - `GET /` → list
   - `GET /{id}` → get by id
   - `POST /` → create (body: OrderRequest)
   - Add `PUT /{id}` or `GET /search` as needed.

7. **Tests**  
   Copy `SampleControllerTest` / `SampleServiceTest`, replace Sample with Order and adjust assertions.

## Response shape

- You can return entities directly (as in the Sample API).
- For a standard envelope (success, data, message), use `ApiResponse<T>` from `api/ApiResponse.java` and return `ApiResponse.ok(data)` or `ApiResponse.error("message")`.

## What to keep vs replace

| Keep (shared) | Replace per resource |
|---------------|----------------------|
| `ApiPaths`, `ApiResponse` | Entity, Repository, DTOs |
| `CacheService`, `SearchService` | Service, Controller |
| `config/`, `HelloController` | Add new controllers and paths in `ApiPaths` |

## Event listeners (Kafka / RabbitMQ)

- Add listener classes in package `listener/` when `app.features.kafka.enabled` or `app.features.rabbitmq.enabled` is true.
- See `listener/EventListenersPlaceholder.md` and `config/KafkaConfig.java`, `config/RabbitMQConfig.java`.
