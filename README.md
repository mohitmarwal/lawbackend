# Matterly — Deployment Guide

Legal case management app: React (Vite) frontend, Spring Boot backend, MySQL
database. This README covers how to run the whole stack, and — the part
that needs care — how credentials (DB password, JWT signing key, SMTP,
WhatsApp/Twilio) are supplied without ever being baked into an image or
committed to git.

Two ways to run it, kept in sync (same Dockerfiles, same service names,
same env var names):

- **Docker Compose** — one machine, values come from a local `.env` file.
- **Kubernetes** (`k8s/`) — a real cluster, values come from `Secret` and
  `ConfigMap` objects instead of a file.

Only the *plumbing* differs. The application code reads the same
environment variables either way.

---

## Can email/WhatsApp/DB credentials be passed as Kubernetes Secrets?

Yes — and that's exactly how the manifests here are already built, not a
change you need to make:

- [k8s/10-mysql-secret.yaml](k8s/10-mysql-secret.yaml) → MySQL root password
- [k8s/20-backend-secret.yaml](k8s/20-backend-secret.yaml) → JWT signing
  key, SMTP username/password, Twilio account SID / auth token / WhatsApp
  sender number
- [k8s/21-backend-configmap.yaml](k8s/21-backend-configmap.yaml) → the
  **non-secret** settings (SMTP host/port, JDBC URL, DB username, the
  `NOTIFICATIONS_ENABLED` flag)

The backend Deployment ([k8s/22-backend-deployment.yaml](k8s/22-backend-deployment.yaml))
pulls the ConfigMap in wholesale via `envFrom`, then maps each Secret key to
a specific env var via `secretKeyRef`:

```yaml
env:
  - name: SMTP_PASSWORD
    valueFrom:
      secretKeyRef:
        name: backend-secret
        key: smtp-password
```

Spring Boot never sees a difference between this and a plain env var — it's
just `${SMTP_PASSWORD}` in `application.properties`. Kubernetes mounts
Secret values as env vars at container start, they're base64-encoded at
rest in etcd (encrypted at rest too, if your cluster has that enabled), and
they never appear in the Deployment YAML, image, or `kubectl describe` output
in plaintext.

**What this does *not* do on its own:** protect secrets from anyone with
`get secrets` RBAC permission in the `matterly` namespace, or replace a
proper secrets manager. If you want rotation, audit trails, or integration
with a vault, treat these `Secret` objects as the injection point and back
them with something like Sealed Secrets, External Secrets Operator, or your
cloud provider's secret manager (AWS Secrets Manager / GCP Secret Manager /
Azure Key Vault) instead of hand-editing YAML. That's an upgrade you can
make later without touching the Deployment manifests — only how the
`Secret` object gets populated changes.

---

## Option A — Docker Compose

**1. Configure secrets**

```bash
cp .env.example .env
```

Edit `.env` and set, at minimum:

```
MYSQL_ROOT_PASSWORD=<a real password>
JWT_SECRET=<a long random string>
```

To turn on real email/WhatsApp sending, also fill in and flip:

```
NOTIFICATIONS_ENABLED=true
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=you@example.com
SMTP_PASSWORD=<app password, not your login password>
TWILIO_ACCOUNT_SID=<from Twilio console>
TWILIO_AUTH_TOKEN=<from Twilio console>
TWILIO_WHATSAPP_FROM=whatsapp:+14155238886
```

`.env` is read by Compose only — it's not built into any image, and should
never be committed (it isn't tracked; `.env.example` is the template that
is).

**2. Build and start**

```bash
docker compose up --build -d
docker compose ps        # wait until all three show "healthy"
```

**3. Open the app**

`http://localhost:8081` (or whatever you set `FRONTEND_PORT` to).

**4. Stop**

```bash
docker compose down          # keeps the mysql-data volume
docker compose down -v       # also deletes it — wipes the database
```

---

## Option B — Kubernetes

### 1. Build the images

```bash
# run from inside lawbackend/
docker build -t matterly-backend:latest .
docker build -t matterly-frontend:latest \
  --build-arg VITE_API_BASE_URL= \
  ../Lawfrontend/lawreact/lawyer-ui
```

`VITE_API_BASE_URL` is left empty deliberately — the frontend calls
same-origin `/api/...`, and nginx inside that container proxies to the
`backend` Service. This is why the frontend image never needs to know a
hostname or IP.

Get the images where the cluster can reach them:

- **Cloud cluster (EKS/GKE/AKS):** tag and push to your registry, then
  update `image:` in [k8s/22-backend-deployment.yaml](k8s/22-backend-deployment.yaml)
  and [k8s/30-frontend-deployment.yaml](k8s/30-frontend-deployment.yaml)
  to the pushed tag.
- **Local cluster (kind/minikube/k3d):** load directly —
  `kind load docker-image matterly-backend:latest` (or the `minikube image
  load` / `k3d image import` equivalent) — no registry needed.

### 2. Set real secrets

Don't hand-edit the placeholder `stringData` values in the YAML files and
apply them as-is — generate them with `kubectl` so the real values only
ever exist in your shell history / secrets manager, not in a file you
might accidentally commit:

```bash
kubectl create namespace matterly --dry-run=client -o yaml | kubectl apply -f -

kubectl create secret generic mysql-secret -n matterly \
  --from-literal=root-password='<a real password>' \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl create secret generic backend-secret -n matterly \
  --from-literal=jwt-secret='<a long random string>' \
  --from-literal=smtp-username='you@example.com' \
  --from-literal=smtp-password='<app password>' \
  --from-literal=twilio-account-sid='<from Twilio console>' \
  --from-literal=twilio-auth-token='<from Twilio console>' \
  --from-literal=twilio-whatsapp-from='whatsapp:+14155238886' \
  --dry-run=client -o yaml | kubectl apply -f -
```

Leave any credential you don't have yet as `''` — just keep
`NOTIFICATIONS_ENABLED: "false"` in [k8s/21-backend-configmap.yaml](k8s/21-backend-configmap.yaml)
until you do, so the backend doesn't try to send through empty
credentials.

This makes applying the *files* `10-mysql-secret.yaml` / `20-backend-secret.yaml`
from this repo unnecessary — the commands above create the same-named
Secret objects directly. Only apply those two files yourself if you'd
rather edit the placeholder values in place (fine for a local
kind/minikube cluster you're not sharing with anyone).

### 3. Apply everything else

```bash
kubectl apply -f k8s/00-namespace.yaml

kubectl apply -f k8s/11-mysql-pvc.yaml \
  -f k8s/12-mysql-deployment.yaml -f k8s/13-mysql-service.yaml

kubectl apply -f k8s/21-backend-configmap.yaml \
  -f k8s/22-backend-deployment.yaml -f k8s/23-backend-service.yaml

kubectl apply -f k8s/30-frontend-deployment.yaml -f k8s/31-frontend-service.yaml
```

(Secrets were already created in step 2 — skip `10-mysql-secret.yaml` and
`20-backend-secret.yaml` if you used the `kubectl create secret` commands
above.)

### 4. Watch it come up and find the URL

```bash
kubectl -n matterly get pods -w
kubectl -n matterly get svc frontend
```

If `EXTERNAL-IP` stays `<pending>` — normal on a cluster with no
LoadBalancer implementation (bare local kind/minikube without metallb) —
either:

- edit [k8s/31-frontend-service.yaml](k8s/31-frontend-service.yaml)'s
  `type` to `NodePort`, or
- use the Ingress template: rename
  [k8s/32-ingress.yaml.example](k8s/32-ingress.yaml.example) to drop
  `.example`, set a real `host`, switch the frontend Service back to
  `ClusterIP`, apply both — requires an ingress controller already
  installed on the cluster (e.g. `ingress-nginx`).

### 5. Turn on real notifications later

Once you have real SMTP/Twilio credentials:

```bash
kubectl patch configmap backend-config -n matterly \
  --type merge -p '{"data":{"NOTIFICATIONS_ENABLED":"true"}}'

kubectl create secret generic backend-secret -n matterly \
  --from-literal=jwt-secret='<keep the same one you already used>' \
  --from-literal=smtp-username='you@example.com' \
  --from-literal=smtp-password='<app password>' \
  --from-literal=twilio-account-sid='<sid>' \
  --from-literal=twilio-auth-token='<token>' \
  --from-literal=twilio-whatsapp-from='whatsapp:+14155238886' \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl rollout restart deployment/backend -n matterly
```

The restart is required — pods only read Secret/ConfigMap values at
container start, so an in-place Secret update doesn't reach a pod that's
already running.

---

## What's already wired up

- **Frontend → backend, no hardcoded hostname.** nginx inside the
  frontend container proxies `/api/*` and `/auth/*` to `http://backend:8080` —
  a name that resolves identically via Docker Compose's network and
  Kubernetes Service DNS, so `nginx.conf` never changes between the two.
- **Health probes.** Spring Boot Actuator exposes `/actuator/health/readiness`
  and `/actuator/health/liveness` separately — used by both the Compose
  healthcheck and the Kubernetes probes.
- **Secrets vs. config split.** Anything sensitive (DB password, JWT key,
  SMTP/Twilio credentials) lives in a `Secret`; anything not sensitive
  (SMTP host/port, JDBC URL, feature flags) lives in a `ConfigMap`. Same
  split conceptually in Compose: sensitive values have no default in
  `docker-compose.yml` and must come from `.env` (`${JWT_SECRET:?set...}`
  fails startup if missing); non-sensitive ones have inline defaults.

## What's still your call

- **TLS/HTTPS.** Not set up here — normally the ingress controller's job
  (cert-manager + the Ingress template is the standard path). Depends on
  your cluster and domain, so left out.
- **Database backups.** The PVC persists data across pod restarts, but
  nothing here schedules backups — that's cluster/provider-specific.
- **Secret rotation / a real vault.** The `Secret` objects here are the
  integration point if you later want Sealed Secrets, External Secrets
  Operator, or a cloud secrets manager instead of `kubectl create secret`.

See [DEPLOYMENT.md](DEPLOYMENT.md) for the original build-and-verify notes
(what was tested, what wasn't).
