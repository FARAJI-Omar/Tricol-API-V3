# Tricol - Supplier Order and Stock Management System

## Project Context

Tricol is a company specializing in the design and manufacturing of professional clothing. As part of the digitalization of its internal processes, following the implementation of the supplier management module, the management team has decided to develop a complementary module dedicated to supplier order management and FIFO stock valuation.

This module represents a strategic step towards a complete supply chain and production management system, enabling rigorous tracking of raw materials and equipment.

## Features

### Supplier Management
- Complete CRUD operations (Create, Read, Update, Delete)
- Search and filter suppliers
- Managed information: company name, full address, contact person, email, phone, city, ICE

### Product Management
- Complete CRUD operations for products
- View available stock per product
- Alert system for minimum stock thresholds
- Managed information: product reference, name, description, unit price, category, current stock, reorder point, unit of measure

### Supplier Orders Management
- Create new supplier orders
- Modify or cancel existing orders
- View list of all orders
- View specific order details
- Filter by supplier, status, period
- Associate orders with suppliers and product lists
- Automatic calculation of total order amount
- Order statuses: PENDING, VALIDATED, DELIVERED, CANCELLED
- Order reception handling

### Stock Management (FIFO Method)
- **Inbound movements**: Automatic recording upon supplier order reception
- **Outbound movements**: Stock consumption using FIFO (First In, First Out - oldest lots used first)
- **Lot traceability**: Each stock entry is identified by:
  - Unique lot number
  - Entry date
  - Quantity
  - Unit purchase price
  - Original supplier order
- **Stock consultation**:
  - Available stock per product
  - Stock valuation (FIFO)
  - Movement history
- **Alerts**: Notification when product stock falls below minimum threshold

### Exit Slip Management
Exit slips allow managing stock outflows to production workshops in a traceable manner. An exit slip is a document that formalizes the withdrawal of products (raw materials, supplies) from central stock to deliver them to a specific workshop.

**Features**:
- Create exit slip for workshop
- Add multiple products with quantities
- Validation automatically triggers FIFO outflows
- Cancellation possible (only for drafts)
- Consultation and filtering

**Exit slip includes**:
- Unique slip number
- Exit date
- Destination workshop
- List of products with quantities
- Exit reason (PRODUCTION, MAINTENANCE, OTHER)
- Status (DRAFT, VALIDATED, CANCELLED)

**Validation**: Automatically triggers FIFO stock movements
**Traceability**: Link between exit slip and stock movements

## Business Rules (FIFO Method)

### 1. Order Reception
When validating a supplier order reception, automatic creation of stock lots with unique number and entry date.

### 2. Stock Outflow
The FIFO algorithm must:
- Identify the oldest lots first
- Consume quantities in chronological order
- Handle cases where an outflow requires multiple lots
- Update remaining quantities for each lot

### 3. Valuation
Stock value calculation must use purchase prices from lots according to their entry order.

### 4. Traceability
Each movement must be recorded with references to the concerned lots.

## REST API Endpoints

Base URL: `http://localhost:8081/tricol/api/v2`

### Authentication

#### Custom JWT Authentication
- `POST /auth/register` - Register new user (syncs to MySQL + Keycloak)
- `POST /auth/login` - Login with custom JWT token

#### Keycloak OAuth2 Authentication
- `POST /keycloak/auth/login` - Login with Keycloak (OAuth2)
- `POST /keycloak/auth/refresh` - Refresh Keycloak access token

**Note**: Both authentication methods work on all protected endpoints. Users can choose either login method after registration.

### User Management (Admin Only)
- `POST /users/{userId}/roles/{roleId}` - Assign role to user (syncs to Keycloak)
- `POST /users/permissions` - Add permission to user (syncs to Keycloak)
- `DELETE /users/{userId}/permissions/{permissionId}` - Remove permission (syncs to Keycloak)

### Suppliers
- `GET /suppliers` - List all suppliers
- `GET /suppliers/{id}` - Get supplier details
- `POST /suppliers/create` - Create new supplier
- `PUT /suppliers/{id}` - Update supplier
- `DELETE /suppliers/{id}` - Delete supplier

### Products
- `GET /products` - List all products
- `GET /products/{id}` - Get product details
- `POST /products/create` - Create new product
- `PUT /products/{id}` - Update product
- `DELETE /products/{id}` - Delete product
- `GET /products/stock/{id}` - View product stock
- `GET /products/lowstock` - Get products below minimum threshold

### Supplier Orders
- `GET /orders` - List all orders
- `GET /orders/{id}` - Get order details
- `POST /orders/create` - Create new order
- `PUT /orders/{id}` - Update order status
- `POST /orders/{id}/receive` - Receive order (generates stock entries with FIFO lots)

### Exit Slips
- `GET /exit-slips` - List all exit slips (supports query params: ?status=DRAFT&workshop=Assembly)
- `GET /exit-slips/{id}` - Get exit slip details
- `POST /exit-slips` - Create exit slip (DRAFT status)
- `POST /exit-slips/{id}/validate` - Validate exit slip (triggers FIFO outflows)
- `POST /exit-slips/{id}/cancel` - Cancel draft exit slip

## Technical Stack

- **Framework**: Spring Boot 3.5.7
- **Database**: MySQL with JPA/Hibernate
- **Security**: Spring Security + Dual JWT (Custom + OAuth2)
- **Authentication**: Custom JWT + Keycloak OAuth2
- **Validation**: Jakarta Validation
- **Mapping**: MapStruct
- **API Documentation**: Springdoc OpenAPI (Swagger)
- **Build Tool**: Maven
- **Java Version**: 17+

## Project Structure

```
src/main/java/com/example/tricol/tricolspringbootrestapi/
├── controller/          # REST controllers
├── dto/
│   ├── request/        # Request DTOs
│   └── response/       # Response DTOs
├── enums/              # Enumerations
├── mapper/             # MapStruct mappers
├── model/              # JPA entities
├── repository/         # JPA repositories
└── service/
    └── impl/           # Service implementations
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL database
- Keycloak 23+ (for OAuth2 authentication)

### Configuration

#### Database Configuration
Update `application.properties` with your database configuration:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tricol_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

#### Keycloak Configuration
1. **Install and start Keycloak** (port 8080 by default)
2. **Create realm**: `tricol-realm` (or your preferred name)
3. **Create client**: `tricol-api-client`
   - Client authentication: ON
   - Direct access grants: ON (required for password grant)
4. **Get client secret** from Credentials tab
5. **Create realm roles** matching your RoleEnum (ADMIN, USER, MANAGER)
6. **Create client roles** matching your PermissionEnum (PRODUCT_READ, ORDER_CREATE, etc.)

Update `application.properties`:
```properties
# Keycloak OAuth2 Configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/tricol-realm
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/tricol-realm/protocol/openid-connect/certs

# Keycloak Admin Client
keycloak.server-url=http://localhost:8080
keycloak.realm=tricol
keycloak.client-id=tricol-api-client
keycloak.client-secret=your-client-secret
keycloak.admin-username=
keycloak.admin-password=
keycloak.enabled=true
```

### Build and Run

#### Local Development
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8081/tricol/api/v2`

## Authentication & Authorization

### Dual JWT Authentication System

The application supports **two authentication methods** that work simultaneously:

#### 1. Custom JWT Authentication
- Traditional username/password authentication
- JWT tokens signed with HMAC-SHA256
- Tokens stored and validated by the application
- Best for: Internal applications, mobile apps

#### 2. Keycloak OAuth2 Authentication  
- Industry-standard OAuth2/OIDC authentication
- JWT tokens signed with RS256 (RSA)
- Centralized authentication via Keycloak
- Best for: SSO, enterprise integration, multiple applications

### How It Works

```
┌─────────────────────────────────────┐
│   Single Registration Endpoint      │
│   POST /auth/register               │
│   → Saves to MySQL (BCrypt)         │
│   → Syncs to Keycloak (Argon2)     │
└─────────────────────────────────────┘
              ↓
    ┌─────────┴─────────┐
    ↓                   ↓
┌──────────────┐  ┌─────────────────┐
│ Custom JWT   │  │ Keycloak OAuth2 │
│ /auth/login  │  │ /keycloak/auth/ │
└──────────────┘  └─────────────────┘
    ↓                   ↓
    └─────────┬─────────┘
              ↓
    ┌──────────────────┐
    │ Protected APIs   │
    │ (Both tokens OK) │
    └──────────────────┘
```

### Authentication Flow

1. **Register**: User registers once via `/auth/register`
   - User created in MySQL database
   - User automatically synced to Keycloak with password
   - Role assignment triggers Keycloak sync

2. **Login**: User can choose either method
   - **Custom JWT**: `POST /auth/login` → Returns custom JWT
   - **Keycloak**: `POST /keycloak/auth/login` → Returns Keycloak JWT

3. **Access APIs**: Both tokens work on all endpoints
   - Add token to `Authorization: Bearer <token>` header
   - Unified filter detects token type automatically
   - Validates and authenticates accordingly

### Permission-Based Authorization

The system uses **fine-grained permission-based access control**:

- **Roles**: ADMIN, USER, MANAGER (assigned by admin)
- **Permissions**: Granular authorities (e.g., PRODUCT_READ, ORDER_CREATE)
- **Role Permissions**: Each role has default permissions
- **User Permissions**: Admin can add/remove individual permissions
- **Auto-sync**: All permission changes sync to Keycloak automatically

### Keycloak Synchronization

Automatic synchronization occurs on:
- ✅ User registration (with password)
- ✅ Role assignment
- ✅ Permission add/remove
- ✅ Permission activate/deactivate

Keycloak tracks:
- User accounts with passwords
- Realm roles (ADMIN, USER, MANAGER)
- Client roles (permissions)
- User-role mappings
- User-permission mappings

#### Docker Deployment (Recommended)

The easiest way to run the application with all dependencies:

```bash
# Build and start all services (MySQL + API)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

Access the application:
- API: http://localhost:8080/tricol/api/v2
- Swagger UI: http://localhost:8080/tricol/api/v2/swagger-ui.html

**For detailed Docker instructions**, see [DOCKER.md](DOCKER.md) which includes:
- Building custom Docker images
- Pushing to Docker Hub
- Manual container management
- Production deployment tips
- Troubleshooting guide

## Key Implementation Details

### FIFO Stock Algorithm
The system implements First In, First Out (FIFO) stock management:
- Stock lots are tracked with entry dates
- When consuming stock, the oldest lots are used first
- Each lot maintains an `availableQuantity` field
- Stock movements are linked to specific lots for complete traceability

### Exit Slip Workflow
1. Create exit slip in DRAFT status
2. Add products with requested quantities
3. Validate slip to trigger automatic FIFO consumption
4. System validates sufficient stock availability
5. Stock movements are created and linked to the exit slip
6. Product stock levels are updated

### Stock Valuation
Stock is valued using the FIFO method, ensuring accurate financial reporting based on actual purchase costs in chronological order.

## Security Features

- **Dual JWT Authentication**: Custom + Keycloak OAuth2
- **Password Hashing**: BCrypt (MySQL) + Argon2 (Keycloak)
- **Token-based Authorization**: Stateless authentication
- **Permission-based Access Control**: Fine-grained authorities
- **Automatic Sync**: Role/permission changes sync to Keycloak
- **Audit Logging**: All authentication and authorization events logged
- **CORS Configuration**: Configurable cross-origin access
- **Method-level Security**: `@PreAuthorize` annotations on endpoints

## API Documentation

Access Swagger UI at: `http://localhost:8081/tricol/api/v2/swagger-ui.html`

The API documentation includes:
- All available endpoints
- Request/response schemas
- Authentication requirements
- Try-it-out functionality

## Production Deployment Checklist

### Keycloak Configuration
- [ ] Change default admin password
- [ ] Use strong client secrets
- [ ] Enable HTTPS/TLS
- [ ] Configure proper token lifespans
- [ ] Set up persistent database (not H2)
- [ ] Configure backup strategy
- [ ] Create all required roles and permissions

### Application Configuration  
- [ ] Use environment variables for secrets
- [ ] Enable HTTPS
- [ ] Configure CORS for production domains
- [ ] Set up proper logging
- [ ] Configure database connection pooling
- [ ] Enable actuator endpoints (with security)
- [ ] Set up monitoring and alerting

### Security
- [ ] Review and test all permission mappings
- [ ] Test both authentication methods
- [ ] Verify token expiration handling
- [ ] Test role-based access control
- [ ] Audit log review process
- [ ] Implement rate limiting
- [ ] Set up WAF (Web Application Firewall)