#!/usr/bin/env bash
# One master entrypoint: make sure this machine can build the project, then
# build it and put it on minikube. Doesn't duplicate the existing pipeline —
# it delegates to create-k8s-secrets.sh / deploy-to-minikube.sh / run-minikube.sh
# for everything after the local compile check.
#
#   1. Java  — find a JDK meeting pom.xml's <java.version>, install Temurin
#              via winget if nothing suitable is found.
#   2. Node/npm — same idea, installs OpenJS.NodeJS via winget if missing.
#   3. Compile both projects locally (fast fail before spending time on
#      docker build).
#   4. Hand off to run-minikube.sh: builds both images, starts minikube if
#      needed, creates k8s secrets on first run, applies manifests, waits
#      for pods, and port-forwards the frontend.
#
# Usage: setup-and-deploy.sh [local-port]   (default port 8081)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
FRONTEND_DIR="/f/Git/Lawfrontend/lawreact/lawyer-ui"
PORT="${1:-8081}"

step() { echo ""; echo "== $1 =="; }
fail() { echo "FAILED: $1"; exit 1; }

# ---------------------------------------------------------------------------
# 1. Java — needs to meet pom.xml's <java.version>, whatever that is set to.
# ---------------------------------------------------------------------------
step "1/4 Checking Java"

REQUIRED_JAVA=$(sed -n 's:.*<java\.version>\([0-9]*\)</java\.version>.*:\1:p' "$BACKEND_DIR/pom.xml" | head -1)
REQUIRED_JAVA="${REQUIRED_JAVA:-17}"
echo "project requires Java >= $REQUIRED_JAVA (from pom.xml)"

java_major_of() {
  # $1 = path to a JDK's bin/java.exe
  local ver
  ver=$("$1" -version 2>&1 | head -1 | grep -oE '"[0-9]+(\.[0-9]+)*"' | tr -d '"')
  echo "${ver%%.*}"
}

find_java_home() {
  local candidates=(
    "${JAVA_HOME:-}"
    "/c/Program Files/Eclipse Adoptium"/jdk-*
    "/c/Program Files/JetBrains/IntelliJ IDEA Community Edition"*/jbr
    "/c/Program Files/Java"/jdk-*
  )
  local c major
  for c in "${candidates[@]}"; do
    [ -n "$c" ] || continue
    [ -x "$c/bin/java.exe" ] || continue
    major=$(java_major_of "$c/bin/java.exe") || continue
    if [ -n "$major" ] && [ "$major" -ge "$REQUIRED_JAVA" ] 2>/dev/null; then
      echo "$c"
      return 0
    fi
  done
  return 1
}

if JH=$(find_java_home); then
  echo "using existing JDK at: $JH"
else
  echo "no suitable JDK found — installing Eclipse Temurin 21 via winget"
  winget install EclipseAdoptium.Temurin.21.JDK --accept-package-agreements --accept-source-agreements \
    || fail "winget install EclipseAdoptium.Temurin.21.JDK"
  JH=$(find_java_home) || fail "still no usable JDK found after installing Temurin 21"
  echo "installed, using JDK at: $JH"
fi
export JAVA_HOME="$JH"
export PATH="$JAVA_HOME/bin:$PATH"

# ---------------------------------------------------------------------------
# 2. Node / npm
# ---------------------------------------------------------------------------
step "2/4 Checking Node.js / npm"

if command -v node >/dev/null 2>&1 && command -v npm >/dev/null 2>&1; then
  echo "found: node $(node -v), npm $(npm -v)"
else
  echo "node/npm not found — installing via winget"
  winget install OpenJS.NodeJS.LTS --accept-package-agreements --accept-source-agreements \
    || fail "winget install OpenJS.NodeJS.LTS"
  export PATH="/c/Program Files/nodejs:$PATH"
  command -v node >/dev/null 2>&1 || fail "node still not found after install"
  echo "installed: node $(node -v), npm $(npm -v)"
fi

# ---------------------------------------------------------------------------
# 3. Compile both projects locally — fails fast, before docker build.
# ---------------------------------------------------------------------------
step "3/4 Compiling backend (mvn compile)"
( cd "$BACKEND_DIR" && ./mvnw -q compile ) || fail "backend does not compile"
echo "backend OK"

step "3/4 Building frontend (npm install if needed, then npm run build)"
(
  cd "$FRONTEND_DIR"
  if [ ! -d node_modules ]; then
    echo "node_modules missing — installing dependencies"
    npm install --silent
  fi
  npm run build --silent
) || fail "frontend does not build"
echo "frontend OK"

# ---------------------------------------------------------------------------
# 4. Build images + deploy to minikube + expose it — reuses the existing
#    pipeline rather than re-implementing it.
# ---------------------------------------------------------------------------
step "4/4 Building images and deploying to minikube"
"$SCRIPT_DIR/run-minikube.sh" "$PORT" || fail "minikube deploy failed"

echo ""
echo "===== DONE — app running at http://localhost:$PORT ====="
