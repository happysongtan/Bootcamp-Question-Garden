package com.example.questionboard.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppConfigController {

    private final String googleAnalyticsMeasurementId;

    public AppConfigController(
            @Value("${app.google-analytics.measurement-id:}") String googleAnalyticsMeasurementId
    ) {
        this.googleAnalyticsMeasurementId = googleAnalyticsMeasurementId;
    }

    @GetMapping(value = "/config.js", produces = "application/javascript")
    public String config() {
        return "window.appConfig = {"
                + "\"googleAnalyticsMeasurementId\":\"" + escapeJavaScript(googleAnalyticsMeasurementId) + "\""
                + "};";
    }

    private String escapeJavaScript(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
