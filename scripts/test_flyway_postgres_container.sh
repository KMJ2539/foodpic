#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="foodpic-pg-flyway-test"
DB_USER="foodpic"
DB_PASSWORD="foodpic"
DB_NAME="foodpic"
DB_PORT="55432"

cleanup() {
  docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true
}
trap cleanup EXIT

cleanup

docker run -d \
  --name "$CONTAINER_NAME" \
  -e POSTGRES_USER="$DB_USER" \
  -e POSTGRES_PASSWORD="$DB_PASSWORD" \
  -e POSTGRES_DB="$DB_NAME" \
  -p "$DB_PORT:5432" \
  postgres:16-alpine >/dev/null

for i in {1..40}; do
  if docker exec "$CONTAINER_NAME" pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1; then
    break
  fi
  sleep 1
  if [[ "$i" == "40" ]]; then
    echo "PostgreSQL did not become ready in time" >&2
    exit 1
  fi
done

# Flyway migration apply (same behavior as boot-time auto-migration)
docker run --rm \
  --network host \
  -v "$(pwd)/backend/src/main/resources/db/migration:/flyway/sql:ro" \
  flyway/flyway:10 \
  -url="jdbc:postgresql://localhost:${DB_PORT}/${DB_NAME}" \
  -user="$DB_USER" \
  -password="$DB_PASSWORD" \
  -connectRetries=10 \
  migrate

TABLE_COUNT=$(docker exec "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" -tAc \
  "SELECT count(*) FROM information_schema.tables WHERE table_schema='public' AND table_name IN ('users','follows','meal_entries','meal_entry_items','food_items','photo_assets','feed_posts','post_likes','post_comments','blocks','reports');")

if [[ "$TABLE_COUNT" != "11" ]]; then
  echo "Expected 11 required tables, got: $TABLE_COUNT" >&2
  exit 1
fi

INDEX_EXISTS=$(docker exec "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" -tAc \
  "SELECT count(*) FROM pg_indexes WHERE schemaname='public' AND tablename='feed_posts' AND indexname='idx_feed_posts_created_at_id_desc';")

if [[ "$INDEX_EXISTS" != "1" ]]; then
  echo "Feed cursor index not found" >&2
  exit 1
fi

echo "Migration validation passed"
