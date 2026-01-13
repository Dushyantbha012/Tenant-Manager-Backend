3.1 User Table
Purpose: Store all system users (owners and assistants)
Table Name: users
Columns:

id (BIGSERIAL PRIMARY KEY) - Unique identifier
email (VARCHAR(255) UNIQUE NOT NULL) - Login email
password_hash (VARCHAR(255) NOT NULL) - Encrypted password
full_name (VARCHAR(255) NOT NULL) - User's name
phone (VARCHAR(20)) - Contact number
user_type (VARCHAR(20) NOT NULL) - 'OWNER' or 'ASSISTANT'
is_active (BOOLEAN DEFAULT TRUE) - Soft delete flag
created_at (TIMESTAMP DEFAULT NOW())
updated_at (TIMESTAMP DEFAULT NOW())

Indexes:

email (unique index for fast login lookup)
user_type (filter by role)

Notes:

Password should be hashed using BCrypt (Spring Security handles this)
user_type at user level indicates if they're an owner or assistant globally
Actual permissions are property-specific via PropertyAccess


3.2 Property Table
Purpose: Store property information
Table Name: properties
Columns:

id (BIGSERIAL PRIMARY KEY)
owner_id (BIGINT NOT NULL) - References users.id
name (VARCHAR(255) NOT NULL) - Property name
address (TEXT NOT NULL) - Full address
city (VARCHAR(100))
state (VARCHAR(100))
postal_code (VARCHAR(20))
country (VARCHAR(100))
total_floors (INTEGER) - Number of floors
is_active (BOOLEAN DEFAULT TRUE)
created_at (TIMESTAMP DEFAULT NOW())
updated_at (TIMESTAMP DEFAULT NOW())

Foreign Keys:

owner_id → users(id) ON DELETE RESTRICT

Indexes:

owner_id (find all properties by owner)
is_active (filter active properties)

Notes:

Owner cannot be deleted if they have properties (RESTRICT)
total_floors is informational; actual floors are in floors table


3.3 PropertyAccess Table (Critical for Role-Based Access)
Purpose: Grant property-specific access to assistants
Table Name: property_access
Columns:

id (BIGSERIAL PRIMARY KEY)
property_id (BIGINT NOT NULL) - References properties.id
user_id (BIGINT NOT NULL) - References users.id
access_level (VARCHAR(20) NOT NULL) - 'READ', 'WRITE', 'ADMIN'
granted_by (BIGINT NOT NULL) - References users.id (who granted)
granted_at (TIMESTAMP DEFAULT NOW())
is_active (BOOLEAN DEFAULT TRUE)
revoked_at (TIMESTAMP) - When access was revoked

Foreign Keys:

property_id → properties(id) ON DELETE CASCADE
user_id → users(id) ON DELETE CASCADE
granted_by → users(id) ON DELETE RESTRICT

Unique Constraint:

(property_id, user_id) - One access record per user per property (when active)

Indexes:

user_id (find all properties accessible to user)
property_id (find all users with access to property)

Notes:

Owners automatically have ADMIN access (enforced in code)
When revoking access, set is_active = FALSE and revoked_at
Never delete this table's records (audit trail)


3.4 Floor Table
Purpose: Represent floors within a property
Table Name: floors
Columns:

id (BIGSERIAL PRIMARY KEY)
property_id (BIGINT NOT NULL) - References properties.id
floor_number (INTEGER NOT NULL) - Floor number (0 for ground, -1 for basement)
floor_name (VARCHAR(100)) - Optional name (e.g., "Ground Floor", "Terrace")
is_active (BOOLEAN DEFAULT TRUE)
created_at (TIMESTAMP DEFAULT NOW())

Foreign Keys:

property_id → properties(id) ON DELETE CASCADE

Unique Constraint:

(property_id, floor_number) - Floor numbers must be unique per property

Indexes:

property_id (get all floors for a property)


3.5 Room Table
Purpose: Individual rentable units
Table Name: rooms
Columns:

id (BIGSERIAL PRIMARY KEY)
floor_id (BIGINT NOT NULL) - References floors.id
room_number (VARCHAR(50) NOT NULL) - Room identifier (e.g., "101", "A-1")
room_type (VARCHAR(50)) - 'SINGLE', 'DOUBLE', 'SUITE', etc.
size_sqft (DECIMAL(10,2)) - Optional size
is_occupied (BOOLEAN DEFAULT FALSE) - Current occupancy status
is_active (BOOLEAN DEFAULT TRUE)
created_at (TIMESTAMP DEFAULT NOW())
updated_at (TIMESTAMP DEFAULT NOW())

Foreign Keys:

floor_id → floors(id) ON DELETE CASCADE

Unique Constraint:

(floor_id, room_number) - Room numbers unique per floor

Indexes:

floor_id (get all rooms on a floor)
is_occupied (quickly find vacant rooms)

Notes:

is_occupied is a denormalized flag for performance
Updated when tenant moves in/out
Can be recalculated from active tenant records


3.6 Tenant Table
Purpose: Store tenant information and occupancy history
Table Name: tenants
Columns:

id (BIGSERIAL PRIMARY KEY)
room_id (BIGINT NOT NULL) - References rooms.id
full_name (VARCHAR(255) NOT NULL)
email (VARCHAR(255))
phone (VARCHAR(20) NOT NULL)
id_proof_type (VARCHAR(50)) - 'AADHAAR', 'PAN', 'PASSPORT'
id_proof_number (VARCHAR(100))
emergency_contact_name (VARCHAR(255))
emergency_contact_phone (VARCHAR(20))
move_in_date (DATE NOT NULL)
move_out_date (DATE) - NULL if currently active
is_active (BOOLEAN DEFAULT TRUE) - Current occupancy status
created_at (TIMESTAMP DEFAULT NOW())
updated_at (TIMESTAMP DEFAULT NOW())

Foreign Keys:

room_id → rooms(id) ON DELETE RESTRICT

Indexes:

room_id (find tenant in a room)
is_active (filter active tenants)
move_out_date (find tenants who moved out)

Business Rules:

Only one active tenant per room (enforced: is_active = TRUE)
When tenant moves out: set is_active = FALSE, set move_out_date
Never delete tenant records


3.7 RentAgreement Table (Most Critical)
Purpose: Store rent agreements with full history
Table Name: rent_agreements
Columns:

id (BIGSERIAL PRIMARY KEY)
tenant_id (BIGINT NOT NULL) - References tenants.id
monthly_rent_amount (DECIMAL(10,2) NOT NULL) - Rent amount
security_deposit (DECIMAL(10,2)) - One-time deposit
start_date (DATE NOT NULL) - Agreement start
end_date (DATE) - NULL if currently active
is_active (BOOLEAN DEFAULT TRUE) - Current agreement status
payment_due_day (INTEGER DEFAULT 1) - Day of month rent is due
created_at (TIMESTAMP DEFAULT NOW())
created_by (BIGINT NOT NULL) - References users.id

Foreign Keys:

tenant_id → tenants(id) ON DELETE RESTRICT
created_by → users(id) ON DELETE RESTRICT

Indexes:

tenant_id (find all agreements for a tenant)
is_active (find current active agreements)
start_date, end_date (date range queries)

Business Rules:

Only one active agreement per tenant at a time
To change rent: close old agreement (is_active = FALSE, set end_date), create new one
Never update monthly_rent_amount directly

Notes:

This design preserves complete rent history
Critical for accounting and legal compliance
end_date = NULL means agreement is ongoing


3.8 RentPayment Table (Immutable)
Purpose: Record every rent payment
Table Name: rent_payments
Columns:

id (BIGSERIAL PRIMARY KEY)
rent_agreement_id (BIGINT NOT NULL) - References rent_agreements.id
tenant_id (BIGINT NOT NULL) - References tenants.id (denormalized for queries)
amount_paid (DECIMAL(10,2) NOT NULL) - Payment amount
payment_date (DATE NOT NULL) - When payment was made
payment_for_month (DATE NOT NULL) - Month/year this payment covers (e.g., '2025-01-01')
payment_mode (VARCHAR(50)) - 'CASH', 'UPI', 'BANK_TRANSFER', 'CHEQUE'
transaction_reference (VARCHAR(255)) - UPI ID, cheque number, etc.
notes (TEXT) - Additional notes
recorded_by (BIGINT NOT NULL) - References users.id
recorded_at (TIMESTAMP DEFAULT NOW())

Foreign Keys:

rent_agreement_id → rent_agreements(id) ON DELETE RESTRICT
tenant_id → tenants(id) ON DELETE RESTRICT
recorded_by → users(id) ON DELETE RESTRICT

Indexes:

tenant_id (payment history per tenant)
rent_agreement_id (payments per agreement)
payment_for_month (find payments by month)
payment_date (chronological queries)

Business Rules:

Payments are NEVER updated or deleted
Corrections are made with reversal entries
Partial payments are allowed (multiple records for same month)

Notes:

tenant_id is denormalized from agreement for faster queries
payment_for_month should be first day of month (e.g., '2025-01-01' for January 2025)


4. Advanced Considerations
4.1 Audit Trail Table (Optional but Recommended)
Table Name: audit_logs
Purpose: Track all critical changes
Columns:

id (BIGSERIAL PRIMARY KEY)
entity_type (VARCHAR(50)) - 'TENANT', 'RENT_AGREEMENT', 'PAYMENT'
entity_id (BIGINT) - ID of affected entity
action (VARCHAR(50)) - 'CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE'
changed_by (BIGINT) - References users.id
changes (JSONB) - Before/after values
timestamp (TIMESTAMP DEFAULT NOW())

4.2 Notification Table (Future Feature)
Table Name: notifications
For tracking rent due reminders, payment confirmations, etc.

5. Database Constraints Summary
Primary Keys: All tables use BIGSERIAL auto-incrementing IDs
Foreign Keys: All relationships use proper FK constraints with appropriate ON DELETE actions:

CASCADE: When parent is deleted, children are deleted (floors, rooms)
RESTRICT: Prevents deletion if references exist (tenants, payments)

Unique Constraints:

users.email
(property_id, floor_number) in floors
(floor_id, room_number) in rooms
(property_id, user_id) in property_access

Check Constraints (implement these):

monthly_rent_amount > 0
amount_paid > 0
payment_due_day BETWEEN 1 AND 28
move_out_date > move_in_date (when not NULL)


6. Indexing Strategy
High-Priority Indexes (Create These First):

Foreign key columns - PostgreSQL doesn't auto-index FKs
Boolean flags - is_active, is_occupied
Date columns - move_in_date, payment_date, start_date
User lookup - users.email

Composite Indexes (For Complex Queries):

(property_id, is_active) on rooms - Find available rooms
(tenant_id, payment_for_month) on rent_payments - Payment history
(tenant_id, is_active) on rent_agreements - Current rent