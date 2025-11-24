package com.example.transformer_manager_backkend.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.example.transformer_manager_backkend.entity.MaintenanceRecord;

public class MaintenanceRecordFormDTO {
    private Long recordId;
    private Boolean finalized;
    private LocalDateTime finalizedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transformer metadata
    private Long transformerId;
    private String transformerName;
    private String locationName;
    private Double locationLat;
    private Double locationLng;
    private Double capacity;
    private String poleNo;
    private String transformerType;

    // Inspection metadata
    private Long inspectionId;
    private LocalDateTime inspectionTimestamp;
    private String inspectorSourceName; // From inspection conductor
    private String inspectorSourceRole; // ADMIN / USER
    private String inspectionNotes;

    // Image metadata
    private Long imageId;
    private String imagePath;
    private String imageType;

    // Engineer editable fields
    private String inspectorName; // manual override / entry
    private MaintenanceRecord.TransformerStatus status;
    private Double voltage;
    private Double current;
    private String recommendedAction;
    private String correctiveActions;
    private String additionalRemarks;

    // Anomalies
    private List<AnomalyDTO> anomalies = new ArrayList<>();

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Boolean getFinalized() { return finalized; }
    public void setFinalized(Boolean finalized) { this.finalized = finalized; }
    public LocalDateTime getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(LocalDateTime finalizedAt) { this.finalizedAt = finalizedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getTransformerId() { return transformerId; }
    public void setTransformerId(Long transformerId) { this.transformerId = transformerId; }
    public String getTransformerName() { return transformerName; }
    public void setTransformerName(String transformerName) { this.transformerName = transformerName; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public Double getLocationLat() { return locationLat; }
    public void setLocationLat(Double locationLat) { this.locationLat = locationLat; }
    public Double getLocationLng() { return locationLng; }
    public void setLocationLng(Double locationLng) { this.locationLng = locationLng; }
    public Double getCapacity() { return capacity; }
    public void setCapacity(Double capacity) { this.capacity = capacity; }
    public String getPoleNo() { return poleNo; }
    public void setPoleNo(String poleNo) { this.poleNo = poleNo; }
    public String getTransformerType() { return transformerType; }
    public void setTransformerType(String transformerType) { this.transformerType = transformerType; }

    public Long getInspectionId() { return inspectionId; }
    public void setInspectionId(Long inspectionId) { this.inspectionId = inspectionId; }
    public LocalDateTime getInspectionTimestamp() { return inspectionTimestamp; }
    public void setInspectionTimestamp(LocalDateTime inspectionTimestamp) { this.inspectionTimestamp = inspectionTimestamp; }
    public String getInspectorSourceName() { return inspectorSourceName; }
    public void setInspectorSourceName(String inspectorSourceName) { this.inspectorSourceName = inspectorSourceName; }
    public String getInspectorSourceRole() { return inspectorSourceRole; }
    public void setInspectorSourceRole(String inspectorSourceRole) { this.inspectorSourceRole = inspectorSourceRole; }
    public String getInspectionNotes() { return inspectionNotes; }
    public void setInspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; }

    public Long getImageId() { return imageId; }
    public void setImageId(Long imageId) { this.imageId = imageId; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }

    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }
    public MaintenanceRecord.TransformerStatus getStatus() { return status; }
    public void setStatus(MaintenanceRecord.TransformerStatus status) { this.status = status; }
    public Double getVoltage() { return voltage; }
    public void setVoltage(Double voltage) { this.voltage = voltage; }
    public Double getCurrent() { return current; }
    public void setCurrent(Double current) { this.current = current; }
    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    public String getCorrectiveActions() { return correctiveActions; }
    public void setCorrectiveActions(String correctiveActions) { this.correctiveActions = correctiveActions; }
    public String getAdditionalRemarks() { return additionalRemarks; }
    public void setAdditionalRemarks(String additionalRemarks) { this.additionalRemarks = additionalRemarks; }

    public List<AnomalyDTO> getAnomalies() { return anomalies; }
    public void setAnomalies(List<AnomalyDTO> anomalies) { this.anomalies = anomalies; }
}
