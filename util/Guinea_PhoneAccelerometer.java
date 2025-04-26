package org.firstinspires.ftc.teamcode.util;

import android.hardware.Sensor;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Created by guinea on 12/26/16.
 * A class (still in testing) used to pull accelerometer values from the robot controller phone itself.
 * Use aX, aY, and aZ to get raw accelerometer values.
 *
 * Kalman filter work needs to be done in order to make them return accurate-enough results.
 * The end product is probably going to be a motion tracker...
 * TODO: kalman filters - do we still need this class?
 *
 */

public class Guinea_PhoneAccelerometer extends Guinea_PhoneSensor {
    public Guinea_PhoneAccelerometer(HardwareMap hardwareMap) {
        super(hardwareMap);
    }

    @Override
    public int getSensorType() {
        // takes default accelerometer and subtracts gravity
        return Sensor.TYPE_LINEAR_ACCELERATION;
    }
    @Override
    public String toString() {
        return String.format("x: %f\ny: %f\nz: %f", values[0], values[1], values[2]);
    }

    @Override
    protected void onNewData() {
        // some sort of low-pass filter data
    }

    public double aX() {
        return values[0];
    }

    public double aY() {
        return values[1];
    }

    public double aZ() {
        return values[2];
    }
}
