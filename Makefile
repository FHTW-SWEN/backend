.PHONY: help db-up db-down db-logs run build test clean

help:
	@echo "Available commands:"
	@echo "  make db-up     Start PostgreSQL with Docker Compose"
	@echo "  make db-down   Stop PostgreSQL"
	@echo "  make db-logs   Show PostgreSQL logs"
	@echo "  make run       Run the Spring Boot application"
	@echo "  make build     Build the application"
	@echo "  make test      Run tests"
	@echo "  make clean     Clean Maven build output"

db-up:
	docker compose up -d

db-down:
	docker compose down

db-logs:
	docker compose logs -f postgres

run:
	./mvnw spring-boot:run

build:
	./mvnw clean package

test:
	./mvnw test

clean:
	./mvnw clean
