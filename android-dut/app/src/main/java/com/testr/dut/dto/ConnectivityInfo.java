package com.testr.dut.dto;

public class ConnectivityInfo {
    //WI-FI
    public boolean wifiEnabled;
    public boolean wifiConnected;
    public String wifiSsid;
    public int wifiRssi;
    public int wifiLinkSpeedMbps;

    //Cellular
    public boolean cellularReady;
    public String carrierName;
    public String dataNetworkType;
    public int cellSignalDbm = Integer.MIN_VALUE;

    //Bluetooth
    public boolean bluetoothSupported;
    public boolean bluetoothEnabled;
    public boolean bluetoothAnyProfileConnected;

    //Other
    public boolean airplaneModeOn;
    public String activeTransport;
}
