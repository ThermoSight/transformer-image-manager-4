import React from "react";
import { Form } from "react-bootstrap";

const EngineerFields = ({ values, onChange, disabled }) => {
  const set = (e) => onChange(e.target.name, e.target.value);
  return (
    <Form>
      <Form.Group className="mb-2">
        <Form.Label>Inspector Name</Form.Label>
        <Form.Control
          name="inspectorName"
            value={values.inspectorName || ""}
          onChange={set}
          disabled={disabled}
        />
      </Form.Group>
      <Form.Group className="mb-2">
        <Form.Label>Status</Form.Label>
        <Form.Select
          name="status"
          value={values.status || "OK"}
          onChange={set}
          disabled={disabled}
        >
          <option value="OK">OK</option>
          <option value="NEEDS_MAINTENANCE">Needs Maintenance</option>
          <option value="URGENT_ATTENTION">Urgent Attention</option>
        </Form.Select>
      </Form.Group>
      <Form.Group className="mb-2">
        <Form.Label>Voltage</Form.Label>
        <Form.Control
          type="number"
          step="0.01"
          name="voltage"
          value={values.voltage || ""}
          onChange={set}
          disabled={disabled}
        />
      </Form.Group>
      <Form.Group className="mb-2">
        <Form.Label>Current</Form.Label>
        <Form.Control
          type="number"
          step="0.01"
          name="current"
          value={values.current || ""}
          onChange={set}
          disabled={disabled}
        />
      </Form.Group>
      <Form.Group className="mb-2">
        <Form.Label>Recommended Action</Form.Label>
        <Form.Control
          as="textarea"
          rows={2}
          name="recommendedAction"
          value={values.recommendedAction || ""}
          onChange={set}
          disabled={disabled}
        />
      </Form.Group>
      <Form.Group className="mb-2">
        <Form.Label>Corrective Actions</Form.Label>
        <Form.Control
          as="textarea"
          rows={2}
          name="correctiveActions"
          value={values.correctiveActions || ""}
          onChange={set}
          disabled={disabled}
        />
      </Form.Group>
      <Form.Group className="mb-2">
        <Form.Label>Additional Remarks</Form.Label>
        <Form.Control
          as="textarea"
          rows={3}
          name="additionalRemarks"
          value={values.additionalRemarks || ""}
          onChange={set}
          disabled={disabled}
        />
      </Form.Group>
    </Form>
  );
};

export default EngineerFields;
