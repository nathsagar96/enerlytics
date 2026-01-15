package com.enerlytics.devices.entities;

import com.enerlytics.devices.enums.DeviceType;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private DeviceType type;

    @Column(name = "location", columnDefinition = "TEXT")
    private String location;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
}
