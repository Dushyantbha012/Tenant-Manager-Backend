package com.dushy.tenantmanage.chatbot.service;

import com.dushy.tenantmanage.entity.Properties;
import com.dushy.tenantmanage.entity.Room;
import com.dushy.tenantmanage.entity.Tenant;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.service.DashboardService;
import com.dushy.tenantmanage.service.PropertyService;
import com.dushy.tenantmanage.service.RentService;
import com.dushy.tenantmanage.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionExecutorService {

    private final PropertyService propertyService;
    private final TenantService tenantService;
    private final RentService rentService;
    private final DashboardService dashboardService;

    /**
     * Execute business action based on intent and entities
     *
     * @param intent   Detected intent
     * @param entities Extracted entities
     * @param user     Current user
     * @return Action result data
     */
    public Map<String, Object> executeAction(String intent, Map<String, Object> entities, User user) {
        log.info("Executing action for intent: {} with entities: {}", intent, entities);

        try {
            return switch (intent) {
                case "view_properties" -> handleViewProperties(user);
                case "view_property_details" -> handleViewPropertyDetails(entities, user);
                case "view_rooms" -> handleViewRooms(entities, user);
                case "view_tenants" -> handleViewTenants(user);
                case "view_tenant_details" -> handleViewTenantDetails(entities, user);
                case "search_tenant" -> handleSearchTenant(entities, user);
                case "view_due_rent" -> handleViewDueRent(user);
                case "view_payment_history" -> handleViewPaymentHistory(entities, user);
                case "view_summary" -> handleViewSummary(user);
                case "help" -> handleHelp();
                default -> Map.of(
                        "success", false,
                        "message", "I'm not sure how to help with that. Try asking 'help' to see what I can do.");
            };
        } catch (Exception e) {
            log.error("Error executing action for intent '{}': {}", intent, e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "error", "An error occurred while processing your request: " + e.getMessage());
        }
    }

    private Map<String, Object> handleViewProperties(User user) {
        List<Properties> properties = propertyService.getPropertiesByOwner(user.getId());
        return Map.of(
                "success", true,
                "action", "view_properties",
                "data", properties);
    }

    private Map<String, Object> handleViewPropertyDetails(Map<String, Object> entities, User user) {
        // Try to get property by ID or name
        if (entities.containsKey("property_id")) {
            Long propertyId = (Long) entities.get("property_id");
            Properties property = propertyService.getPropertyById(propertyId);
            return Map.of(
                    "success", true,
                    "action", "view_property_details",
                    "data", property);
        } else if (entities.containsKey("property_name")) {
            String propertyName = (String) entities.get("property_name");
            // Search property by name
            List<Properties> properties = propertyService.getPropertiesByOwner(user.getId());
            Properties matchedProperty = properties.stream()
                    .filter(p -> p.getName().toLowerCase().contains(propertyName.toLowerCase()))
                    .findFirst()
                    .orElse(null);

            if (matchedProperty != null) {
                return Map.of(
                        "success", true,
                        "action", "view_property_details",
                        "data", matchedProperty);
            }
        }

        return Map.of(
                "success", false,
                "message", "I couldn't find the property you're looking for. Please specify the property name or ID.");
    }

    private Map<String, Object> handleViewRooms(Map<String, Object> entities, User user) {
        if (entities.containsKey("property_id")) {
            Long propertyId = (Long) entities.get("property_id");
            List<Room> rooms = propertyService.getRoomsByProperty(propertyId);
            Properties property = propertyService.getPropertyById(propertyId);

            return Map.of(
                    "success", true,
                    "action", "view_rooms",
                    "property", property,
                    "data", rooms);
        } else if (entities.containsKey("property_name")) {
            String propertyName = (String) entities.get("property_name");
            List<Properties> properties = propertyService.getPropertiesByOwner(user.getId());
            Properties matchedProperty = properties.stream()
                    .filter(p -> p.getName().toLowerCase().contains(propertyName.toLowerCase()))
                    .findFirst()
                    .orElse(null);

            if (matchedProperty != null) {
                List<Room> rooms = propertyService.getRoomsByProperty(matchedProperty.getId());

                return Map.of(
                        "success", true,
                        "action", "view_rooms",
                        "property", matchedProperty,
                        "data", rooms);
            }
        }

        // If no property specified, show all rooms
        List<Properties> properties = propertyService.getPropertiesByOwner(user.getId());
        List<Room> allRooms = properties.stream()
                .flatMap(p -> propertyService.getRoomsByProperty(p.getId()).stream())
                .collect(Collectors.toList());

        return Map.of(
                "success", true,
                "action", "view_rooms",
                "data", allRooms);
    }

    private Map<String, Object> handleViewTenants(User user) {
        // Get all properties owned by user, then get tenants from those properties
        List<Properties> properties = propertyService.getPropertiesByOwner(user.getId());
        List<Tenant> allTenants = properties.stream()
                .flatMap(property -> tenantService.getTenantsByProperty(property.getId()).stream())
                .collect(Collectors.toList());

        return Map.of(
                "success", true,
                "action", "view_tenants",
                "data", allTenants);
    }

    private Map<String, Object> handleViewTenantDetails(Map<String, Object> entities, User user) {
        if (entities.containsKey("tenant_name")) {
            String tenantName = (String) entities.get("tenant_name");

            // Get all tenants from user's properties
            List<Properties> properties = propertyService.getPropertiesByOwner(user.getId());
            List<Tenant> allTenants = properties.stream()
                    .flatMap(property -> tenantService.getTenantsByProperty(property.getId()).stream())
                    .collect(Collectors.toList());

            Tenant matchedTenant = allTenants.stream()
                    .filter(t -> t.getFullName().toLowerCase().contains(tenantName.toLowerCase()))
                    .findFirst()
                    .orElse(null);

            if (matchedTenant != null) {
                return Map.of(
                        "success", true,
                        "action", "view_tenant_details",
                        "data", matchedTenant);
            }
        }

        return Map.of(
                "success", false,
                "message", "I couldn't find the tenant you're looking for.");
    }

    private Map<String, Object> handleSearchTenant(Map<String, Object> entities, User user) {
        // Get all tenants from user's properties
        List<Properties> properties = propertyService.getPropertiesByOwner(user.getId());
        List<Tenant> allTenants = properties.stream()
                .flatMap(property -> tenantService.getTenantsByProperty(property.getId()).stream())
                .collect(Collectors.toList());

        List<Tenant> matchedTenants = allTenants;

        if (entities.containsKey("tenant_name")) {
            String name = (String) entities.get("tenant_name");
            matchedTenants = allTenants.stream()
                    .filter(t -> t.getFullName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        } else if (entities.containsKey("phone_number")) {
            String phone = (String) entities.get("phone_number");
            matchedTenants = allTenants.stream()
                    .filter(t -> t.getPhone() != null && t.getPhone().contains(phone))
                    .collect(Collectors.toList());
        }

        return Map.of(
                "success", true,
                "action", "search_tenant",
                "data", matchedTenants);
    }

    private Map<String, Object> handleViewDueRent(User user) {
        var dueReport = rentService.getDueRentReport(LocalDate.now());
        return Map.of(
                "success", true,
                "action", "view_due_rent",
                "data", dueReport);
    }

    private Map<String, Object> handleViewPaymentHistory(Map<String, Object> entities, User user) {
        // Implementation depends on your RentService methods
        // This is a placeholder
        return Map.of(
                "success", true,
                "action", "view_payment_history",
                "message", "Payment history feature coming soon");
    }

    private Map<String, Object> handleViewSummary(User user) {
        var summary = dashboardService.getDashboardSummary(user.getId());
        return Map.of(
                "success", true,
                "action", "view_summary",
                "data", summary);
    }

    private Map<String, Object> handleHelp() {
        return Map.of(
                "success", true,
                "action", "help",
                "message", "help");
    }
}
