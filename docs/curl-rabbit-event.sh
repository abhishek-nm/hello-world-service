#!/usr/bin/env bash
# POST event to RabbitMQ endpoint. Service runs on port 5001 (see docker-compose.yml).

curl -X POST http://localhost:5001/api/v1/events/rabbit \
  -H "Content-Type: application/json" \
  -H "X-Client-Id: web" -H "X-Client-Version: 1.0.0" \
  -H "X-Idempotency-Key: idem-$(date +%s)" \
  -d '{"message": "test"}'
