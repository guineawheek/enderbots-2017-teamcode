package org.firstinspires.ftc.teamcode.util;

import org.firstinspires.ftc.teamcode.logging.Loggable;

/**
 * A non-blocking timer and stopwatch, measuring everything to the millisecond.
 *
 * <p>Created on 11/17/16.</p>
 * @author Guinea
 */

public class Guinea_Timer implements Loggable {
    private long target;
    private long start;

    /**
     * The constructor.
     *
     * <p>Alone, one only needs this to use it as a stopwatch from the moment of initialization.
     * All one has to do is call {@link #elapsed()} and one will get time since initialization.</p>
     *
     * <p>By default, however, {@link #done()} will always return true, so in order to use it as a countdown,
     * one will have to call {@link #setTarget(long)} which also resets the point of zero time elapsed.
     * </p>
     */
    public Guinea_Timer() {
        target = 0;
        reset();
    }

    /**
     * Constructs the object with a timer.
     *
     * <p>Equivalent to calling {@code setTarget(target)} right after constructing the object.</p>
     * @param target
     */
    public Guinea_Timer(long target) {
        setTarget(target);
    }

    /**
     * Sets the countdown target, in milliseconds, and resets the elapsed time counter back to zero.
     * @param target the number of milliseconds in the future required to pass before {@link #done()} will return true.
     */
    public void setTarget(long target) {
        this.target = target;
        reset();
    }

    /**
     * Gets the number of milliseconds required to elapse after the last reset of the elapsed time
     * counter before {@link #done()} will return true.
     * @return the set number of milliseconds as a long
     */
    public long getTarget() {
        return target;
    }

    /**
     * Resets the elapsed time counter back to zero.
     *
     * <p>If a target is set, {@link #done()} will not return zero until the set target number of
     * milliseconds have elapsed again.</p>
     *
     * <p>This also means {@link #elapsed()} will return the number of milliseconds since this
     * function was last called.</p>
     */
    public void reset() {
        start = System.currentTimeMillis();
    }

    /**
     * Checks if the set target number of milliseconds has elapsed since the last time the timer was
     * reset.
     * @return true if such is indeed true.
     */
    public boolean done() {
        return System.currentTimeMillis() >= (start + target);
    }

    /**
     * Returns as a double the number of milliseconds since the last time the timer was reset.
     * @return the number of milliseconds that have elapsed as a double
     */
    public double elapsed() {
        return System.currentTimeMillis() - start;
    }

    /**
     * Returns the same value as {@link #elapsed()}, but in seconds.
     * @see Loggable#getLogData()
     */
    public Object getLogData() { return (elapsed() / 1000d); }
}
