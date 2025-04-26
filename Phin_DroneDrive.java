package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.util.Guinea_AdjustedGyro;

/**
 * Created by Avery on 2/19/17.
 */

public class Phin_DroneDrive  {

    public double fl;
    public double fr;
    public double bl;
    public double br;

    public Phin_DroneDrive() {

    }

    public void setMotorPowers(double x1, double x2, double y1, double heading) {
        fl = x1 * (Math.cos(heading) + Math.sin(heading)) + y1 * (Math.cos(heading) - Math.sin(heading)) - x2;
        fr = x1 * (Math.cos(heading) - Math.sin(heading)) + y1 * (-Math.cos(heading) - Math.sin(heading)) + x2;
        bl = x1 * (-Math.cos(heading) + Math.sin(heading)) + y1 * (Math.cos(heading) + Math.sin(heading)) + x2;
        br = x1 * (-Math.cos(heading) - Math.sin(heading)) + y1 * (-Math.cos(heading) + Math.sin(heading)) - x2;
    }

}
