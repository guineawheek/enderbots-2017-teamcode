package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.util.SpeedController;

import static org.firstinspires.ftc.teamcode.Guinea_hwInit.*;
/**
 * Created by guinea on 1/12/17.
 */
@TeleOp(name="Shooter test teleop (DO NOT USE)", group="test")
public class TestShooter extends LinearOpMode {

    boolean rightEdge = false;
    boolean leftEdge = false;
    boolean upEdge = false;
    boolean downEdge = false;
    double runningSum = 0;
    long n = 1;
    double power = 0.30;
    long velocity = 1700;
    @Override
    public void runOpMode() throws InterruptedException {
        Guinea_hwInit.init(hardwareMap);
        mtrShooter.setMaxSpeed(3000);
        shooter.enablePid();
        //Avery_ColorSensor16Bit_V1 snsColorRevolver = new Avery_ColorSensor16Bit_V1(hardwareMap, "snsColorRevolver", 0x3a);
        waitForStart();
        svoFeeder.setPosition(0.99);
        while (opModeIsActive()) {
            if (gamepad2.a) {
                try {
                    // swallow the InterruptedException so we can guarentee the pid thread dies
                    shoot();
                } finally {
                    //shooter.stop();
                }
            } else {
                svoFlick.setPosition(0.1);
            }

            if (gamepad1.x) {
                mtrCollector.setPower(1);
            } else if (gamepad1.y) {
                mtrCollector.setPower(-1);
            } else {
                mtrCollector.setPower(0);
            }

            if (gamepad1.right_bumper) {
                rightEdge = true;
            } else if (rightEdge) {
                rightEdge = false;
                svoFeeder.setPosition(Range.clip(svoFeeder.getPosition() - 0.001, 0.01, 0.99));
            }


            if (gamepad1.left_bumper) {
                leftEdge = true;
            } else if (leftEdge) {
                leftEdge = false;
                svoFeeder.setPosition(Range.clip(svoFeeder.getPosition() + 0.001, 0.01, 0.99));
            }

            if (gamepad1.b) {
                svoFeeder.setPosition(0.99);
            }

            if (gamepad2.dpad_up) {
                shooter.setPower(SpeedController.AUTONOMOUS_POWER);
            } else if (gamepad2.dpad_left) {
                shooter.setPower(SpeedController.HIGH_POWER);
            } else if (gamepad2.dpad_right) {
                shooter.setPower(SpeedController.MED_POWER);
            } else if (gamepad2.dpad_down) {
                shooter.setPower(SpeedController.NO_POWER);
            }
            /*
            if (gamepad1.dpad_up) {
                upEdge = true;
            } else if (upEdge) {
                upEdge = false;
                power = Math.min(1, power + 0.05);
            }
            if (gamepad1.dpad_down) {
                downEdge = true;
            } else if (downEdge) {
                downEdge = false;
                power = Math.max(0, power - 0.05);
            }
            mtrShooter.setPower(power);*/

            double x1 =  gamepad1.left_stick_x;
            double y1 = -gamepad1.left_stick_y;
            double x2 =  gamepad1.right_stick_x;

            // omni-wheel drive code
            double frontLeft  =  x1 + y1 + x2;
            double frontRight = -x1 + y1 - x2;
            double backLeft   =  x1 - y1 - x2;
            double backRight  = -x1 - y1 + x2;
            mtrFL.setPower(Range.clip(-frontLeft, -1, 1));
            mtrFR.setPower(Range.clip(frontRight, -1, 1));
            mtrBL.setPower(Range.clip(backLeft, -1, 1));
            mtrBR.setPower(Range.clip(-backRight, -1, 1));
            telemetry.addData("position:", svoFeeder.getPosition());
            telemetry.addData("speed:", shooter.getVelocity());
            telemetry.addData("avg_speed:", shooter.getAvgVelocity());
            telemetry.addData("err:", shooter.getErr());
            telemetry.addData("correction:", shooter.getCorrection());
            telemetry.addData("power:", mtrShooter.getPower());
            /*int[] Values16 = snsColorRevolver.Avery_ReadColor16();
            telemetry.addData("16 bit data readings RGBW: ", String.format("%d %d %d %d", Values16[0], Values16[1], Values16[2], Values16[3]));
            */
            updateTelemetry(telemetry);
        }
        shooter.stop();

    }
}
