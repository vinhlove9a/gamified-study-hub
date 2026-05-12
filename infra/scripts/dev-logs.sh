#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

cd "${REPO_ROOT}"

echo "[dev-logs] Streaming logs. Press Ctrl+C to stop."

if [ "$#" -gt 0 ]; then
  docker compose logs -f "$@"
else
  docker compose logs -f
fi
