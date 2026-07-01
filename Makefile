SHELL := /bin/bash

.PHONY: help dev-up dev-down dev-logs dev-rebuild backend-run backend-test frontend-dev frontend-build docker-config

help:
	@echo "Available commands:"
	@echo "  make dev-up         - Start all services with Docker Compose"
	@echo "  make dev-down       - Stop all services"
	@echo "  make dev-logs       - Tail service logs"
	@echo "  make dev-rebuild    - Rebuild and restart services"
	@echo "  make backend-run    - Run Spring Boot backend locally"
	@echo "  make backend-test   - Run backend test suite"
	@echo "  make frontend-dev   - Run frontend dev server locally"
	@echo "  make frontend-build - Build frontend production bundle"
	@echo "  make docker-config  - Validate merged Docker Compose config"

dev-up:
	@./infra/scripts/dev-up.sh

dev-down:
	@./infra/scripts/dev-down.sh

dev-logs:
	@./infra/scripts/dev-logs.sh

dev-rebuild:
	@./infra/scripts/dev-rebuild.sh

backend-run:
	@cd apps/backend && mvn spring-boot:run

backend-test:
	@cd apps/backend && mvn test -Dnet.bytebuddy.experimental=true

frontend-dev:
	@cd apps/frontend && npm run dev

frontend-build:
	@cd apps/frontend && npm run build

docker-config:
	@docker compose config
