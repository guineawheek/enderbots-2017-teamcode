package org.firstinspires.ftc.teamcode.util;

/**
 * Apparently a class that was supposed to ramp up motors to a set speed over a set amount of time.
 * Not used.
 * @author Guinea on 11/26/16.
 * @deprecated will likely be removed from the codebase before supers.
 */

public class Guinea_MotorRamp {
    private Guinea_Timer timer;
    private double power;
    public Guinea_MotorRamp(double power, long ms) {
        this.power = power;
        this.timer = new Guinea_Timer();
        this.timer.setTarget(ms);
    }
    public void start() {
        timer.reset();
    }

    public double getPower() {
        long ms = timer.getTarget();
        return power * ((double) ms / Math.max(timer.elapsed(), ms));
    }

}
