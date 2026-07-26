#!/usr/bin/env bash
# Installs post-commit hooks into both repos so that every commit kicks off
# the minikube deploy pipeline in the background (like a local CI/CD).
# .git/hooks is never version-controlled, so re-run this after a fresh
# clone or if the hooks folder gets wiped.
set -euo pipefail

install_hook() {
  local repo="$1" service="$2"
  local hook="$repo/.git/hooks/post-commit"
  cat > "$hook" <<EOF
#!/bin/sh
# Installed by scripts/install-git-hooks.sh — do not edit directly.
nohup /f/Git/lawbackend/scripts/deploy-to-minikube.sh $service >/dev/null 2>&1 &
disown
echo "[post-commit] deploy pipeline started in background (tail /f/Git/lawbackend/scripts/deploy.log to watch)"
EOF
  chmod +x "$hook"
  echo "installed: $hook -> deploy-to-minikube.sh $service"
}

install_hook /f/Git/lawbackend backend
install_hook "/f/Git/Lawfrontend" frontend

chmod +x /f/Git/lawbackend/scripts/deploy-to-minikube.sh /f/Git/lawbackend/scripts/create-k8s-secrets.sh
echo "done."
