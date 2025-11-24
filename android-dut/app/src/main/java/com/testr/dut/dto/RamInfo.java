package com.testr.dut.dto;

public class RamInfo {

    // RAM condition as % of expected (0-100), -1 if unknown
    public int ramHealthPct;

    public RamInfo() { }

    public RamInfo(int ramHealthPct) {
        this.ramHealthPct = ramHealthPct;
    }
}
