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
| MySQL | 8.0 |
| Lombok | Latest |
| JJWT | 0.11.5 |

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
- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6+
- Postman (for API testing)

### Step 1: Clone the Repository
```bash
git clone https://github.com/ansonapeter/fintech-onboarding-api.git
cd fintech-onboarding-api
```

### Step 2: Create MySQL Database
```sql
CREATE DATABASE lucidplus_onboarding;
```

### Step 3: Configure application.properties
Create `src/main/resources/application.properties` using the example file:
```properties
spring.application.name=lucidplus-onboarding

# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/lucidplus_onboarding?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
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

The server will start at: `http://localhost:8080`

Tables will be **auto-created** by Hibernate on first run.

---

## 🗄️ Database Design

### Tables

**users**
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-generated |
| name | VARCHAR | User full name |
| email | VARCHAR (unique) | User email |
| mobile | VARCHAR (unique) | 10-digit mobile |
| otp | VARCHAR | One-time password |
| otp_expiry | DATETIME | OTP expiry time |
| status | ENUM | PENDING / ACTIVE |
| created_at | DATETIME | Registration time |

**accounts**
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-generated |
| user_id | BIGINT (FK) | References users |
| balance | DECIMAL(15,2) | Current balance |
| created_at | DATETIME | Account creation time |

**transactions**
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-generated |
| sender_id | BIGINT (FK) | Sender account |
| receiver_id | BIGINT (FK) | Receiver account |
| amount | DECIMAL(15,2) | Transfer amount |
| type | ENUM | DEBIT / CREDIT |
| user_id | BIGINT | Owner of this record |
| timestamp | DATETIME | Transaction time |

---

## 🔐 Authentication

This API uses **JWT (JSON Web Token)** based authentication.

- Public endpoints: `/api/register`, `/api/verify-otp`, `/api/login`
- Protected endpoints: `/api/transfer`, `/api/transactions/{userId}`

To access protected endpoints, include the JWT token in the request header:
```
Authorization: Bearer <your_jwt_token>
```

---

## 📡 API Endpoints

### A. User Registration with OTP Flow

#### 1. Register User
```
POST /api/register
```
**Request Body:**
```json
{
    "name": "John Doe",
    "email": "john@gmail.com",
    "mobile": "9999999999"
}
```
**Response:**
```json
{
    "success": true,
    "message": "User registered. OTP: 206365",
    "data": null
}
```
> ⚠️ OTP expires in 5 minutes. In production this would be sent via SMS.

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
**Response:**
```json
{
    "success": true,
    "message": "OTP verified. Account created with ₹1000 balance.",
    "data": null
}
```
> ✅ This automatically creates a wallet account with ₹1000 default balance.

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
> Can use mobile number or email as identifier.

**Response:**
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
> 🔑 Copy the token — required for all protected endpoints.

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
**Response:**
```json
{
    "success": true,
    "message": "Transfer successful",
    "data": null
}
```

**Error Cases:**
```json
{ "success": false, "message": "Insufficient balance", "data": null }
{ "success": false, "message": "Cannot transfer to yourself", "data": null }
{ "success": false, "message": "Receiver account not found", "data": null }
```

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
**Response:**
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

---

## ✅ Key Concepts Implemented

| Concept | Implementation |
|---------|---------------|
| Layered Architecture | Controller → Service → Repository |
| JWT Security | JwtFilter validates token on every request |
| OTP Flow | 6-digit OTP with 5-minute expiry |
| Transaction Management | @Transactional ensures atomic money transfers |
| Exception Handling | GlobalExceptionHandler with custom exceptions |
| Input Validation | @Valid, @NotBlank, @Email, @Pattern annotations |
| Database Design | 3 normalized tables with proper relationships |

---

## 🧪 Testing Flow

Test the APIs in this exact order:

```
1. Register User 1    →  POST /api/register
2. Verify User 1 OTP  →  POST /api/verify-otp
3. Register User 2    →  POST /api/register
4. Verify User 2 OTP  →  POST /api/verify-otp
5. Login User 1       →  POST /api/login  (copy JWT token)
6. Transfer Money     →  POST /api/transfer  (use JWT token)
7. Check History      →  GET /api/transactions/1  (use JWT token)
```

---

## 📦 Postman Collection

Import the included `LucidPlus-Onboarding-API.postman_collection.json` file into Postman to test all endpoints with pre-configured requests.

---

## 👨‍💻 Author

Built for **LucidPlus IT Solutions** – Junior Java Developer Assignment  

