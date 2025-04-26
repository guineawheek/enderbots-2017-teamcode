package org.firstinspires.ftc.teamcode.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Dumps arbitrary csv data to the phone's internal storage.
 * <p>Used to record motor power and sensor data in autonomous.</p>
 * Usage:
 *
 * Logger logger = Logger.getLogger();
 *
 * logger.logRow("data", "more data", 0xdeadmeat, Math.PI, new StringBuilder("asdf"), ...);
 *
 * created 12/22/16
 */

public class Logger {
    private static Logger instance;
    private File file;
    private Writer writer;
    private String path;
    private Queue<String> writeBuffer;

    /**
     * Constructs the logger with a little more information in the name than just a timestamp, as
     * passed as a string.
     * @param extname extra text inserted between the timestamp and file extension part of the logfile filename.
     */
    public Logger(String extname) {
        if (!isExternalStorageWritable()) {
            Log.e("5484-teamcode", "we can't write to ext storage! discarding all data...");
        }
        writeBuffer = new LinkedList();
        //openFile("default.csv", true);
        openDefaultFile(extname);
    }

    /**
     * Constructs the logger.
     */
    public Logger() { this(""); }


    /**
     *  Checks if external storage is available for read and write
     *
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Residue from the days this was intended as a singleton.
     * @Deprecated use the constructor instead.
     * @return a new instance of the logger
     */
    public static Logger getLogger() {
        return new Logger();
    }

    /**
     * Flushes the write buffer of the logger to the external storage of the phone.
     */
    public synchronized void flush() {
        if (writer == null) return;
        while (!writeBuffer.isEmpty()) {
            try {
                writer.write(writeBuffer.remove());
            } catch (IOException e){}
        }
    }

    /**
     * Closes the logfile the logger was writing to. Doesn't do anything on an already closed logger.
     */
    public void close() {
        if (writer == null) return;
        try {
            flush();
            writer.close();
            writer = null;
        } catch (IOException e) {}
    }

    private void openDefaultFile(String extname) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String now = dateFormat.format(new Date());

        File destdir = new File(Environment.getExternalStorageDirectory(), "ftc-logfiles");
        destdir.mkdir();
        file = new File(destdir, now + extname + ".csv");
        if (writer != null) {
            close();
        }

        try {
            if (isExternalStorageWritable()) {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)));
            }
        } catch (IOException e) {}

    }
    public void openFile(String name, boolean append) {
        file = new File(Environment.getExternalStorageDirectory(), name);
        if (writer != null) {
            close();
        }

        try {
            if (isExternalStorageWritable()) {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append)));
            }
        } catch (IOException e) {}

    }
    public synchronized void logRow(String... entries) {
        StringBuilder line = new StringBuilder();
        for (String v : entries) {
            line.append(v);
            line.append(",");
        }
        // replace the last comma with a newline
        line.setCharAt(line.length() - 1, '\n');
        try {
            writeBuffer.add(line.toString());
            flush();
        } catch (Exception e) {}
    }

    public synchronized void logRow(double... entries) {
        String[] strEntries = new String[entries.length];
        for (int i = 0; i < entries.length; i++){
            strEntries[i] = Double.toString(entries[i]);
        }
        logRow(strEntries);
    }

    public synchronized void logRow(Object... entries) {
        String[] strEntries = new String[entries.length];
        for (int i = 0; i < entries.length; i++){
            strEntries[i] = entries[i].toString();
        }
        logRow(strEntries);
    }
}
