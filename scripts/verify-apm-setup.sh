#!/usr/bin/env bash
# Run this to verify why APM might not show in Kibana.
# Usage: ./scripts/verify-apm-setup.sh

set -e

echo "=== 1. Observability stack containers ==="
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "observability|NAMES" || true
if ! docker ps --format "{{.Names}}" | grep -q observability-apm-server; then
  echo "FAIL: observability stack not running. From shared-infra-observability repo run: ./run-observability.sh start"
  exit 1
fi

echo ""
echo "=== 2. APM Server reachable (host) ==="
if curl -s -o /dev/null -w "%{http_code}" --connect-timeout 2 http://localhost:8200/ 2>/dev/null | grep -qE "200|401"; then
  echo "OK: APM Server at http://localhost:8200 responds"
else
  echo "FAIL: Cannot reach http://localhost:8200 (is observability stack up?)"
fi

echo ""
echo "=== 3. Elasticsearch indices (APM / traces) ==="
ALL_INDICES=$(curl -s "http://localhost:9200/_cat/indices?v" 2>/dev/null || true)
if echo "$ALL_INDICES" | grep -qE "apm|traces|metrics"; then
  echo "OK: APM-related indices exist:"
  echo "$ALL_INDICES" | grep -E "apm|traces|metrics|health"
else
  echo "No apm/traces indices yet."
  echo "All indices:"
  echo "$ALL_INDICES" || echo "(could not reach Elasticsearch)"
  echo ""
  echo "If agent reached APM Server but no indices: check APM Server logs:"
  echo "  docker logs observability-apm-server 2>&1 | tail -50"
fi

echo ""
echo "=== 4. What you should do ==="
echo "  - Start app with: export ELASTIC_APM_SERVER_URL=http://localhost:8200  (then start app)"
echo "  - Or in Docker: -e ELASTIC_APM_SERVER_URL=http://host.docker.internal:8200"
echo "  - Generate traffic: curl http://localhost:9100/api/v1/events"
echo "  - In Kibana APM (http://localhost:5601/app/apm): set time range to 'Last 24 hours', refresh"
echo "  - Check app startup logs for: [APM] Agent attached; server=..."
