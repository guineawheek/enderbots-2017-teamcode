package org.firstinspires.ftc.teamcode.util;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Created by guinea on 1/29/17.
 */

public class TelemetryManager {
    private Telemetry telemetry;
    private String[] headers;
    private Logger logger;

    public TelemetryManager(Telemetry telemetry) {
        this.telemetry = telemetry;
        this.logger = Logger.getLogger();
    }
    public void pushHeaders(String... headers) {
        logger.logRow(headers);
        this.headers = headers;
    }
    public void pushValues(Object... values) {
        for (int i = 0; i < values.length; i++) {
            if (i < headers.length) {
                telemetry.addData(headers[i], values[i]);
            } else {
                telemetry.addData(String.format("value%d", i), values[i]);
            }
        }
        logger.logRow(values);
        telemetry.update();
    }
    public void close() {
        logger.close();
    }
}
