package org.firstinspires.ftc.teamcode.logging;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This is the main controlling class in our logging framework.
 * <p>The way this framework works is that this class subclasses {@link Thread}, and thus can be
 * treated exactly like one. This class is first created with a predefined polling interval, which if not specified
 * is by default every 100 milliseconds or 10Hz.
 * Before it is started to log data to various outputs, an instance of this
 * class can have data inputs (like sensors) added to it using {@link #addSource(String, Loggable)}
 * and outputs to which data is sent using {@link #addOutput(LogOutput)}. Then, when everything is all
 * added in, the logging object can then be sent to start logging by calling {@link #start()} just like any
 * other {@link Thread} and stopped with {@link #interrupt()}. To ensure the thread is dead when the opmode exits,
 * the opmode should also check to ensure {@link #isAlive()} is false before exiting.</p>
 */

public class BackgroundLogger extends Thread {
    private LinkedHashMap<String, DataSource> sources;
    private LinkedList<LogOutput> outputs;
    private long msInterval;
    private boolean enabled;

    private class DataSource {
        private Loggable loggable;
        private boolean enabled;
        public DataSource(Loggable loggable) {
            this.loggable = loggable;
            this.enabled = true;
        }
        public synchronized String getData() {
            if (!enabled) return "";
            return loggable.getLogData().toString();
        }
        public synchronized boolean isEnabled() { return enabled; }
        public synchronized void enable() { enabled = true; }
        public synchronized void disable() { enabled = false; }
    }

    public BackgroundLogger(long msInterval) {
        sources = new LinkedHashMap<>();
        outputs = new LinkedList<>();
        this.msInterval = msInterval;
        this.enabled = true;
    }

    public BackgroundLogger() {
        this(100);
    }

    public long getPollingInterval() {
        return msInterval;
    }

    public void setPollingInterval(long newInterval) {
        msInterval = newInterval;
    }


    public void addSource(String name, Loggable source) {
        sources.put(name, new DataSource(source));
    }

    public void removeSource(String name)  {
        if (sources.containsKey(name)) sources.remove(name);
    }

    public synchronized void enableSource(String name) {
        if (sources.containsKey(name)) sources.get(name).enable();
    }

    public synchronized void disableSource(String name) {
        if (sources.containsKey(name)) sources.get(name).disable();
    }

    public synchronized void enableSources(String... names) {
        for (String name: names) enableSource(name);
    }

    public synchronized void disableSources(String... names) {
        for (String name: names) disableSource(name);
    }

    public void addOutput(LogOutput output) {
        outputs.add(output);
    }

    public void removeOutput(LogOutput output) {
        outputs.remove(output);
    }

    public synchronized void enable() { enabled = true; }
    public synchronized void disable() { enabled = false; }
    public synchronized boolean getEnabled() { return enabled; }

    @Override
    public void run() {
        String[] headers = new String[sources.keySet().size()];
        int i = 0;
        for (String header : sources.keySet()) {
            if (i == headers.length) break;
            headers[i] = header;
            i++;
        }
        for (LogOutput output: outputs)  {
            output.open();
            output.setHeaders(headers);
        }
        while (!isInterrupted()) {
            if (getEnabled()) {
                for (Map.Entry<String, DataSource> entry : sources.entrySet()) {
                    for (LogOutput output: outputs) output.logData(entry.getKey(), entry.getValue().getData());
                }
                for (LogOutput output : outputs) output.flush();
            }
            try {
                Thread.sleep(msInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
        for (LogOutput output : outputs) output.close();
    }

}
