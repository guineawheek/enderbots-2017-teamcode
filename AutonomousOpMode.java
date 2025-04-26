package org.firstinspires.ftc.teamcode;

import android.os.Environment;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cColorSensor;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.logging.ColorSensorLogger;
import org.firstinspires.ftc.teamcode.logging.FileLogOutput;
import org.firstinspires.ftc.teamcode.logging.BackgroundLogger;
import org.firstinspires.ftc.teamcode.logging.MotorPowerLogger;
import org.firstinspires.ftc.teamcode.logging.RawGyroLogger;
import org.firstinspires.ftc.teamcode.logging.TelemetryLogOutput;
import org.firstinspires.ftc.teamcode.util.AverageVoltageSensor;
import org.firstinspires.ftc.teamcode.util.Guinea_AdjustedGyro;
import org.firstinspires.ftc.teamcode.util.Guinea_EncoderTracker;
import org.firstinspires.ftc.teamcode.util.Guinea_Timer;
import org.firstinspires.ftc.teamcode.util.Dir;
import org.firstinspires.ftc.teamcode.logging.Loggable;
import org.firstinspires.ftc.teamcode.util.IniConfig;
import org.firstinspires.ftc.teamcode.util.SmoothedRangeSensor;
import org.firstinspires.ftc.teamcode.util.SpeedController;

import java.io.File;

import static java.lang.Thread.sleep;

/**
 *
 * This class serves as a base class for linear autonomouses, as a spiritual successor to the deprecated
 * {@link Guinea_hwInit}.
 *
 * <p>
 * It provides common functionality such as initialization, calibration, and sensor logging facilities.
 * </p>
 *
 * <p>To use, simply subclass and override {@link #runAutonomous()}} and optionally {@link #initHook()}.</p>
 * @author Guinea on 10/22/2016.
 */



public abstract class AutonomousOpMode extends LinearOpMode implements Loggable {
    /** The {@link BackgroundLogger} instance that provides a handle to the opmode's robot logging facilities. */
    protected BackgroundLogger logger;
    protected boolean fileLogEnabled = true;

    protected IniConfig robotConfig;
    protected IniConfig.ConfigSection servoConfig;
    protected IniConfig.ConfigSection thisConfig;

    // Dip-switches used to set certain autonomous settings.
    /**
     * This dip-switch sets the alliance color of the robot. To check for being red or blue alliance, use
     * use {@link #isRedAlliance()} or {@link #isBlueAlliance()}.
     */
    protected DigitalChannel allianceColor;
    /**
     * This dip switch allows checking if the autonomous is set to its normal or alternative modes.
     * To use, one should compare the value of its {@link DigitalChannel#getState()} function to either
     * {@link #NORMAL_MODE} or {@link #ALT_MODE}.
     */
    protected DigitalChannel autoMode;

    /* Every relevant motor and sensor handle, as well as their wrappers
     * We name the motors after their position on the robot; for example, mtrFL is the front left motor.
     */
    /**A {@link DcMotor} object handle to the front left motor of the robot.*/
    protected DcMotor mtrFL;

    /**A {@link DcMotor} object handle to the front right motor of the robot.*/
    protected DcMotor mtrFR;

    /**A {@link DcMotor} object handle to the back left motor of the robot.*/
    protected DcMotor mtrBL;

    /**A {@link DcMotor} object handle to the back right motor of the robot.*/
    protected DcMotor mtrBR;

    /**A {@link DcMotor} object handle to the shooter motor of the robot.*/
    protected DcMotor mtrShooter;

    /**A {@link DcMotor} object handle to the collector motor of the robot.*/
    protected DcMotor mtrCollector;

    // Classes that wrap around the motors and allow us to reset encoder distances in software
    // instead of hardware, as that is slow

    /** A {@link Guinea_EncoderTracker} object that represents the front left encoder's current displacement from its last reset.*/
    protected Guinea_EncoderTracker encFL;
    /** A {@link Guinea_EncoderTracker} object that represents the front right encoder's current displacement from its last reset.*/
    protected Guinea_EncoderTracker encFR;
    /** A {@link Guinea_EncoderTracker} object that represents the back left encoder's current displacement from its last reset.*/
    protected Guinea_EncoderTracker encBL;
    /** A {@link Guinea_EncoderTracker} object that represents the back right encoder's current displacement from its last reset.*/
    protected Guinea_EncoderTracker encBR;
    /** A {@link Guinea_EncoderTracker} object that represents the shooter encoder's current displacement from its last reset.
     * This is used to measure the velocity of the shooter itself.
     */
    protected Guinea_EncoderTracker encShooter;
    /**
     * The PID-driven {@link SpeedController} object that keeps the shooter motor running at a constant speed.
     */
    protected SpeedController shooter;
    /** A timer that signals when the spooler is presumably done spooling up. */
    protected Guinea_Timer spoolTimer;

    // Various sensors on our robot
    /** The object handle to the gyro sensor */
    protected ModernRoboticsI2cGyro snsGyroRaw;
    /** The object handle to the line color sensor used to find lines in front of beacons. */
    protected ModernRoboticsI2cColorSensor snsColorLine;
    /** The object handle to the left beacon-reading color sensor. */
    protected ModernRoboticsI2cColorSensor snsColorBeaconLeft;
    /** The object handle to the right beacon-reading color sensor. */
    protected ModernRoboticsI2cColorSensor snsColorBeaconRight;
    /** The object handle to the range sensor used for beacon approaches.*/
    protected ModernRoboticsI2cRangeSensor snsRange;
    /** A wrapper around the range sensor class that throws out outlier readings. */
    protected SmoothedRangeSensor snsRangeSmooth;

    /** a wrapper object around the gyro that allows us to zero the gyro with an offset in software */
    protected Guinea_AdjustedGyro snsGyro;

    /** a wrapper object that averages the voltage readings from all motor controllers to give an estimate of overall voltage. */
    protected AverageVoltageSensor snsAvgVoltage;

    // Servos on the robot.
    /** The servo that flicks a ball in the firing pin straight into the shooter flywheels*/
    protected Servo svoFlick;
    /** The servo that operates the center revolver. */
    protected Servo svoFeeder;
    /** The servo that is used in autonomous to press the beacons using the four-bar linkage. */
    protected Servo svoThwack;
    /** The servo that operates the holding arms of the cap ball lift. */
    protected Servo svoLift;
    // 0x28: address of range snesor

    // for hardware switches; true is when the switch is tilted towards the black wire

    /**
     * This attribute is used for debugging/documentation; it is often set during the course of
     * autonomous to record and label what the robot is doing at each step of its task.
     */
    protected String state = "undefined";
    /**
     * This holds the alliance color value set by a dipswitch at the back of the robot. It can be either
     * equal to {@link #ALLIANCE_RED} or {@link #ALLIANCE_BLUE}.
     */
    protected boolean alliance;
    private boolean gyroResetWithDrive = true;

    // useful constants

    // used to determine set alliance color specified by the alliance color digital switch

    /** Represents the value the alliance color dipswitch returns when set to red alliance. */
    protected static final boolean ALLIANCE_RED = false;

    /** Represents the value the alliance color dipswitch returns when set to blue alliance. */
    protected static final boolean ALLIANCE_BLUE = !ALLIANCE_RED;

    /**
     * Represents the value the autonomous mode dipswitch returns is set to run without any special behavior.
     * Usually this means the robot will not attempt to park on the center vortex.
     */
    protected static final boolean NORMAL_MODE = true;
    /**
     * Represents the value the autonomous mode dipswitch returns is set to run with special behavior.
     *  Usually this means the robot will try and park on the corner vortex.
     */
    protected static final boolean ALT_MODE = !NORMAL_MODE;

    // constant relevant to how many encoder ticks is a full rotation

    /** The number of encoder counts per one rotation in an Andymark Neverest 40:1 motor, as is used in the robot's drivebase. */
    protected static final int DISTANCE_PER_ROT = 1120;
    /** The proportial gain constant for gyro-driven straight driving in all the driveStraight and wallFollow methods. */
    protected static final double DRIVE_STRAIGHT_GAIN = 0.015;

    protected int shootDelay;


    // used to determine direction in function arguments for gyro and encoder driving.

    /**
     * Called during init periods of OpModes that use this class to populate the handles to the
     * motors and sensors of the robot and initialize them.
     *
     * <p>If overriding {@link #runAutonomous()} this function is called for you.</p>
     *
     */
    protected void initialize() {
        shootDelay = 2000;
        telemetry.setAutoClear(false);
        robotConfig = new IniConfig(new File(Environment.getExternalStorageDirectory().getPath() + "/ftc-config/velocity.ini"));
        robotConfig.setPermissive(true);
        if (!robotConfig.readConfig()) {
            telemetry.addLine("Config read error: " + robotConfig.getLastError());
            telemetry.addLine("This will probably lead to weirdness, expect auto failiure");
        }

        servoConfig = robotConfig.getSection("Servos");
        servoConfig.setPermissive(true);

        thisConfig = robotConfig.getSection("AutonomousOpMode");
        thisConfig.setPermissive(true);

        shootDelay = thisConfig.getInteger("shoot.delay", 2000);

        double feederInitPos = servoConfig.getd("svoFeeder.initPosition", 0.98);
        double liftInitPos = servoConfig.getd("svoLift.initPosition", 0.9);



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

        snsColorBeaconLeft.enableLed(false);
        snsColorBeaconRight.enableLed(false);

        // initialize wrapper classes
        snsGyro = new Guinea_AdjustedGyro(snsGyroRaw);
        encFL = new Guinea_EncoderTracker(mtrFL);
        encFR = new Guinea_EncoderTracker(mtrFR);
        encBL = new Guinea_EncoderTracker(mtrBL);
        encBR = new Guinea_EncoderTracker(mtrBR);
        encShooter = new Guinea_EncoderTracker(mtrShooter);
        snsAvgVoltage = new AverageVoltageSensor(hardwareMap);
        snsRangeSmooth = new SmoothedRangeSensor(snsRange);

        // digital switches
        allianceColor = hardwareMap.digitalChannel.get("swAllianceColor");
        autoMode = hardwareMap.digitalChannel.get("swAutoStartPosition");
        allianceColor.setMode(DigitalChannelController.Mode.INPUT);
        autoMode.setMode(DigitalChannelController.Mode.INPUT);

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
        svoFeeder.setPosition(feederInitPos);
        svoLift.setPosition(liftInitPos);

        mtrFL.setDirection(DcMotor.Direction.REVERSE);
        mtrFR.setDirection(DcMotor.Direction.REVERSE);
        mtrBL.setDirection(DcMotor.Direction.REVERSE);
        mtrBR.setDirection(DcMotor.Direction.REVERSE);
        mtrCollector.setDirection(DcMotor.Direction.REVERSE);

        // prevents damage to the shooter by disabling braking in case of abrupt loss of power
        mtrShooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooter = new SpeedController(mtrShooter, encShooter, 60 * 60 * 1000);
        shooter.start();
        spoolTimer = new Guinea_Timer();
        alliance = allianceColor.getState();
        state = "initialized";

    }

    /**
     * Returns the first argument if the robot color is set to red, else blue.
     * <p>For consistency's sake, both objects <i>must</i> be the same type.</p>
     * @param red the value to return if red
     * @param blue the value to return if blue
     * @return the value of the red parameter if the robot is on the red alliance, else the blue parameter.
     */
    protected <E> E redblue(E red, E blue) {

        return (isRedAlliance()) ? red : blue;
    }

    /**
     * Checks to see if a color sensor is actually plugged in lol
     * @param snsColor the color sensor to check
     * @return if the color sensor seems to be in working condition
     */
    protected boolean checkColorSensor(ColorSensor snsColor) {
        return snsColor.red() + snsColor.green() + snsColor.blue() + snsColor.alpha() < 255 * 4;
    }

    /**
     * Checks to see if a range sensor is plugged in or facing a wall.
     * Don't rely too much on it.
     * @param snsRange
     * @return
     */
    protected boolean checkRangeSensor(ModernRoboticsI2cRangeSensor snsRange) throws InterruptedException {
        int bigcounter = 0;
        for (int i = 0; i < 5; i++) {
            if (snsRange.getDistance(DistanceUnit.CM) - 255 < 0.5) {
                bigcounter++;
            }
            sleep(50);
        }
        return bigcounter > 3;
    }

    /**
     * Calibrates sensors on the robot, and also provides visual checks to ensure correct operation.
     *
     * <p>If running {@link #runAutonomous()} this is called for you.</p>
     */
    protected void calibrateSensors() throws InterruptedException {
        //TODO: check if color sensors are reading (255, 255, 255) pairs as an error check
        snsGyroRaw.calibrate();
        snsColorBeaconLeft.enableLed(true);
        snsColorBeaconRight.enableLed(true);
        telemetry.addLine("Checking sensors...always check the DIM for unplugged sensors before matches.");
        if (!checkColorSensor(snsColorLine)) telemetry.addLine("Warning: line color sensor not plugged in or faulty!");
        if (!checkColorSensor(snsColorBeaconLeft)) telemetry.addLine("Warning: left beacon color sensor not plugged in or faulty!!");
        if (!checkColorSensor(snsColorBeaconRight)) telemetry.addLine("Warning: right beacon color sensor not plugged in or faulty!!");
        if (!checkRangeSensor(snsRange)) telemetry.addLine("Warning: range sensor reading constant value of 255!!");
        telemetry.addLine("Alliance color: " +  ((isRedAlliance()) ? "red" : "blue"));
        telemetry.addLine("Are you ready for a MIRACLE?");
        telemetry.update();
        while (snsGyroRaw.isCalibrating());

        snsColorBeaconLeft.enableLed(false);
        snsColorBeaconRight.enableLed(false);
        telemetry.addLine("The robot is ready! Praise Kamen \uD83D\uDC4C");
        telemetry.update();
        state = "calibrated";
    }

    /**
     * Stops all motors on the robot.
     * <p>Use this in the stop() method of an OpMode for clean stopping.</p>
     */
    protected void stopMotors() {
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
    protected void resetEncoders() {
        encFL.reset();
        encFR.reset();
        encBL.reset();
        encBR.reset();
    }

    /**
     * Resets the drivebase and gyro/encoders for the next movement operation.
     */
    protected void resetDrive() {
        setDrivePower(0, 0, 0, 0);
        resetEncoders();
        if (gyroResetWithDrive) snsGyro.reset();
    }

    protected boolean isGyroResetWithDrive() {
        return gyroResetWithDrive;
    }

    protected void setGyroResetWithDrive(boolean value) {
        gyroResetWithDrive = value;
    }

    /**
     * Directly set the power for the motors, clipping the range to prevent horrible crashes.
     * @param fl power for front left motor
     * @param fr power for front right motor
     * @param bl power for back left motor
     * @param br power for back right motor
     */
    protected void setDrivePower(double fl, double fr, double bl, double br) {
        mtrFL.setPower(Range.clip(fl, -1, 1));
        mtrFR.setPower(Range.clip(fr, -1, 1));
        mtrBL.setPower(Range.clip(bl, -1, 1));
        mtrBR.setPower(Range.clip(br, -1, 1));
    }

    /**
     * apparently this is horribly broken for some reason....
     * edit: i think i know why it was horribly broken!!
     * @param vert
     * @param horiz
     * @param rotation unlike most functions, positive rotates the robot clockwise.
     */
    protected void omniDrive(double vert, double horiz, double rotation) {
        double frontLeft = -horiz - vert - rotation;
        double frontRight = -horiz + vert - rotation;
        double backLeft = horiz - vert - rotation;
        double backRight = horiz + vert - rotation;
        setDrivePower(frontLeft, frontRight, backLeft, backRight);
    }

    /**
     * Starts spooling the shooter to the canonical motor power.
     */
    protected void spool() {
        mtrShooter.setPower(0.6);
        spoolTimer.setTarget(1500);
    }

    /**
     * Waits until the shooter is done spooling up, assuming {@link #spool()} was previously called.
     */
    protected void spoolWait() {
        while (!spoolTimer.done() && opModeIsActive());
    }

    /**
     * Autonomously shoots two balls, assuming the robot is spooled up.
     * @throws InterruptedException if opmode stopped during shooting sequence.
     */
    protected void shoot() throws InterruptedException {
        shoot(false);
    }

    /**
     * Autonomously shoots two to three balls, assuming the robot is spooled up.
     * @param shootThird set to true if the robot should try shooting three balls.
     * @throws InterruptedException if opmode stopped during shooting sequence.
     */
    protected void shoot(boolean shootThird) throws InterruptedException {
        /*while (mtrShooter.getPower() < power) {
            mtrShooter.setPower(mtrShooter.getPower() + 0.05);
        }*/
        svoFlick.setPosition(0.6);
        sleep(750);
        svoFlick.setPosition(0.1);
        svoFeeder.setPosition(svoFeeder.getPosition() - 0.014);
        sleep(shootDelay);
        svoFlick.setPosition(0.6);
        sleep(750);
        svoFlick.setPosition(0.1);

        if (shootThird) {
            svoFeeder.setPosition(svoFeeder.getPosition() - 0.026);
            sleep(1500);
            svoFlick.setPosition(0.6);
            sleep(750);
            svoFlick.setPosition(0.1);

        }

        //svoFeeder.setPosition(0.99);
        //shooter.disablePid();
        mtrShooter.setPower(0);
    }


    /**
     * Updates the robot's motors with adjusted power to keep it going straight in a specified
     * direction at a certain proportion of its max velocity
     * @param power power to drive the robot at
     * @param dir enum representing the direction to drive the robot
     */
    protected void driveStraight(double power, Dir dir) {
        double error = Math.min(Math.max(snsGyro.getHeading() * DRIVE_STRAIGHT_GAIN, -1), 1);
        double left = Math.min(Math.max(power + error, 0), 1);
        double right = Math.min(Math.max(power - error, 0), 1);
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

    /**
     * Checks if the front left encoder's rotation since the last reset is greater than the
     * specified value, in motor rotations.
     * @param rotations the number of rotations to compare against
     * @return whether or not the front left encoder has rotated more than the specified value.
     */
    protected boolean encDistanceGt(double rotations) {
        return Math.abs(encFL.getRotations()) > rotations;
    }

    /**
     * Checks if the front left encoder's rotation since the last reset is less than the
     * specified value, in motor rotations.
     * @param rotations the number of rotations to compare against
     * @return whether or not the front left encoder has rotated less than the specified value.
     */
    protected boolean encDistanceLt(double rotations) {
        return encDistanceLt(encFL, rotations);
    }

    /**
     * Checks if the passed encoder's rotation since the last reset is less than the
     * specified value, in motor rotations.
     * @param enc the object representing the encoder to check
     * @param rotations the number of rotations to compare against
     * @return whether or not the front left encoder has rotated less than the specified value.
     */
    protected boolean encDistanceLt(Guinea_EncoderTracker enc, double rotations) {
        return Math.abs(enc.getRotations()) < rotations;
    }

    /**
     * Checks if the onboard range sensor is reading a distance greater than the specified threshold
     * in centimeters.
     *
     * <p>The accuracy of this is low at very close distances to the wall.</p>
     * <p>May not account for occasional random fluctuation generated by the sensor.</p>
     * @param cm the number of centimeters to compare against.
     * @return whether the sensor is reading a greater distance than the specified value.
     */
    protected boolean rangeDistanceGt(double cm) {
        return snsRangeSmooth.getDistanceCm() > cm; //snsRange.getDistance(DistanceUnit.CM) > cm;
    }

    /**
     * Checks if the onboard range sensor is reading a distance less than the specified threshold
     * in centimeters.
     *
     * <p>The accuracy of this is low at very close distances to the wall.</p>
     * <p>May not account for occasional random fluctuation generated by the sensor.</p>
     * @param cm the number of centimeters to compare against.
     * @return whether the sensor is reading a lesser distance than the specified value.
     */
    protected boolean rangeDistanceLt(double cm) {
        return snsRange.getDistance(DistanceUnit.CM) < cm;
    }

    /**
     * Returns true if the robot's alliance switch is set to red.
     *
     * Only works after {@link #initialize()} has been called. If using {@link #runAutonomous()}
     * {@link #initialize()} has been called for you.
     * @return true if the robot is set to red.
     */
    protected boolean isRedAlliance() { return alliance == ALLIANCE_RED; }

    /**
     * Returns true if the robot's alliance switch is set to blue.
     *
     * Only works after {@link #initialize()} has been called. If using {@link #runAutonomous()}
     * {@link #initialize()} has been called for you.
     * @return true if the robot is set to blue.
     */
    protected boolean isBlueAlliance() { return alliance == ALLIANCE_BLUE; }

    private Guinea_EncoderTracker getDistanceEncoder(Dir dir) {

        if (dir == Dir.frontleft || dir == Dir.backright) {
            // adjust for the fact encFL isn't supposed to move along these orientations
            return encFR;
        } else {
            return encFL;
        }
    }

    /**
     * Drives straight at the specified power, in the specified direction, for the specified number
     * of rotations.
     *
     * <p>This is a blocking method.</p>
     * @param power the power to drive the motors at. Excessively high or low values (&lt;0.1 or &gt;0.9)
     *              may reduce the effectiveness of the method's ability to keep the robot straight.
     * @param dir A member of the {@link Dir} enumeration representing the direction of the robot, such as
     *            {@link Dir#forward}
     * @param rotations The number of rotations, as a double, the robot's wheels should drive for.
     *                  Values less than zero will result in the robot driving forever. Don't do that.
     */
    protected void driveStraightDistance(double power, Dir dir, double rotations) {
        resetDrive();
        while (encDistanceLt(getDistanceEncoder(dir), rotations) && opModeIsActive()) {
            driveStraight(power, dir);
        }
        setDrivePower(0, 0, 0, 0);
    }

    /**
     * Drives straight at the specified power, in the specified direction, as long as the range sensor
     * reads higher distances than the number of centimeters provided.
     *
     * <p>This is a blocking method.</p>
     * @param power the power to drive the motors at. Excessively high or low values (&lt;0.1 or &gt;0.9)
     *              may reduce the effectiveness of the method's ability to keep the robot straight.
     * @param dir A member of the {@link Dir} enumeration representing the direction of the robot, such as
     *            {@link Dir#forward}
     * @param cm the threshold at which if the robot's range sensor reads values that are less than
     *           or equal to the robot will stop its drive motors.
     */
    protected void driveStraightRangeGt(double power, Dir dir, double cm) {
        resetDrive();
        while (rangeDistanceGt(cm) && opModeIsActive()) {
            driveStraight(power, dir);
        }
        setDrivePower(0, 0, 0, 0);
    }

    /**
     * Drives straight at the specified power, in the specified direction, as long as the range sensor
     * reads lower distances than the number of centimeters provided.
     *
     * <p>This is a blocking method.</p>
     * @param power the power to drive the motors at. Excessively high or low values (&lt;0.1 or &gt;0.9)
     *              may reduce the effectiveness of the method's ability to keep the robot straight.
     * @param dir A member of the {@link Dir} enumeration representing the direction of the robot, such as
     *            {@link Dir#forward}
     * @param cm the threshold at which if the robot's range sensor reads values that are greater than
     *           or equal to the robot will stop its drive motors.
     */
    protected void driveStraightRangeLt(double power, Dir dir, double cm) {
        resetDrive();
        while (rangeDistanceLt(cm) && opModeIsActive()) {
            driveStraight(power, dir);
        }
        setDrivePower(0, 0, 0, 0);
    }

    /**
     * Turns the robot a specified number of degrees, positive or negative, relative to its current
     * position with proportional control.
     *
     * <p>This is a blocking method.</p>
     * @param degrees the number of degrees to turn. Like most functions, positive is counter-clockwise.
     * @param p the proportionality constant at which to turn the robot. 0.01 is good for small adjustments,
     *          0.005 is better for 180 degree turns.
     * @param maxErr The maximum error in degrees allowable by the function. A good value is somewhere
     *               between 1-4 inclusive.
     */
    protected void turnRelative(double degrees, double p, double maxErr) {
        resetDrive();
        while (Math.abs(snsGyro.getHeading() - degrees) > maxErr && opModeIsActive()) {
            double correction = Range.clip(-(snsGyro.getHeading() - maxErr) * p, -1, 1);
            setDrivePower(correction, correction, correction, correction);
        }
        setDrivePower(0, 0, 0, 0);
    }

    /**
     * Turns the robot a specified number of degrees, positive or negative, relative to the orientation
     * with which it started autonomous (or the frame of refrence since the last time
     * {@link ModernRoboticsI2cGyro#resetZAxisIntegrator()} was called on {@link #snsGyroRaw})
     * with proportional control.
     *
     * <p>This is a blocking method.</p>
     * @param degrees the number of degrees to turn to. Like most functions, positive is counter-clockwise.
     * @param p the proportionality constant at which to turn the robot. 0.01 is good for small adjustments,
     *          0.005 is better for 180 degree turns.
     * @param maxErr The maximum error in degrees allowable by the function. A good value is somewhere
     *               between 1-4 inclusive.
     */
    protected void turnAbsolute(double degrees, double p, double maxErr) {
        resetDrive();
        while (Math.abs(snsGyroRaw.getIntegratedZValue() - degrees) > maxErr && opModeIsActive()) {
            double correction = Range.clip(-(snsGyroRaw.getIntegratedZValue() - degrees) * p, -1, 1);
            setDrivePower(correction, correction, correction, correction);
        }
        setDrivePower(0, 0, 0, 0);
    }

    /**
     * Wall follows using the range sensor for a specified number of rotations, incorporating gyro
     * measurements to help keep the robot straight while doing so.
     * @param power the power to run the motors at
     * @param dir the direction the robot should go in. As the range sensor is on the left side of
     *            the robot, the only valid values are {@link Dir#forward} and {@link Dir#backward}
     * @param rotations the number of rotations that must pass before the robot stops its motors
     * @param cm the target distance the robot will try to wall follow from
     */
    protected void wallFollowDistance(double power, Dir dir, double rotations, double cm) {
        resetDrive();
        double dirCoeff = (dir == Dir.forward) ? 1 : -1;
        while (/*encDistanceLt(getDistanceEncoder(dir), rotations) && */opModeIsActive()) {
            double avg = (Math.abs(encFL.getRotations()) + Math.abs(encFR.getRotations())) / 2d;
            if (avg >= rotations) break;
            omniDrive(dirCoeff * power, -dirCoeff * Range.clip((cm - snsRange.getDistance(DistanceUnit.CM)) * 0.002, -0.8, 0.8),
                    Range.clip(snsGyroRaw.getIntegratedZValue() * 0.02, -1, 1));
        }
        //setDrivePower(0, 0, 0, 0);
    }

    /**
     * Follows the wall using the range and gyro sensors until either the encoders have reached a
     * set number of rotations or the line color sensor reads a line, to prevent possibly overshooting.
     *
     * @param power the power to run the drivebase at
     * @param dir the direction the robot should go in. As the range sensor is on the left side of
     *            the robot, the only valid values are {@link Dir#forward} and {@link Dir#backward}
     * @param rotations the number of rotations that must pass before the robot stops its motors
     * @param cm the target distance the robot will try to wall follow from
     * @return whether or not the line color sensor was used to stop the function
     */
    protected boolean wallFollowWithGuard(double power, Dir dir, double rotations, double cm) {
        power = Math.abs(power);
        boolean itwastheline = false;
        if (dir == Dir.backward) {
            power = -power;
        }
        // cooldown timer to prevent reading lines too early
        Guinea_Timer waitTime = new Guinea_Timer(1500);

        resetDrive();
        while ((itwastheline = snsColorLine.alpha() < 40 || !waitTime.done()) && encDistanceLt(rotations) && opModeIsActive()) {
            omniDrive(power, Range.clip((cm - snsRangeSmooth.getDistanceCm()) * 0.06, -0.5, 0.5),
                    Range.clip(snsGyroRaw.getIntegratedZValue() * DRIVE_STRAIGHT_GAIN, -1, 1));
        }
        setDrivePower(0, 0, 0, 0);
        return itwastheline;
    }

    protected void setFileLogEnabled(boolean enabled) {
        fileLogEnabled = enabled;
    }

    protected boolean getFileLogEnabled() {
        return fileLogEnabled;
    }

    private void prepLogger() {
        logger = new BackgroundLogger(100);
        logger.addSource("time", new Guinea_Timer());
        logger.addSource("state", this);
        logger.addSource("voltage", snsAvgVoltage);
        logger.addSource("mtrFL", new MotorPowerLogger(mtrFL));
        logger.addSource("mtrFR", new MotorPowerLogger(mtrFR));
        logger.addSource("mtrBL", new MotorPowerLogger(mtrBL));
        logger.addSource("mtrBR", new MotorPowerLogger(mtrBR));
        logger.addSource("encFL", encFL);
        logger.addSource("encFR", encFR);
        logger.addSource("encBL", encBL);
        logger.addSource("encBR", encBR);
        logger.addSource("snsGyroRaw", new RawGyroLogger(snsGyroRaw));
        logger.addSource("snsGyro", snsGyro);
        logger.addSource("snsRange", snsRangeSmooth);
        logger.addSource("snsColorLine", new ColorSensorLogger(snsColorLine));
        logger.addSource("snsColorBeaconLeft", new ColorSensorLogger(snsColorBeaconLeft));
        logger.addSource("snsColorBeaconRight", new ColorSensorLogger(snsColorBeaconRight));

        String color = isRedAlliance() ? "red" : "blue";

        if (fileLogEnabled)
            logger.addOutput(new FileLogOutput("-" + this.getClass().getSimpleName() + "-" + color));
        logger.addOutput(new TelemetryLogOutput(telemetry));
        telemetry.setAutoClear(true);
    }



    /**
     * Default boilerplate for autonomouses, complete with logging and automatic initialization
     * facilities.
     * <p>May be overriden for special situations.</p>
     * @throws InterruptedException
     */
    @Override
    public void runOpMode() throws InterruptedException {
        initialize();
        calibrateSensors();
        initHook();
        prepLogger();
        waitForStart();
        logger.start();

        try {
            runAutonomous();
        } finally {
            shooter.stop();
            mtrShooter.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            logger.interrupt();
            while (logger.isAlive() || shooter.pidThreadAlive());
        }
    }

    /**
     * Actual autonomous code should be in here - subclasses should override this function, and put
     * linear opmode code that would usually belong after waitForStart() in here.
     * Don't even need to deal with init.
     * @throws InterruptedException
     */
    public abstract void runAutonomous() throws InterruptedException;

    /**
     * Override this method to do things like disable logging, or custom initialization for an opmode-specific
     * purpose. This method will then be called at the end of init, after everything else has started, but before the driver presses play
     */
    public void initHook() {}

    /**
     * Used to log the programmer-specified state of the autonomous to help improve the usefulness of logging, as
     * knowing what the autonomous is intended to do makes it much easier to interpret logfiles.
     * @return the value of {@link #state}
     */
    public Object getLogData() { return state; }


}

