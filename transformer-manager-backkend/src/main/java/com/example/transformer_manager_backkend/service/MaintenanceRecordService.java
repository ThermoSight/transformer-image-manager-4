package com.example.transformer_manager_backkend.service;

import com.example.transformer_manager_backkend.entity.*;
import com.example.transformer_manager_backkend.repository.*;
import com.example.transformer_manager_backkend.dto.MaintenanceRecordFormDTO;
import com.example.transformer_manager_backkend.dto.AnomalyDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaintenanceRecordService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final TransformerRecordRepository transformerRecordRepository;
    private final InspectionRepository inspectionRepository;
    private final ImageRepository imageRepository;
    private final AnnotationRepository annotationRepository;

    public MaintenanceRecordService(MaintenanceRecordRepository maintenanceRecordRepository,
                                    TransformerRecordRepository transformerRecordRepository,
                                    InspectionRepository inspectionRepository,
                                    ImageRepository imageRepository,
                                    AnnotationRepository annotationRepository) {
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.transformerRecordRepository = transformerRecordRepository;
        this.inspectionRepository = inspectionRepository;
        this.imageRepository = imageRepository;
        this.annotationRepository = annotationRepository;
    }

    @Transactional
    public MaintenanceRecord createRecord(Long transformerRecordId,
                                          Long inspectionId,
                                          Long imageId,
                                          Long annotationId) {
        TransformerRecord transformerRecord = transformerRecordRepository.findById(transformerRecordId)
                .orElseThrow(() -> new RuntimeException("Transformer record not found"));

        MaintenanceRecord record = new MaintenanceRecord();
        record.setTransformerRecord(transformerRecord);

        if (inspectionId != null) {
            Inspection inspection = inspectionRepository.findById(inspectionId)
                    .orElseThrow(() -> new RuntimeException("Inspection not found"));
            record.setInspection(inspection);

            // Auto-attach annotation & image if not explicitly provided
            if (annotationId == null) {
                // Find annotations for this inspection (latest updated)
                List<Annotation> inspectionAnnotations = annotationRepository.findByInspectionId(inspectionId);
                if (inspectionAnnotations != null && !inspectionAnnotations.isEmpty()) {
                    Annotation latest = inspectionAnnotations.stream()
                            .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                            .findFirst().get();
                    record.setAnnotation(latest);
                    // Prefer image from the annotation's analysis job if present
                    if (latest.getAnalysisJob() != null && latest.getAnalysisJob().getImage() != null) {
                        record.setImage(latest.getAnalysisJob().getImage());
                    }
                }
            }
        }
        // Explicit image override (if user selected a different one)
        if (imageId != null) {
            Image image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new RuntimeException("Image not found"));
            record.setImage(image);
        }
        // Explicit annotation override (sets annotation and, if image not set yet, use its image)
        if (annotationId != null) {
            Annotation annotation = annotationRepository.findById(annotationId)
                    .orElseThrow(() -> new RuntimeException("Annotation not found"));
            record.setAnnotation(annotation);
            if (record.getImage() == null && annotation.getAnalysisJob() != null && annotation.getAnalysisJob().getImage() != null) {
                record.setImage(annotation.getAnalysisJob().getImage());
            }
        }

        return maintenanceRecordRepository.save(record);
    }

    @Transactional
    public MaintenanceRecord updateRecord(Long id,
                                          String inspectorName,
                                          MaintenanceRecord.TransformerStatus status,
                                          Double voltage,
                                          Double current,
                                          String recommendedAction,
                                          String correctiveActions,
                                          String additionalRemarks,
                                          Long annotationId) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found"));

        if (inspectorName != null) record.setInspectorName(inspectorName);
        if (status != null) record.setStatus(status);
        if (voltage != null) record.setVoltage(voltage);
        if (current != null) record.setCurrent(current);
        if (recommendedAction != null) record.setRecommendedAction(recommendedAction);
        if (correctiveActions != null) record.setCorrectiveActions(correctiveActions);
        if (additionalRemarks != null) record.setAdditionalRemarks(additionalRemarks);
        if (annotationId != null) {
            Annotation annotation = annotationRepository.findById(annotationId)
                    .orElseThrow(() -> new RuntimeException("Annotation not found"));
            record.setAnnotation(annotation);
            if (annotation.getAnalysisJob() != null && annotation.getAnalysisJob().getImage() != null) {
                // Update image reference when switching annotation if available
                record.setImage(annotation.getAnalysisJob().getImage());
            }
        }

        return maintenanceRecordRepository.save(record);
    }

    @Transactional
    public MaintenanceRecord finalizeRecord(Long id) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found"));
        if (Boolean.TRUE.equals(record.getFinalized())) {
            return record; // Already finalized
        }
        record.setFinalized(true);
        record.setFinalizedAt(LocalDateTime.now());
        return maintenanceRecordRepository.save(record);
    }

    public MaintenanceRecord getRecord(Long id) {
        return maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found"));
    }

    public List<MaintenanceRecord> getRecordsByTransformer(Long transformerRecordId) {
        return maintenanceRecordRepository.findByTransformerRecordId(transformerRecordId);
    }

    public MaintenanceRecordFormDTO buildFormDTO(Long id) {
        MaintenanceRecord record = getRecord(id);
        TransformerRecord tr = record.getTransformerRecord();
        Inspection inspection = record.getInspection();
        Image image = record.getImage();
        Annotation annotation = record.getAnnotation();

        MaintenanceRecordFormDTO dto = new MaintenanceRecordFormDTO();
        dto.setRecordId(record.getId());
        dto.setFinalized(Boolean.TRUE.equals(record.getFinalized()));
        dto.setFinalizedAt(record.getFinalizedAt());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());

        if (tr != null) {
            dto.setTransformerId(tr.getId());
            dto.setTransformerName(tr.getName());
            dto.setLocationName(tr.getLocationName());
            dto.setLocationLat(tr.getLocationLat());
            dto.setLocationLng(tr.getLocationLng());
            dto.setCapacity(tr.getCapacity());
            dto.setPoleNo(tr.getPoleNo());
            dto.setTransformerType(tr.getTransformerType());
        }

        if (inspection != null) {
            dto.setInspectionId(inspection.getId());
            dto.setInspectionTimestamp(inspection.getInspectionDate() != null ? inspection.getInspectionDate() : inspection.getCreatedAt());
            dto.setInspectorSourceName(inspection.getConductorName());
            dto.setInspectorSourceRole(inspection.getConductorRole());
            dto.setInspectionNotes(inspection.getNotes());
        }

        if (image != null) {
            dto.setImageId(image.getId());
            dto.setImagePath(image.getFilePath());
            dto.setImageType(image.getType());
        }

        dto.setInspectorName(record.getInspectorName());
        dto.setStatus(record.getStatus());
        dto.setVoltage(record.getVoltage());
        dto.setCurrent(record.getCurrent());
        dto.setRecommendedAction(record.getRecommendedAction());
        dto.setCorrectiveActions(record.getCorrectiveActions());
        dto.setAdditionalRemarks(record.getAdditionalRemarks());

        // Map anomalies from annotation boxes (if any)
        if (annotation != null && annotation.getAnnotationBoxes() != null) {
            for (AnnotationBox box : annotation.getAnnotationBoxes()) {
                AnomalyDTO a = new AnomalyDTO();
                a.setId(box.getId());
                a.setType(box.getType());
                a.setX(box.getX());
                a.setY(box.getY());
                a.setWidth(box.getWidth());
                a.setHeight(box.getHeight());
                a.setConfidence(box.getConfidence());
                a.setComments(box.getComments());
                a.setAction(box.getAction().name());
                dto.getAnomalies().add(a);
            }
        }
        return dto;
    }

    @Transactional
    public void deleteRecord(Long id) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found"));
        maintenanceRecordRepository.delete(record);
    }
}
