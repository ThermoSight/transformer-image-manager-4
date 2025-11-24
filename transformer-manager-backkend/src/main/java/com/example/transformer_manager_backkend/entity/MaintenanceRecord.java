package com.example.transformer_manager_backkend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_records")
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to transformer and inspection/image used for this maintenance record
    @ManyToOne
    @JoinColumn(name = "transformer_record_id", nullable = false)
    private TransformerRecord transformerRecord;

    @ManyToOne
    @JoinColumn(name = "inspection_id")
    private Inspection inspection; // Optional: maintenance can stem from an inspection

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image; // Thermal image associated with this record

    @ManyToOne
    @JoinColumn(name = "annotation_id")
    private Annotation annotation; // Chosen / validated annotation set

    // System generated metadata
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Engineer editable fields
    private String inspectorName; // Free-text or derived from authenticated user

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransformerStatus status = TransformerStatus.OK;

    private Double voltage; // Example electrical reading
    private Double current; // Example electrical reading

    @Column(columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(columnDefinition = "TEXT")
    private String correctiveActions; // Actions performed / planned

    @Column(columnDefinition = "TEXT")
    private String additionalRemarks;

    private Boolean finalized = false;
    private LocalDateTime finalizedAt;

    @Version
    private Long version;

    // Enum for transformer status in maintenance context
    public enum TransformerStatus {
        OK,
        NEEDS_MAINTENANCE,
        URGENT_ATTENTION
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TransformerRecord getTransformerRecord() { return transformerRecord; }
    public void setTransformerRecord(TransformerRecord transformerRecord) { this.transformerRecord = transformerRecord; }

    public Inspection getInspection() { return inspection; }
    public void setInspection(Inspection inspection) { this.inspection = inspection; }

    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }

    public Annotation getAnnotation() { return annotation; }
    public void setAnnotation(Annotation annotation) { this.annotation = annotation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }

    public TransformerStatus getStatus() { return status; }
    public void setStatus(TransformerStatus status) { this.status = status; }

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

    public Boolean getFinalized() { return finalized; }
    public void setFinalized(Boolean finalized) { this.finalized = finalized; }

    public LocalDateTime getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(LocalDateTime finalizedAt) { this.finalizedAt = finalizedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
