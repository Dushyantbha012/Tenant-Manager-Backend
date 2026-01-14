GET  /api/properties/{propertyId}          - Get single property details
GET  /api/properties/{propertyId}/floors   - Get all floors of a property
GET  /api/floors/{floorId}                 - Get single floor details
GET  /api/floors/{floorId}/rooms           - Get all rooms on a floor
GET  /api/rooms/{roomId}                   - Get single room details
GET  /api/properties/{propertyId}/rooms    - Get ALL rooms in a property (flat list)
GET  /api/tenants                          - Get all tenants (with filters)
GET  /api/tenants/{id}                     - Get single tenant details
GET  /api/rooms/{roomId}/tenant            - Get tenant in a specific room
GET  /api/properties/{propertyId}/tenants  - Get all tenants in a property
GET  /api/rent/payments/tenant/{tenantId}  - Get payment history for a tenant
GET  /api/rent/due/report                  - Get dues report (mentioned but not documented)
GET  /api/rent/due/tenant/{tenantId}       - Get outstanding dues for a tenant
GET  /api/rent/summary/property/{id}       - Monthly rent collection summary
PUT  /api/properties/{id}                  - Update property details
PUT  /api/floors/{id}                      - Update floor details
PUT  /api/rooms/{id}                       - Update room details
PUT  /api/tenants/{id}                     - Update tenant information
PUT  /api/tenants/{id}/agreement           - Update rent agreement

DELETE /api/properties/{id}                - Delete property
DELETE /api/floors/{id}                    - Delete floor
DELETE /api/rooms/{id}                     - Delete room
GET  /api/dashboard/summary
GET  /api/dashboard/property/{propertyId}/summary  - Property-specific stats
GET  /api/analytics/rent-trends?months=6           - Rent collection trends
GET  /api/analytics/occupancy-trends?months=6      - Occupancy over time
GET  /api/rooms/vacant                            - Get all vacant rooms
GET  /api/rooms/search?status=VACANT&propertyId=1&roomType=SINGLE
GET  /api/tenants/search?query=john&propertyId=1
GET  /api/rent/payments/search?startDate=2024-01-01&endDate=2024-12-31
GET  /api/users/me                        - Get current user profile
PUT  /api/users/me                        - Update profile
PUT  /api/users/me/password               - Change password
GET  /api/users/assistants                - Get all assistants (for owners)
GET  /api/users/access                    - Get properties user has access to
DELETE /api/users/access/{accessId}       - Revoke access
POST /api/floors/bulk                     - Create multiple floors at once
POST /api/rooms/bulk                      - Create multiple rooms at once
POST /api/rent/payments/bulk              - Record multiple payments
POST   /api/tenants/{id}/documents        - Upload ID proof, agreement copy
GET    /api/tenants/{id}/documents        - List tenant documents
DELETE /api/documents/{documentId}        - Delete document
GET    /api/documents/{documentId}/download