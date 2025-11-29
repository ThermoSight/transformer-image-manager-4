# ThermoSight — Project & Phase 4 Maintenance Records

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![React](https://img.shields.io/badge/React-19.1.1-blue.svg)](https://reactjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/)
[![Python](https://img.shields.io/badge/Python-3.8+-yellow.svg)](https://www.python.org/)

> ThermoSight — AI-powered thermal anomaly detection and digital maintenance records for electrical transformers.

This document is an extended project README which includes the original project overview plus the Phase 4 features: generation, editing, saving and exporting of digital maintenance records tied to inspections and thermal analysis.

---

## 🌟 Key Highlights (updated — Phase 4 included)

- 🤖 **AI-Powered Detection**: PatchCore-based anomaly detection (high accuracy) with bounding boxes and confidence scores.
- 🎨 **Interactive Annotations**: Canvas editor for validating and editing AI-generated annotations (Phase 3).
- 📊 **Real-time Analytics**: Live monitoring and queue management.
- 🔄 **Continuous Learning**: Feedback loop for improving models.
- 🗺️ **Geographic Mapping**: Transformer location tracking (Leaflet).
- 🔐 **Enterprise Security**: JWT authentication with role-based access control.
- 📱 **Responsive Design**: Bootstrap-powered interface for devices.
- 🧾 **Phase 4 — Maintenance Record Forms**: Digital, inspector-editable maintenance record generation and storage per inspection (includes anomaly thumbnails/markers, editable engineer fields, versioned timestamps, PDF export and history viewer).

---

## 📸 System Screenshots (additions for Phase 4)

### Main Dashboard
![Dashboard Overview](docs/images/dashboard-overview.png)

### Interactive Annotation Editor
![Annotation Editor](docs/images/annotation-editor.png)

### Thermal Analysis Results
![Analysis Results](docs/images/analysis-results.png)

### Phase 4 — Maintenance Record Form (example)
![Maintenance Record Form](docs/images/maintenance-record.png)
*Maintenance record form showing thumbnail, anomaly list, editable engineer fields and action buttons (Save/Submit/PDF).* 

---

## 🏗️ System Architecture & Project Structure (current workspace)

ThermoSight is split into core modules. The repository structure (top-level) is:

```
README.md
README_PHASE4.md   # (this file)
automatic-anamoly-detection/   # ML models and inference scripts
transformer-manager-backkend/  # Spring Boot backend (REST API)
transformer-manager-frontend/  # React frontend
temp/                         # temporary analysis outputs and uploads
```

Details inside each module (high level):

- `automatic-anamoly-detection/` — Python code for PatchCore inference, scripts to run batch/online inference, Dockerfile and README for model usage.
- `transformer-manager-backkend/` — Spring Boot app (Maven), controllers, services, entities, and `application.properties` (DB config). Exposes REST endpoints for inspections, maintenance-records, annotations, transformer-records, auth, and file access.
- `transformer-manager-frontend/` — React app with components: `AnalysisDisplay`, `InteractiveAnnotationEditor`, `ImageViewer`, `MaintenanceRecordForm`, `MaintenanceRecordsHistory`, `InspectionDetail`, `TransformerRecordDetail`, etc.

---

## Phase 4 — Maintenance Records (Overview)

Phase 4 introduces a full digital maintenance-record workflow so each inspection can produce a structured, editable, and exportable maintenance record.

- Generate: When an inspection has a maintenance image and analysis, the system can create a maintenance record form tied to the inspection and transformer.
- Edit: Authorized users can edit fields (inspector name, status, electrical readings, corrective action, recommended action, engineer notes, follow-up dates, etc.).
- Save / Version: Records are saved to the database and include `createdAt` and `updatedAt` timestamps and a `recordStatus` (DRAFT, SUBMITTED, REVIEWED, APPROVED).
- Export: PDF export of a maintenance record (PDF generation excludes raw maintenance images by default but includes annotated thumbnails and baseline images to keep reports concise).
- History: Each transformer has a maintenance history viewer listing past records with download and delete (admin-only) actions.

### Maintenance Subtopic — How form generation & saving works

Flow (high level):

1. Frontend (`MaintenanceRecordForm` component) opens for a given `inspectionId`. It calls:
   - `GET /api/maintenance-records/inspection/{inspectionId}` to fetch an existing maintenance record (if any).
2. If a record exists, the form is populated with saved values. If not, the form is shown with defaults and the detected anomalies from the analysis are displayed in a `Detected Anomalies` field (pulled from the inspection/analysis). The image/annotation thumbnail(s) are available via the `AnalysisDisplay`/`ImageViewer` components.
3. The engineer edits fields (text, selects, date-time pickers) and either:
   - Saves as Draft: frontend sends POST to `POST /api/maintenance-records/inspection/{inspectionId}` with the form payload. Backend `MaintenanceRecordService` will create/update the `MaintenanceRecord` entity and return the saved record.
   - Submits: same endpoint but with `recordStatus = SUBMITTED` — backend sets submit metadata and persists accordingly.
4. Backend Persistence: `MaintenanceRecordService` handles createOrUpdate flows. It links the record to the existing `Inspection` entity and persists via `MaintenanceRecordRepository` (Spring Data JPA). Timestamps (`createdAt`, `updatedAt`) are managed with Hibernate annotations.
5. After save/submit, frontend refreshes the record and the history viewer is refreshed (cache-busting headers/params used to avoid stale responses).

Notes about concurrency and integrity:

- The system uses JPA entity relationships: `MaintenanceRecord` <-> `Inspection` (one-to-one). To prevent cascade/persistence surprises when deleting, the backend breaks the bidirectional link before deleting a maintenance record.
- PDF export is handled by `MaintenanceRecordPdfService` which composes a PDF from the record, annotated images, and transformer metadata.

### API Endpoints (relevant)

- `GET /api/maintenance-records/inspection/{inspectionId}` — fetch record for inspection.
- `POST /api/maintenance-records/inspection/{inspectionId}` — create or update a maintenance record (Admin/User depending on auth).
- `GET /api/maintenance-records/transformer/{transformerId}` — list records for a transformer (history viewer).
- `DELETE /api/maintenance-records/{id}` — admin-only delete (service unlinks associations before delete).
- `GET /api/maintenance-records/{id}/export/pdf` — download PDF for a record.

### Database Schema for Record Storage

Below is a concise schema reflecting the entities used for maintenance records. The actual JPA entities and fields exist in `transformer-manager-backkend/src/main/java/.../entity`.

SQL (representative) — `maintenance_records` table:

```sql
CREATE TABLE maintenance_records (
  id BIGSERIAL PRIMARY KEY,
  inspection_id BIGINT NOT NULL UNIQUE REFERENCES inspections(id),
  inspector_name VARCHAR(255),
  inspector_id VARCHAR(100),
  inspector_email VARCHAR(255),
  transformer_status VARCHAR(50),
  voltage_phase_a DOUBLE PRECISION,
  voltage_phase_b DOUBLE PRECISION,
  voltage_phase_c DOUBLE PRECISION,
  current_phase_a DOUBLE PRECISION,
  current_phase_b DOUBLE PRECISION,
  current_phase_c DOUBLE PRECISION,
  power_factor DOUBLE PRECISION,
  frequency DOUBLE PRECISION,
  ambient_temperature DOUBLE PRECISION,
  oil_temperature DOUBLE PRECISION,
  winding_temperature DOUBLE PRECISION,
  oil_level VARCHAR(50),
  oil_color VARCHAR(50),
  oil_analysis_remarks TEXT,
  cooling_system_condition TEXT,
  bushing_condition TEXT,
  tank_condition TEXT,
  gauges_condition TEXT,
  detected_anomalies TEXT,
  corrective_actions TEXT,
  recommended_action TEXT,
  maintenance_priority VARCHAR(50),
  scheduled_maintenance_date TIMESTAMP,
  engineer_notes TEXT,
  additional_remarks TEXT,
  weather_condition VARCHAR(50),
  load_condition VARCHAR(100),
  parts_replaced TEXT,
  materials_used TEXT,
  requires_follow_up BOOLEAN,
  follow_up_date TIMESTAMP,
  follow_up_notes TEXT,
  safety_observations TEXT,
  compliance_check BOOLEAN,
  compliance_notes TEXT,
  created_at TIMESTAMP DEFAULT now(),
  updated_at TIMESTAMP DEFAULT now(),
  record_status VARCHAR(50),
  submitted_by_admin BIGINT REFERENCES admins(id),
  submitted_by_user BIGINT REFERENCES users(id),
  reviewed_by_admin BIGINT REFERENCES admins(id),
  reviewed_at TIMESTAMP
);
```

`inspections` table (simplified) must include at least:

```sql
CREATE TABLE inspections (
  id BIGSERIAL PRIMARY KEY,
  transformer_record_id BIGINT REFERENCES transformer_records(id),
  inspection_date TIMESTAMP,
  notes TEXT,
  created_at TIMESTAMP DEFAULT now()
);
```

Indexes, foreign keys, and constraints should be added according to operational needs. The JPA entities in the codebase map these relationships with `@OneToOne` and `@ManyToOne` annotations.

---

## How to Run (quick)

1. Backend (Spring Boot):

```powershell
cd transformer-manager-backkend
.\mvnw.cmd spring-boot:run
# ensure application.properties points to a running PostgreSQL (see src/main/resources/application.properties)
```

2. Frontend (React):

```powershell
cd transformer-manager-frontend
npm install
npm start
```

3. ML inference and dataset scripts: see `automatic-anamoly-detection/README.md` and `Model_Inference/` for how to run local inference.

---

## PDF & Export notes

- PDF generation is implemented in the backend service `MaintenanceRecordPdfService`. By design, maintenance images are not included as raw full-size images in the PDF; instead, annotated thumbnails and baseline imagery are used to keep reports compact and compliant with export rules.

---

## Security & Permissions

- Editing and saving maintenance records requires authentication. The API enforces role-based access control (`ROLE_ADMIN` / `ROLE_USER`).
- Delete operations are restricted to admins only.

---

## Developer Notes & Where to Look

- Frontend form: `transformer-manager-frontend/src/components/MaintenanceRecordForm.js` — UI, fetching `GET /api/maintenance-records/inspection/{inspectionId}`, and saving `POST /api/maintenance-records/inspection/{inspectionId}`.
- History & exports: `transformer-manager-frontend/src/components/MaintenanceRecordsHistory.js` — list, CSV/PDF export, delete action (admin-only).
- Backend service: `transformer-manager-backkend/src/main/java/.../service/MaintenanceRecordService.java` — create/update, submit, review, delete (unlink associations before delete).
- Backend controller: `transformer-manager-backkend/src/main/java/.../controller/MaintenanceRecordController.java` — REST endpoints and cache-control header on list endpoints.
- Entity definitions: `transformer-manager-backkend/src/main/java/.../entity/MaintenanceRecord.java` and `Inspection.java`.
- PDF generator: `transformer-manager-backkend/src/main/java/.../service/MaintenanceRecordPdfService.java`.

---

## Traceability & Versioning

- Each saved maintenance record includes `createdAt` and `updatedAt` timestamps and a `recordStatus` field to support lifecycle tracking and basic version history. For stricter versioning, an audit table or event log can be added (recommended for production audits).

---

## Contribution & Next Steps

- Add automated tests for the `MaintenanceRecordService` create/update/delete flows.
- Add e2e tests to verify record creation → submit → PDF export → history listing.
- Consider adding a history/audit table for immutable change logs if regulatory compliance is required.

---

If you'd like, I can also:
- Add a `docs/` markdown file with ER diagrams and sequence diagrams for the maintenance record flow.
- Generate a simplified SQL migration file (Flyway/Liquibase) for the `maintenance_records` table.

Created: README_PHASE4.md
