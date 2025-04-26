package org.firstinspires.ftc.teamcode.logging;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;

/**
 * Created by guinea on 2/19/17.
 */

public class RawGyroLogger implements Loggable {
    ModernRoboticsI2cGyro snsGyroRaw;
    public RawGyroLogger(ModernRoboticsI2cGyro snsGyroRaw) {
        this.snsGyroRaw = snsGyroRaw;
    }
    @Override
    public Object getLogData() {
        return snsGyroRaw.getIntegratedZValue();
    }
}
