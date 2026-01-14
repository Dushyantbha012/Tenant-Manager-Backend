# Tenant Management System - API Documentation

This document provides a detailed reference for all the REST APIs available in the Tenant Management System.

## Base URL
All API requests should be made to:
`http://localhost:8080` (Default for local development)

## Authentication
The system supports two authentication methods:
1. **Email/Password**: Traditional signup/login flow
2. **Google OAuth2**: Sign in with Google for seamless access

Most APIs require a Bearer Token for authentication.
Include the token in the `Authorization` header of your requests:
`Authorization: Bearer <your_jwt_token>`

### Property-Based Access Control
The system implements granular access control for all property-related resources (Floors, Rooms, Tenants, Payments).
- **Owners**: Full access to all their properties and related data.
- **Assistants**: Access is granted per-property via the Access Management APIs.
- **Access Levels**:
  - `READ`: Can view property details, tenants, and payments.
  - `WRITE`: Can create/update floors, rooms, tenants, and record payments.
  - `ADMIN`: Full property management (excluding property deletion).

### Common Error Responses
- `401 Unauthorized`: Missing or invalid Bearer token.
- `403 Forbidden`: User does not have the required access level for the target property.
- `404 Not Found`: Resource (Property, Tenant, etc.) does not exist.

---

## 1. Authentication APIs

### **POST** `/api/auth/signup`
Register a new user (Owner or Assistant).
- **Request Body (`SignupRequest`):**
  - `email` (String) - **Required**: Unique email address.
  - `password` (String) - **Required**: Minimum 6 characters.
  - `fullName` (String) - **Required**: Full name of the user.
  - `phone` (String) - *Optional*: Contact number.
  - `userType` (Enum) - *Optional*: `OWNER` or `ASSISTANT` (Default: `OWNER`).
- **Response:** `200 OK` with Success Message.

### **POST** `/api/auth/login`
Authenticate user and receive a JWT token.
- **Request Body (`LoginRequest`):**
  - `email` (String) - **Required**
  - `password` (String) - **Required**
- **Response (`JwtResponse`):**
  - `token`, `type`, `id`, `email`, `fullName`, `userType`.

### **POST** `/api/auth/logout`
Log out the user. (Stateless; client should discard the token).
- **Response:** `200 OK` with Success Message.

### **GET** `/oauth2/authorization/google`
Initiate Google OAuth2 login flow.
- **Behavior**: Redirects user to Google's login page.
- **On Success**: Redirects to `http://localhost:3000/oauth2/redirect?token=<jwt_token>`

---

## 2. Property Management APIs

### Properties

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/properties` | Create a new property |
| GET | `/api/properties` | Get all owned and accessible properties |
| GET | `/api/properties/{id}` | Get property by ID (Read Access required) |
| PUT | `/api/properties/{id}` | Update property (**Owner only**) |
| DELETE | `/api/properties/{id}` | Delete property (**Owner only**) |
| GET | `/api/properties/{propertyId}/rooms` | Get rooms (Read Access required) |
| GET | `/api/properties/{propertyId}/tenants` | Get tenants (Read Access required) |

#### **POST** `/api/properties`
Create a new property.
- **Request Body (`PropertyDto`):**
  - `name`, `address`, `city`, `state`, `postalCode`, `country` (all **Required**)
  - `totalFloors` (Integer) - **Required**
- **Response:** Created `Properties` entity.

#### **PUT** `/api/properties/{id}`
Update a property.
- **Request Body:** Same as POST.

---

### Floors

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/properties/{propertyId}/floors` | Get floors (Read Access required) |
| POST | `/api/properties/{propertyId}/floors` | Add a floor (Write Access required) |
| GET | `/api/floors/{floorId}` | Get floor (Read Access required) |
| PUT | `/api/floors/{id}` | Update floor (Write Access required) |
| DELETE | `/api/floors/{id}` | Delete floor (Write Access required) |
| POST | `/api/floors/bulk` | Bulk create (Write Access required) |

#### **POST** `/api/properties/{propertyId}/floors`
Add a floor to a property.
- **Request Body (`FloorDto`):**
  - `floorNumber` (Integer) - **Required**
  - `floorName` (String) - *Optional*

#### **POST** `/api/floors/bulk`
Bulk create multiple floors.
- **Request Body (`BulkFloorDto`):**
  - `propertyId` (Long) - **Required**
  - `floors` (Array of FloorDto) - **Required**

---

### Rooms

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/floors/{floorId}/rooms` | Get all rooms on a floor |
| GET | `/api/floors/{floorId}/rooms/available` | Get available rooms on a floor |
| POST | `/api/floors/{floorId}/rooms` | Add a room to a floor |
| GET | `/api/rooms/{roomId}` | Get room by ID |
| PUT | `/api/rooms/{id}` | Update room |
| DELETE | `/api/rooms/{id}` | Delete room (soft delete) |
| GET | `/api/rooms/vacant` | Get all vacant rooms |
| POST | `/api/rooms/bulk` | Bulk create rooms |

#### **POST** `/api/floors/{floorId}/rooms`
Add a room to a floor.
- **Request Body (`RoomDto`):**
  - `roomNumber` (String) - **Required**: e.g., "101", "A-1"
  - `roomType` (Enum) - *Optional*: `SINGLE`, `DOUBLE`, `STUDIO`, etc.
  - `sizeSqft` (Decimal) - *Optional*

#### **POST** `/api/rooms/bulk`
Bulk create multiple rooms.
- **Request Body (`BulkRoomDto`):**
  - `floorId` (Long) - **Required**
  - `rooms` (Array of RoomDto) - **Required**

---

## 3. Tenant Management APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/tenants` | Move-in tenant (Write Access required) |
| GET | `/api/tenants` | Get active tenants from accessible properties |
| GET | `/api/tenants/{id}` | Get tenant (Read Access required) |
| PUT | `/api/tenants/{id}` | Update tenant (Write Access required) |
| DELETE | `/api/tenants/{id}` | Move out (Write Access required) |
| POST | `/api/tenants/{id}/swap` | Swap tenant (Write Access required) |
| PUT | `/api/tenants/{id}/agreement` | Update agreement (Write Access required) |
| GET | `/api/tenants/search` | Search accessible tenants |
| GET | `/api/rooms/{roomId}/tenant` | Get room tenant (Read Access required) |

### **POST** `/api/tenants`
Move-in a new tenant.
- **Request Body (`CreateTenantRequest`):**
  - `tenant` (Object) - **Required**:
    - `fullName`, `phone`, `moveInDate` - **Required**
    - `email`, `idProofType`, `idProofNumber`, `emergencyContactName`, `emergencyContactPhone` - *Optional*
  - `roomId` (Long) - **Required**
  - `agreement` (Object) - **Required**:
    - `monthlyRentAmount`, `startDate` - **Required**
    - `securityDeposit`, `paymentDueDay` - *Optional*

### **PUT** `/api/tenants/{id}`
Update tenant information.
- **Request Body (`TenantDto`):** Same fields as tenant object above.

### **PUT** `/api/tenants/{id}/agreement`
Update the active rent agreement for a tenant.
- **Request Body (`RentAgreementDto`):**
  - `monthlyRentAmount` (Decimal) - **Required**
  - `securityDeposit` (Decimal) - *Optional*
  - `paymentDueDay` (Integer) - *Optional*

### **GET** `/api/tenants/search`
Search tenants by name or phone.
- **Query Parameters:**
  - `query` (String) - **Required**: Search term
  - `propertyId` (Long) - *Optional*: Filter by property

---

## 4. Rent Management APIs

### Payments

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/rent/payments/tenant/{tenantId}` | Record a rent payment |
| GET | `/api/rent/payments/tenant/{tenantId}` | Get payment history for tenant |
| GET | `/api/rent/payments/month/{month}` | Get all payments for a month |
| GET | `/api/rent/payments/search` | Search payments by date range |
| POST | `/api/rent/payments/bulk` | Bulk record payments |

#### **POST** `/api/rent/payments/tenant/{tenantId}`
Record a rent payment.
- **Request Body (`RentPaymentDto`):**
  - `amountPaid` (Decimal) - **Required**
  - `paymentDate` (Date) - **Required** (yyyy-MM-dd)
  - `paymentForMonth` (Date) - **Required**: First day of the month
  - `paymentMode` (Enum) - *Optional*: `CASH`, `UPI`, `CARD`, etc.
  - `transactionReference`, `notes` - *Optional*
- **Response:** Created `RentPayment` entity.
- **Note**: Requires Write Access to the tenant's property.

#### **GET** `/api/rent/payments/month/{month}`
Get payments for a specific month.
- **Filtering**: Returns only payments for properties the user has access to.

#### **GET** `/api/rent/payments/search`
Search payments by date range.
- **Query Parameters:**
  - `startDate`, `endDate` (Date) - **Required**
- **Filtering**: Returns only payments for properties the user has access to.

#### **POST** `/api/rent/payments/bulk`
Bulk record multiple payments.
- **Note**: Requires Write Access to all involved properties. Returns 403 if any property is inaccessible.

### Due Rent

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/rent/due/{tenantId}` | Calculate due rent for a tenant |
| GET | `/api/rent/due/report` | Due rent report for all tenants |

#### **GET** `/api/rent/due/{tenantId}`
Calculate due rent for a tenant.
- **Query Parameters:**
  - `month` (Date) - *Optional*
- **Response (`DueRentDto`):**
  - `tenantId`, `tenantName`, `roomNumber`, `propertyId`, `propertyName`
  - `expectedAmount`, `paidAmount`, `dueAmount`, `month`

#### **GET** `/api/rent/due/report`
Get due rent report for all active tenants in accessible properties.
- **Query Parameters:**
  - `month` (Date) - *Optional*
- **Response**: Array of `DueRentDto`.

### Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/rent/summary/property/{propertyId}` | Rent collection summary for property |

### Agreements

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/rent/agreements/{tenantId}` | Get active rent agreement for tenant |

---

## 5. User & Access APIs

### Profile Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/me` | Get current user profile |
| PUT | `/api/users/me` | Update current user profile |
| PUT | `/api/users/me/password` | Change password |

#### **PUT** `/api/users/me`
Update user profile.
- **Request Body (`UserDto`):**
  - `fullName` (String) - *Optional*
  - `phone` (String) - *Optional*

#### **PUT** `/api/users/me/password`
Change password.
- **Request Body (`UpdatePasswordDto`):**
  - `currentPassword` (String) - **Required**
  - `newPassword` (String) - **Required**: Minimum 6 characters

### User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| GET | `/api/users/assistants` | Get all assistants |

### Access Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/access` | Grant property access to a user |
| GET | `/api/users/{userId}/access` | Get property access for a user |
| DELETE | `/api/users/access/{accessId}` | Revoke property access |

#### **POST** `/api/users/access`
Grant property access to a user.
- **Request Body (`PropertyAccessDto`):**
  - `propertyId` (Long) - **Required**
  - `userId` (Long) - **Required**
  - `accessLevel` (Enum) - **Required**: `READ`, `WRITE`, or `ADMIN`

---

## 6. Dashboard & Analytics APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/summary` | Overall dashboard summary |
| GET | `/api/dashboard/summary/property/{propertyId}` | Property-specific summary |
| GET | `/api/dashboard/analytics/rent` | Rent collection trends |
| GET | `/api/dashboard/analytics/occupancy` | Occupancy trends |

### **GET** `/api/dashboard/summary`
Get overall dashboard statistics.
- **Response (`DashboardSummaryDto`):**
  - `totalProperties`, `totalRooms`, `occupiedRooms`, `vacantRooms`
  - `totalTenants`, `totalRentExpected`, `totalRentCollected`, `occupancyRate`

### **GET** `/api/dashboard/analytics/rent`
Get rent collection trends.
- **Query Parameters:**
  - `months` (Integer) - *Optional*: Number of months (default: 6)
- **Filtering**: Trends are calculated based on all properties the user has access to.
- **Response**: Array of `{month, value}` trend data points

### **GET** `/api/dashboard/analytics/occupancy`
Get occupancy rate trends.
- **Query Parameters:**
  - `months` (Integer) - *Optional*: Number of months (default: 6)
- **Filtering**: Trends are calculated based on all properties the user has access to.

---

# Getting Started Guide

1. **Sign Up**: Register using `/api/auth/signup` OR use Google OAuth
2. **Login**: Authenticate via `/api/auth/login` and save the token
3. **Create Property**: Add your building using `POST /api/properties`
4. **Add Floors**: Use `POST /api/properties/{id}/floors` or bulk create
5. **Add Rooms**: Use `POST /api/floors/{id}/rooms` or bulk create
6. **Move-in Tenant**: Use `POST /api/tenants` to add tenants
7. **Record Rent**: Use `POST /api/rent/payments/tenant/{id}` monthly
8. **Monitor**: Use `/api/dashboard/summary` for quick stats
9. **Check Dues**: Use `GET /api/rent/due/report` for pending payments
10. **Move-out**: Use `DELETE /api/tenants/{id}` when tenant leaves
