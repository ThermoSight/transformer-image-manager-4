package com.example.transformer_manager_backkend.controller;

import com.example.transformer_manager_backkend.entity.MaintenanceRecord;
import com.example.transformer_manager_backkend.service.MaintenanceRecordService;
import com.example.transformer_manager_backkend.dto.MaintenanceRecordFormDTO;
import com.example.transformer_manager_backkend.dto.MaintenanceRecordUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-records")
@CrossOrigin(origins = "http://localhost:3000")
public class MaintenanceRecordController {

    private final MaintenanceRecordService maintenanceRecordService;

    public MaintenanceRecordController(MaintenanceRecordService maintenanceRecordService) {
        this.maintenanceRecordService = maintenanceRecordService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<MaintenanceRecord> createRecord(@RequestParam Long transformerRecordId,
                                                          @RequestParam(required = false) Long inspectionId,
                                                          @RequestParam(required = false) Long imageId,
                                                          @RequestParam(required = false) Long annotationId) {
        return ResponseEntity.ok(
                maintenanceRecordService.createRecord(transformerRecordId, inspectionId, imageId, annotationId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<MaintenanceRecord> updateRecord(@PathVariable Long id,
                                                          @RequestParam(required = false) String inspectorName,
                                                          @RequestParam(required = false) MaintenanceRecord.TransformerStatus status,
                                                          @RequestParam(required = false) Double voltage,
                                                          @RequestParam(required = false) Double current,
                                                          @RequestParam(required = false) String recommendedAction,
                                                          @RequestParam(required = false) String correctiveActions,
                                                          @RequestParam(required = false) String additionalRemarks,
                                                          @RequestParam(required = false) Long annotationId) {
        return ResponseEntity.ok(
                maintenanceRecordService.updateRecord(id, inspectorName, status, voltage, current, recommendedAction, correctiveActions, additionalRemarks, annotationId));
    }

        // JSON body update variant (cleaner for frontend)
        @PutMapping("/{id}/json")
        @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
        public ResponseEntity<MaintenanceRecord> updateRecordJson(@PathVariable Long id,
                                      @RequestBody MaintenanceRecordUpdateRequest body) {
        return ResponseEntity.ok(
            maintenanceRecordService.updateRecord(id,
                body.getInspectorName(),
                body.getStatus(),
                body.getVoltage(),
                body.getCurrent(),
                body.getRecommendedAction(),
                body.getCorrectiveActions(),
                body.getAdditionalRemarks(),
                body.getAnnotationId()));
        }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<MaintenanceRecord> finalizeRecord(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceRecordService.finalizeRecord(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<MaintenanceRecord> getRecord(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceRecordService.getRecord(id));
    }

    @GetMapping("/{id}/form")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<MaintenanceRecordFormDTO> getRecordForm(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceRecordService.buildFormDTO(id));
    }

    @GetMapping("/transformer/{transformerRecordId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<MaintenanceRecord>> getRecordsByTransformer(@PathVariable Long transformerRecordId) {
        return ResponseEntity.ok(maintenanceRecordService.getRecordsByTransformer(transformerRecordId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        maintenanceRecordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }
}
