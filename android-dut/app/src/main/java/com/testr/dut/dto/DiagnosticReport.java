package com.testr.dut.dto;

import java.util.List;

public class DiagnosticReport {
    public String sessionId;
    public String manufacturer;
    public String model;
    public String product;
    public String androidVersion;
    public String securityPatch;
    public BatteryInfo battery;
    public StorageInfo storage;
    public MemoryCpuInfo memoryCpu;
    public List<SensorInfo> sensors;
    public ConnectivityInfo connectivity;
    public CameraInfo cameras;
    public long collectedAtEpochMs;
}
