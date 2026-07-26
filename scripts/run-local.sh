#!/usr/bin/env bash
# Starts the Spring Boot backend and the Vite/React frontend for local dev.
# Requires local MySQL already running on the port configured in
# lawbackend/src/main/resources/application.properties (currently 3306).
set -euo pipefail

BACKEND_DIR="/f/Git/lawbackend"
FRONTEND_DIR="/f/Git/Lawfrontend/lawreact/lawyer-ui"
MVN="$USERPROFILE/.m2/wrapper/dists/apache-maven-3.9.16/56ba1f9f/bin/mvn.cmd"
JAVA_HOME_WIN="C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2025.2.6.2\\jbr"

free_port() {
  local port="$1"
  local pid
  pid=$(netstat -ano | grep ":$port " | grep LISTENING | awk '{print $5}' | head -n1)
  if [ -n "${pid:-}" ]; then
    echo "port $port in use by PID $pid, stopping it"
    powershell -NoProfile -Command "Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue"
    sleep 1
  fi
}

echo "== starting backend (mvn spring-boot:run) on :8080 =="
free_port 8080
JAVA_HOME="$JAVA_HOME_WIN" \
  powershell -NoProfile -Command "\$env:JAVA_HOME='$JAVA_HOME_WIN'; Set-Location '$BACKEND_DIR'; Start-Process -FilePath '$MVN' -ArgumentList 'spring-boot:run' -RedirectStandardOutput '$BACKEND_DIR/backend-dev.log' -RedirectStandardError '$BACKEND_DIR/backend-dev-err.log' -WindowStyle Hidden"
echo "backend log: $BACKEND_DIR/backend-dev.log"

echo "== starting frontend (npm run dev) on :5173 =="
free_port 5173
powershell -NoProfile -Command "Set-Location '$FRONTEND_DIR'; Start-Process -FilePath 'npm' -ArgumentList 'run','dev' -RedirectStandardOutput '$FRONTEND_DIR/frontend-dev.log' -RedirectStandardError '$FRONTEND_DIR/frontend-dev-err.log' -WindowStyle Hidden"
echo "frontend log: $FRONTEND_DIR/frontend-dev.log"

echo "done. tail the logs above to watch startup, or check http://localhost:8080 and http://localhost:5173"
