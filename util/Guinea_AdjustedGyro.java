package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;

import org.firstinspires.ftc.teamcode.logging.Loggable;

/**
 * This class is a wrapper around the Modern Robotics gyroscope sensor handle.
 * <p>The primary purpose of the class is to provide an object can zero its position in software
 * without sending slow and expensive hardware commands to the sensor itself. </p>
 *
 * <p>This class was initially made on 10/22/16</p>
 * @author Guinea
 */

// 103.75%
public class Guinea_AdjustedGyro implements Loggable {

    /* A coefficient that's supposed to be a ratio between the size of a
     * degree according to the gyro and the size of a degree according to reality.
     *
     * In this case it's 1. Historically, we found it to occasionally be 1.0375.
     */
    public static final double ADJUST_FACTOR = 1;
    private ModernRoboticsI2cGyro gyro;
    private double offset; // offset used to store the gyro's new "zero"
    public Guinea_AdjustedGyro(ModernRoboticsI2cGyro gyro) {
        this.gyro = gyro;
        offset = 0;
    }

    /**
     * <p>Yields the continuous heading of the gyro, adjusted for error, relative to the last reset point.
     * This is a wrapper around the gyro's integratedZValue function with an error adjustment factor
     * applied as well as the offset to account for the last position the gyro was reset at.</p>
     *
     * <p>Unlike the getHeading function of the underlying ModernRoboticsI2cGyro, this function is not
     * bounded between 0 and 359, and its returned value will continue to rise as the gyro is turned
     * counterclockwise or fall if the gyro is turned counterclocKwise.</p>
     *
     * <p>To simulate the effect of the original getHeading, one can use the expression
     *     {@code snsGyro.getHeading() % 360}
     * to yield an equivalent result (albeit as a double-precision floating point, not an integer) </p>
     * @return a double representing the value of the gyro's rotation, relative to the last position .reset() was called.
     */
    public double getHeading() {
        return gyro.getIntegratedZValue() * ADJUST_FACTOR - offset;
    }

    /**
     * Resets the gyro's zero position to be its current reading.
     */
    public void reset() {
        offset = gyro.getIntegratedZValue() * ADJUST_FACTOR;
    }

    /**
     * Equivalent to {@link #getHeading()} - should only be used by logging systems.
     * @see Loggable#getLogData()
     * @return the same value as {@link #getHeading()}
     */
    public Object getLogData() { return getHeading(); }
}
