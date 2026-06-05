# SACCOS Dividend Engine

Year-end dividend calculation and disbursement microservice for the OneLipa SACCOS platform.

## Features

- Create dividend cycle per fiscal year
- Preview simulation before board approval
- Board approval workflow
- Auto-calculate per member: `dividend = (member_shares / total_shares) × distributable_profit`
- Tanzania WHT (5%) deduction
- Post net dividend to member's Fineract savings account
- SMS notification via notification service (RabbitMQ)
- Dividend certificates per member

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/dividends` | Create dividend cycle |
| GET | `/api/v1/dividends/{year}/preview` | Simulate before approval |
| POST | `/api/v1/dividends/{year}/approve` | Board approves |
| POST | `/api/v1/dividends/{year}/disburse` | Disburse to all members |

## Running Locally

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Environment Variables

| Variable | Description |
|----------|-------------|
| `DB_HOST` | MariaDB host |
| `DB_USERNAME` | DB username |
| `DB_PASSWORD` | DB password |
| `RABBITMQ_HOST` | RabbitMQ host |
| `RABBITMQ_USER` | RabbitMQ username |
| `RABBITMQ_PASSWORD` | RabbitMQ password |
| `FINERACT_BASE_URL` | Fineract API URL |
| `FINERACT_USERNAME` | Fineract credentials |
| `FINERACT_PASSWORD` | Fineract credentials |
| `JWT_SECRET` | JWT signing key |
