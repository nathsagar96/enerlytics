package com.enerlytics.devices.repositories;

import com.enerlytics.devices.entities.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Page<Device> findAllByUserId(Long userId, Pageable pageable);
}
