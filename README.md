## Overview

This document provides comprehensive documentation for the AI Bank Solution implemented using Spring AI. The solution integrates three key components:

1. **Agentic RAG (Retrieval Augmented Generation)** - For fraud detection and financial advice
2. **Agentic Guardrails** - For policy compliance, role-based access control, and audit trails
3. **Memory Components** - Short-term memory using Redis and long-term memory using PostgreSQL/PGVector

The implementation follows cloud-native principles and incorporates software engineering best practices for resilience, performance, security, and event-driven architecture.

## Architecture

The solution is built on a Spring Boot foundation with the following architecture:

```
AI Bank Solution
├── Agentic RAG
│   ├── Fraud Detection
│   └── Financial Advice
├── Agentic Guardrails
│   ├── Policy & Compliance Rules
│   ├── Role-Based Access Control
│   └── Audit Logging
└── Memory Components
    ├── Short-Term Memory (Redis)
    └── Long-Term Memory (PostgreSQL/PGVector)
```

### Integration Flow

1. User query is received by the `BankingAiController`
2. `BankingAiIntegrationService` processes the query through:
    - Permission checking with RBAC
    - Compliance validation with guardrails
    - Session memory management
    - Customer profile retrieval
    - AI query processing
    - Audit logging

## Components

### 1. Agentic RAG

The Agentic RAG component uses Spring AI to implement:

#### Fraud Detection

- `TransactionFraudAdvisor`: Creates prompts for fraud detection
- `FraudDetectionService`: Processes transactions and detects potential fraud
- `FraudDetectionController`: Exposes REST endpoints for fraud detection

#### Financial Advice

- `FinancialAdviceAdvisor`: Creates prompts for personalized financial advice
- `FinancialAdviceService`: Generates financial advice based on customer profiles
- `FinancialAdviceController`: Exposes REST endpoints for financial advice

### 2. Agentic Guardrails

The Agentic Guardrails component implements:

#### Policy & Compliance

- `ComplianceRule`: Model for compliance rules
- `ComplianceGuardrailService`: Validates user input against compliance rules
- `ComplianceController`: Manages compliance rules

#### Role-Based Access Control

- `Role`: Model for roles and permissions
- `RBACService`: Manages roles and checks permissions
- `RBACController`: Manages roles and permissions

#### Audit Logging

- `AuditLog`: Model for audit logs
- `AuditService`: Logs events and provides query methods
- `AuditController`: Retrieves audit logs

### 3. Memory Components

The Memory Components implement:

#### Short-Term Memory

- `SessionMemory`: Model for session memory stored in Redis
- `SessionMemoryService`: Manages session memory with proper timeout
- `SessionMemoryController`: Manages session memory

#### Long-Term Memory

- `CustomerProfile`: Model for customer profiles with vector embeddings
- `CustomerProfileService`: Manages customer profiles with retention policies
- `CustomerProfileController`: Manages customer profiles

## Configuration

### Redis Configuration

```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // Redis configuration for short-term memory
    }
}
```

### AI Configuration

```java
@Configuration
public class AiConfig {
    @Bean
    public OpenAiApi openAiApi() {
        return new OpenAiApi(openAiApiKey);
    }

    @Bean
    public OpenAiEmbeddingModel embeddingModel(){
        // Embedding model configuration
    }
}
```

### Database Configuration

```java
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
    @Bean
    public DataSource dataSource() {
        // PostgreSQL configuration for long-term memory
    }
}
```

## Integration

The `BankingAiIntegrationService` connects all components:

```java
@Service
public class BankingAiIntegrationService {
    // Integrates RAG, Guardrails, and Memory components
    public ProcessResult processUserQuery(
            String userId,
            String customerId,
            String userRole,
            String userQuery,
            String sessionId,
            String ipAddress) {
        // Integration flow
    }
}
```

## API Endpoints

### Banking AI API

- `POST /api/banking-ai/query`: Process a user query with full integration

### Fraud Detection API

- `POST /api/fraud/process`: Process a transaction and detect potential fraud
- `GET /api/fraud/transactions/{customerId}`: Get recent transactions for a customer
- `GET /api/fraud/flagged`: Get transactions flagged for review

### Financial Advice API

- `POST /api/financial-advice/{customerId}`: Get personalized financial advice

### Compliance API

- `POST /api/compliance/validate`: Validate user input against compliance rules
- `GET /api/compliance/rules/active`: Get all active compliance rules
- `GET /api/compliance/rules/category/{category}`: Get compliance rules by category
- `POST /api/compliance/rules`: Create a new compliance rule
- `PUT /api/compliance/rules/{id}`: Update an existing compliance rule
- `DELETE /api/compliance/rules/{id}`: Delete a compliance rule

### RBAC API

- `POST /api/rbac/check-permission`: Check if a user has a specific permission
- `GET /api/rbac/roles/{roleName}/permissions`: Get all permissions for a role
- `POST /api/rbac/roles`: Create a new role
- `PUT /api/rbac/roles/{id}`: Update an existing role
- `DELETE /api/rbac/roles/{id}`: Delete a role
- `GET /api/rbac/roles`: Get all roles
- `GET /api/rbac/roles/id/{id}`: Get a role by ID
- `GET /api/rbac/roles/name/{name}`: Get a role by name

### Audit API

- `GET /api/audit/user/{userId}`: Get audit logs for a specific user
- `GET /api/audit/user/{userId}/recent`: Get recent user activity
- `GET /api/audit/failed`: Get recent failed actions
- `GET /api/audit/resource/{resourceType}/{resourceId}`: Get audit logs for a specific resource
- `GET /api/audit/timerange`: Get audit logs for a specific time period
- `GET /api/audit/ip/{ipAddress}`: Get audit logs by IP address

### Session Memory API

- `POST /api/memory/session`: Create a new session
- `GET /api/memory/session/{sessionId}`: Get a session by ID
- `GET /api/memory/session/user/{userId}`: Get all sessions for a user
- `GET /api/memory/session/user/{userId}/type/{sessionType}`: Get all sessions of a specific type for a user
- `POST /api/memory/session/{sessionId}/data`: Add data to a session
- `GET /api/memory/session/{sessionId}/data/{key}`: Get data from a session
- `DELETE /api/memory/session/{sessionId}/data/{key}`: Remove data from a session
- `DELETE /api/memory/session/{sessionId}`: Delete a session
- `DELETE /api/memory/session/user/{userId}`: Delete all sessions for a user

### Customer Profile API

- `POST /api/memory/profile`: Create a new customer profile
- `GET /api/memory/profile/{id}`: Get a customer profile by ID
- `GET /api/memory/profile/customer/{customerId}`: Get a customer profile by customer ID
- `PUT /api/memory/profile/{id}`: Update a customer profile
- `PUT /api/memory/profile/customer/{customerId}/preferences`: Update customer preferences
- `PUT /api/memory/profile/customer/{customerId}/behavioral`: Update customer behavioral data
- `GET /api/memory/profile/customer/{customerId}/similar`: Find similar customer profiles
- `DELETE /api/memory/profile/{id}`: Delete a customer profile
- `POST /api/memory/profile/customer/{customerId}/retention`: Extend the retention period for a customer profile

## Resilience Patterns

The implementation includes several resilience patterns:

1. **Circuit Breaker**: Used in services to prevent cascading failures
2. **Retry**: Implemented for transient failures in external service calls
3. **Timeout**: Applied to limit the duration of operations
4. **Fallback**: Provides alternative responses when primary operations fail
5. **Bulkhead**: Isolates failures to prevent system-wide impact

## Security Features

1. **Role-Based Access Control**: Restricts access based on user roles
2. **Compliance Validation**: Ensures all interactions comply with banking regulations
3. **Audit Logging**: Tracks all system activities for accountability
4. **Data Encryption**: Protects sensitive customer information
5. **Input Validation**: Prevents injection attacks

## Memory Management

1. **Short-Term Memory**: Uses Redis for session state with configurable TTL
2. **Long-Term Memory**: Uses PostgreSQL with PGVector for customer profiles
3. **Retention Policies**: Implements GDPR-compliant data retention
4. **Vector Embeddings**: Enables semantic search of customer profiles

## Getting Started

### Prerequisites

- Java 21 or higher
- Spring AI 1.0.0-M6
- Maven 3.8 or higher
- PostgreSQL 14 or higher with pgvector extension
- Redis 6 or higher
- OpenAI API key

### Configuration

Set the following environment variables:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/aibank
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_AI_OPENAI_API_KEY=your_openai_api_key
```

### Running the Application

```bash
mvn clean package
java -jar target/ai-bank-solution-0.0.1-SNAPSHOT.jar
```

## Deployment

The application can be deployed to any cloud provider that supports Java applications. Docker and Kubernetes configurations are provided for containerized deployment.

### Docker

```bash
docker build -t ai-bank-solution .
docker-compose up -d
```

## Conclusion

This AI Bank Solution demonstrates the integration of Spring AI with banking applications, implementing Agentic RAG, Agentic Guardrails, and Memory Components. The solution follows best practices for cloud-native applications with a focus on resilience, performance, security, and event-driven architecture.
