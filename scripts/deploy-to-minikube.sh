#!/usr/bin/env bash
# CI/CD-style pipeline: ensure minikube exists and is running, rebuild the
# image(s) for whichever service changed, load them into minikube, and
# roll the deployment. Safe to re-run — every step is idempotent.
#
# Usage: deploy-to-minikube.sh [backend|frontend|both]
set -uo pipefail

REPO_ROOT="/f/Git"
K8S_DIR="$REPO_ROOT/lawbackend/k8s"
SERVICE="${1:-both}"
LOG="$REPO_ROOT/lawbackend/scripts/deploy.log"

export PATH="$PATH:/c/Program Files/Kubernetes/Minikube"

exec >>"$LOG" 2>&1
echo ""
echo "===== deploy pipeline started $(date) — target=$SERVICE ====="

fail() { echo "FAILED: $1"; exit 1; }

# 1. minikube CLI present?
if ! command -v minikube >/dev/null 2>&1; then
  echo "-- minikube not found, installing via winget"
  winget install Kubernetes.minikube --accept-package-agreements --accept-source-agreements \
    || fail "winget install Kubernetes.minikube"
fi

# 2. cluster running?
if ! minikube status >/dev/null 2>&1; then
  echo "-- minikube not running, starting it"
  minikube start --driver=docker || fail "minikube start"
fi

# 3. namespace + secrets + mysql exist? (no-ops if already applied)
kubectl apply -f "$K8S_DIR/00-namespace.yaml" || fail "apply namespace"

for s in mysql-secret backend-secret; do
  if ! kubectl -n matterly get secret "$s" >/dev/null 2>&1; then
    echo "-- WARNING: secret '$s' does not exist in the cluster."
    echo "   Run scripts/create-k8s-secrets.sh once before relying on this pipeline."
  fi
done

kubectl apply -f "$K8S_DIR/11-mysql-pvc.yaml" \
              -f "$K8S_DIR/12-mysql-deployment.yaml" \
              -f "$K8S_DIR/13-mysql-service.yaml" || fail "apply mysql"

# minikube's `image load` silently no-ops when a tag it already has cached
# gets rebuilt with different content underneath it — confirmed live: after
# rebuilding and reloading matterly-backend:latest several times, the pod
# kept running a build from hours earlier because minikube never actually
# re-imported it. Removing the old tag first only works if nothing is still
# running on it, which fights an in-progress rollout. The reliable fix: tag
# every build uniquely, load THAT tag, and point the Deployment at it via
# `kubectl set image` — there's never a stale name to accidentally reuse.
# (Trade-off: old tagged images accumulate in minikube's cache over time;
# fine for local dev, prune with `minikube image ls` / `image rm` if it grows.)

deploy_backend() {
  local tag="build-$(date +%Y%m%d%H%M%S)"
  echo "-- building backend image (tag: $tag)"
  docker build -t "matterly-backend:$tag" -t matterly-backend:latest "$REPO_ROOT/lawbackend" || fail "docker build backend"
  echo "-- loading into minikube"
  minikube image load "matterly-backend:$tag" || fail "minikube image load backend"
  kubectl apply -f "$K8S_DIR/21-backend-configmap.yaml" \
                -f "$K8S_DIR/22-backend-deployment.yaml" \
                -f "$K8S_DIR/23-backend-service.yaml" || fail "apply backend manifests"
  echo "-- pointing backend deployment at $tag"
  kubectl -n matterly set image deployment/backend backend="matterly-backend:$tag" || fail "set image backend"
  kubectl -n matterly rollout status deployment/backend --timeout=180s || fail "rollout status backend"
}

deploy_frontend() {
  local tag="build-$(date +%Y%m%d%H%M%S)"
  echo "-- building frontend image (tag: $tag)"
  docker build -t "matterly-frontend:$tag" -t matterly-frontend:latest --build-arg VITE_API_BASE_URL= \
    "$REPO_ROOT/Lawfrontend/lawreact/lawyer-ui" || fail "docker build frontend"
  echo "-- loading into minikube"
  minikube image load "matterly-frontend:$tag" || fail "minikube image load frontend"
  kubectl apply -f "$K8S_DIR/30-frontend-deployment.yaml" \
                -f "$K8S_DIR/31-frontend-service.yaml" || fail "apply frontend manifests"
  echo "-- pointing frontend deployment at $tag"
  kubectl -n matterly set image deployment/frontend frontend="matterly-frontend:$tag" || fail "set image frontend"
  kubectl -n matterly rollout status deployment/frontend --timeout=120s || fail "rollout status frontend"
}

case "$SERVICE" in
  backend)  deploy_backend ;;
  frontend) deploy_frontend ;;
  both)     deploy_backend; deploy_frontend ;;
  *) fail "unknown target '$SERVICE' (expected backend|frontend|both)" ;;
esac

echo "===== deploy pipeline finished $(date) — SUCCESS ====="
