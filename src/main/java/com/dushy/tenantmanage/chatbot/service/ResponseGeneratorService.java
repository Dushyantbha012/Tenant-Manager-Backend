package com.dushy.tenantmanage.chatbot.service;

import com.dushy.tenantmanage.entity.Floor;
import com.dushy.tenantmanage.entity.Properties;
import com.dushy.tenantmanage.entity.Room;
import com.dushy.tenantmanage.entity.Tenant;
import com.dushy.tenantmanage.service.PropertyService;
import com.dushy.tenantmanage.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResponseGeneratorService {

    private final GroqApiService groqApiService;
    private final PropertyService propertyService;
    private final TenantService tenantService;

    /**
     * Generate human-friendly response from action result
     *
     * @param intent       Detected intent
     * @param actionResult Result from ActionExecutorService
     * @return Formatted response text
     */
    public String generateResponse(String intent, Map<String, Object> actionResult) {
        boolean success = (boolean) actionResult.getOrDefault("success", false);

        if (!success) {
            return (String) actionResult.getOrDefault("message",
                    actionResult.getOrDefault("error", "Sorry, I couldn't process that request."));
        }

        return switch (intent) {
            case "view_properties" -> formatPropertyList(actionResult);
            case "view_property_details" -> formatPropertyDetails(actionResult);
            case "view_rooms" -> formatRoomsList(actionResult);
            case "view_tenants" -> formatTenantsList(actionResult);
            case "view_tenant_details", "search_tenant" -> formatTenantDetails(actionResult);
            case "view_due_rent" -> formatDueRentReport(actionResult);
            case "view_summary" -> formatDashboardSummary(actionResult);
            case "help" -> formatHelp();
            default -> "I processed your request successfully.";
        };
    }

    private String formatPropertyList(Map<String, Object> actionResult) {
        @SuppressWarnings("unchecked")
        List<Properties> properties = (List<Properties>) actionResult.get("data");

        if (properties.isEmpty()) {
            return "You don't have any properties registered yet. Would you like to add one?";
        }

        StringBuilder response = new StringBuilder();
        response.append(String.format("📊 Your Properties (%d total):\n\n", properties.size()));

        for (int i = 0; i < properties.size(); i++) {
            Properties prop = properties.get(i);
            List<Floor> floors = propertyService.getFloorsByProperty(prop.getId());
            List<Room> allRooms = propertyService.getRoomsByProperty(prop.getId());

            long occupiedRooms = allRooms.stream()
                    .filter(room -> room.getIsOccupied())
                    .count();

            response.append(String.format("%d. 🏢 %s | %s\n", i + 1, prop.getName(), prop.getCity()));
            response.append(String.format("   • %d Floors | %d Rooms\n", floors.size(), allRooms.size()));
            response.append(String.format("   • Occupied: %d | Vacant: %d\n\n", occupiedRooms,
                    allRooms.size() - occupiedRooms));
        }

        response.append("💡 Say \"details of [property name]\" to see more.");
        return response.toString();
    }

    private String formatPropertyDetails(Map<String, Object> actionResult) {
        Properties property = (Properties) actionResult.get("data");
        if (property == null) {
            return "Property not found.";
        }

        StringBuilder response = new StringBuilder();
        response.append(String.format("🏢 Property Details: %s\n\n", property.getName()));
        response.append(String.format("📍 Address: %s, %s, %s\n", property.getAddress(), property.getCity(),
                property.getState()));

        List<Floor> floors = propertyService.getFloorsByProperty(property.getId());
        List<Room> allRooms = propertyService.getRoomsByProperty(property.getId());

        response.append(String.format("🏗️ Floors: %d\n", floors.size()));

        long occupiedRooms = allRooms.stream()
                .filter(room -> room.getIsOccupied())
                .count();

        response.append(String.format("\n🏠 Total Rooms: %d\n", allRooms.size()));
        response.append(String.format("✅ Occupied: %d\n", occupiedRooms));
        response.append(String.format("🔓 Vacant: %d\n", allRooms.size() - occupiedRooms));

        return response.toString();
    }

    private String formatRoomsList(Map<String, Object> actionResult) {
        @SuppressWarnings("unchecked")
        List<Room> rooms = (List<Room>) actionResult.get("data");

        if (rooms.isEmpty()) {
            return "No rooms found.";
        }

        StringBuilder response = new StringBuilder();
        Properties property = (Properties) actionResult.get("property");
        if (property != null) {
            response.append(String.format("🏠 Rooms in %s:\n\n", property.getName()));
        } else {
            response.append(String.format("🏠 All Rooms (%d total):\n\n", rooms.size()));
        }

        long occupiedCount = rooms.stream().filter(room -> room.getIsOccupied()).count();
        long vacantCount = rooms.size() - occupiedCount;

        for (Room room : rooms) {
            String status = room.getIsOccupied() ? "✅" : "🔓";
            String occupantInfo;

            if (room.getIsOccupied()) {
                // Get tenant for this room
                Optional<Tenant> tenantOpt = tenantService.getActiveTenantByRoom(room.getId());
                if (tenantOpt.isPresent()) {
                    Tenant tenant = tenantOpt.get();
                    occupantInfo = String.format(" - %s", tenant.getFullName());
                } else {
                    occupantInfo = " - Occupied";
                }
            } else {
                occupantInfo = " - VACANT";
            }

            response.append(String.format("%s Room %s%s\n", status, room.getRoomNumber(), occupantInfo));
        }

        response.append(String.format("\n📊 Summary: %d Occupied | %d Vacant\n", occupiedCount, vacantCount));
        return response.toString();
    }

    private String formatTenantsList(Map<String, Object> actionResult) {
        @SuppressWarnings("unchecked")
        List<Tenant> tenants = (List<Tenant>) actionResult.get("data");

        if (tenants.isEmpty()) {
            return "You don't have any active tenants.";
        }

        StringBuilder response = new StringBuilder();
        response.append(String.format("👥 Your Tenants (%d active):\n\n", tenants.size()));

        for (int i = 0; i < Math.min(10, tenants.size()); i++) {
            Tenant tenant = tenants.get(i);
            response.append(String.format("%d. %s\n", i + 1, tenant.getFullName()));
            response.append(String.format("   📞 %s\n", tenant.getPhone()));
            if (tenant.getRoom() != null) {
                response.append(String.format("   🏠 Room %s\n", tenant.getRoom().getRoomNumber()));
            }
            response.append("\n");
        }

        if (tenants.size() > 10) {
            response.append(String.format("... and %d more tenants\n", tenants.size() - 10));
        }

        return response.toString();
    }

    private String formatTenantDetails(Map<String, Object> actionResult) {
        Object data = actionResult.get("data");

        if (data instanceof Tenant) {
            Tenant tenant = (Tenant) data;
            StringBuilder response = new StringBuilder();
            response.append(String.format("👤 Tenant Details: %s\n\n", tenant.getFullName()));
            response.append(String.format("📞 Phone: %s\n", tenant.getPhone()));

            if (tenant.getEmail() != null) {
                response.append(String.format("📧 Email: %s\n", tenant.getEmail()));
            }

            if (tenant.getRoom() != null) {
                response.append(String.format("\n🏠 Room: %s\n", tenant.getRoom().getRoomNumber()));
            }

            if (tenant.getMoveInDate() != null) {
                response.append(String.format("📅 Move-in Date: %s\n", tenant.getMoveInDate()));
            }

            return response.toString();
        } else if (data instanceof List) {
            @SuppressWarnings("unchecked")
            List<Tenant> tenants = (List<Tenant>) data;

            if (tenants.isEmpty()) {
                return "No tenants found matching your search.";
            }

            StringBuilder response = new StringBuilder();
            response.append(String.format("🔍 Found %d tenant(s):\n\n", tenants.size()));

            for (int i = 0; i < tenants.size(); i++) {
                Tenant tenant = tenants.get(i);
                response.append(String.format("%d. %s\n", i + 1, tenant.getFullName()));
                response.append(String.format("   📞 %s\n", tenant.getPhone()));
                if (tenant.getRoom() != null) {
                    response.append(String.format("   🏠 Room %s\n", tenant.getRoom().getRoomNumber()));
                }
                response.append("\n");
            }

            return response.toString();
        }

        return "Tenant information not available.";
    }

    private String formatDueRentReport(Map<String, Object> actionResult) {
        // Use LLM to generate a nice formatted report
        Object data = actionResult.get("data");
        String dataStr = data != null ? data.toString() : "No due rent data available";

        String prompt = String.format("""
                Format the following due rent report in a user-friendly way.
                Use emojis, clear formatting, and highlight important information.
                Keep it concise and actionable.

                Data: %s

                Return the formatted report.
                """, dataStr);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content",
                        "You are a helpful assistant that formats data into user-friendly reports."),
                Map.of("role", "user", "content", prompt));

        return groqApiService.chat(messages);
    }

    private String formatDashboardSummary(Map<String, Object> actionResult) {
        Object data = actionResult.get("data");
        String dataStr = data != null ? data.toString() : "No dashboard data available";

        String prompt = String.format("""
                Format the following dashboard summary in a user-friendly way.
                Use emojis, progress bars (using unicode), and clear sections.
                Make it visually appealing and easy to scan.

                Data: %s

                Return the formatted dashboard summary.
                """, dataStr);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content",
                        "You are a helpful assistant that formats data into user-friendly summaries."),
                Map.of("role", "user", "content", prompt));

        return groqApiService.chat(messages);
    }

    private String formatHelp() {
        return """
                👋 Welcome! I can help you manage your properties and tenants.

                Here's what I can do:

                🏢 Properties:
                • "Show my properties" - View all your properties
                • "Details of [property name]" - Get property details
                • "Rooms in [property name]" - See all rooms

                👥 Tenants:
                • "List all tenants" - View all active tenants
                • "Details of [tenant name]" - Get tenant details
                • "Find tenant [name/phone]" - Search for a tenant

                💰 Payments:
                • "Who hasn't paid?" - See pending rent
                • "Payment history" - View payment records

                📊 Dashboard:
                • "Dashboard" or "Summary" - View portfolio overview

                Just ask me anything in natural language!
                """;
    }

    /**
     * Generate quick action suggestions based on intent
     */
    public List<String> generateQuickActions(String intent) {
        return switch (intent) {
            case "view_properties" -> List.of("Show vacant rooms", "Dashboard", "Who hasn't paid?");
            case "view_tenants" -> List.of("Search tenant", "Who hasn't paid?", "Show properties");
            case "view_due_rent" -> List.of("Show all tenants", "Dashboard", "List properties");
            case "help" -> List.of("Show my properties", "List all tenants", "Dashboard");
            default -> new ArrayList<>();
        };
    }
}
