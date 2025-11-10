package com.testr.dut.dto;

public class StorageInfo {
    // Storage speed as % of baseline (0-100), -1 if not available/failed
    public int speedPct;

    public StorageInfo() {
    }

    public StorageInfo(int speedPct) {
        this.speedPct = speedPct;
    }
}
