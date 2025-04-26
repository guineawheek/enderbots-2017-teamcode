package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.util.Logger;

import java.util.LinkedList;

/**
 * Created by guinea on 2/20/17.
 */

public class FileLogOutput implements LogOutput {
    private Logger logger;
    private LinkedList<String> writebuf;

    public FileLogOutput(String extname) {
        this.logger = new Logger(extname);
        writebuf = new LinkedList<>();
    }

    @Override
    public void open() {
    }

    @Override
    public void setHeaders(String... headers) {
        logger.logRow(headers);
    }

    @Override
    public void logData(String name, String value) {
        writebuf.add(value);
    }

    @Override
    public void flush() {
        logger.logRow(writebuf.toArray());
        writebuf.clear();
    }

    @Override
    public void close() {
        logger.close();
    }
}
