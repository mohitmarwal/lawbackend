#!/usr/bin/env bash
# Provisions a brand-new tenant (law firm) onto the already-running backend/
# frontend pods - schema-per-tenant multi-tenancy, live, no restart. Once
# this script finishes, TenantRoutingDataSource (the app's routing
# DataSource) picks the new tenant up on its very next request: it's a
# lazily-populated cache keyed by slug, not a fixed map built at startup.
#
# Usage: scripts/provision-tenant.sh <slug> "<Firm Name>" [path-to-logo-file]
#   slug            lowercase letters/digits/hyphens only - becomes the
#                   subdomain (<slug>.matterly.in) and the schema name
#                   (tenant_<slug>).
#   Firm Name       shown in place of "Matterly" in the new tenant's UI.
#   logo file       optional; can also be uploaded later via
#                   PUT /api/branding (see TenantBrandingController).
#
# What it does:
#   1. Clones law_db's table structure (no data) into a new tenant_<slug>
#      schema on the same MySQL pod - law_db doubles as the template schema.
#   2. Inserts the slug -> schema mapping into tenant_registry.tenant.
#   3. Seeds one working admin login (admin@login.com / admin, same as
#      DataSeeder's default) and the firm's branding row into the new schema.
#
# Existing create-k8s-secrets.sh / deploy-to-minikube.sh need no changes -
# they deploy code, which is identical across every tenant; this script is a
# purely data-side, one-time-per-tenant operation.
set -euo pipefail

SLUG="${1:-}"
FIRM_NAME="${2:-}"
LOGO_FILE="${3:-}"
NAMESPACE=matterly
TEMPLATE_DB=law_db
REGISTRY_DB=tenant_registry

export PATH="$PATH:/c/Program Files/Kubernetes/Minikube"

fail() { echo "FAILED: $1" >&2; exit 1; }

[ -n "$SLUG" ] && [ -n "$FIRM_NAME" ] || fail "usage: provision-tenant.sh <slug> \"<Firm Name>\" [logo-file]"
[[ "$SLUG" =~ ^[a-z0-9]([a-z0-9-]*[a-z0-9])?$ ]] || fail "slug must be lowercase letters/digits/hyphens only, got: $SLUG"
[ "$SLUG" != "default" ] || fail "slug 'default' is reserved for the pre-existing $TEMPLATE_DB tenant"
if [ -n "$LOGO_FILE" ]; then
  [ -f "$LOGO_FILE" ] || fail "logo file not found: $LOGO_FILE"
fi

TENANT_DB="tenant_${SLUG//-/_}"

echo "== waiting for mysql pod to be ready =="
kubectl -n "$NAMESPACE" wait --for=condition=ready pod -l app=mysql --timeout=120s \
  || fail "no ready mysql pod in namespace $NAMESPACE"

POD=$(kubectl -n "$NAMESPACE" get pod -l app=mysql -o jsonpath='{.items[0].metadata.name}')
ROOT_PW=$(kubectl -n "$NAMESPACE" get secret mysql-secret -o jsonpath='{.data.root-password}' | base64 -d)

mysql_exec() {
  # Runs a SQL statement/stream (via stdin) against a given database.
  # Pass "" as db to run without selecting one (e.g. CREATE DATABASE).
  local db="$1"; shift || true
  if [ -n "$db" ]; then
    kubectl -n "$NAMESPACE" exec -i "$POD" -- env MYSQL_PWD="$ROOT_PW" mysql -uroot "$@" "$db"
  else
    kubectl -n "$NAMESPACE" exec -i "$POD" -- env MYSQL_PWD="$ROOT_PW" mysql -uroot "$@"
  fi
}

echo "== checking slug '$SLUG' isn't already provisioned =="
EXISTING=$(echo "SELECT COUNT(*) FROM tenant WHERE slug='$SLUG';" | mysql_exec "$REGISTRY_DB" -N 2>/dev/null || echo "0")
[ "$EXISTING" = "0" ] || fail "slug '$SLUG' is already provisioned (see tenant_registry.tenant)"

echo "== creating schema $TENANT_DB and cloning table structure from $TEMPLATE_DB (no data) =="
echo "CREATE DATABASE IF NOT EXISTS $TENANT_DB;" | mysql_exec ""
kubectl -n "$NAMESPACE" exec "$POD" -- env MYSQL_PWD="$ROOT_PW" mysqldump --no-data -uroot "$TEMPLATE_DB" \
  | kubectl -n "$NAMESPACE" exec -i "$POD" -- env MYSQL_PWD="$ROOT_PW" mysql -uroot "$TENANT_DB" \
  || fail "cloning table structure"

echo "== registering slug '$SLUG' -> $TENANT_DB in $REGISTRY_DB =="
echo "INSERT INTO tenant (id, slug, schema_name, created_at) VALUES (UUID(), '$SLUG', '$TENANT_DB', NOW());" \
  | mysql_exec "$REGISTRY_DB" \
  || fail "inserting registry row"

echo "== seeding default admin login (admin@login.com / admin) =="
echo "INSERT INTO users (id, name, surname, email, password, role, enabled) VALUES (UUID(), 'Admin', '', 'admin@login.com', 'admin', 'admin', 1);" \
  | mysql_exec "$TENANT_DB" \
  || fail "seeding admin user"

echo "== seeding branding (firm name: $FIRM_NAME) =="
if [ -n "$LOGO_FILE" ]; then
  LOGO_HEX=$(od -An -v -tx1 "$LOGO_FILE" | tr -d ' \n')
  LOGO_CONTENT_TYPE="image/$(echo "${LOGO_FILE##*.}" | tr '[:upper:]' '[:lower:]')"
  [ "$LOGO_CONTENT_TYPE" = "image/jpg" ] && LOGO_CONTENT_TYPE="image/jpeg"
  echo "INSERT INTO tenant_branding (id, firm_name, logo_content_type, logo_data, updated_at) VALUES (UUID(), '$FIRM_NAME', '$LOGO_CONTENT_TYPE', UNHEX('$LOGO_HEX'), NOW());" \
    | mysql_exec "$TENANT_DB" \
    || fail "seeding branding with logo"
else
  echo "INSERT INTO tenant_branding (id, firm_name, updated_at) VALUES (UUID(), '$FIRM_NAME', NOW());" \
    | mysql_exec "$TENANT_DB" \
    || fail "seeding branding"
fi

cat <<EOF

== done - no restart needed, this tenant is live immediately ==
  Slug:        $SLUG
  Schema:      $TENANT_DB
  Firm name:   $FIRM_NAME
  Admin login: admin@login.com / admin (change this immediately)
  URL:         https://$SLUG.matterly.in  (or http://$SLUG.matterly.local for a local hosts-file entry)

For local testing without real DNS, add a hosts-file entry pointing
"$SLUG.matterly.local" (or whatever local base domain you're using) at the
ingress/minikube IP, matching app.tenant.base-domain / TENANT_BASE_DOMAIN.
EOF
