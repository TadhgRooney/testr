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
    private int displayTouchPct;
    private int cameraCheckPct;


    private LocalDateTime timestamp = LocalDateTime.now();

    public DiagnosticRun() {}

    public DiagnosticRun(String deviceModel, int batteryHealth, int storageSpeedPct, int cpuPerformancePct, int ramHealthPct, int displayTouchPct, int cameraCheckPct) {
        this.deviceModel = deviceModel;
        this.batteryHealth = batteryHealth;
        this.storageSpeedPct = storageSpeedPct;
        this.cpuPerformancePct = cpuPerformancePct;
        this.ramHealthPct = ramHealthPct;
        this.displayTouchPct = displayTouchPct;
        this.cameraCheckPct = cameraCheckPct;
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }

    public int getBatteryHealth() { return batteryHealth; }
    public void setBatteryHealth(int batteryHealth) { this.batteryHealth = batteryHealth; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getCameraCheckPct() {
        return cameraCheckPct;
    }

    public void setCameraCheckPct(int cameraCheckPct) {
        this.cameraCheckPct = cameraCheckPct;
    }

    public int getDisplayTouchPct() {
        return displayTouchPct;
    }

    public void setDisplayTouchPct(int displayTouchPct) {
        this.displayTouchPct = displayTouchPct;
    }

    public int getRamHealthPct() {
        return ramHealthPct;
    }

    public void setRamHealthPct(int ramHealthPct) {
        this.ramHealthPct = ramHealthPct;
    }

    public int getCpuPerformancePct() {
        return cpuPerformancePct;
    }

    public void setCpuPerformancePct(int cpuPerformancePct) {
        this.cpuPerformancePct = cpuPerformancePct;
    }
}

