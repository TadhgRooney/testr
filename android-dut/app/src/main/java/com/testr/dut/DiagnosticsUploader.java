package com.testr.dut;

import android.content.Context;

import com.google.gson.Gson;
import com.testr.dut.dto.DiagnosticReport;

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

    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

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


     // Build the payload to send to the backend from the collected report.

    public DiagnosticRunPayload buildPayloadFromReport(DiagnosticReport report) {
        DiagnosticRunPayload payload = new DiagnosticRunPayload();

        // Basic info
        payload.deviceModel = report.model;

        // Battery: 0-100 health %, -1 if unknown
        payload.batteryHealth = (report.battery != null)
                ? report.battery.healthPct
                : -1;

        // Storage speed: 0-100 %, -1 if failed
        payload.storageSpeedPct = (report.storage != null)
                ? report.storage.speedPct
                : -1;

        // CPU performance: 0-100 %, -1 if failed
        payload.cpuPerformancePct = (report.cpu != null)
                ? report.cpu.performancePct
                : -1;

        payload.ramHealthPct = (report.ram != null)
                ? report.ram.ramHealthPct
                : -1;

        //-1 for now, might add camera check and display % later
        payload.cameraCheckPct = -1;
        payload.displayTouchPct = -1;
        return payload;
    }




    // Send the payload to the backend.
    public void upload(Context ctx,
                       DiagnosticRunPayload payload,
                       ResultCallback cb) {

        String bodyJson = gson.toJson(payload);

        Request req = new Request.Builder()
                .url(baseUrl + "/v1/diagnostics")
                .post(RequestBody.create(bodyJson, JSON))
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (cb != null) cb.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (Response res = response) {
                    String resp = (res.body() != null) ? res.body().string() : "";
                    if (res.isSuccessful()) {
                        if (cb != null) cb.onSuccess(resp);
                    } else {
                        if (cb != null) {
                            cb.onError(new RuntimeException("HTTP " + res.code() + ": " + resp));
                        }
                    }
                } catch (Exception ex) {
                    if (cb != null) cb.onError(ex);
                }
            }
        });
    }
}

