package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import static org.firstinspires.ftc.teamcode.Guinea_hwInit.*;

/**
 * Created by guinea on 11/30/16.
 * A test OpMode used to run test code.
 * In this case it investigates the use of velocity control of the shooter motor
 * Turns out that fails poorly as the added load on the motor screws up firmware speed PID.
 */

@TeleOp(name="test op mode please ignore", group="seperate")
public class Guinea_Test extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {

        Guinea_hwInit.init(hardwareMap);
        while (snsGyroRaw.isCalibrating());
        mtrFL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        mtrFR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        mtrBL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        mtrBR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        mtrShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        mtrShooter.setMaxSpeed(2900);
        waitForStart();

        while (opModeIsActive()) {
            dumpEncoders();
            updateTelemetry(telemetry);
            double power = .5;
            while (mtrShooter.getPower() < power) {
                mtrShooter.setPower(mtrShooter.getPower() + 0.05);
            }
            sleep(3000);
            encShooter.reset();
            sleep(1000 * 30);
            mtrShooter.setPower(0);
            waitForInput(power);
        }
    }

    private void waitForInput(double power) {
        while (!gamepad1.a && opModeIsActive()) {
            telemetry.addData("power:", Double.toString(power));
            dumpEncoders();
            updateTelemetry(telemetry);
        }
    }

    private void dumpEncoders() {
        telemetry.addData("shooter:", Double.toString(encShooter.getCurrentPosition()));
    }
}
