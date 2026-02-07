.PHONY: help setup install-frontend install-backend dev dev-frontend dev-backend build-frontend build-backend test-backend

help:
	@echo "Available targets:"
	@echo "  setup            Install frontend and backend dependencies"
	@echo "  install-frontend Install frontend dependencies (npm ci)"
	@echo "  install-backend  Resolve backend dependencies (Gradle)"
	@echo "  dev              Start frontend and backend together"
	@echo "  dev-frontend     Start frontend dev server (Vite)"
	@echo "  dev-backend      Start backend dev server (Spring Boot)"
	@echo "  build-frontend   Build frontend"
	@echo "  build-backend    Build backend"
	@echo "  test-backend     Run backend tests"

setup: install-frontend install-backend

//フロントとバックを同時起動
dev:
	@$(MAKE) -j2 dev-backend dev-frontend

install-frontend:
	cd frontend && npm ci

install-backend:
	cd backend && ./gradlew dependencies > /dev/null

//フロントエンド起動
dev-frontend:
	cd frontend && npm run dev

//バックエンド起動
dev-backend:
	cd backend && ./gradlew bootRun

build-frontend:
	cd frontend && npm run build

build-backend:
	cd backend && ./gradlew build

test-backend:
	cd backend && ./gradlew test
