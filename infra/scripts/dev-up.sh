#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

cd "${REPO_ROOT}"

echo "[dev-up] Starting Gamified Study Hub services..."
docker compose up -d

echo "[dev-up] Services started."
echo "[dev-up] Use './infra/scripts/dev-logs.sh' to tail logs."
