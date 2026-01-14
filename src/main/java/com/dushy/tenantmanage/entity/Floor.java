package com.dushy.tenantmanage.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Table(name = "floors", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "property_id", "floor_number" })
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Properties property;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(name = "floor_name", length = 100)
    private String floorName;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
