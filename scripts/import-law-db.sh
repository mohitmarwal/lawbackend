#!/usr/bin/env bash
# Imports law_db.sql into the mysql pod running on minikube.
#
# Streams the dump straight into `mysql` inside the pod over `kubectl exec -i`
# stdin — no `kubectl cp` / temp file needed inside the pod.
#
# Usage: scripts/import-law-db.sh [path-to-sql-file]
#   defaults to lawbackend/law_db.sql
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SQL_FILE="${1:-$BACKEND_DIR/law_db.sql}"
NAMESPACE=matterly
DB=law_db

export PATH="$PATH:/c/Program Files/Kubernetes/Minikube"

[ -f "$SQL_FILE" ] || { echo "FAILED: sql file not found: $SQL_FILE"; exit 1; }

echo "== waiting for mysql pod to be ready =="
kubectl -n "$NAMESPACE" wait --for=condition=ready pod -l app=mysql --timeout=120s \
  || { echo "FAILED: no ready mysql pod in namespace $NAMESPACE — is it deployed? (scripts/run-minikube.sh)"; exit 1; }

POD=$(kubectl -n "$NAMESPACE" get pod -l app=mysql -o jsonpath='{.items[0].metadata.name}')
ROOT_PW=$(kubectl -n "$NAMESPACE" get secret mysql-secret -o jsonpath='{.data.root-password}' | base64 -d)

echo "== recreating $DB (dump has no DROP TABLE / IF NOT EXISTS, so it must start from empty) =="
kubectl -n "$NAMESPACE" exec "$POD" -- env MYSQL_PWD="$ROOT_PW" mysql -uroot -e \
  "DROP DATABASE IF EXISTS $DB; CREATE DATABASE $DB;"

echo "== importing $SQL_FILE into $DB on pod $POD =="
kubectl -n "$NAMESPACE" exec -i "$POD" -- env MYSQL_PWD="$ROOT_PW" mysql -uroot "$DB" < "$SQL_FILE"

TABLE_COUNT=$(kubectl -n "$NAMESPACE" exec "$POD" -- env MYSQL_PWD="$ROOT_PW" mysql -uroot -N -e \
  "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB';")

echo "done. $DB now has $TABLE_COUNT tables."
