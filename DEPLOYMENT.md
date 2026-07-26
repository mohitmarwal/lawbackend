# Deployment

Two things were built, kept in sync with each other so nothing changes
between them except *how* they're run:

- `docker-compose.yml` — for running everything on one machine (dev/staging,
  or evaluating the stack before it goes to a cluster).
- `k8s/` — Kubernetes manifests for running the same three services
  (MySQL, backend, frontend) on an actual cluster.

**Important:** a `docker-compose.yml` file cannot itself be applied to a
Kubernetes cluster — Compose and Kubernetes are different formats, and
`kubectl apply -f docker-compose.yml` doesn't do anything meaningful. What
makes this "the same setup" across both is that the two Dockerfiles, the
service names (`mysql`, `backend`, `frontend`), and the environment
variables are identical either way — only the orchestration layer differs.

## Option A: Docker Compose

```bash
cp .env.example .env
# edit .env — at minimum set MYSQL_ROOT_PASSWORD and JWT_SECRET

docker compose up --build -d
docker compose ps        # wait until all three show healthy
```

App is then at `http://localhost:8081` (or whatever `FRONTEND_PORT` you set).

To stop: `docker compose down` (add `-v` to also delete the MySQL volume).

## Option B: Kubernetes

### 1. Build and publish the images

The manifests reference `matterly-backend:latest` and
`matterly-frontend:latest`. Build them and get them somewhere your cluster
can pull from:

```bash
# run from inside lawbackend/
docker build -t matterly-backend:latest .
docker build -t matterly-frontend:latest \
  --build-arg VITE_API_BASE_URL= \
  ../Lawfrontend/lawreact/lawyer-ui
```

- **Cloud cluster (EKS/GKE/AKS/etc.):** tag and push both to your registry
  (ECR/GCR/ACR/Docker Hub), then update the `image:` field in
  `k8s/22-backend-deployment.yaml` and `k8s/30-frontend-deployment.yaml` to
  the pushed tag.
- **Local cluster (kind/minikube/k3d):** load the images directly instead of
  pushing anywhere, e.g. `kind load docker-image matterly-backend:latest`
  (or `minikube image load ...`).

### 2. Set real secrets

`k8s/10-mysql-secret.yaml` and `k8s/20-backend-secret.yaml` ship with
placeholder values — replace them before applying (the comments in each
file show the `kubectl create secret ... --dry-run=client -o yaml` one-liner
to generate them properly instead of hand-editing).

### 3. Apply everything

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/10-mysql-secret.yaml -f k8s/11-mysql-pvc.yaml \
  -f k8s/12-mysql-deployment.yaml -f k8s/13-mysql-service.yaml
kubectl apply -f k8s/20-backend-secret.yaml -f k8s/21-backend-configmap.yaml \
  -f k8s/22-backend-deployment.yaml -f k8s/23-backend-service.yaml
kubectl apply -f k8s/30-frontend-deployment.yaml -f k8s/31-frontend-service.yaml

kubectl -n matterly get pods -w   # wait for everything to go Running
kubectl -n matterly get svc frontend   # get the external IP/port
```

If your cluster has no LoadBalancer implementation (common on bare local
clusters), either change `31-frontend-service.yaml`'s type to `NodePort`, or
use the Ingress template at `k8s/32-ingress.yaml.example` (rename to drop
`.example`, set a real host, and switch the frontend Service back to
`ClusterIP` — see the comments in that file).

## What's already wired up for this

- **Frontend → backend, without hardcoding a hostname.** The frontend image
  is built with `VITE_API_BASE_URL=""`, so all API calls go to the same
  origin the page was served from (`/api/...`, `/auth/...`). nginx inside
  the frontend container reverse-proxies those two paths to
  `http://backend:8080` — a name that resolves via Docker Compose's network
  and via Kubernetes Service DNS identically, which is why `nginx.conf`
  doesn't need to change between the two.
- **Health probes.** Added Spring Boot Actuator with `/actuator/health/readiness`
  and `/actuator/health/liveness` (split so "process is up" and "DB pool is
  actually ready" are distinguished) — used by both the Compose healthcheck
  and the Kubernetes probes.
- **Config externalization.** Datasource creds, JWT secret, and the
  notification (SMTP/Twilio) settings were already designed to read from
  environment variables with local-dev fallbacks — Compose and the k8s
  ConfigMap/Secret just supply those same variable names.

## What you still need to decide

- **Notifications.** `NOTIFICATIONS_ENABLED` defaults to `false` everywhere
  above. Flip it to `true` and fill in real SMTP/Twilio credentials in
  `.env` (Compose) or `k8s/20-backend-secret.yaml` (k8s) once you have them.
- **TLS/HTTPS.** Neither the Compose setup nor these manifests terminate
  TLS. For Kubernetes, that's normally the Ingress controller's job (cert-manager
  + the Ingress in `32-ingress.yaml.example` is the standard path). Not
  included here since it depends on your cluster's ingress controller and
  domain.
- **Database backups.** The PVC persists data across pod restarts, but
  nothing here schedules backups — that's cluster/provider-specific.
