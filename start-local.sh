#!/usr/bin/env bash
# Start shared-infra-observability (infra + observability) then build and run this service.
# Prereq: Clone both repos. Default: shared-infra-observability is a sibling directory.
# Override: export SHARED_INFRA_OBSERVABILITY_REPO=/path/to/shared-infra-observability
#
# Config (edit these or export before running):
#   HOST_PORT       - Host port for the app (default 9100)
#   CONTAINER_PORT  - Container port the app listens on (default 5000)
#   IMAGE_NAME      - Docker image name and APM service name (default hello-world-service)
#
# Example: HOST_PORT=9100, CONTAINER_PORT=5000, IMAGE_NAME=hello-world-service-image
#   → App URL: http://localhost:9100
#   → curl -X POST http://localhost:9100/api/v1/events/rabbit \
#        -H "Content-Type: application/json" \
#        -H "X-Client-Id: web" -H "X-Client-Version: 1.0.0" \
#        -H "X-Idempotency-Key: idem-$(date +%s)" \
#        -d '{"message": "test"}'
#
# Usage: ./start-local.sh

set -e

# --- Config: port numbers and service (image) name ---
HOST_PORT="${HOST_PORT:-9100}"
CONTAINER_PORT="${CONTAINER_PORT:-5000}"
IMAGE_NAME="${IMAGE_NAME:-hello-world-service}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_REPO="$SCRIPT_DIR"
INFRA_REPO="${SHARED_INFRA_OBSERVABILITY_REPO:-$SCRIPT_DIR/../shared-infra-observability}"

echo "[start-local] Using app repo:    $APP_REPO"
echo "[start-local] Using infra repo:  $INFRA_REPO"
echo "[start-local] Host port: $HOST_PORT  Container port: $CONTAINER_PORT  Image: $IMAGE_NAME"
echo "[start-local] App URL: http://localhost:$HOST_PORT"
echo ""

if [[ ! -d "$INFRA_REPO" ]]; then
  echo "ERROR: shared-infra-observability not found at: $INFRA_REPO"
  echo "Clone it or set: export SHARED_INFRA_OBSERVABILITY_REPO=/path/to/shared-infra-observability"
  exit 1
fi

echo "[start-local] 1. Creating Docker networks (ignore 'already exists' if any)..."
docker network create infra         2>/dev/null || true
docker network create observability 2>/dev/null || true

echo "[start-local] 2. Starting shared-infra-observability (infra + observability)..."
cd "$INFRA_REPO"
docker compose -f docker-compose.infra.yml up -d
docker compose -f docker-compose.observability.yml up -d
cd "$APP_REPO"

echo "[start-local] Waiting ~30s for Postgres/Redis/RabbitMQ/Kafka to be ready..."
sleep 30

echo "[start-local] 3. Building and running $IMAGE_NAME..."
docker build -t "$IMAGE_NAME" .
docker run --rm -p "${HOST_PORT}:${CONTAINER_PORT}" --network infra \
  --add-host=host.docker.internal:host-gateway \
  -e SPRING_PROFILES_ACTIVE=docker,dev \
  -e ELASTIC_APM_SERVER_URL=http://host.docker.internal:8200 \
  -e ELASTIC_APM_SERVICE_NAME="$IMAGE_NAME" \
  -e APP_FEATURES_APM_ENABLED=true \
  "$IMAGE_NAME"
