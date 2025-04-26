package org.firstinspires.ftc.teamcode.logging;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Created by guinea on 2/19/17.
 */

public class RangeSensorLogger implements Loggable {
    ModernRoboticsI2cRangeSensor snsRange;
    public RangeSensorLogger(ModernRoboticsI2cRangeSensor snsRange) {
        this.snsRange = snsRange;
    }
    @Override
    public Object getLogData() {
        return snsRange.getDistance(DistanceUnit.CM);
    }
}
