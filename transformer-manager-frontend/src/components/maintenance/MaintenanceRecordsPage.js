import React, { useEffect, useState } from "react";
import { Card, Row, Col, Spinner, Alert, Button, Badge, Dropdown, InputGroup, Form, Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../axiosConfig";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlus, faClipboardList, faBolt, faSearch, faEye, faTrash } from "@fortawesome/free-solid-svg-icons";
import { useAuth } from "../../AuthContext";

// Minimal listing & creation page for Maintenance Records
// Flow: select transformer -> view existing maintenance records -> create new (only transformer required)

const MaintenanceRecordsPage = () => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [transformers, setTransformers] = useState([]);
  const [selectedTransformer, setSelectedTransformer] = useState(null);
  const [records, setRecords] = useState([]);
  const [inspections, setInspections] = useState([]);
  const [selectedInspection, setSelectedInspection] = useState(null);
  const [annotations, setAnnotations] = useState([]);
  const [selectedAnnotation, setSelectedAnnotation] = useState(null);
  const [loadingTransformers, setLoadingTransformers] = useState(true);
  const [loadingRecords, setLoadingRecords] = useState(false);
  const [error, setError] = useState("");
  const [createError, setCreateError] = useState("");
  const [searchTerm, setSearchTerm] = useState("");

  useEffect(() => {
    fetchTransformers();
  }, []);

  useEffect(() => {
    if (selectedTransformer) {
      fetchMaintenanceRecords(selectedTransformer.id);
    }
  }, [selectedTransformer]);

  useEffect(() => {
    if (selectedInspection) {
      fetchAnnotations(selectedInspection.id);
    } else {
      setAnnotations([]);
      setSelectedAnnotation(null);
    }
  }, [selectedInspection]);

  const fetchTransformers = async () => {
    try {
      setLoadingTransformers(true);
      const res = await axiosInstance.get("/transformer-records");
      setTransformers(res.data);
      if (res.data.length > 0) {
        setSelectedTransformer(res.data[0]);
      }
      setError("");
    } catch (e) {
      setError("Failed to load transformers");
    } finally {
      setLoadingTransformers(false);
    }
  };

  const fetchMaintenanceRecords = async (transformerId) => {
    try {
      setLoadingRecords(true);
      const res = await axiosInstance.get(`/maintenance-records/transformer/${transformerId}`);
      setRecords(res.data);
      setError("");
    } catch (e) {
      setError("Failed to load maintenance records");
      setRecords([]);
    } finally {
      setLoadingRecords(false);
    }
  };

  const fetchInspections = async (transformerId) => {
    try {
      const res = await axiosInstance.get(`/inspections/transformer/${transformerId}`);
      setInspections(res.data);
      if (res.data.length > 0) {
        setSelectedInspection(res.data[0]);
      } else {
        setSelectedInspection(null);
      }
    } catch (e) {
      setInspections([]);
      setSelectedInspection(null);
    }
  };

  const fetchAnnotations = async (inspectionId) => {
    try {
      const res = await axiosInstance.get(`/annotations/inspection/${inspectionId}`);
      setAnnotations(res.data);
      if (res.data.length > 0) {
        setSelectedAnnotation(res.data[0]);
      } else {
        setSelectedAnnotation(null);
      }
    } catch (e) {
      setAnnotations([]);
      setSelectedAnnotation(null);
    }
  };

  const handleCreate = async () => {
    if (!selectedTransformer) return;
    setCreateError("");
    try {
      const params = new URLSearchParams();
      params.append("transformerRecordId", selectedTransformer.id);
      if (selectedInspection) params.append("inspectionId", selectedInspection.id);
      if (selectedAnnotation && selectedAnnotation.analysisJob && selectedAnnotation.analysisJob.image) {
        params.append("imageId", selectedAnnotation.analysisJob.image.id);
      }
      if (selectedAnnotation) params.append("annotationId", selectedAnnotation.id);
      const res = await axiosInstance.post(`/maintenance-records?${params.toString()}`);
      const newRecord = res.data;
      fetchMaintenanceRecords(selectedTransformer.id);
      navigate(`/maintenance-records/${newRecord.id}`);
    } catch (e) {
      setCreateError(e.response?.data?.message || "Failed to create maintenance record");
    }
  };

  const filteredTransformers = transformers.filter(t =>
    t.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (t.locationName && t.locationName.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  // When transformer changes fetch inspections
  useEffect(() => {
    if (selectedTransformer) {
      fetchInspections(selectedTransformer.id);
    } else {
      setInspections([]);
      setSelectedInspection(null);
    }
  }, [selectedTransformer]);

  const handleDelete = async (recordId) => {
    if (!window.confirm(`Delete maintenance record #${recordId}? This cannot be undone.`)) return;
    try {
      await axiosInstance.delete(`/maintenance-records/${recordId}`);
      setRecords(records.filter(r => r.id !== recordId));
    } catch (e) {
      setError(e.response?.data?.message || "Failed to delete record");
    }
  };

  return (
    <div className="moodle-container">
      <div className="page-header d-flex justify-content-between align-items-center">
        <h2 className="d-flex align-items-center">
          <FontAwesomeIcon icon={faClipboardList} className="me-2" />
          Maintenance Records
        </h2>
        {isAuthenticated && selectedTransformer && (
          <Button
            variant="primary"
            onClick={handleCreate}
            disabled={!selectedTransformer}
            title={!selectedInspection ? "Select inspection first (optional)" : (annotations.length === 0 ? "No annotations available; record will lack anomaly overlay" : "Create maintenance record")}
          >
            <FontAwesomeIcon icon={faPlus} className="me-2" />
            New Record
          </Button>
        )}
      </div>

      {error && (
        <Alert variant="danger" className="mb-3">{error}</Alert>
      )}
      {createError && (
        <Alert variant="danger" className="mb-3">{createError}</Alert>
      )}

      {/* Transformer selection */}
      <Card className="mb-4">
        <Card.Body>
          <Row className="g-3 align-items-end">
            <Col md={5}>
              <Form.Label>Select Transformer</Form.Label>
              <Dropdown className="w-100">
                <Dropdown.Toggle variant="outline-secondary" className="w-100 text-start">
                  {selectedTransformer ? selectedTransformer.name : "Select a transformer"}
                </Dropdown.Toggle>
                <Dropdown.Menu className="w-100" style={{ maxHeight: "300px", overflowY: "auto" }}>
                  {filteredTransformers.map(t => (
                    <Dropdown.Item
                      key={t.id}
                      onClick={() => setSelectedTransformer(t)}
                      active={selectedTransformer && selectedTransformer.id === t.id}
                    >
                      <div className="d-flex justify-content-between align-items-center">
                        <span>
                          {t.name}
                          {t.poleNo && ` (Pole #${t.poleNo})`}
                        </span>
                        <Badge bg="info">{t.capacity || 0}kVA</Badge>
                      </div>
                      {t.locationName && (
                        <div className="text-muted small mt-1">{t.locationName.split(',').slice(0,2).join(', ')}</div>
                      )}
                    </Dropdown.Item>
                  ))}
                  {filteredTransformers.length === 0 && (
                    <Dropdown.Item disabled>No matches</Dropdown.Item>
                  )}
                </Dropdown.Menu>
              </Dropdown>
            </Col>
            <Col md={4}>
              <Form.Label>Search</Form.Label>
              <InputGroup>
                <Form.Control
                  placeholder="Search by name or location"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
                <Button variant="outline-secondary">
                  <FontAwesomeIcon icon={faSearch} />
                </Button>
              </InputGroup>
            </Col>
            <Col md={3}>
              {selectedTransformer && (
                <div className="mt-3 mt-md-0">
                  <Badge bg="secondary" className="me-2">
                    {records.length} record{records.length !== 1 && 's'}
                  </Badge>
                  <Badge bg="dark">Transformer ID: {selectedTransformer.id}</Badge>
                </div>
              )}
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Inspection selection */}
      {selectedTransformer && (
        <Card className="mb-4">
          <Card.Header>Inspection & Anomaly Source</Card.Header>
          <Card.Body>
            {inspections.length === 0 ? (
              <Alert variant="info" className="mb-3">No inspections found for this transformer; you can still create a record but anomaly/image context will be empty.</Alert>
            ) : (
              <Row className="g-3">
                <Col md={4}>
                  <Form.Label>Select Inspection</Form.Label>
                  <Dropdown className="w-100">
                    <Dropdown.Toggle variant="outline-secondary" className="w-100 text-start">
                      {selectedInspection ? `Inspection #${selectedInspection.id}` : 'Select inspection'}
                    </Dropdown.Toggle>
                    <Dropdown.Menu className="w-100" style={{ maxHeight: '300px', overflowY: 'auto' }}>
                      {inspections.map(i => (
                        <Dropdown.Item
                          key={i.id}
                          active={selectedInspection && selectedInspection.id === i.id}
                          onClick={() => setSelectedInspection(i)}
                        >
                          <div className="d-flex justify-content-between align-items-center">
                            <span>#{i.id} {i.inspectionDate ? new Date(i.inspectionDate).toLocaleDateString() : ''}</span>
                            <Badge bg="info">{i.images?.length || 0} imgs</Badge>
                          </div>
                          {i.notes && <div className="text-muted small mt-1">{i.notes.substring(0,40)}{i.notes.length>40?'...':''}</div>}
                        </Dropdown.Item>
                      ))}
                    </Dropdown.Menu>
                  </Dropdown>
                </Col>
                <Col md={4}>
                  <Form.Label>Select Annotation</Form.Label>
                  {selectedInspection && annotations.length === 0 && (
                    <Alert variant="warning" className="py-2">No annotations for selected inspection.</Alert>
                  )}
                  {annotations.length > 0 && (
                    <Dropdown className="w-100">
                      <Dropdown.Toggle variant="outline-secondary" className="w-100 text-start">
                        {selectedAnnotation ? `Annotation #${selectedAnnotation.id}` : 'Select annotation'}
                      </Dropdown.Toggle>
                      <Dropdown.Menu className="w-100" style={{ maxHeight: '300px', overflowY: 'auto' }}>
                        {annotations.map(a => (
                          <Dropdown.Item
                            key={a.id}
                            active={selectedAnnotation && selectedAnnotation.id === a.id}
                            onClick={() => setSelectedAnnotation(a)}
                          >
                            <div className="d-flex justify-content-between">
                              <span>Annotation #{a.id}</span>
                              {a.analysisJob?.image && <Badge bg="secondary">Img {a.analysisJob.image.id}</Badge>}
                            </div>
                            {a.annotationType && <div className="text-muted small">{a.annotationType}</div>}
                          </Dropdown.Item>
                        ))}
                      </Dropdown.Menu>
                    </Dropdown>
                  )}
                </Col>
                <Col md={4}>
                  <Form.Label>Selected Image</Form.Label>
                  {selectedAnnotation && selectedAnnotation.analysisJob?.image ? (
                    <Card className="mb-2">
                      <Card.Body className="p-2">
                        <div className="small">Image ID: {selectedAnnotation.analysisJob.image.id}</div>
                        <div className="text-truncate" style={{maxWidth:'100%'}}>{selectedAnnotation.analysisJob.image.filePath}</div>
                        {selectedAnnotation.analysisJob.image.type && <Badge bg="info" className="mt-1">{selectedAnnotation.analysisJob.image.type}</Badge>}
                      </Card.Body>
                    </Card>
                  ) : (
                    <div className="text-muted small">No image selected</div>
                  )}
                </Col>
              </Row>
            )}
          </Card.Body>
        </Card>
      )}

      {/* Records list */}
      {loadingTransformers || loadingRecords ? (
        <div className="text-center mt-5">
          <Spinner animation="border" variant="primary" />
          <p className="mt-2">Loading data...</p>
        </div>
      ) : !selectedTransformer ? (
        <Alert variant="info">Please select a transformer.</Alert>
      ) : records.length === 0 ? (
        <Alert variant="secondary" className="text-center">
          No maintenance records yet for this transformer.
          {isAuthenticated && (
            <div className="mt-2">
              <Button size="sm" variant="primary" onClick={handleCreate}>
                <FontAwesomeIcon icon={faPlus} className="me-1" /> Create First Record
              </Button>
            </div>
          )}
        </Alert>
      ) : (
        <Card className="mb-4">
          <Card.Header>Records for {selectedTransformer.name}</Card.Header>
          <Card.Body>
            <Table responsive hover size="sm" className="align-middle">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Status</th>
                  <th>Inspector</th>
                  <th>Voltage</th>
                  <th>Current</th>
                  <th>Finalized</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {records.map(r => (
                  <tr key={r.id}>
                    <td>{r.id}</td>
                    <td>{r.status || '-'}</td>
                    <td>{r.inspectorName || '-'}</td>
                    <td>{r.voltage != null ? r.voltage : '-'}</td>
                    <td>{r.current != null ? r.current : '-'}</td>
                    <td>{r.finalized ? <Badge bg="success">Yes</Badge> : <Badge bg="warning">No</Badge>}</td>
                    <td>
                      <div className="d-flex gap-2">
                        <Button size="sm" variant="outline-primary" onClick={() => navigate(`/maintenance-records/${r.id}`)}>
                          <FontAwesomeIcon icon={faEye} />
                        </Button>
                        <Button size="sm" variant="outline-danger" onClick={() => handleDelete(r.id)}>
                          <FontAwesomeIcon icon={faTrash} />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </Card.Body>
        </Card>
      )}

      <Card className="mt-4">
        <Card.Body className="text-muted small">
          <p className="mb-1"><strong>Workflow:</strong> Select transformer → (optional) inspection → annotation/image → create record → fill engineer fields & finalize.</p>
          <p className="mb-0">If no inspection/annotation chosen, record will not have anomaly overlay until updated.</p>
        </Card.Body>
      </Card>
    </div>
  );
};

export default MaintenanceRecordsPage;
