package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Created by guinea on 2/20/17.
 */

public class TelemetryLogOutput implements LogOutput {

    private Telemetry telemetry;

    public TelemetryLogOutput(Telemetry telemetry) {
        this.telemetry = telemetry;
    }

    @Override
    public void open() {
        // derp
    }

    @Override
    public void setHeaders(String... headers) {
        // derp
    }

    @Override
    public void logData(String name, String value) {
        telemetry.addData(name, value);
    }

    @Override
    public void flush() {
        telemetry.update();
    }

    @Override
    public void close() {
        // derp
    }
}
