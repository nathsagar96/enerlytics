package com.enerlytics.alerts.repositories;

import com.enerlytics.alerts.entities.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {}
