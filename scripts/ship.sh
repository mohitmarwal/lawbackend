#!/usr/bin/env bash
# Full pipeline: compile both projects, commit + push both repos, then build
# images and deploy to minikube. Reuses run-minikube.sh / deploy-to-minikube.sh
# for the build/deploy/port-forward part rather than re-implementing it.
#
# Usage: ship.sh ["commit message"] [local-port]
#   commit message defaults to a timestamped "Deploy" message if omitted.
#   port defaults to 8081.
#
# Note: committing here also fires each repo's post-commit hook (see
# install-git-hooks.sh), which independently kicks off its own background
# deploy-to-minikube.sh run. That's harmless and idempotent — every build
# gets a unique tag, so an overlapping hook-triggered run just means two
# deploys land back to back instead of one. This script's own step 3 is the
# one whose exit code/output you should trust.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
FRONTEND_REPO="/f/Git/Lawfrontend"
FRONTEND_DIR="$FRONTEND_REPO/lawreact/lawyer-ui"

MSG="${1:-Deploy $(date '+%Y-%m-%d %H:%M:%S')}"
PORT="${2:-8081}"

step() { echo ""; echo "== $1 =="; }
fail() { echo "FAILED: $1"; exit 1; }

# ---------------------------------------------------------------------------
# 1. Make sure a suitable JDK is actually on JAVA_HOME/PATH before trying to
#    compile — this script may be run in a shell that never set it. Same
#    detection logic as setup-and-deploy.sh: an already-set JAVA_HOME, then
#    known install locations, in preference order; installs Temurin 21 via
#    winget only if nothing suitable is found.
# ---------------------------------------------------------------------------
step "1/4 Checking Java"

REQUIRED_JAVA=$(sed -n 's:.*<java\.version>\([0-9]*\)</java\.version>.*:\1:p' "$BACKEND_DIR/pom.xml" | head -1)
REQUIRED_JAVA="${REQUIRED_JAVA:-17}"

java_major_of() {
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
fi
export JAVA_HOME="$JH"
export PATH="$JAVA_HOME/bin:$PATH"

# ---------------------------------------------------------------------------
# 2. Compile both projects locally — fail fast, before committing/pushing or
#    building images for something that doesn't even build.
# ---------------------------------------------------------------------------
step "2/4 Compiling backend (mvn compile)"
( cd "$BACKEND_DIR" && ./mvnw -q compile ) || fail "backend does not compile"
echo "backend OK"

step "2/4 Building frontend (npm install if needed, then npm run build)"
(
  cd "$FRONTEND_DIR"
  [ -d node_modules ] || npm install --silent
  npm run build --silent
) || fail "frontend does not build"
echo "frontend OK"

# ---------------------------------------------------------------------------
# 3. Commit + push both repos. Skips the commit (not the push) when a repo
#    has nothing staged, so re-running with no new changes still pushes any
#    already-committed-but-unpushed commits and still redeploys in step 4.
# ---------------------------------------------------------------------------
commit_and_push() {
  local repo="$1" name="$2"
  step "3/4 $name: git add + commit + push"
  (
    cd "$repo"
    git add -A
    if git diff --cached --quiet; then
      echo "$name: nothing to commit"
    else
      git commit -m "$MSG"
    fi
    git push origin main
  ) || fail "$name: git commit/push failed"
}

commit_and_push "$BACKEND_DIR" "backend"
commit_and_push "$FRONTEND_REPO" "frontend"

# ---------------------------------------------------------------------------
# 4. Build images, deploy to minikube, start it, and port-forward — reuses
#    the existing pipeline rather than re-implementing it.
# ---------------------------------------------------------------------------
step "4/4 Building images, deploying to minikube, and port-forwarding"
"$SCRIPT_DIR/run-minikube.sh" "$PORT" || fail "minikube deploy failed"

echo ""
echo "===== DONE — pushed, deployed, and running at http://localhost:$PORT ====="
