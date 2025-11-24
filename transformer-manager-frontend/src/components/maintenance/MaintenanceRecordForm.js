import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import axiosInstance from "../../axiosConfig";
import ImageWithOverlay from "./ImageWithOverlay";
import EngineerFields from "./EngineerFields";
import AnomaliesTable from "./AnomaliesTable";
import { Button, Spinner, Alert, Card, Row, Col } from "react-bootstrap";

const MaintenanceRecordForm = () => {
  const { id } = useParams();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);
  const [finalizing, setFinalizing] = useState(false);
  const [fields, setFields] = useState({});
  const [successMsg, setSuccessMsg] = useState("");

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await axiosInstance.get(`/maintenance-records/${id}/form`);
      setData(res.data);
      setFields({
        inspectorName: res.data.inspectorName || "",
        status: res.data.status || "OK",
        voltage: res.data.voltage || "",
        current: res.data.current || "",
        recommendedAction: res.data.recommendedAction || "",
        correctiveActions: res.data.correctiveActions || "",
        additionalRemarks: res.data.additionalRemarks || "",
      });
    } catch (e) {
      setError(e.message || "Failed to load record form");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [id]);

  const handleFieldChange = (name, value) => {
    setFields((prev) => ({ ...prev, [name]: value }));
  };

  const save = async () => {
    setSaving(true);
    setSuccessMsg("");
    try {
      await axiosInstance.put(`/maintenance-records/${id}/json`, fields);
      setSuccessMsg("Saved changes.");
      await load();
    } catch (e) {
      setError(e.message || "Save failed");
    } finally {
      setSaving(false);
    }
  };

  const finalize = async () => {
    if (!window.confirm("Finalize this record? No further edits allowed.")) return;
    setFinalizing(true);
    setSuccessMsg("");
    try {
      await axiosInstance.post(`/maintenance-records/${id}/finalize`);
      setSuccessMsg("Record finalized.");
      await load();
    } catch (e) {
      setError(e.message || "Finalize failed");
    } finally {
      setFinalizing(false);
    }
  };

  if (loading) return <Spinner animation="border" />;
  if (error) return <Alert variant="danger">{error}</Alert>;
  if (!data) return <Alert variant="warning">No data.</Alert>;

  const disableEdits = data.finalized;
  const imageUrl = data.imagePath ? `http://localhost:8080${data.imagePath}` : null;

  return (
    <div className="mt-3">
      <h4>Maintenance Record #{data.recordId}</h4>
      {data.finalized && (
        <Alert variant="success">
          Finalized at {data.finalizedAt || "(timestamp unavailable)"}
        </Alert>
      )}
      {successMsg && <Alert variant="info">{successMsg}</Alert>}
      <Row>
        <Col md={6} className="mb-3">
          <Card>
            <Card.Header>Transformer Metadata</Card.Header>
            <Card.Body>
              <div><strong>Name:</strong> {data.transformerName}</div>
              <div><strong>Location:</strong> {data.locationName}</div>
              <div>
                <strong>Coordinates:</strong> {data.locationLat},{" "}
                {data.locationLng}
              </div>
              <div><strong>Capacity:</strong> {data.capacity}</div>
              <div><strong>Type:</strong> {data.transformerType}</div>
              <div><strong>Pole No:</strong> {data.poleNo}</div>
            </Card.Body>
          </Card>
        </Col>
        <Col md={6} className="mb-3">
          <Card>
            <Card.Header>Inspection</Card.Header>
            <Card.Body>
              <div><strong>Inspection ID:</strong> {data.inspectionId}</div>
              <div><strong>Timestamp:</strong> {data.inspectionTimestamp}</div>
              <div><strong>Source:</strong> {data.inspectorSourceName} ({data.inspectorSourceRole})</div>
              <div><strong>Notes:</strong> {data.inspectionNotes}</div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
      <Row>
        <Col md={7} className="mb-3">
          <Card>
            <Card.Header>Thermal Image & Anomalies</Card.Header>
            <Card.Body>
              {imageUrl ? (
                <ImageWithOverlay imageUrl={imageUrl} anomalies={data.anomalies} />
              ) : (
                <Alert variant="secondary">No image linked.</Alert>
              )}
              <AnomaliesTable anomalies={data.anomalies} />
            </Card.Body>
          </Card>
        </Col>
        <Col md={5} className="mb-3">
          <Card>
            <Card.Header>Engineer Inputs</Card.Header>
            <Card.Body>
              <EngineerFields
                values={fields}
                onChange={handleFieldChange}
                disabled={disableEdits}
              />
              <div className="mt-3 d-flex gap-2">
                <Button
                  variant="primary"
                  onClick={save}
                  disabled={disableEdits || saving}
                >
                  {saving ? "Saving..." : "Save"}
                </Button>
                <Button
                  variant="danger"
                  onClick={finalize}
                  disabled={disableEdits || finalizing}
                >
                  {finalizing ? "Finalizing..." : "Finalize"}
                </Button>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default MaintenanceRecordForm;
