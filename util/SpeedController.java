package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.logging.Loggable;

/**
 * A class that properly velocity-control the flywheel motors at acceptable speeds.
 *
 * <p>
 *      What's special about it is how it goes about its task - it implements its own proportional velocity control
 *      on top of the motor controller's own velocity control. The motor controller's built-in velocity
 *      control is very good at making the relationship between power and velocity more linear, but
 *      ultimately isn't great at keeping the motors spinning at a velocity consistent across battery voltage.
 *      It's very precise, but not accurate. Thus, using proportional control, the robot can compensate
 *      for this inconsistency by manually measuring shooting speed and automatically adjusting for differences.
 * </p>
 *
 * <p>
 *     Alternatively, if proportional control isn't desired, it works just fine as a monitor of shooter spinning
 *     velocity.
 * </p>
 */

public class SpeedController implements Loggable {
    private DcMotor mtr;
    private Guinea_EncoderTracker enc;
    private long startTime;
    private long timeLimit;
    public static PidThread pidThread;
    private int targetPower;
    private double velocity;
    private boolean active;
    private boolean pidActive;
    private int spoolTimer;
    private double err;
    private double correction;

    private CircleBufferAvg avg;

    // the shooter has five modes: off, low, medium, high, and autonomous power.
    // the low/med/high modes are used in teleop, but the autonomous power is used for autonomous shooting.

    // the powers listed here are an initial guess of shooter power - from here, velocity can be adjusted for
    private double[] powers = {0, .25, .35, .50, .25};

    // these are the target velocities in encoder counts per second.
    private double[] velocities = {0, 1700, 1900, 2000, 2200};
    public static final int NO_POWER = 0;
    public static final int LOW_POWER = 1;
    public static final int MED_POWER = 2;
    public static final int HIGH_POWER = 3;
    public static final int AUTONOMOUS_POWER = 4;

    // the proportional constant used
    private static final double gain = 1/3000d;
    // allows the shooter time to spool up to prevent overcompensating from lower values.
    private static int spoolTime = 2;

    // This pid thread is responsible for measuring motor power and speed in the background.
    // To prevent crashing, the AutonomousOpMode is made to guarantee the thread dies at the end of
    // autonomous
    private class PidThread extends Thread {
        public void run() {
            startTime = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();
            enc.reset();
            long previousTime = currentTime;
            long timeDiff;
            long previousEnc = 0;
            long currentEnc = 0;
            long encDiff = 0;

            while ((currentTime - startTime) < timeLimit && active ) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
                currentTime = System.currentTimeMillis();
                timeDiff = currentTime - previousTime;
                currentEnc = enc.getCurrentPosition();
                encDiff = currentEnc - previousEnc;

                // calculate velocity
                velocity = encDiff / ((double) timeDiff / 1000d);

                // output = base + error * coeff

                previousTime = currentTime;
                previousEnc = currentEnc;

                avg.update(velocity);

                if (pidActive) {
                    // if we are allowed to control shooter power, adjust the motor powers
                    if (spoolTimer > 0) {
                        mtr.setPower(powers[targetPower]);
                        spoolTimer--;
                        continue;
                    }
                    double targetVelocity = velocities[targetPower];
                    double basePower = powers[targetPower];
                    err = targetVelocity - avg.getAverage();
                    correction = err * gain;
                    mtr.setPower(Range.clip(basePower + correction, 0, 1));
                }
            }
        }
    }
    public SpeedController(DcMotor mtr, Guinea_EncoderTracker enc, long timeLimit) {
        this.mtr = mtr;
        this.enc = enc;
        this.timeLimit = timeLimit;
        startTime = -1;
        targetPower = NO_POWER;
        velocity = 0;
        active = false;
        pidActive = false;
        spoolTimer = spoolTime;
        avg = new CircleBufferAvg(10);
    }

    // start measuring motor velocity and if enabled also start controlling motor power
    public void start() {
        if (pidThread != null) {
            active = false;
            pidThread.interrupt();
        }
        pidThread = new PidThread();
        pidThread.start();
        active = true;
    }

    // kill the pid threads
    public void stop() {
        active = false;
        if (pidThread == null) return;
        pidThread.interrupt();
    }

    // used to check if the pid thread is alive or dead to help synchronization and prevent crashes
    public boolean pidThreadAlive() {
        return pidThread.isAlive();
    }

    // enable control over motor power
    public void enablePid() {
        pidActive = true;
        spoolTimer = spoolTime;
    }

    // disable control over motor power
    public void disablePid() {
        pidActive = false;
        spoolTimer = spoolTime;
    }

    public synchronized void setPower(int powerLevel) {
        targetPower = powerLevel;
        spoolTimer = spoolTime;
    }

    public synchronized double getPower() {
        return targetPower;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getAvgVelocity() {
        return avg.getAverage();
    }
    public double getErr() { return err; }
    public double getCorrection() { return correction; }
    // debatable
    public Object getLogData() { return getVelocity(); }

}
