# 🏦 Fintech Onboarding API

A Spring Boot backend system simulating a fintech user onboarding and transaction flow.
Built as part of the **LucidPlus IT Solutions – Junior Java Developer Assignment**.

---

## 🛠️ Technology Stack

| Technology | Version |
|------------|---------|
| Java | 21 |
| Spring Boot | 4.0.5 |
| Spring Security | JWT-based |
| Spring Data JPA | Hibernate ORM |
| MySQL | 8.0+ |
| Lombok | Latest |
| JJWT | 0.11.5 |
| Maven | 3.6+ |

---

## 📁 Project Structure

```
src/main/java/com/example/lucidplus_onboarding/
│
├── controller/
│   ├── AuthController.java
│   └── TransactionController.java
│
├── service/
│   ├── AuthService.java
│   ├── AccountService.java
│   └── TransactionService.java
│
├── repository/
│   ├── UserRepository.java
│   ├── AccountRepository.java
│   └── TransactionRepository.java
│
├── entity/
│   ├── User.java
│   ├── Account.java
│   └── Transaction.java
│
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   ├── OtpVerifyRequest.java
│   │   ├── LoginRequest.java
│   │   └── TransferRequest.java
│   └── response/
│       ├── ApiResponse.java
│       └── LoginResponse.java
│
├── security/
│   ├── JwtUtil.java
│   ├── JwtFilter.java
│   └── SecurityConfig.java
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── UserNotFoundException.java
│   ├── InvalidOtpException.java
│   ├── InsufficientBalanceException.java
│   └── AccountNotFoundException.java
│
└── enums/
    ├── UserStatus.java
    └── TransactionType.java
```

---

## ⚙️ Setup & Installation

### Prerequisites

- Java 21 or higher
- MySQL 8.0 or higher
- Maven 3.6+
- Postman (for API testing)

### Step 1: Clone the Repository

```bash
git clone https://github.com/ansonapeter/lucidplus-onboarding.git
cd lucidplus-onboarding
```

### Step 2: Create MySQL Database

```sql
CREATE DATABASE lucidplus_onboarding;
```

### Step 3: Configure application.properties

Create `src/main/resources/application.properties`. A template is provided in `application.properties.example` — copy and fill in your values:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Then update with your credentials:

```properties
spring.application.name=lucidplus-onboarding

# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/lucidplus_onboarding?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT
app.jwt.secret=lucidplus_super_secret_jwt_key_2024_make_it_long
app.jwt.expiration=86400000

# Server
server.port=8080
```

### Step 4: Run the Application

```bash
mvn spring-boot:run
```

The server starts at `http://localhost:8080`. All three database tables are **auto-created by Hibernate** on first run — no manual SQL migration needed.

---

## 🗄️ Database Design

### Tables

**users**

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-generated |
| name | VARCHAR | User full name |
| email | VARCHAR (unique) | User email |
| mobile | VARCHAR (unique) | 10-digit mobile number |
| otp | VARCHAR | One-time password |
| otp_expiry | DATETIME | OTP expiry time (5 min) |
| status | ENUM | PENDING / ACTIVE |
| created_at | DATETIME | Registration timestamp |

**accounts**

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-generated |
| user_id | BIGINT (FK) | References users.id |
| balance | DECIMAL(15,2) | Current wallet balance |
| created_at | DATETIME | Account creation timestamp |

**transactions**

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-generated |
| sender_id | BIGINT (FK) | Sender account |
| receiver_id | BIGINT (FK) | Receiver account |
| amount | DECIMAL(15,2) | Transfer amount |
| type | ENUM | DEBIT / CREDIT |
| user_id | BIGINT | Owner of this record |
| timestamp | DATETIME | Transaction timestamp |

---

## 🔐 Authentication

This API uses **JWT (JSON Web Token)** based authentication.

- **Public endpoints** (no token required): `/api/register`, `/api/verify-otp`, `/api/login`
- **Protected endpoints** (JWT required): `/api/transfer`, `/api/transactions/{userId}`

Include the JWT token in the `Authorization` header for all protected requests:

```
Authorization: Bearer <your_jwt_token>
```

Accessing a protected endpoint without a valid token returns `401 Unauthorized`.

---

## 📡 API Endpoints

### A. User Registration with OTP Flow

#### 1. Register User

```
POST /api/register
```

| Field | Type | Validation |
|-------|------|------------|
| name | String | Required, not blank |
| email | String | Required, valid email format |
| mobile | String | Required, 10-digit number |

**Request Body:**
```json
{
    "name": "John Doe",
    "email": "john@gmail.com",
    "mobile": "9999999999"
}
```

**Response — `201 Created`:**
```json
{
    "success": true,
    "message": "User registered. OTP: 206365",
    "data": null
}
```

> ⚠️ The OTP is returned in the response message for development purposes. In production it would be delivered via SMS. OTP expires in **5 minutes**.

**Error responses:**

| HTTP Status | Message |
|-------------|---------|
| `400 Bad Request` | Validation error (invalid email, missing field, etc.) |
| `409 Conflict` | Email or mobile already registered |

---

#### 2. Verify OTP

```
POST /api/verify-otp
```

**Request Body:**
```json
{
    "mobile": "9999999999",
    "otp": "206365"
}
```

**Response — `200 OK`:**
```json
{
    "success": true,
    "message": "OTP verified. Account created with ₹1000 balance.",
    "data": null
}
```

> ✅ Successful OTP verification automatically creates a wallet account with a default balance of **₹1000**. User status changes from `PENDING` to `ACTIVE`.

**Error responses:**

| HTTP Status | Message |
|-------------|---------|
| `400 Bad Request` | OTP expired or invalid |
| `404 Not Found` | Mobile number not found |

---

### B. Login

#### 3. Login

```
POST /api/login
```

**Request Body:**
```json
{
    "identifier": "9999999999"
}
```

> The `identifier` field accepts either a **mobile number** or an **email address**.

**Response — `200 OK`:**
```json
{
    "success": true,
    "message": "Login successful",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "name": "John Doe",
        "email": "john@gmail.com",
        "mobile": "9999999999"
    }
}
```

> 🔑 Copy the `token` value — it is required for all protected endpoints.

**Error responses:**

| HTTP Status | Message |
|-------------|---------|
| `404 Not Found` | User not found |
| `403 Forbidden` | Account not yet verified (OTP pending) |

---

### C. Transfer Money

#### 4. Transfer

```
POST /api/transfer
```

**Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
    "senderId": 1,
    "receiverId": 2,
    "amount": 200
}
```

**Response — `200 OK`:**
```json
{
    "success": true,
    "message": "Transfer successful",
    "data": null
}
```

**Error responses:**

| HTTP Status | Message |
|-------------|---------|
| `400 Bad Request` | `"Insufficient balance"` |
| `400 Bad Request` | `"Cannot transfer to yourself"` |
| `404 Not Found` | `"Receiver account not found"` |
| `401 Unauthorized` | Missing or invalid JWT token |

> 🔒 The transfer is wrapped in `@Transactional` — both the debit and credit happen atomically. If either operation fails, the entire transfer is rolled back.

---

### D. Transaction History

#### 5. Get Transactions

```
GET /api/transactions/{userId}
```

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response — `200 OK`:**
```json
{
    "success": true,
    "message": "Transactions fetched",
    "data": [
        {
            "id": 1,
            "amount": 200.00,
            "type": "DEBIT",
            "timestamp": "2026-04-14T11:43:31"
        }
    ]
}
```

**Error responses:**

| HTTP Status | Message |
|-------------|---------|
| `401 Unauthorized` | Missing or invalid JWT token |
| `404 Not Found` | User not found |

---

## 📦 Postman Collection

Import `LucidPlus-Onboarding-API_postman_collection.json` into Postman to test all endpoints with pre-configured requests.

### Postman Environment Variables

The collection uses the following variables — set these before running:

| Variable | Default Value | Description |
|----------|--------------|-------------|
| `base_url` | `http://localhost:8080` | API server base URL |
| `jwt_token` | *(auto-filled)* | JWT token — auto-saved after Step 5 (Login) |
| `user1_mobile` | `9999999999` | Mobile number for User 1 |
| `user2_mobile` | `8888888888` | Mobile number for User 2 |
| `user1_otp` | *(auto-filled)* | OTP for User 1 — auto-extracted after Step 1 |
| `user2_otp` | *(auto-filled)* | OTP for User 2 — auto-extracted after Step 3 |

> The Postman collection includes automated test scripts. OTPs are **automatically extracted** from the registration response and saved to collection variables — no manual copy-paste needed. The JWT token is also saved automatically after login.

---

## 🧪 Testing Flow

Run requests in this exact order:

```
1. Register User 1       →  POST /api/register         (OTP auto-saved)
2. Verify User 1 OTP     →  POST /api/verify-otp        (account created)
3. Register User 2       →  POST /api/register         (OTP auto-saved)
4. Verify User 2 OTP     →  POST /api/verify-otp        (account created)
5. Login User 1          →  POST /api/login             (JWT token auto-saved)
6. Transfer Money        →  POST /api/transfer          (uses saved JWT)
7. Check History User 1  →  GET  /api/transactions/1    (should show DEBIT)
8. Check History User 2  →  GET  /api/transactions/2    (should show CREDIT)
```

The collection also includes dedicated **error case** and **security** tests:
- Transfer with insufficient balance
- Self-transfer attempt
- Access protected endpoint without token (`401`)
- Access protected endpoint with invalid/tampered token (`401`)

---

## ✅ Key Concepts Implemented

| Concept | Implementation |
|---------|----------------|
| Layered Architecture | Controller → Service → Repository |
| JWT Security | `JwtFilter` validates token on every protected request |
| OTP Flow | 6-digit OTP with 5-minute expiry, returned in response for dev |
| Atomic Transactions | `@Transactional` ensures both debit and credit succeed or both roll back |
| Exception Handling | `GlobalExceptionHandler` with custom exception classes |
| Input Validation | `@Valid`, `@NotBlank`, `@Email`, `@Pattern` annotations |
| Database Design | 3 normalized tables with proper FK relationships |
| Flexible Login | `identifier` field accepts mobile number or email |

---

## ⚠️ Assumptions & Limitations

- OTP is returned in the API response for **development/testing purposes only**. In a production system it would be delivered via SMS (e.g., Twilio) and never exposed in the response body.
- All accounts are created with a hardcoded default balance of **₹1000** on OTP verification.
- The application is **single-currency** (INR only).
- No pagination is implemented for transaction history — all records are returned in a single response.
- No email/SMS integration is included in this version.

---

## 👨‍💻 Author

Built for **LucidPlus IT Solutions** – Junior Java Developer Assignment
Submission Deadline: April 16, 2026
