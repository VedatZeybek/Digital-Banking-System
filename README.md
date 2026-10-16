# NovaBank — Banking Transaction Application

A small full-stack banking application built with Java 21, Spring Boot, PostgreSQL, React, and Docker Compose.

## Features

- Register and sign in with a Spring Security session
- Automatically receive a **1,000.00 TL** opening balance
- Deposit and withdraw money
- Transfer money to another account number
- Review incoming and outgoing transaction history
- Prevent zero/negative amounts, overdrafts, and self-transfers
- Safely serialize concurrent balance changes with database row locks

## Run with Docker Compose

Requirements: Docker with the Compose plugin.

```bash
docker compose up --build
```

Open <http://localhost:3000> in a browser. The first build downloads the required images and dependencies, so it can take a few minutes.

To stop the application:

```bash
docker compose down
```

PostgreSQL data is kept in the `postgres_data` Docker volume. To stop the application and remove its data:

```bash
docker compose down -v
```

## How to try a transfer

1. Register the first user and copy the account number shown on the dashboard.
2. Sign out and register a second user.
3. Open **Transfer**, enter the first user's account number and an amount.
4. Sign back in as the first user to see the received transfer in **History**.

## Local development

### Database

Start only PostgreSQL:

```bash
docker compose up -d database
```

The default local database settings are:

- Database: `banking`
- Username: `banking`
- Password: `banking`
- Port: `5432`

### Backend

Requires JDK 21 and Maven 3.9+.

```bash
cd backend
mvn spring-boot:run
```

The REST API runs at <http://localhost:8080>. Hibernate creates or updates the database tables on startup.

### Frontend

Requires Node.js 20+.

```bash
cd frontend
npm install
npm run dev
```

The development UI runs at <http://localhost:5173> and proxies `/api` requests to the backend.

## Main API endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/auth/register` | Register and sign in |
| `POST` | `/api/auth/login` | Sign in |
| `POST` | `/api/auth/logout` | Sign out |
| `GET` | `/api/auth/me` | Get the signed-in user's account |
| `POST` | `/api/account/deposit` | Deposit money |
| `POST` | `/api/account/withdraw` | Withdraw money |
| `POST` | `/api/account/transfer` | Transfer money |
| `GET` | `/api/account/transactions` | View transaction history |

All money values use `BigDecimal` with two decimal places. Deposits, withdrawals, and transfers use pessimistic database locks. Transfers lock both accounts in ID order and run in one `@Transactional` operation.

## Project structure

```text
backend/     Spring Boot REST API (Controller / Service / Repository)
frontend/    React + Vite interface served by Nginx in Docker
compose.yaml PostgreSQL, backend, and frontend services
```

This project intentionally contains no unit tests, integration tests, or other automated test cases.
