SHELL := /bin/bash
.SHELLFLAGS := -eu -o pipefail -c

EC2_USER ?= ec2-user
EC2_HOST ?=
EC2_KEY ?= gacha-ec2-key.pem

REMOTE_BACKEND_DIR ?= /opt/gacha/backend
REMOTE_FRONTEND_DIR ?= /var/www/gacha
BACKEND_SERVICE ?= gacha-backend

.PHONY: help setup install-frontend install-backend dev dev-frontend dev-backend \
	build-frontend build-backend test-backend package package-frontend package-backend \
	check-ec2 ssh-ec2 ec2-setup-dirs deploy-frontend deploy-backend deploy \
	restart-backend reload-nginx

# --- Help ---
help:
	@echo "Available targets:"
	@echo "  make setup"
	@echo "  make dev"
	@echo "  make deploy EC2_HOST=<public-ipv4>"

# --- Local ---
setup: install-frontend install-backend

install-frontend:
	cd frontend && npm ci

install-backend:
	cd backend && ./gradlew dependencies > /dev/null

dev:
	@$(MAKE) -j2 dev-backend dev-frontend

dev-frontend:
	cd frontend && npm run dev

dev-backend:
	cd backend && ./gradlew bootRun

build-frontend:
	cd frontend && npm run build

build-backend:
	cd backend && ./gradlew bootJar

test-backend:
	cd backend && ./gradlew test

package: package-frontend package-backend

package-frontend: build-frontend
package-backend: build-backend

# --- EC2 ---
check-ec2:
	@test -n "$(EC2_HOST)" || (echo "EC2_HOST is required"; exit 1)
	@test -f "$(EC2_KEY)" || (echo "EC2_KEY not found"; exit 1)

ssh-ec2: check-ec2
	ssh -i "$(EC2_KEY)" "$(EC2_USER)@$(EC2_HOST)"

ec2-setup-dirs: check-ec2
	ssh -i "$(EC2_KEY)" "$(EC2_USER)@$(EC2_HOST)" \
	"sudo mkdir -p $(REMOTE_BACKEND_DIR) $(REMOTE_FRONTEND_DIR) && \
	 sudo chown -R $(EC2_USER):$(EC2_USER) /opt/gacha /var/www/gacha"

deploy-frontend: check-ec2 package-frontend
	scp -i "$(EC2_KEY)" -r frontend/dist/* "$(EC2_USER)@$(EC2_HOST):$(REMOTE_FRONTEND_DIR)/"

deploy-backend: check-ec2 package-backend
	scp -i "$(EC2_KEY)" backend/build/libs/*.jar "$(EC2_USER)@$(EC2_HOST):$(REMOTE_BACKEND_DIR)/app.jar"

restart-backend: check-ec2
	ssh -i "$(EC2_KEY)" "$(EC2_USER)@$(EC2_HOST)" \
	"sudo systemctl restart $(BACKEND_SERVICE)"

reload-nginx: check-ec2
	ssh -i "$(EC2_KEY)" "$(EC2_USER)@$(EC2_HOST)" \
	"sudo nginx -t && sudo systemctl reload nginx"

deploy: deploy-backend deploy-frontend restart-backend reload-nginx