package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsAnalogOpticalDistanceSensor;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.logging.Loggable;

/**
 * Created by guinea on 2/22/17.
 * Wraps around a range sensor's output, but it throws out bad values (like random 255s)
 */

public class SmoothedRangeSensor implements Loggable {
    private ModernRoboticsI2cRangeSensor snsRange;
    private double cache;
    public SmoothedRangeSensor(ModernRoboticsI2cRangeSensor snsRange) {
        this.snsRange = snsRange;
        cache = 0;
    }

    public double getDistanceCm() {
        double value = snsRange.getDistance(DistanceUnit.CM);
        if (value > 254) return cache;
        cache = value;
        return value;
    }
    @Override
    public Object getLogData() {
        return getDistanceCm();
    }
}
