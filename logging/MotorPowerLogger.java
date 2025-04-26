package org.firstinspires.ftc.teamcode.logging;

import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Created by guinea on 2/19/17.
 */

public class MotorPowerLogger implements Loggable {
    private DcMotor mtr;
    public MotorPowerLogger(DcMotor mtr) {
        this.mtr = mtr;
    }

    public Object getLogData() {
        return mtr.getPower();
    }
}
