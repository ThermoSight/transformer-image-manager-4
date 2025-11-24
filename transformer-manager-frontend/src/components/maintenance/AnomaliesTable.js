import React from "react";
import { Table } from "react-bootstrap";

const AnomaliesTable = ({ anomalies }) => {
  if (!anomalies || anomalies.length === 0) {
    return <div className="text-muted">No anomalies.</div>; 
  }
  return (
    <Table size="sm" bordered hover className="mt-3">
      <thead>
        <tr>
          <th>ID</th>
          <th>Type</th>
          <th>Box</th>
          <th>Confidence</th>
          <th>Action</th>
          <th>Comments</th>
        </tr>
      </thead>
      <tbody>
        {anomalies.map((a) => (
          <tr key={a.id}>
            <td>{a.id}</td>
            <td>{a.type}</td>
            <td>
              {a.x},{a.y} {" "}({a.width}x{a.height})
            </td>
            <td>{a.confidence != null ? a.confidence.toFixed(2) : "-"}</td>
            <td>{a.action}</td>
            <td>{a.comments || ""}</td>
          </tr>
        ))}
      </tbody>
    </Table>
  );
};

export default AnomaliesTable;
