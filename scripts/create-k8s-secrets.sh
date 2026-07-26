#!/usr/bin/env bash
# One-time setup: creates mysql-secret and backend-secret in the matterly
# namespace with random values, so the commit-triggered pipeline
# (deploy-to-minikube.sh) has something to roll out against.
#
# Re-run any time to rotate the JWT secret / MySQL password (this deletes
# and recreates both, which will invalidate any logged-in sessions).
#
# To use real SMTP/Twilio credentials instead of blanks, edit the
# --from-literal values below before running, or patch afterwards:
#   kubectl -n matterly create secret generic backend-secret \
#     --from-literal=smtp-username=... --dry-run=client -o yaml | kubectl apply -f -
set -euo pipefail

export PATH="$PATH:/c/Program Files/Kubernetes/Minikube"

JWT_SECRET=$(node -e "console.log(require('crypto').randomBytes(48).toString('base64'))")
MYSQL_PW=$(node -e "console.log(require('crypto').randomBytes(16).toString('hex'))")

kubectl apply -f /f/Git/lawbackend/k8s/00-namespace.yaml

kubectl create secret generic mysql-secret -n matterly \
  --from-literal=root-password="$MYSQL_PW" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl create secret generic backend-secret -n matterly \
  --from-literal=jwt-secret="$JWT_SECRET" \
  --from-literal=smtp-username='' \
  --from-literal=smtp-password='' \
  --from-literal=twilio-account-sid='' \
  --from-literal=twilio-auth-token='' \
  --from-literal=twilio-whatsapp-from='' \
  --dry-run=client -o yaml | kubectl apply -f -

{
  echo "MYSQL_ROOT_PASSWORD=$MYSQL_PW"
  echo "JWT_SECRET=$JWT_SECRET"
} > /f/Git/lawbackend/.k8s-generated-secrets.txt

echo "Secrets created. Values saved to /f/Git/lawbackend/.k8s-generated-secrets.txt (not committed)."
