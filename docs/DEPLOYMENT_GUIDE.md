# Deployment & Setup Guide

## Prerequisites

- **Java 21+** (JDK)
- **Node.js 20+** with npm
- **Docker Desktop** (for PostgreSQL or full deployment)
- **Git**

## Local Development Setup

### 1. Clone and prepare

```bash
git clone <repo-url>
cd placement-management-system
```

### 2. Start PostgreSQL

```bash
docker compose up -d postgres
```

Verification:

```bash
docker compose exec postgres psql -U postgres -d placement_db -c "\dt"
```

### 3. Backend setup

```bash
cd backend

# Set JWT secret (required — no fallback in production)
export JWT_SECRET="your-256-bit-secret-key-change-in-production"

# Compile
./mvnw compile

# Run tests (needs Docker for Testcontainers)
./mvnw test

# Start dev server (with live reload)
./mvnw spring-boot:run
```

The backend starts on `http://localhost:8080`. Flyway applies all 16 migrations automatically.

**Optional dev profile** (for verbose SQL and debug logs):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Frontend setup

```bash
cd frontend

# Install dependencies
npm install

# Start dev server
npm run dev
```

The frontend starts on `http://localhost:5173`. It proxies API calls to the backend via `VITE_API_URL` (defaults to `http://localhost:8080/api/v1`).

**Environment file** (optional):

```bash
cp .env.example .env
# Edit .env if your backend runs on a different port
```

### 5. Seed admin login

| Email | Password | Role |
|-------|----------|------|
| admin@placement.com | Admin@123 | ADMIN |

## Docker Compose (Full Stack)

### Start everything

```bash
docker compose up -d --build
```

This starts:
- **PostgreSQL 16** on port 5432
- **Backend** (Spring Boot) on port 8080

The frontend still requires `npm run dev` separately (for dev workflow).

### Stop everything

```bash
docker compose down
```

### View logs

```bash
docker compose logs -f backend
docker compose logs -f postgres
```

## Production Deployment

### Backend (Spring Boot JAR)

#### 1. Build the JAR

```bash
cd backend
./mvnw package -DskipTests
# Produces: target/placement-management-0.0.1-SNAPSHOT.jar
```

#### 2. Run the JAR

```bash
java -jar target/placement-management-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:postgresql://<db-host>:5432/placement_db \
  --spring.datasource.username=<db-user> \
  --spring.datasource.password=<db-password> \
  --jwt.secret=<your-256-bit-secret> \
  --file.upload-dir=/data/uploads \
  --server.port=8080
```

#### 3. Systemd service (Linux)

Create `/etc/systemd/system/placement-backend.service`:

```ini
[Unit]
Description=Placement Management System Backend
After=network.target postgresql.service

[Service]
Type=simple
User=placement
WorkingDirectory=/opt/placement/backend
ExecStart=/usr/bin/java -jar /opt/placement/backend/target/placement-management-0.0.1-SNAPSHOT.jar
Environment=JWT_SECRET=<your-secret>
Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/placement_db
Environment=SPRING_DATASOURCE_USERNAME=postgres
Environment=SPRING_DATASOURCE_PASSWORD=<db-password>
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable placement-backend
sudo systemctl start placement-backend
sudo systemctl status placement-backend
```

### Frontend (Static Build)

#### 1. Build

```bash
cd frontend
npm run build
# Produces: dist/
```

#### 2. Serve via Nginx

Create `/etc/nginx/sites-available/placement`:

```nginx
server {
    listen 80;
    server_name placement.example.com;

    root /opt/placement/frontend/dist;
    index index.html;

    # SPA fallback — all routes serve index.html
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API proxy (optional — or use direct API URL)
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/placement /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### Docker (Production)

For a fully containerized production stack, create a `docker-compose.prod.yml`:

```yaml
version: "3.9"

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: placement_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/placement_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      postgres:
        condition: service_healthy
    volumes:
      - uploads:/app/uploads
    restart: unless-stopped
    ports:
      - "8080:8080"

  frontend:
    image: nginx:alpine
    volumes:
      - ./frontend/dist:/usr/share/nginx/html:ro
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: unless-stopped

volumes:
  postgres_data:
  uploads:
```

## Database Migrations

Flyway manages all schema changes. Migrations are in:

```
backend/src/main/resources/db/migration/
```

**Migration timeline** (16 migrations):

| File | Purpose |
|------|---------|
| V1 | users table |
| V2 | student_profiles |
| V3 | recruiter_profiles |
| V4 | placement_officer_profiles |
| V5 | drives |
| V6 | job_posts |
| V7 | applications |
| V8 | notifications |
| V9 | seed admin user |
| V10–V15 | indexes, password fix, link column, search indexes |
| V16 | parsed_resumes, scoring fields |

**To add a new migration**:
1. Create `V17__description.sql` in `db/migration/`
2. Add DDL/DML (must be idempotent if possible)
3. Restart the backend — Flyway applies it automatically

**Check migration status**:

```bash
docker compose exec postgres psql -U postgres -d placement_db -c "SELECT version, installed_on, success FROM flyway_schema_history;"
```

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `JWT_SECRET` | **Yes** | — | HS256 key (≥256 bits). **No fallback in production.** |
| `SPRING_DATASOURCE_URL` | No | `jdbc:postgresql://localhost:5432/placement_db` | PostgreSQL connection URL |
| `SPRING_DATASOURCE_USERNAME` | No | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | No | `postgres` | Database password |
| `FILE_UPLOAD_DIR` | No | `./uploads` | Directory for uploaded resumes |
| `SERVER_PORT` | No | `8080` | Backend HTTP port |
| `VITE_API_URL` | No | `http://localhost:8080/api/v1` | Frontend → backend API base URL |

## Verification Checklist

After deployment, verify each endpoint:

```bash
# 1. Health check (backend running)
curl -I http://localhost:8080/api/v1/auth/login

# 2. Register a user
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test@123","fullName":"Test User","role":"STUDENT"}'

# 3. Login and get JWT
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test@123"}' | jq -r '.data.token')

# 4. Access protected endpoint
curl -s http://localhost:8080/api/v1/profile/me \
  -H "Authorization: Bearer $TOKEN" | jq .

# 5. Frontend accessible
curl -I http://localhost:5173
```

## Troubleshooting

| Problem | Likely cause | Fix |
|---------|-------------|-----|
| `FlywayValidateException` | Migration mismatch | Check `flyway_schema_history` table; repair: `./mvnw flyway:repair` |
| `PSQLException: Connection refused` | PostgreSQL not running | `docker compose up -d postgres` |
| `JWT secret not set` | Missing env var | `export JWT_SECRET="your-secret"` |
| `401 Unauthorized` | Invalid/expired JWT | Re-login |
| `403 Forbidden` | Wrong role | Check the user's role in the DB |
| `413 Payload Too Large` | File >5MB | Reduce file size or increase `spring.servlet.multipart.max-file-size` |
| Frontend shows blank page | Vite proxy misconfigured | Check `VITE_API_URL`; check console for CORS errors |
| `relation does not exist` | Migration not applied | Restart backend; check `flyway_schema_history` |
| OutOfMemoryError | Heap too small | `java -Xmx512m -jar ...` |
