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
Manage user registration, authentication, and session.

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
- **On Success**: After Google authentication, the user is redirected to:
  `http://localhost:3000/oauth2/redirect?token=<jwt_token>`
- **Frontend Handling**:
  1. Extract the `token` query parameter from the URL.
  2. Store it (e.g., in localStorage).
  3. Use it in the `Authorization` header for subsequent API calls.

> **Note**: Users created via OAuth2 do not have passwords and cannot use `/api/auth/login`.

---

## 2. Property Management APIs
Manage Properties, Floors, and Rooms.

### **POST** `/api/properties`
Create a new property.
- **Request Body (`PropertyDto` - All fields are Required):**
  - `name`: Building name.
  - `address`: Street address.
  - `city`, `state`, `postalCode`, `country`.
  - `totalFloors`: Total number of floors in the building.
- **Response:** Created `Properties` entity.

### **GET** `/api/properties`
Get all properties owned by the authenticated user.

### **POST** `/api/properties/{propertyId}/floors`
Add a floor to a property.
- **Request Body (`FloorDto`):**
  - `floorNumber` (Integer) - **Required**
  - `floorName` (String) - *Optional*: e.g., "Main Lobby".
- **Response:** Created `Floor` entity.

### **POST** `/api/floors/{floorId}/rooms`
Add a room to a floor.
- **Request Body (`RoomDto`):**
  - `roomNumber` (String) - **Required**: e.g., "101", "A-1".
  - `roomType` (Enum) - *Optional*: `SINGLE`, `DOUBLE`, `STUDIO`, etc.
  - `sizeSqft` (Decimal) - *Optional*: Room size in square feet.
- **Response:** Created `Room` entity.

---

## 3. Tenant Management APIs
Manage tenant lifecycle (Move-in, Move-out, Swap).

### **POST** `/api/tenants`
Move-in a new tenant.
- **Request Body (`CreateTenantRequest`):**
  - **`tenant` (Object) - Required:**
    - `fullName` (String) - **Required**
    - `phone` (String) - **Required**
    - `moveInDate` (Date) - **Required** (yyyy-mm-dd)
    - `email`, `idProofType`, `idProofNumber`, `emergencyContactName`, `emergencyContactPhone` - *Optional*.
  - **`roomId` (Long) - Required**
  - **`agreement` (Object) - Required:**
    - `monthlyRentAmount` (Decimal) - **Required**
    - `startDate` (Date) - **Required** (yyyy-mm-dd)
    - `securityDeposit` (Decimal) - *Optional*.
    - `paymentDueDay` (Integer) - *Optional* (Default: 1).

### **DELETE** `/api/tenants/{id}`
Move out a tenant.

### **POST** `/api/tenants/{id}/swap`
Replace an existing tenant with a new one atomically.
- **Request Body (`SwapTenantRequest`):**
  - `newTenant` (Object) - **Required** (Same as `tenant` above).
  - `agreement` (Object) - **Required** (Same as `agreement` above).

---

## 4. Rent Management APIs
Record payments and track dues.

### **POST** `/api/rent/payments/tenant/{tenantId}`
Record a rent payment.
- **Request Body (`RentPaymentDto`):**
  - `amountPaid` (Decimal) - **Required**
  - `paymentDate` (Date) - **Required**
  - `paymentForMonth` (Date) - **Required**: The month this payment covers (usually 1st of month).
  - `paymentMode` (Enum) - *Optional*: `CASH`, `UPI`, `CARD`, etc.
  - `transactionReference` (String) - *Optional*.
  - `notes` (String) - *Optional*.

---

## 5. User & Access APIs
Manage property access for assistants.

### **POST** `/api/users/access`
Grant an assistant access to a property.
- **Request Body (`PropertyAccessDto` - All fields are Required):**
  - `propertyId`, `userId`, `accessLevel` (`VIEW` or `EDIT`).

---

# How to Use: Getting Started Guide

Follow these steps to set up and manage your first property:

1. **Sign Up**: Register as an `OWNER` using `/api/auth/signup` OR use Google OAuth via `/oauth2/authorization/google`.
2. **Login**: Authenticate via `/api/auth/login` (or extract token from OAuth redirect) and save the `token`.
3. **Create Property**: Add your building using `POST /api/properties`.
4. **Add Floors**: Add floors to your property using `POST /api/properties/{id}/floors`.
5. **Add Rooms**: Add rooms to each floor using `POST /api/floors/{id}/rooms`.
6. **Move-in Tenant**: Register a tenant and assign them to a room using `POST /api/tenants`. This automatically starts their rent agreement.
7. **Record Rent**: Every month, record their payment using `POST /api/rent/payments/tenant/{id}`.
8. **Monitor Dues**: Use `GET /api/rent/due/report` to see who hasn't paid for the current month.
9. **Move-out**: When a tenant leaves, use `DELETE /api/tenants/{id}` to free up the room.
10. **(Optional) Grant Access**: If you have an assistant, register them and use `POST /api/users/access` to let them manage properties on your behalf.
