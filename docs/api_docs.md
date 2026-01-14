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
| GET | `/api/properties` | Get all properties for authenticated user |
| GET | `/api/properties/{id}` | Get property by ID |
| PUT | `/api/properties/{id}` | Update property |
| DELETE | `/api/properties/{id}` | Delete property (soft delete) |
| GET | `/api/properties/{propertyId}/rooms` | Get all rooms in a property |
| GET | `/api/properties/{propertyId}/tenants` | Get all tenants in a property |

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
| GET | `/api/properties/{propertyId}/floors` | Get all floors for a property |
| POST | `/api/properties/{propertyId}/floors` | Add a floor to a property |
| GET | `/api/floors/{floorId}` | Get floor by ID |
| PUT | `/api/floors/{id}` | Update floor |
| DELETE | `/api/floors/{id}` | Delete floor (soft delete) |
| POST | `/api/floors/bulk` | Bulk create floors |

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
| POST | `/api/tenants` | Add new tenant (move-in) |
| GET | `/api/tenants` | Get all active tenants |
| GET | `/api/tenants/{id}` | Get tenant by ID |
| PUT | `/api/tenants/{id}` | Update tenant info |
| DELETE | `/api/tenants/{id}` | Move out tenant |
| POST | `/api/tenants/{id}/swap` | Swap tenant (atomic move-out and move-in) |
| PUT | `/api/tenants/{id}/agreement` | Update rent agreement |
| GET | `/api/tenants/search` | Search tenants by name/phone |
| GET | `/api/rooms/{roomId}/tenant` | Get active tenant for a room |

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

#### **GET** `/api/rent/payments/search`
Search payments by date range.
- **Query Parameters:**
  - `startDate` (Date) - **Required**
  - `endDate` (Date) - **Required**

#### **POST** `/api/rent/payments/bulk`
Bulk record multiple payments.
- **Request Body (`BulkPaymentDto`):**
  - `payments` (Array) - **Required**: List of `{tenantId, payment}` objects

### Due Rent

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/rent/due/{tenantId}` | Calculate due rent for a tenant |
| GET | `/api/rent/due/report` | Due rent report for all tenants |

#### **GET** `/api/rent/due/{tenantId}`
Calculate due rent for a tenant.
- **Query Parameters:**
  - `month` (Date) - *Optional*: Defaults to current month

#### **GET** `/api/rent/due/report`
Get due rent report for all active tenants.
- **Query Parameters:**
  - `month` (Date) - *Optional*: Defaults to current month

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
- **Response:** Array of `{month, value}` trend data points

### **GET** `/api/dashboard/analytics/occupancy`
Get occupancy rate trends.
- **Query Parameters:**
  - `months` (Integer) - *Optional*: Number of months (default: 6)

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
