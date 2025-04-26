package org.firstinspires.ftc.teamcode.logging;

/**
 * Created by guinea on 2/20/17.
 */

public interface LogOutput {
    void open();
    void setHeaders(String... headers);
    void logData(String name, String value);
    void flush();
    void close();
}
