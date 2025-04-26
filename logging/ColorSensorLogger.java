package org.firstinspires.ftc.teamcode.logging;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cColorSensor;

import org.firstinspires.ftc.teamcode.logging.Loggable;

/**
 * Created by guinea on 2/19/17.
 */

public class ColorSensorLogger implements Loggable {
    ModernRoboticsI2cColorSensor snsColor;
    public ColorSensorLogger(ModernRoboticsI2cColorSensor snsColor) {
        this.snsColor = snsColor;
    }
    @Override
    public Object getLogData() {
        return String.format("(%d %d %d %d)", snsColor.red(), snsColor.green(), snsColor.blue(), snsColor.alpha());
    }
}
