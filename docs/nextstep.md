# Next Steps for Tenant Management System

Based on the `overview.md` and the completed Repository layer, here are the immediate next steps to progress the application.

## 1. Service Layer Implementation (Business Logic)
This is the core logic layer that bridges Repositories and Controllers.

### 1.1 UserService
- **Goal**: Handle Authentication and User Management.
- **Tasks**:
  - Implement `registerUser(UserDto)`: Create Owner or Assistant.
  - Implement `authenticate(String email, String password)`: Verify credentials.
  - Implement `assignPropertyAccess`: Grant assistants access to specific properties.

### 1.2 PropertyService
- **Goal**: Manage Property hierarchy (Property -> Floor -> Room).
- **Tasks**:
  - `createProperty`: Create a new property for an owner.
  - `addFloor`: Add floors to a property.
  - `addRoom`: Add rooms to a floor.
  - `getPropertiesByOwner`: List properties for the dashboard.

### 1.3 TenantService
- **Goal**: Manage Tenant Lifecycle.
- **Tasks**:
  - `addTenant`: Assign a tenant to a room (Mark room occupied).
  - `moveOutTenant`: Mark tenant inactive, close agreement, free up room.
  - `swapTenant`: Atomic operation to move out old tenant and add new one.

### 1.4 RentService
- **Goal**: Financial Operations.
- **Tasks**:
  - `createRentAgreement`: Generate agreement when tenant is added.
  - `recordPayment`: Save `RentPayment` and update logs.
  - `calculateDueRent`: Dynamic calculation (`Expected - Paid`) for reports.

## 2. Security Layer (Spring Security + JWT)
Before exposing APIs, we need a secure environment.

### 2.1 Configuration
- **Tasks**:
  - Implement `JwtUtils`: Generate and validate tokens.
  - Implement `JwtAuthenticationFilter`: Intercept requests to check headers.
  - Configure `SecurityFilterChain`: Allow public access to `/auth/**` and secure others.

## 3. Controller Layer (REST APIs)
Expose the Services to the frontend.

### 3.1 AuthController
- `POST /api/auth/register`
- `POST /api/auth/login`

### 3.2 PropertyController
- `POST /api/properties` (Create)
- `GET /api/properties` (List)
- `GET /api/properties/{id}/floors` (View structure)

### 3.3 TenantController
- `POST /api/tenants` (Onboard)
- `DELETE /api/tenants/{id}` (Move out - Soft delete)

### 3.4 RentController
- `POST /api/rent/payment` (Record payment)
- `GET /api/rent/due` (Get due report)

## 4. Verification Checkpoints
- **Database Connectivity**: Ensure `application.yml` connects to NeonDB.
- **Unit Tests**: Add tests for `RentService` (calculation logic) and `TenantService` (lifecycle).
