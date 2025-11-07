package com.testr_backend.web;

public class DiagnosticRunRequest {
    public String deviceModel;
    public int batteryHealth; //0-100 = health %, -1 = unknown
    public double storageFreeGb;
    public double storageTotalGb;
    public double ramFreeGb;
    public double ramTotalGb;

}
