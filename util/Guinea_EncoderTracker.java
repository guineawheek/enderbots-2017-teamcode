package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.Guinea_hwInit;
import org.firstinspires.ftc.teamcode.logging.Loggable;

/**
 * Created by guinea on 10/24/16.
 * A huge mess.
 * It has two primary goals - measure encoder counts from a resettable offset, and check if the encoder
 * is within a set target range.
 * TODO: strip this class down after we kill {@link Guinea_hwInit}
 *
 * Apparently some other incomplete code got shoved in here.
 */

public class Guinea_EncoderTracker implements Loggable {
    public int zero;
    public static final double slowCoeff = 0.001;
    public static final int threshold = 50;
    public static final int deadzone = 50;

    public double velocity; // in encoder counts/time; "instantanious"
    private long lastMillis;
    private double lastPos;
    private DcMotor motor;

    // doesn't do anything on it's own, allows us to poll easier
    private int target;
    private int error;
    public Guinea_EncoderTracker(DcMotor motor) {
        this.motor = motor;
        zero = 0;
        target = 0;
        velocity = 0;
        lastPos = 0;
        lastMillis = System.currentTimeMillis();
    }

    public void reset() {
        zero = motor.getCurrentPosition();
        velocity = 0;
        lastMillis = System.currentTimeMillis();
    }

    public int getCurrentPosition() {
        double pos = motor.getCurrentPosition() - zero;
        long now = System.currentTimeMillis();
        // prevent divide by zero chance
        if (now == lastMillis) { now++; }
        velocity = (pos - lastPos) / (now - lastMillis) * 1000;
        lastMillis = now;
        lastPos = pos;
        return (int) pos;
    }

    public double getRotations() {
        //TODO: this expression is INVALID for motors that aren't the same as drivebase motors!!!
        return (double) getCurrentPosition() / Guinea_hwInit.DISTANCE_PER_ROT;
    }

    public void setTarget(int newtarget)  {
        target = newtarget;
    }

    public int getTarget() {
        return target;
    }

    public void setError(int newError) {
        error = newError;
    }

    public int getError() {
        return error;
    }

    // helps tell us whether we should go forward or backwards
    // returns 1 if we need to go forward
    // returns 0 if we are on target
    // returns -1 if we overshot
    public double whereTarget() {
        int dist =  target - getCurrentPosition();

        if (Math.abs(dist) < deadzone) return 0;
        if (Math.abs(dist) < threshold) return dist * slowCoeff;
        if (dist > threshold) return 1;
        if (dist < -threshold) return -1;
        return 0;
    }

    /**
     * also accounts for deadzone
     * @return
     */
    public double pid() {
        int err = target - getCurrentPosition();

        return 0;
    }


    public boolean onTarget() {
        return whereTarget() == 0;
    }

    public Object getLogData() { return getCurrentPosition(); }
}
