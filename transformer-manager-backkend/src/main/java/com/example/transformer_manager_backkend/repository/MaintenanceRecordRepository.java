package com.example.transformer_manager_backkend.repository;

import com.example.transformer_manager_backkend.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    List<MaintenanceRecord> findByTransformerRecordId(Long transformerRecordId);
}
