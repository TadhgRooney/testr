package com.testr.dut.dto;

public class BatteryInfo {
    public int levelPct; //Current %
    public int status; //charging status
    public int health; // Battery health
    public int voltageMv; //Millivolts from the sticky BATTERY_CHANGED intent (e.g., 3871)
    public int tempTenthsC; //Temperature in tenths of a °C from BATTERY_CHANGED (e.g., 320 => 32.0°C)
}
