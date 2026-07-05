# backend

## Local setup


### 1. Start PostgreSQL

```bash
docker compose up -d
```

### 2. Run the backend

```bash
./mvnw spring-boot:run
```

### 3. Stop PostgreSQL

```bash
docker compose down
```