package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsAnalogOpticalDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.util.Guinea_AdjustedGyro;
import org.firstinspires.ftc.teamcode.util.ModernRoboticsI2cFullColorSensor;
import org.firstinspires.ftc.teamcode.util.SpeedController;

import static org.firstinspires.ftc.teamcode.Guinea_hwInit.DISTANCE_PER_ROT;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.encShooter;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.mtrShooter;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.snsColorBeaconLeft;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.snsColorBeaconRight;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.snsColorLine;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.snsRange;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.svoFeeder;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.svoFlick;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.svoLift;

/**
 *
 * This teleop is used to measure various sensors on the robot.
 * It's mostly used to measure how far a robot should drive in encoder rotations, as well as get
 * live color sensor and gyro readings from the robot to the driver station phone.
 *
 * Mostly present as a programming tool, and not used during competitions
 *
 * For switches on the robot:
 * true = red, false = blue
 * true = normal start position, false = irregular start position
 * @author Guinea on 10/6/2016.
 */
@Disabled
@TeleOp(name="Sensor/lift tester (DON'T USE)", group="test")

public class Guinea_MeasureOp extends OpMode {

    DcMotor mtrFL, mtrFR, mtrBL, mtrBR;
    DcMotor mtrSlide;
    ColorSensor snsColorBeacon;
    Guinea_AdjustedGyro snsGyro;
    ModernRoboticsAnalogOpticalDistanceSensor snsEOPDFront;
    Avery_ColorSensor16Bit_V1 snsColorRevolver;
    SpeedController speed;
    double tdiff = 0;
    double slap = 0;
    @Override
    public void init() {
        Guinea_hwInit.init(hardwareMap);

        mtrFL = Guinea_hwInit.mtrFL;
        mtrFR = Guinea_hwInit.mtrFR;
        mtrBL = Guinea_hwInit.mtrBL;
        mtrBR = Guinea_hwInit.mtrBR;
        mtrSlide = hardwareMap.dcMotor.get("mtrSlide");

        snsColorBeacon = snsColorBeaconLeft;
        //TODO: find out the actual address - 0x3b is a proposed address
        snsColorRevolver = new Avery_ColorSensor16Bit_V1(hardwareMap, "snsColorRevolver", 0x3b);
        snsGyro = Guinea_hwInit.snsGyro;
        tdiff = getRuntime();
        svoLift.setPosition(0);
        speed = new SpeedController(mtrShooter, encShooter, 1000 * 60 * 30);
        speed.start();
        speed.enablePid();
    }

    @Override
    public void loop() {

        double x1 =  gamepad1.left_stick_x;
        double y1 = -gamepad1.left_stick_y;
        double x2 =  gamepad1.right_stick_x;

        // omni-wheel drive code
        double frontLeft  =  x1 + y1 + x2;
        double frontRight = -x1 + y1 - x2;
        double backLeft   =  x1 - y1 - x2;
        double backRight  = -x1 - y1 + x2;

        // allows for two-wheel diagonal movement
        /*if (gamepad1.dpad_up) {
            // frontleft
            frontLeft = backRight = 0;
            backLeft = -1;
            frontRight = 1;
        } else if (gamepad1.dpad_down) {
            // backright
            frontLeft = backRight = 0;
            backLeft = 1;  // negated later so
            frontRight = -1;
        } else if (gamepad1.dpad_right) {
            //frontright
            backLeft = frontRight = 0;
            frontLeft = 1;
            backRight = -1;
        } else if (gamepad1.dpad_left) {
            // backleft
            backLeft = frontRight = 0;
            backRight = 1;
            frontLeft = -1;
        } */
        // 1850, 1950, 2100, 2150
        if(gamepad2.dpad_up) {
            speed.setPower(SpeedController.HIGH_POWER);
            //mtrShooter.setPower(.55);
        } else if(gamepad2.dpad_left) {
            speed.setPower(SpeedController.MED_POWER);
            //mtrShooter.setPower(.45);
        } else if(gamepad2.dpad_right) {
            speed.setPower(SpeedController.LOW_POWER);
            //mtrShooter.setPower(.35);
        } else if(gamepad2.dpad_down) {
            speed.setPower(SpeedController.NO_POWER);
            //mtrShooter.setPower(0);
        } /*else if (gamepad1.dpad_up) {
            speed.setPower(SpeedController.AUTONOMOUS_POWER);
            //mtrShooter.setPower(0.65);
        }*/

        if (gamepad2.a) {
            svoFlick.setPosition(0.6);
        } else {
            svoFlick.setPosition(0.1);
        }

        // power values not between -1 and 1 inclusive crash the code
        mtrFL.setPower(Range.clip(-frontLeft, -1, 1));
        mtrFR.setPower(Range.clip(frontRight, -1, 1));
        mtrBL.setPower(Range.clip(backLeft, -1, 1));
        mtrBR.setPower(Range.clip(-backRight, -1, 1));

        // reset all resettable sensor values (gyro, encoders)
        if (gamepad1.b) {
            Guinea_hwInit.encFL.reset();
            Guinea_hwInit.encFR.reset();
            Guinea_hwInit.encBL.reset();
            Guinea_hwInit.encBR.reset();
            Guinea_hwInit.snsGyro.reset();
        }

        if (gamepad1.a) {
            if (gamepad1.dpad_up) {
                mtrSlide.setPower(0.7);
            } else if (gamepad1.dpad_down) {
                mtrSlide.setPower(-0.7);
            } else {
                mtrSlide.setPower(0);
            }
        } else {
            mtrSlide.setPower(0);
        }
        telemetry.addData("slide position", mtrSlide.getCurrentPosition());

        Color.RGBToHSV(snsColorBeacon.red() * 8, snsColorBeacon.green() * 8, snsColorBeacon.blue() * 8, Guinea_hwInit.hsvValues1);

        tdiff = getRuntime() - tdiff;

        // push all that juicy telemetry
        telemetry.addData("speed", Double.toString(speed.getVelocity()));
        telemetry.addData("power", Double.toString(mtrShooter.getPower()));
        telemetry.addData("Revolver Pos: ", svoFeeder.getPosition());
        telemetry.addData("allianceColor", Boolean.toString(Guinea_hwInit.allianceColor.getState()));
        telemetry.addData("autoMode", Boolean.toString(Guinea_hwInit.autoStartPosition.getState()));
        telemetry.addData("fl", Double.toString((double) Guinea_hwInit.encFL.getCurrentPosition() / DISTANCE_PER_ROT));
        telemetry.addData("fr", Double.toString((double) Guinea_hwInit.encFR.getCurrentPosition() / DISTANCE_PER_ROT));
        telemetry.addData("bl", Double.toString((double) Guinea_hwInit.encBL.getCurrentPosition() / DISTANCE_PER_ROT));
        telemetry.addData("br", Double.toString((double) Guinea_hwInit.encBR.getCurrentPosition() / DISTANCE_PER_ROT));
        telemetry.addData("Gryo heading:", String.format("%f", snsGyro.getHeading()));
        telemetry.addData("Left Beacon color heading:", String.format("r:%d, g:%d, b:%d, a,:%d",
                snsColorBeaconLeft.red(), snsColorBeaconLeft.green(), snsColorBeaconLeft.blue(), snsColorBeaconLeft.alpha()));
        telemetry.addData("Right Beacon color heading:", String.format("r:%d, g:%d, b:%d, a,:%d",
                snsColorBeaconRight.red(), snsColorBeaconRight.green(), snsColorBeaconRight.blue(), snsColorBeaconRight.alpha()));
        telemetry.addData("Line color heading:", String.format("r:%d, g:%d, b:%d, a,:%d",
                snsColorLine.red(), snsColorLine.green(), snsColorLine.blue(), snsColorLine.alpha()));
        telemetry.addData("Range sensor:", String.format("%f", snsRange.getDistance(DistanceUnit.CM)));
        telemetry.addData("EOPD sensor:", Double.toString(snsEOPDFront.getLightDetected()));
        telemetry.addData("EOPD sensor raw:", Double.toString(snsEOPDFront.getRawLightDetected()));

        /*telemetry.addData("Revolver color sensor:", String.format("r: %d, g: %d, b: %d, a: %d", snsColorRevolver.red16(), snsColorRevolver.green16(), snsColorRevolver.blue16(), snsColorRevolver.alpha16()));
        telemetry.addData("Normalized revolver color sensor:", String.format("r: %d, g: %d, b: %d, a: %d", snsColorRevolver.red16Norm(), snsColorRevolver.green16Norm(), snsColorRevolver.blue16Norm(), snsColorRevolver.alpha16Norm()));
        snsEOPDFront.readRawVoltage();*/

    }

    @Override
    public void stop() {
        speed.disablePid();
        speed.stop();

    }

}

