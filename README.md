# backend

## Local setup

The backend is prepared for PostgreSQL and reads database settings from environment variables.

Default values:

- `DB_URL=jdbc:postgresql://localhost:5432/tourplanner`
- `DB_USERNAME=tourplanner`
- `DB_PASSWORD=tourplanner`
- `DDL_AUTO=update`

Example run:

```bash
DB_URL=jdbc:postgresql://localhost:5432/tourplanner \
DB_USERNAME=tourplanner \
DB_PASSWORD=tourplanner \
./mvnw spring-boot:run
```
