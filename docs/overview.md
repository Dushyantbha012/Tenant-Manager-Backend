Below is a clear, detailed, presentation-ready project document you can use to explain your system to anyone (developer, interviewer, manager, or mentor) and make them understand every dynamic and design decision.

You can literally read this out or share it as a doc/PDF.

🏢 Tenant Management System
(Spring Boot + React | Multi-Owner | Role-Based | Rent Tracking)
1. Project Overview

The Tenant Management System is a full-stack web application designed to help property owners efficiently manage their rental properties, tenants, rent collection, and delegated access to assistants or family members.

The system supports:

Multiple owners

Multiple properties per owner

Floors and rooms within properties

One tenant per room

Rent tracking with historical changes

Secure authentication and property-based role access

This application solves real-world problems faced by landlords who manage multiple properties and tenants and need structured, auditable, and scalable rent management.

2. Problem Statement

In real life:

Owners manage properties using notebooks or spreadsheets

Rent changes over time but history is lost

Assistants help manage properties but access is informal

No reliable way to track:

Due rent

Payment history

Tenant movement

This system digitizes and standardizes the entire workflow.

3. User Roles & Access Model
3.1 User Types

There are two global user roles:

Role	Description
Owner	Property owner with full control
Assistant	Family member or helper delegated by owner
3.2 Property-Based Access Control (Key Design Decision)

Access is NOT global, it is property-specific.

A user can have different permissions for different properties.

Property Access Levels:

READ → View tenant & rent details

WRITE → Add/update tenants and rent

ADMIN → Manage property access & structure

📌 Example:

An assistant may manage Property A but have no access to Property B.

This mimics real SaaS permission systems.

4. Property Structure Hierarchy

The system models properties exactly as they exist in the real world.

Owner
 └── Property
      └── Floor
           └── Room
                └── Tenant

Explanation:

Owner owns multiple properties

Each property has multiple floors

Each floor has multiple rooms

Each room can have only one active tenant

This hierarchy allows:

Easy navigation

Scalable reporting

Accurate ownership boundaries

5. Tenant Lifecycle Management
5.1 Tenant Addition

When a tenant is added:

Tenant is assigned to a specific room

A rent agreement is created

Room becomes occupied

5.2 Tenant Deletion (Move-Out)

❌ Tenants are never hard deleted

Instead:

Tenant is marked inactive

Move-out date is recorded

Rent agreement is closed

✅ Why?

Payment history must remain

Legal and accounting traceability

Enables future reports

5.3 Tenant Swapping

Tenant swapping is a state transition, not a shortcut.

Steps:

Close existing tenant’s rent agreement

Mark room as vacant

Assign new tenant

Create new rent agreement

This ensures zero data loss.

6. Rent Management System (Core Feature)
6.1 Rent Model

Rent is assigned per tenant

Rent can change over time

Each rent change creates a new rent agreement

This design ensures:

Full rent history

No overwriting of financial data

6.2 Rent Agreement

Each agreement contains:

Tenant

Monthly rent amount

Start date

End date (nullable if active)

📌 Rule:

Old rent agreements are closed, never updated.

6.3 Rent Payment Tracking

Each payment records:

Tenant

Rent agreement

Month & year

Amount paid

Payment date

Payment mode (cash, UPI, bank)

6.4 Due Rent Calculation (Important Design Choice)

The system does not store isDue = true.

Instead:

Due rent is calculated dynamically

Formula:

Expected Rent − Paid Amount


Benefits:

Supports partial payments

Prevents inconsistency

Accurate reports at any time

7. Authentication & Security
7.1 Authentication

JWT-based authentication

Secure password hashing

Stateless backend

Flow:

User logs in

Backend issues JWT

React sends JWT with each request

Backend validates token

7.2 Authorization

Authorization checks:

User role (Owner / Assistant)

Property-level permissions

Requested operation

This ensures:

No cross-property data leaks

Secure delegation of responsibility

8. REST API Architecture

The backend exposes clean, RESTful APIs designed for React consumption.

API Principles:

Resource-based URLs

Proper HTTP verbs

DTO-based communication

Validation at API boundary

Example API Domains:

Authentication

Property management

Floor & room management

Tenant lifecycle

Rent agreements

Rent payments

Reports (due rent, history)

9. Backend Architecture (Spring Boot)

The backend follows industry-standard layered architecture:

Controller → Service → Repository → Database

Responsibilities:

Controller: HTTP handling

Service: Business rules & validation

Repository: Database access only

Benefits:

Clean separation of concerns

Easy testing

Easy future scaling

10. Database Strategy (PostgreSQL)
Key Principles:

Strong relational modeling

Foreign key enforcement

History preservation

Transaction safety

Data Safety Rules:

Never delete financial data

Always keep historical records

Use flags and end dates

11. Reporting & Insights

The system supports:

Due rent report

Payment history per tenant

Property-wise revenue

Tenant occupancy history

These reports are generated dynamically from stored data, ensuring accurac