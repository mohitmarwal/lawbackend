#!/usr/bin/env bash
# Deploys the full stack to minikube (mysql, backend, frontend as pods) and
# exposes the frontend at http://localhost:8081 via kubectl port-forward.
#
# First run creates k8s secrets if they don't exist yet. Safe to re-run —
# every step is idempotent (delegates the build/deploy to deploy-to-minikube.sh).
set -euo pipefail

REPO_ROOT="/f/Git"
SCRIPTS_DIR="$REPO_ROOT/lawbackend/scripts"
PORT="${1:-8081}"

export PATH="$PATH:/c/Program Files/Kubernetes/Minikube"

if ! minikube status >/dev/null 2>&1; then
  echo "== minikube not running, starting it =="
  minikube start --driver=docker
fi

if ! kubectl -n matterly get secret mysql-secret backend-secret >/dev/null 2>&1; then
  echo "== k8s secrets missing, creating them =="
  "$SCRIPTS_DIR/create-k8s-secrets.sh"
fi

echo "== deploying backend + frontend to minikube (see scripts/deploy.log for details) =="
"$SCRIPTS_DIR/deploy-to-minikube.sh" both

echo "== waiting for pods to be ready =="
# deploy-to-minikube.sh already waited on rollout status before returning, so
# this is just a final confirmation — don't fail the whole run over it if a
# pod takes a few extra seconds past the timeout to report ready.
kubectl -n matterly wait --for=condition=ready pod -l app=frontend --timeout=180s \
  || echo "warning: frontend not yet reporting ready, continuing anyway (check 'kubectl -n matterly get pods')"
kubectl -n matterly wait --for=condition=ready pod -l app=backend --timeout=180s \
  || echo "warning: backend not yet reporting ready, continuing anyway (check 'kubectl -n matterly get pods')"

free_port() {
  local port="$1"
  local pid
  pid=$(netstat -ano | grep ":$port " | grep LISTENING | awk '{print $5}' | head -n1 || true)
  if [ -n "${pid:-}" ]; then
    echo "port $port in use by PID $pid, stopping it"
    powershell -NoProfile -Command "Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue"
    sleep 1
  fi
}

echo "== exposing frontend on localhost:$PORT =="
free_port "$PORT"
powershell -NoProfile -Command "Start-Process -FilePath 'kubectl' -ArgumentList '-n','matterly','port-forward','svc/frontend','$PORT:80' -RedirectStandardOutput '$SCRIPTS_DIR/port-forward.log' -RedirectStandardError '$SCRIPTS_DIR/port-forward-err.log' -WindowStyle Hidden"
sleep 2

echo "done. app: http://localhost:$PORT"
echo "(port-forward runs in the background; log at scripts/port-forward.log)"
