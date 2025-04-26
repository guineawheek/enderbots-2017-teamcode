package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;

/**
 * Created by Avery on 1/23/17.
 */
@Disabled
public class testbot_teleOP extends OpMode {

    DcMotor[] motors = new DcMotor[4];

    @Override
    public void init() {
        motors[0] = hardwareMap.dcMotor.get("mtrFL");
        motors[1] = hardwareMap.dcMotor.get("mtrBL");
        motors[2] = hardwareMap.dcMotor.get("mtrFR");
        motors[3] = hardwareMap.dcMotor.get("mtrBR");

        for(int i = 0; i < motors.length; i++) {
            motors[i].setDirection(DcMotorSimple.Direction.REVERSE);
        }
    }

    @Override
    public void loop() {
        double x1 = gamepad1.left_stick_x;
        double x2 = gamepad1.right_stick_x;

        double y1 = gamepad1.left_stick_y;

        double frontLeft  = Range.clip(x1 + y1 + x2, -1, 1);
        double frontRight = Range.clip(-x1 + y1 - x2, -1, 1);
        double backLeft   = Range.clip(x1 - y1 - x2, -1, 1);
        double backRight  = Range.clip(-x1 - y1 + x2, -1, 1);

        motors[0].setPower(frontLeft);
        motors[1].setPower(backLeft);
        motors[2].setPower(frontRight);
        motors[3].setPower(backRight);
    }
}
