package com.example.transformer_manager_backkend.dto;

import com.example.transformer_manager_backkend.entity.MaintenanceRecord;

public class MaintenanceRecordUpdateRequest {
    private String inspectorName;
    private MaintenanceRecord.TransformerStatus status;
    private Double voltage;
    private Double current;
    private String recommendedAction;
    private String correctiveActions;
    private String additionalRemarks;
    private Long annotationId; // optional to switch annotation

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
    public Long getAnnotationId() { return annotationId; }
    public void setAnnotationId(Long annotationId) { this.annotationId = annotationId; }
}
