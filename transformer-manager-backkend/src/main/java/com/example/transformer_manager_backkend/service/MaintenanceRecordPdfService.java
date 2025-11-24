package com.example.transformer_manager_backkend.service;

import com.example.transformer_manager_backkend.dto.MaintenanceRecordFormDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
public class MaintenanceRecordPdfService {

    private final MaintenanceRecordService maintenanceRecordService;

    public MaintenanceRecordPdfService(MaintenanceRecordService maintenanceRecordService) {
        this.maintenanceRecordService = maintenanceRecordService;
    }

    public byte[] generatePdf(Long recordId) {
        MaintenanceRecordFormDTO dto = maintenanceRecordService.buildFormDTO(recordId);

        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float margin = 40;
            float y = page.getMediaBox().getHeight() - margin;
            float width = page.getMediaBox().getWidth() - margin * 2;

            // Header
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
            cs.newLineAtOffset(margin, y);
            cs.showText("Maintenance Record #" + dto.getRecordId());
            cs.endText();
            y -= 24;

            if (dto.isFinalized()) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.newLineAtOffset(margin, y);
                cs.showText("Finalized: " + (dto.getFinalizedAt() != null ? dto.getFinalizedAt().toString() : "Yes"));
                cs.endText();
                y -= 16;
            }

            // Transformer details
            y = sectionTitle(cs, margin, y, "Transformer Details");
            y = keyValue(cs, margin, y, "Name", safe(dto.getTransformerName()));
            y = keyValue(cs, margin, y, "Location", safe(dto.getLocationName()));
            y = keyValue(cs, margin, y, "Type", safe(dto.getTransformerType()));
            y = keyValue(cs, margin, y, "Pole No", str(dto.getPoleNo()));
            y = keyValue(cs, margin, y, "Capacity", str(dto.getCapacity(), "kVA"));

            // Inspection
            y = sectionTitle(cs, margin, y, "Inspection");
            y = keyValue(cs, margin, y, "Inspection ID", str(dto.getInspectionId()));
            y = keyValue(cs, margin, y, "Timestamp", dto.getInspectionTimestamp() != null ? dto.getInspectionTimestamp().toString() : "-");
            y = keyValue(cs, margin, y, "Source", safe(dto.getInspectorSourceName()) + (dto.getInspectorSourceRole() != null ? " (" + dto.getInspectorSourceRole() + ")" : ""));
            y = keyValue(cs, margin, y, "Notes", safe(dto.getInspectionNotes()));

            // Engineer fields
            y = sectionTitle(cs, margin, y, "Engineer Inputs");
            y = keyValue(cs, margin, y, "Inspector", safe(dto.getInspectorName()));
            y = keyValue(cs, margin, y, "Status", dto.getStatus() != null ? dto.getStatus().name() : "-");
            y = keyValue(cs, margin, y, "Voltage", str(dto.getVoltage(), "V"));
            y = keyValue(cs, margin, y, "Current", str(dto.getCurrent(), "A"));
            y = keyValue(cs, margin, y, "Recommended Action", safe(dto.getRecommendedAction()));
            y = keyValue(cs, margin, y, "Corrective Actions", safe(dto.getCorrectiveActions()));
            y = keyValue(cs, margin, y, "Remarks", safe(dto.getAdditionalRemarks()));

            // Anomalies summary
            y = sectionTitle(cs, margin, y, "Anomalies");
            String anomaliesLine = dto.getAnomalies().isEmpty() ? "None" : (dto.getAnomalies().size() + " detected");
            y = keyValue(cs, margin, y, "Summary", anomaliesLine);

            // Try embedding image (boxed image path or provided imagePath)
            if (dto.getImagePath() != null) {
                Path imgPath = resolveLocal(dto.getImagePath());
                if (Files.exists(imgPath)) {
                    try {
                        PDImageXObject pdImage = PDImageXObject.createFromFile(imgPath.toString(), doc);
                        float imgMaxWidth = width;
                        float imgMaxHeight = 260;
                        float scale = Math.min(imgMaxWidth / pdImage.getWidth(), imgMaxHeight / pdImage.getHeight());
                        float imgW = pdImage.getWidth() * scale;
                        float imgH = pdImage.getHeight() * scale;

                        if (y - imgH < margin) {
                            cs.close();
                            page = new PDPage(PDRectangle.A4);
                            doc.addPage(page);
                            cs = new PDPageContentStream(doc, page);
                            y = page.getMediaBox().getHeight() - margin;
                        }

                        cs.drawImage(pdImage, margin, y - imgH, imgW, imgH);
                        y -= (imgH + 10);
                    } catch (Exception ignore) {
                        // If embedding fails, continue without image
                    }
                }
            }

            cs.close();

            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF for maintenance record " + recordId, e);
        }
    }

    private String safe(String s) { return s == null ? "-" : s; }
    private String str(Number n) { return n == null ? "-" : String.valueOf(n); }
    private String str(Number n, String unit) { return n == null ? "-" : (n + " " + unit); }

    private float sectionTitle(PDPageContentStream cs, float x, float y, String title) throws Exception {
        y -= 12;
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.newLineAtOffset(x, y);
        cs.showText(title);
        cs.endText();
        return y - 8;
    }

    private float keyValue(PDPageContentStream cs, float x, float y, String k, String v) throws Exception {
        if (y < 60) { // minimal space safeguard; in this simple version, we won't paginate text blocks
            y = 60;
        }
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
        cs.newLineAtOffset(x, y);
        cs.showText(k + ": ");
        cs.endText();
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.newLineAtOffset(x + 120, y);
        cs.showText(v != null ? v : "-");
        cs.endText();
        return y - 14;
    }

    private Path resolveLocal(String webPath) {
        // Convert a web path like "/analysis/xxx.jpg" or "/uploads/.." to local relative path
        if (webPath == null || webPath.isBlank()) return Paths.get("");
        String normalized = webPath.replace("\\", "/");
        while (normalized.startsWith("/")) normalized = normalized.substring(1);
        return Paths.get(normalized);
    }
}
