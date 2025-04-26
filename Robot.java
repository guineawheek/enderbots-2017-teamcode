package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cColorSensor;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.util.Guinea_AdjustedGyro;
import org.firstinspires.ftc.teamcode.util.Guinea_EncoderTracker;
import org.firstinspires.ftc.teamcode.util.Dir;
import org.firstinspires.ftc.teamcode.util.SpeedController;

import static java.lang.Thread.sleep;

/**
 * This class holds shared object handles for just about every relevant peripheral on the robot,
 * as well as an initialization function that runs during init periods and populates the object
 * handles, as well as helper functions for driving using encoders and the gyroscope.
 *
 * To use the objects in most files, the correct import to use is generally:
 *
 * <pre>
 * {@code
 * import static org.firstinspires.ftc.teamcode.Guinea_hwInit.*;
 * }
 * </pre>
 *
 * From there, one can access all of its functions and attributes as if they were part of the local
 * class in question.
 *
 * @author Guinea on 10/22/2016.
 */



public class Robot {

    // Dip-switches used to set certain autonomous settings.
    public DigitalChannel allianceColor;
    public DigitalChannel autoStartPosition;

    /* Every relevant motor and sensor handle, as well as their wrappers
     * We name the motors after their position on the robot; for example, mtrFL is the front left motor.
     */
    public DcMotor mtrFL;
    public DcMotor mtrFR;
    public DcMotor mtrBL;
    public DcMotor mtrBR;
    public DcMotor mtrShooter;
    public DcMotor mtrCollector;

    // Classes that wrap around the motors and allow us to reset encoder distances in software
    // instead of hardware, as that is slow
    public Guinea_EncoderTracker encFL;
    public Guinea_EncoderTracker encFR;
    public Guinea_EncoderTracker encBL;
    public Guinea_EncoderTracker encBR;
    public Guinea_EncoderTracker encShooter;
    public SpeedController shooter;

    // Various sensors on our robot
    public ModernRoboticsI2cGyro snsGyroRaw;
    public ModernRoboticsI2cColorSensor snsColorLine;
    public ModernRoboticsI2cColorSensor snsColorBeaconLeft;
    public ModernRoboticsI2cColorSensor snsColorBeaconRight;
    public ModernRoboticsI2cRangeSensor snsRange;

    // Servos on the robot.
    public Servo svoFlick; // moves a ball in the shooter holder into the shooter rollers
    public Servo svoFeeder; // the wind servo that drives the revolver-like feed
    public Servo svoThwack; // beacon pressing appendage
    public Servo svoLift; // the servo for the lift appendage
    // a wrapper object around the gyro that allows us to zero the gyro with an offset in software
    public Guinea_AdjustedGyro snsGyro;
    // 0x28: address of range snesor

    // for hardware switches; true is when the switch is tilted towards the black wire

    // useful constants

    // used to determine set alliance color specified by the alliance color digital switch
    public static final boolean ALLIANCE_RED = false;
    public static final boolean ALLIANCE_BLUE = !ALLIANCE_RED;

    // these are for a switch initially intended to set the robot's starting position on the field;
    // in practice they are used to set whether to try and park on the corner vortex at high speed
    public static final boolean NORMAL_MODE = true;
    public static final boolean ALT_MODE = !NORMAL_MODE;

    // constant relevant to how many encoder ticks is a full rotation
    public static final int DISTANCE_PER_ROT = 1120;
    // the proportial gain constant for gyro-driven straight driving.
    public static final double DRIVE_STRAIGHT_GAIN = 0.015;


    public static float[] hsvValues1 = {0F, 0F, 0F};

    // used to determine direction in function arguments for gyro and encoder driving.

    /**
     * Called during init periods of OpModes that use this class to populate the handles to the
     * motors and sensors of the robot and initialize them
     *
     * Any OpMode that uses hwInit <b>MUST</b> call this method during initialization.
     * @param hardwareMap the hardwareMap object present in all OpModes
     */
    public Robot(HardwareMap hardwareMap) {
        mtrFL = hardwareMap.dcMotor.get("mtrFL");
        mtrFR = hardwareMap.dcMotor.get("mtrFR");
        mtrBL = hardwareMap.dcMotor.get("mtrBL");
        mtrBR = hardwareMap.dcMotor.get("mtrBR");
        mtrShooter = hardwareMap.dcMotor.get("mtrShooter");
        mtrCollector = hardwareMap.dcMotor.get("mtrCollector");
        //mtrSlide = hardwareMap.dcMotor.get("mtrSlide");

        // servos for the feed, the rudder that raises the whiffle ball into the shooter, and the beacon presser
        svoFeeder = hardwareMap.servo.get("svoFeeder");
        svoThwack = hardwareMap.servo.get("svoBeacon");
        svoFlick = hardwareMap.servo.get("svoFlick");
        svoLift = hardwareMap.servo.get("svoLift");

        snsGyroRaw = (ModernRoboticsI2cGyro) hardwareMap.gyroSensor.get("snsGyro");
        snsColorBeaconLeft = hardwareMap.get(ModernRoboticsI2cColorSensor.class, "snsColorBeaconLeft");
        snsColorBeaconRight = hardwareMap.get(ModernRoboticsI2cColorSensor.class, "snsColorBeaconRight");
        snsColorLine = hardwareMap.get(ModernRoboticsI2cColorSensor.class, "snsColorLine");
        snsRange = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "snsRange");

        // set the i2c addresses of the actual sensors themselves
        snsColorBeaconLeft.setI2cAddress(I2cAddr.create8bit(0x3c));
        snsColorBeaconRight.setI2cAddress(I2cAddr.create8bit(0x3a));
        snsColorLine.setI2cAddress(I2cAddr.create8bit(0x3e));
        // workaround for buggy implementation; underlying class assumes that enabling the led was the last thing you did
        // so you have to tell it to turn off first otherwise it skips it as a duplicate instruction.
        snsColorLine.enableLed(false);
        snsColorLine.enableLed(true);

        snsRange.setI2cAddress(I2cAddr.create8bit(0x28));

        snsGyroRaw.calibrate();

        snsColorBeaconLeft.enableLed(false);

        // initialize wrapper classes
        snsGyro = new Guinea_AdjustedGyro(snsGyroRaw);
        encFL = new Guinea_EncoderTracker(mtrFL);
        encFR = new Guinea_EncoderTracker(mtrFR);
        encBL = new Guinea_EncoderTracker(mtrBL);
        encBR = new Guinea_EncoderTracker(mtrBR);
        encShooter = new Guinea_EncoderTracker(mtrShooter);

        // digital switches
        allianceColor = hardwareMap.digitalChannel.get("swAllianceColor");
        autoStartPosition = hardwareMap.digitalChannel.get("swAutoStartPosition");
        allianceColor.setMode(DigitalChannelController.Mode.INPUT);
        autoStartPosition.setMode(DigitalChannelController.Mode.INPUT);

        // tells the drive motors to run in velocity mode, which tries to get all motors running at about the same speed
        mtrFL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        mtrFR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        mtrBL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        mtrBR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        mtrFL.setMaxSpeed(2640);
        mtrFR.setMaxSpeed(2640);
        mtrBL.setMaxSpeed(2640);
        mtrBR.setMaxSpeed(2640);


        // moves the servos to their starting positions
        svoThwack.setPosition(0.85);
        svoFlick.setPosition(0.1);
        svoFeeder.setPosition(0.99);
        svoLift.setPosition(0.99);

        mtrFL.setDirection(DcMotor.Direction.REVERSE);
        mtrFR.setDirection(DcMotor.Direction.REVERSE);
        mtrBL.setDirection(DcMotor.Direction.REVERSE);
        mtrBR.setDirection(DcMotor.Direction.REVERSE);
        mtrCollector.setDirection(DcMotor.Direction.REVERSE);

        // prevents damage to the shooter by disabling braking in case of abrupt loss of power
        mtrShooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooter = new SpeedController(mtrShooter, encShooter, 60 * 60 * 1000);
        shooter.start();
    }

    /**
     * Stops all motors on the robot.
     * Use this in the stop() method of an OpMode for clean stopping.
     */
    public void stop() {
        setDrivePower(0, 0, 0, 0);
        mtrCollector.setPower(0);
        shooter.disablePid();
        mtrShooter.setPower(0);
        //mtrSlide.setPower(0);
    }

    /**
     * Soft-resets the encoder wrappers on the robot, telling them to use the current
     * position as the new zero.
     */
    public void resetEncoders() {
        encFL.reset();
        encFR.reset();
        encBL.reset();
        encBR.reset();
    }

    /**
     * Resets the drivebase for the next movement operation
     */
    public void resetDrive() {
        setDrivePower(0, 0, 0, 0);
        resetEncoders();
        snsGyro.reset();
    }

    /**
     * Directly set the power for the motors.
     * inputs are all -1 <= argument <= 1
     * @param fl power for front left motor
     * @param fr power for front right motor
     * @param bl power for back left motor
     * @param br power for back right motor
     */
    public void setDrivePower(double fl, double fr, double bl, double br) {
        mtrFL.setPower(fl);
        mtrFR.setPower(fr);
        mtrBL.setPower(bl);
        mtrBR.setPower(br);
    }

    /**
     * Moves the robot's motors a set number of rotations at a specified power.
     * Also resets encoders in the process.
     * This is a blocking operation.
     * @param fl number of rotations for front left motor
     * @param fr number of rotations for front right motor
     * @param bl number of rotations for back left motor
     * @param br number of rotations for back right motor
     * @param power power all motors will be driven at
     */
    public void moveTo(double fl, double fr, double bl, double br, double power) {

        resetEncoders();
        encFL.setTarget((int) (fl * DISTANCE_PER_ROT));
        encFR.setTarget((int) (fr * DISTANCE_PER_ROT));
        encBL.setTarget((int) (bl * DISTANCE_PER_ROT));
        encBR.setTarget((int) (br * DISTANCE_PER_ROT));

        while (!(encFL.onTarget() && encFR.onTarget() && encBL.onTarget() && encBR.onTarget())) {
            mtrFL.setPower(encFL.whereTarget() * power);
            mtrFR.setPower(encFR.whereTarget() * power);
            mtrBL.setPower(encBL.whereTarget() * power);
            mtrBR.setPower(encBR.whereTarget() * power);
        }
        setDrivePower(0, 0, 0, 0);
    }

    /**
     * Like moveTo(double, double, double, double, double), but takes a Dir direction
     * @param direction is a Dir enum
     * @param distance number of rotations to move the motors
     * @param power the power to drive them at
     */
    public void moveTo(Dir direction, double distance, double power) {
        double fl = 1;
        double fr = 1;
        double bl = 1;
        double br = 1;
        switch (direction) {
            case forward:
                fl = bl = -1;
                break;
            case backward:
                fr = br = -1;
                break;
            case right:
                fr = fl = -1;
                break;
            case left:
                br = bl = -1;
                break;
            case frontleft:
                fl = br = 0;
                bl = -1;
                break;
            case frontright:
                bl = fr = 0;
                fl = -1;
                break;
            case backleft:
                bl = fr = 0;
                br = -1;
                break;
            case backright:
                fl = br = 0;
                fr = -1;
                break;
            case clockwise:
                fl = fr = bl = br = -1;
                break;
            // default is counter-clockwise anyway
        }
        moveTo(fl * distance, fr * distance, bl * distance, br * distance, power);
    }

    public void spool() throws InterruptedException {

        shooter.setPower(SpeedController.AUTONOMOUS_POWER);
        shooter.enablePid();
        //mtrShooter.setPower(0.27);

        sleep(1500);
    }
    /**
     * Autonomously shoots two balls, assuming the robot is spooled up.
     * @throws InterruptedException
     */
    public void shoot() throws InterruptedException {
        /*while (mtrShooter.getPower() < power) {
            mtrShooter.setPower(mtrShooter.getPower() + 0.05);
        }*/
        svoFlick.setPosition(0.6);
        sleep(1000);
        svoFlick.setPosition(0.1);
        svoFeeder.setPosition(svoFeeder.getPosition() - 0.026);
        sleep(500);
        svoFeeder.setPosition(svoFeeder.getPosition() - 0.027);
        sleep(1500); // 1400
        svoFlick.setPosition(0.6);
        sleep(1000);
        svoFlick.setPosition(0.1);
        svoFeeder.setPosition(0.99);
        //shooter.disablePid();
        mtrShooter.setPower(0);
    }

    /**
     * gets the proportional error that the gyro has drifted from zero degrees
     * @param max_err maximum error allowable, usually can be set to 1
     * @return the error
     */
    public double driveStraightError(double max_err) {
        return Math.min(Math.max(snsGyro.getHeading() * DRIVE_STRAIGHT_GAIN, -max_err), max_err);
    }

    /**
     * gets the magnitude that the left side of the robot has to output to keep driving straight
     * @param max_err maximum error allowable, usually can be set to 1
     * @return the left side power output
     */

    public double driveStraightLeft(double power, double max_err) {
        return Math.min(Math.max(power + driveStraightError(max_err), 0), 1);
    }

    /**
     * gets the magnitude that the right side of the robot has to output to keep driving straight
     * @param max_err maximum error allowable, usually can be set to 1
     * @return the right side power output
     */
    public double driveStraightRight(double power, double max_err) {
        return Math.min(Math.max(power - driveStraightError(max_err), 0), 1);
    }

    /**
     * Updates the robot's motors with adjusted power to keep it going straight in a specified
     * direction at a certain proportion of its max velocity
     * @param power power to drive the robot at
     * @param dir enum representing the direction to drive the robot
     */
    public void driveStraight(double power, Dir dir) {
        double left = driveStraightLeft(power, 1);
        double right = driveStraightRight(power, 1);
        switch (dir) {
            case forward:
                setDrivePower(-left, right, -left, right);
                break;
            case backward:
                setDrivePower(right, -left, right, -left);
                break;
            case right:
                setDrivePower(-left, -right, left, right);
                break;
            case left:
                setDrivePower(left, right, -left, -right);
                break;
            case frontleft:
                setDrivePower(0, right, -left, 0);
                break;
            case frontright:
                setDrivePower(-left, 0, 0, right);
                break;
            case backleft:
                setDrivePower(right, 0, 0, -left);
                break;
            case backright:
                setDrivePower(0, -left, right, 0);
                break;
        }
    }

    public boolean encDistanceGt(double rotations) {
        return Math.abs(encFL.getRotations()) > rotations;
    }

    public boolean rangeDistanceGt(double cm) {
        return snsRange.getDistance(DistanceUnit.CM) > cm;
    }

    public boolean rangeDistanceLt(double cm) {
        return snsRange.getDistance(DistanceUnit.CM) < cm;
    }
}

