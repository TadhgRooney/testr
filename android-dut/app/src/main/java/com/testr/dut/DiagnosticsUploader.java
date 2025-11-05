package com.testr.dut;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DiagnosticsUploader {


    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final String baseUrl;
    private final Gson gson = new Gson();


    public interface ResultCallback {
        void onSuccess(String resp);
        void onError(Exception e);
    }


    public DiagnosticsUploader(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .build();
    }

    // --- helpers ---
    private static double bytesToGb(long bytes) {
        return (bytes > 0) ? (bytes / (1024.0 * 1024.0 * 1024.0)) : -1.0;
    }

    private double[] getStorageTotalsGb() { return new double[]{ -1.0, -1.0 }; }
    private double[] getRamTotalsGb()     { return new double[]{ -1.0, -1.0 }; }

    public DiagnosticRunPayload buildPayloadFromReport(com.testr.dut.dto.DiagnosticReport report) {
        DiagnosticRunPayload payload = new DiagnosticRunPayload();

        // basics
        payload.deviceModel   = report.model;
        payload.batteryHealth = (report.battery != null) ? report.battery.levelPct : -1;

        // --- STORAGE ---
        if (report.storage != null) {
            double totalGb = bytesToGb(report.storage.internalTotalBytes);
            double freeGb  = bytesToGb(report.storage.internalFreeBytes);
            if (totalGb > 0 && freeGb >= 0) {
                payload.storageTotalGb = totalGb;
                payload.storageFreeGb  = freeGb;
            } else {
                double[] s = getStorageTotalsGb();
                payload.storageFreeGb  = s[0];
                payload.storageTotalGb = s[1];
            }
        } else {
            double[] s = getStorageTotalsGb();
            payload.storageFreeGb  = s[0];
            payload.storageTotalGb = s[1];
        }

        // --- RAM ---
        if (report.memoryCpu != null) {
            double ramTotalGb = bytesToGb(report.memoryCpu.totalRamBytes);
            double ramFreeGb  = bytesToGb(report.memoryCpu.availRamBytes);
            if (ramTotalGb > 0 && ramFreeGb >= 0) {
                payload.ramTotalGb = ramTotalGb;
                payload.ramFreeGb  = ramFreeGb;
            } else {
                double[] r = getRamTotalsGb();
                payload.ramFreeGb  = r[0];
                payload.ramTotalGb = r[1];
            }
        } else {
            double[] r = getRamTotalsGb();
            payload.ramFreeGb  = r[0];
            payload.ramTotalGb = r[1];
        }

        return payload;
    }



    public void upload(Context ctx, DiagnosticRunPayload payload, ResultCallback cb) {
        String bodyJson = gson.toJson(payload);

        Request req = new Request.Builder()
                .url(baseUrl + "/v1/diagnostics")
                .post(RequestBody.create(bodyJson, JSON))
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                if (cb != null) cb.onError(e);
            }

            @Override public void onResponse(Call call, Response response) {
                try (Response res = response) {
                    String resp = res.body() != null ? res.body().string() : "";
                    if (res.isSuccessful()) {
                        if (cb != null) cb.onSuccess(resp);
                    } else {
                        if (cb != null) cb.onError(new RuntimeException("HTTP " + res.code() + ": " + resp));
                    }
                } catch (Exception ex) {
                    if (cb != null) cb.onError(ex);
                }
            }
        });
    }
}

