import React from "react";
import "./overlay.css";

const ImageWithOverlay = ({ imageUrl, anomalies }) => {
  return (
    <div style={{ position: "relative", display: "inline-block" }}>
      <img
        src={imageUrl}
        alt="thermal"
        style={{ maxWidth: "100%", display: "block" }}
      />
      {anomalies.map((a) => (
        <div
          key={a.id}
          title={`${a.type} (${a.action})`}
          style={{
            position: "absolute",
            left: a.x,
            top: a.y,
            width: a.width,
            height: a.height,
            border: "2px solid #ff5722",
            boxSizing: "border-box",
            pointerEvents: "none",
          }}
        />
      ))}
    </div>
  );
};

export default ImageWithOverlay;
