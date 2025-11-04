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
    private double storageFreeGb;
    private double storageTotalGb;
    private double ramFreeGb;
    private double ramTotalGb;
    private LocalDateTime timestamp = LocalDateTime.now();

    public DiagnosticRun() {}

    public DiagnosticRun(String deviceModel, int batteryHealth, double storageFreeGb, double storageTotalGb, double ramFreeGb, double ramTotalGb) {
        this.deviceModel = deviceModel;
        this.batteryHealth = batteryHealth;
        this.storageFreeGb = storageFreeGb;
        this.storageTotalGb = storageTotalGb;
        this.ramFreeGb = ramFreeGb;
        this.ramTotalGb = ramTotalGb;
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }

    public int getBatteryHealth() { return batteryHealth; }
    public void setBatteryHealth(int batteryHealth) { this.batteryHealth = batteryHealth; }

    public double getStorageFreeGb() { return storageFreeGb; }
    public void setStorageFreeGb(double storageFreeGb) { this.storageFreeGb = storageFreeGb; }

    public double getStorageTotalGb() { return storageTotalGb; }
    public void setStorageTotalGb(double storageTotalGb) { this.storageTotalGb = storageTotalGb; }

    public double getRamFreeGb() { return ramFreeGb; }
    public void setRamFreeGb(double ramFreeGb) { this.ramFreeGb = ramFreeGb; }

    public double getRamTotalGb() { return ramTotalGb; }
    public void setRamTotalGb(double ramTotalGb) { this.ramTotalGb = ramTotalGb; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

