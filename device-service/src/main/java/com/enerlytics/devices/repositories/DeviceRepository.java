package com.enerlytics.devices.repositories;

import com.enerlytics.devices.entities.Device;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Page<Device> findAllByUserId(UUID userId, Pageable page);
}
