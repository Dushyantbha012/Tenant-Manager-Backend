package com.dushy.tenantmanage.service.impl;

import com.dushy.tenantmanage.dto.RentAgreementDto;
import com.dushy.tenantmanage.dto.TenantDto;
import com.dushy.tenantmanage.entity.RentAgreement;
import com.dushy.tenantmanage.entity.Room;
import com.dushy.tenantmanage.entity.Tenant;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.exception.InvalidOperationException;
import com.dushy.tenantmanage.exception.ResourceNotFoundException;
import com.dushy.tenantmanage.repository.RentAgreementRepository;
import com.dushy.tenantmanage.repository.RoomRepository;
import com.dushy.tenantmanage.repository.TenantRepository;
import com.dushy.tenantmanage.repository.UserRepository;
import com.dushy.tenantmanage.service.TenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of TenantService.
 * Handles tenant lifecycle: add, move-out, and swap operations.
 */
@Service
@Transactional
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final RoomRepository roomRepository;
    private final RentAgreementRepository rentAgreementRepository;
    private final UserRepository userRepository;

    public TenantServiceImpl(TenantRepository tenantRepository,
            RoomRepository roomRepository,
            RentAgreementRepository rentAgreementRepository,
            UserRepository userRepository) {
        this.tenantRepository = tenantRepository;
        this.roomRepository = roomRepository;
        this.rentAgreementRepository = rentAgreementRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Tenant addTenant(TenantDto tenantDto, Long roomId, RentAgreementDto agreementDto, Long createdById) {
        // Validate room exists
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        // Check if room is already occupied
        if (Boolean.TRUE.equals(room.getIsOccupied())) {
            throw new InvalidOperationException("Room is already occupied");
        }

        // Validate creator exists
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new ResourceNotFoundException("User", createdById));

        // Create tenant
        Tenant tenant = Tenant.builder()
                .room(room)
                .fullName(tenantDto.getFullName())
                .email(tenantDto.getEmail())
                .phone(tenantDto.getPhone())
                .idProofType(tenantDto.getIdProofType())
                .idProofNumber(tenantDto.getIdProofNumber())
                .emergencyContactName(tenantDto.getEmergencyContactName())
                .emergencyContactPhone(tenantDto.getEmergencyContactPhone())
                .moveInDate(tenantDto.getMoveInDate() != null ? tenantDto.getMoveInDate() : LocalDate.now())
                .isActive(true)
                .build();

        tenant = tenantRepository.save(tenant);

        // Mark room as occupied
        room.setIsOccupied(true);
        roomRepository.save(room);

        // Create rent agreement
        RentAgreement agreement = RentAgreement.builder()
                .tenant(tenant)
                .monthlyRentAmount(agreementDto.getMonthlyRentAmount())
                .securityDeposit(agreementDto.getSecurityDeposit())
                .startDate(agreementDto.getStartDate() != null ? agreementDto.getStartDate() : LocalDate.now())
                .paymentDueDay(agreementDto.getPaymentDueDay() != null ? agreementDto.getPaymentDueDay() : 1)
                .isActive(true)
                .createdBy(createdBy)
                .build();

        rentAgreementRepository.save(agreement);

        return tenant;
    }

    @Override
    public Tenant moveOutTenant(Long tenantId) {
        // Get tenant
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        // Check if already inactive
        if (Boolean.FALSE.equals(tenant.getIsActive())) {
            throw new InvalidOperationException("Tenant is already inactive");
        }

        // Mark tenant as inactive
        tenant.setIsActive(false);
        tenant.setMoveOutDate(LocalDate.now());
        tenantRepository.save(tenant);

        // Free up the room
        Room room = tenant.getRoom();
        room.setIsOccupied(false);
        roomRepository.save(room);

        // Close active rent agreement
        rentAgreementRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .ifPresent(agreement -> {
                    agreement.setIsActive(false);
                    agreement.setEndDate(LocalDate.now());
                    rentAgreementRepository.save(agreement);
                });

        return tenant;
    }

    @Override
    public Tenant swapTenant(Long oldTenantId, TenantDto newTenantDto, RentAgreementDto agreementDto,
            Long createdById) {
        // Get old tenant
        Tenant oldTenant = tenantRepository.findById(oldTenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", oldTenantId));

        // Get the room before moving out
        Room room = oldTenant.getRoom();

        // Move out old tenant (this frees the room)
        moveOutTenant(oldTenantId);

        // Add new tenant to the same room
        return addTenant(newTenantDto, room.getId(), agreementDto, createdById);
    }

    @Override
    @Transactional(readOnly = true)
    public Tenant getTenantById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tenant> getActiveTenants() {
        return tenantRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> getActiveTenantByRoom(Long roomId) {
        return tenantRepository.findByRoomIdAndIsActiveTrue(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> getTenantByPhone(String phone) {
        return tenantRepository.findByPhone(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tenant> getTenantsByProperty(Long propertyId) {
        return tenantRepository.findByIsActiveTrueAndRoomFloorPropertyId(propertyId);
    }

    @Override
    public Tenant updateTenant(Long id, TenantDto tenantDto) {
        Tenant tenant = getTenantById(id);
        tenant.setFullName(tenantDto.getFullName());
        tenant.setEmail(tenantDto.getEmail());
        tenant.setPhone(tenantDto.getPhone());
        tenant.setIdProofType(tenantDto.getIdProofType());
        tenant.setIdProofNumber(tenantDto.getIdProofNumber());
        tenant.setEmergencyContactName(tenantDto.getEmergencyContactName());
        tenant.setEmergencyContactPhone(tenantDto.getEmergencyContactPhone());
        return tenantRepository.save(tenant);
    }

    @Override
    public RentAgreement updateAgreement(Long tenantId, RentAgreementDto agreementDto) {
        RentAgreement agreement = rentAgreementRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Active RentAgreement for tenant", tenantId));

        agreement.setMonthlyRentAmount(agreementDto.getMonthlyRentAmount());
        agreement.setSecurityDeposit(agreementDto.getSecurityDeposit());
        if (agreementDto.getPaymentDueDay() != null) {
            agreement.setPaymentDueDay(agreementDto.getPaymentDueDay());
        }
        return rentAgreementRepository.save(agreement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tenant> searchTenants(String query, Long propertyId) {
        if (propertyId != null) {
            return tenantRepository.searchByPropertyId(query, propertyId);
        }
        return tenantRepository.findByFullNameContainingIgnoreCaseOrPhoneContaining(query, query);
    }
}
