package com.example.transformer_manager_backkend.dto;

public class AnomalyDTO {
    private Long id;
    private String type;
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    private Double confidence;
    private String comments;
    private String action; // UNCHANGED / MODIFIED / ADDED / DELETED

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getX() { return x; }
    public void setX(Integer x) { this.x = x; }
    public Integer getY() { return y; }
    public void setY(Integer y) { this.y = y; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
