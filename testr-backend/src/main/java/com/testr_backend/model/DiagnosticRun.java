package com.testr_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class DiagnosticRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceModel;
    private int batteryHealth;
    private int storageSpeedPct;
    private int cpuPerformancePct;
    private int ramHealthPct;


    private LocalDateTime timestamp = LocalDateTime.now();

    public DiagnosticRun() {}

    public DiagnosticRun(String deviceModel, int batteryHealth, int storageSpeedPct, int cpuPerformancePct, int ramHealthPct) {
        this.deviceModel = deviceModel;
        this.batteryHealth = batteryHealth;
        this.storageSpeedPct = storageSpeedPct;
        this.cpuPerformancePct = cpuPerformancePct;
        this.ramHealthPct = ramHealthPct;
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }

    public int getBatteryHealth() { return batteryHealth; }
    public void setBatteryHealth(int batteryHealth) { this.batteryHealth = batteryHealth; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

