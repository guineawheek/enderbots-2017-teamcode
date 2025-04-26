package org.firstinspires.ftc.teamcode;

import android.os.Environment;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.util.Guinea_EncoderTracker;
import org.firstinspires.ftc.teamcode.util.Guinea_Timer;
import org.firstinspires.ftc.teamcode.util.IniConfig;
import org.firstinspires.ftc.teamcode.util.SpeedController;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Avery on 10/6/2016.
 */

@TeleOp(name="Velocity TeleOp", group="K9bot")

public class TeleOpVelocity5484_velocity extends OpMode {

    // debug constant to enable/disable velocity at compile time
    // merged branches to master should NEVER have this set to false.
    public static final boolean VELOCITY = false;

    IniConfig config;
    IniConfig.ConfigSection teleopConfig;
    IniConfig.ConfigSection servoConfig;

    DcMotor mtrFL;
    DcMotor mtrFR;
    DcMotor mtrBL;
    DcMotor mtrBR;

    DcMotor mtrShooter;
    DcMotor mtrCollector;
    SpeedController speed;
    Guinea_EncoderTracker encShooter;
    Guinea_EncoderTracker encSlide;
    DcMotor mtrSlide;

    //Avery_ColorSensor16Bit_V1 snsColorBall;

    //Drive states
    public static boolean MOVING = false;
    public static boolean SLOW_SPEED = false;
    public static boolean SUPER_SLOW_SPEED = false;
    public static boolean DT_STOP = true;

    public static boolean RAMPING_UP_SHOOTER = false;
    public static boolean RAMPING_DOWN_SHOOTER = false;
    public static boolean SHOOTER_STOP = true;
    public static boolean SHOOTER_RUNNING = false;

    public static boolean COLLECTOR_RUNNING = false;
    public static boolean COLLECTOR_BACKWARDS = false;
    public static boolean COLLECTOR_STOP = true;

    public static boolean FEED_WATCH = false;
    public static boolean FEED_WATCH1 = false;
    public static boolean FEED_BALL = false;
    public static boolean FEED_REVERSE = false;
    public static boolean FEED_STOP = true;
    public static boolean FEED_BACK = false;

    public static boolean FLICK_BALL = false;
    public static boolean FLICK_STOP = true;

    public static boolean LIFT_FLAG = false;
    public static boolean LIFT_UP = false;
    public static boolean LIFT_DOWN = false;
    public static boolean LIFT_STOP = true;

    public static boolean FORK_OUT = false;
    public static boolean FORK_IN = true;

    public static double flickZero = 0;
    public static double feederAngle = 0;

    // Drive constants
    public static double shooterTargetSpeed = 0;
    public static double shooterCurrentSpeed = 0;

    public static double deltaEncoderClicks;

    GyroSensor snsGyro;
    ColorSensor snsColorBeacon;
    ModernRoboticsI2cRangeSensor snsRange;
    OpticalDistanceSensor snsCounterEOPD;
    Avery_ColorSensor16Bit_V1 snsColorRevolver;

    Servo svoFlick;
    Servo svoFeeder;
    Servo svoBeacon;
    Servo svoLift;
    Servo svoFlag;
    static boolean BALL_DETECTED = false;
    Guinea_Timer ballTimer;

    DigitalChannel highLimit;
    DigitalChannel lowLimit;

    Guinea_Timer lagTracker;

    float hsvValues1[] = {0F, 0F, 0F};
    float hsvValues2[] = {0F, 0F, 0F};

    double liftInitPos, liftMedPos, liftHighPos;
    double liftEnc;
    int liftUp, liftDown;
    double lowPower, medPower, highPower, superPower;
    double feederInitPos, feederIndex;

    public int counter = 0;

    @Override
    public void init() {
        config = new IniConfig(new File(Environment.getExternalStorageDirectory().getPath() + "/ftc-config/velocity.ini"));
        config.setPermissive(true);
        if (!config.readConfig()) {
            telemetry.setAutoClear(false);
            telemetry.addLine("Error reading config file!");
        }
        teleopConfig = config.getSection("TeleOp");
        teleopConfig.setPermissive(true);

        servoConfig = config.getSection("Servos");
        servoConfig.setPermissive(true);

        mtrFL = hardwareMap.dcMotor.get("mtrFL");
        mtrFR = hardwareMap.dcMotor.get("mtrFR");
        mtrBL = hardwareMap.dcMotor.get("mtrBL");
        mtrBR = hardwareMap.dcMotor.get("mtrBR");

        highLimit = hardwareMap.digitalChannel.get("highLimit");
        lowLimit = hardwareMap.digitalChannel.get("lowLimit");

        svoFlick = hardwareMap.servo.get("svoFlick");
        svoFeeder = hardwareMap.servo.get("svoFeeder");
        svoBeacon = hardwareMap.servo.get("svoBeacon");
        svoLift = hardwareMap.servo.get("svoLift");
        svoFlag = hardwareMap.servo.get("svoFlag");

        mtrCollector = hardwareMap.dcMotor.get("mtrCollector");
        mtrShooter = hardwareMap.dcMotor.get("mtrShooter");
        mtrSlide = hardwareMap.dcMotor.get("mtrSlide");


        encShooter = new Guinea_EncoderTracker(mtrShooter);
        encShooter.reset();
        speed = new SpeedController(mtrShooter, encShooter, 1000 * 9999);
        speed.start();
        if (VELOCITY) speed.enablePid();

        snsGyro = hardwareMap.gyroSensor.get("snsGyro");
        snsRange = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "snsRange");
        snsColorBeacon = hardwareMap.colorSensor.get("snsColorBeaconLeft");
        snsCounterEOPD = hardwareMap.opticalDistanceSensor.get("snsEOPDcounter");

        mtrBL.setDirection(DcMotorSimple.Direction.REVERSE);
        mtrBR.setDirection(DcMotorSimple.Direction.REVERSE);
        mtrFL.setDirection(DcMotorSimple.Direction.REVERSE);
        mtrFR.setDirection(DcMotorSimple.Direction.REVERSE);
        mtrSlide.setDirection(DcMotorSimple.Direction.REVERSE);

        mtrFL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        mtrFR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        mtrBL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        mtrBR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //mtrShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        mtrCollector.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        encSlide = new Guinea_EncoderTracker(mtrSlide);

        // drive motor velocity control
        mtrFL.setMaxSpeed(2700);
        mtrFR.setMaxSpeed(2700);
        mtrBL.setMaxSpeed(2700);
        mtrBR.setMaxSpeed(2700);

        mtrCollector.setMaxSpeed(2800);
        //mtrShooter.setMaxSpeed(5100);

        mtrShooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        snsColorBeacon.getI2cAddress();
        snsColorBeacon.setI2cAddress(I2cAddr.create8bit(0x3c));

        snsGyro.calibrate();
        snsColorBeacon.enableLed(false);

        //mtrShooter.setDirection(DcMotorSimple.Direction.REVERSE);
        mtrCollector.setDirection(DcMotorSimple.Direction.REVERSE);

        liftInitPos = servoConfig.getd("svoLift.initPosition", 0.9);
        liftMedPos = servoConfig.getd("svoLift.medPosition", 0.5);
        liftHighPos = servoConfig.getd("svoLift.highPosition", 0.1);
        liftEnc = teleopConfig.getd("lift.encoderHigh", 0.2);
        liftUp = teleopConfig.getInteger("lift.upThreshold", 27);
        liftDown = teleopConfig.getInteger("lift.downThreshold", 12);

        // the defaults are a compromise which should keep the bevels spinning under most levels of wear
        lowPower = teleopConfig.getNumber("shooter.low", 0.2);
        medPower = teleopConfig.getNumber("shooter.med", 0.3);
        highPower = teleopConfig.getNumber("shooter.high", 0.4);
        superPower = teleopConfig.getNumber("shooter.super", 0.55);

        feederInitPos = servoConfig.getd("svoFeeder.initPosition", 0.98);
        feederIndex = servoConfig.getd("svoFeeder.index", 0.013);

        telemetry.addLine("thicc daddy jake sobel");
        telemetry.addLine("If any of the following values are extremely large the config file is likely corrupt.");
        String[] keys = {"lift.encoderHigh", "lift.upThreshold", "lift.downThreshold", "shooter.low", "shooter.med", "shooter.high", "shooter.super"};
        for (String key : keys) {
            telemetry.addData(key + " = ", teleopConfig.getNumber(key, 42069));
        }

        String[] servoConfigKeys = {"svoLift.initPosition", "svoLift.medPosition", "svoLift.highPosition", "svoFeeder.initPosition", "svoFeeder.index"};

        for (String key : servoConfigKeys) {
            telemetry.addData(key + " = ", servoConfig.getNumber(key, 42069));
        }
        snsColorRevolver = new Avery_ColorSensor16Bit_V1(hardwareMap, "snsColorRevolver", 0x38);
        snsColorRevolver.disengage();
        lagTracker = new Guinea_Timer();
        svoBeacon.setPosition(0.85);
        svoLift.setPosition(liftInitPos);
        svoFlick.setPosition(0.1);
        svoFeeder.setPosition(feederInitPos);
        svoFlag.setPosition(0);
        ballTimer = new Guinea_Timer();
        ballTimer.setTarget(1000);
    }

    @Override
    public void loop() {
        if (!telemetry.isAutoClear()) telemetry.setAutoClear(true);
        try {
            double startLoop = System.currentTimeMillis();
            double startClicks = mtrShooter.getCurrentPosition();


            //this is code for the first player


            double x1 = gamepad1.left_stick_x;
            double y1 = -gamepad1.left_stick_y;
            double x2 = gamepad1.right_stick_x;

            double frontLeft;
            double frontRight;
            double backLeft;
            double backRight;

            shooterCurrentSpeed = Math.abs(mtrShooter.getPower());

            // Code for the drive train
            if (deadzone(x1) == 0 && deadzone(y1) == 0 && deadzone(x2) == 0) {
                MOVING = false;
                DT_STOP = true;
            } else {
                MOVING = true;
                DT_STOP = false;
            }

            if (gamepad1.left_trigger > 0.5) {
                SLOW_SPEED = true;
            } else {
                SLOW_SPEED = false;
            }

            if (gamepad1.right_trigger > 0.5) {
                SUPER_SLOW_SPEED = true;
            } else {
                SUPER_SLOW_SPEED = false;
            }


            //this is code for the speed of the wheels, controlled by the first player since its the one positioning the robot.
            /*
            Defaults are 0.3/0.25/0.2/0/0.35
             */
            if (gamepad1.dpad_up) {
                mtrShooter.setPower(highPower);
            } else if (gamepad1.dpad_left) {
                mtrShooter.setPower(medPower);
            } else if (gamepad1.dpad_right) {
                mtrShooter.setPower(lowPower);
            } else if (gamepad1.dpad_down) {
                mtrShooter.setPower(0);
            } else if (gamepad1.left_bumper) {
                mtrShooter.setPower(superPower);
            }


            //this section is code for second player


            //Beacon servo code
            if (gamepad2.a) {
                svoBeacon.setPosition(.25);
            } else {
                svoBeacon.setPosition(0.85);
            }


            //Collector code, controlled by the second player for intake
            if (gamepad2.x) {
                COLLECTOR_RUNNING = true;
                COLLECTOR_BACKWARDS = false;
                COLLECTOR_STOP = false;
            } else if (gamepad2.y) {
                COLLECTOR_RUNNING = false;
                COLLECTOR_BACKWARDS = true;
                COLLECTOR_STOP = false;
            } else {
                COLLECTOR_RUNNING = false;
                COLLECTOR_BACKWARDS = false;
                COLLECTOR_STOP = true;
            }


            //code for the cap ball lasso
            if (gamepad1.right_bumper) {
                FORK_OUT = true;
                FORK_IN = false;
            } else {
                FORK_OUT = false;
                FORK_IN = true;
            }

            //code for the Linear slide
            if (-gamepad2.left_stick_y > .2 && (highLimit.getState() || gamepad1.a)) {
                LIFT_UP = true;
                LIFT_DOWN = false;
                LIFT_STOP = false;
            } else if (-gamepad2.left_stick_y < -.2 && (!lowLimit.getState() || gamepad1.a)) {
                LIFT_UP = false;
                LIFT_DOWN = true;
                LIFT_STOP = false;
            } else {
                LIFT_UP = false;
                LIFT_DOWN = false;
                LIFT_STOP = true;
            }


            //code for the firing pin, controlled by the second player
            if (gamepad2.b) {
                FLICK_BALL = true;
                FLICK_STOP = false;
                MOVING = false;
                DT_STOP = true;
            } else {
                FLICK_BALL = false;
                FLICK_STOP = true;
                MOVING = true;
                DT_STOP = false;
            }


            //Code for the revolver states, controlled by the second player
            if (gamepad2.left_bumper) {
                FEED_BALL = true;
                FEED_REVERSE = false;
                FEED_STOP = false;
                FEED_BACK = false;
            } else if (gamepad2.right_bumper) {
                FEED_BALL = false;
                FEED_REVERSE = false;
                FEED_STOP = false;
                FEED_BACK = true;
            } else if (gamepad2.right_trigger > 0.5) {
                FEED_BALL = false;
                FEED_REVERSE = true;
                FEED_STOP = false;
                FEED_BACK = false;
            } else {
                FEED_BALL = false;
                FEED_REVERSE = false;
                FEED_STOP = true;
                FEED_BACK = false;
            }

            // lift encoder code
            if (LIFT_UP && !LIFT_DOWN) {
                if (snsCounterEOPD.getLightDetected() > liftEnc && !LIFT_FLAG) {
                    counter++;
                    LIFT_FLAG = true;
                } else if (snsCounterEOPD.getLightDetected() <= liftEnc && LIFT_FLAG) {
                    counter++;
                    LIFT_FLAG = false;
                }
            } else if (LIFT_DOWN && !LIFT_UP) {
                if (snsCounterEOPD.getLightDetected() > liftEnc && !LIFT_FLAG) {
                    counter--;
                    LIFT_FLAG = true;
                } else if (snsCounterEOPD.getLightDetected() <= liftEnc && LIFT_FLAG) {
                    counter--;
                    LIFT_FLAG = false;
                }
            }

            // slowing for lift
            if (LIFT_UP && counter > liftUp) {
                mtrSlide.setPower(-gamepad1.left_stick_y * 4 / 5);
            } else if (LIFT_DOWN && (counter < liftDown || gamepad1.a)) {
                mtrSlide.setPower(-gamepad1.left_stick_y * 4 / 5);
            }

            //now we enter the section showing the actual movement of motors instead of the logic behind it

            // manual lift control
            if (LIFT_UP) {
                mtrSlide.setPower(-gamepad2.left_stick_y);
            } else if (LIFT_DOWN) {
                mtrSlide.setPower(-gamepad2.left_stick_y);
            } else if (LIFT_STOP) {
                mtrSlide.setPower(0);
            }

            // forklift operation
            if (FORK_OUT) {
                svoLift.setPosition(liftHighPos);
            } else if (gamepad1.x) {
                svoLift.setPosition(liftMedPos);
            } else {
                svoLift.setPosition(liftInitPos);
            }

            // feeder operation
            if (FEED_BACK && !FEED_WATCH1) {
                FEED_WATCH1 = true;
            } else if (!FEED_BACK && FEED_WATCH1 && svoFeeder.getPosition() < feederInitPos) {
                FEED_WATCH1 = false;
                svoFeeder.setPosition(svoFeeder.getPosition() + feederIndex);
            }

            if (FEED_BALL && !FEED_WATCH) {
                FEED_WATCH = true;
            } else if (!FEED_BALL && FEED_WATCH && svoFeeder.getPosition() > 0.03) {
                FEED_WATCH = false;
                svoFeeder.setPosition(svoFeeder.getPosition() - feederIndex);
            }

            // revolver reset
            if (FEED_REVERSE) {
                svoFeeder.setPosition(feederInitPos);
            }

            // collector operation
            if (COLLECTOR_RUNNING) {
                mtrCollector.setPower(1);
            } else if (COLLECTOR_BACKWARDS) {
                mtrCollector.setPower(-1);
            } else if (COLLECTOR_STOP) {
                mtrCollector.setPower(0);
            }

            // firing pin operation
            if (FLICK_BALL) {
                svoFlick.setPosition(0.6);
            } else if (FLICK_STOP) {
                svoFlick.setPosition(0.1);
            }

            // drive code and speed settings
            if (DT_STOP) {
                mtrFL.setPower(0);
                mtrFR.setPower(0);
                mtrBL.setPower(0);
                mtrBR.setPower(0);
            } else if (MOVING) {
                if (SLOW_SPEED) {
                    frontLeft = (x1 + y1 + x2) / 3;
                    frontRight = (-x1 + y1 - x2) / 3;
                    backLeft = (x1 - y1 - x2) / 3;
                    backRight = (-x1 - y1 + x2) / 3;
                } else if (SUPER_SLOW_SPEED) {
                    frontLeft = (x1 + y1 + x2) / 9;
                    frontRight = (-x1 + y1 - x2) / 9;
                    backLeft = (x1 - y1 - x2) / 9;
                    backRight = (-x1 - y1 + x2) / 9;
                } else {
                    frontLeft = x1 + y1 + x2;
                    frontRight = -x1 + y1 - x2;
                    backLeft = x1 - y1 - x2;
                    backRight = -x1 - y1 + x2;
                }

                mtrFL.setPower(Range.clip(-frontLeft, -1, 1));
                mtrFR.setPower(Range.clip(frontRight, -1, 1));
                mtrBL.setPower(Range.clip(backLeft, -1, 1));
                mtrBR.setPower(Range.clip(-backRight, -1, 1));
            }

            if (!BALL_DETECTED || ballTimer.done()) {
                svoFlag.setPosition(0);
                BALL_DETECTED = false;
            } else {
                svoFlag.setPosition(0.5);
            }

            // ball detection
            /*if (COLLECTOR_RUNNING || COLLECTOR_BACKWARDS) {
                snsColorRevolver.engage();
                int[] results = snsColorRevolver.Avery_ReadColor16();
                telemetry.addData("RGBW 16 Bit: ", Arrays.toString(results));
                if (results[0] >= 30 || results[2] >= 30) {
                    BALL_DETECTED = true;
                    ballTimer.reset();
                } else {
                    BALL_DETECTED = false;
                }
            } else {
                snsColorRevolver.disengage();
            }*/


            double endLoop = System.currentTimeMillis();
            double deltaTime = endLoop - startLoop;

            double endClicks = mtrShooter.getCurrentPosition();
            double deltaClicks = endClicks - startClicks;

            deltaEncoderClicks = (deltaClicks / deltaTime) * 1000;

            telemetry.addData("Revolver Pos: ", svoFeeder.getPosition());
            telemetry.addData("Shooter Power: ", mtrShooter.getPower());
            telemetry.addData("Lift Pos: ", mtrSlide.getCurrentPosition());

            telemetry.addData("Time in the loop (ms): ", deltaTime);
            telemetry.addData("Delta Encoders (per second): ", deltaEncoderClicks);
            telemetry.addData("Delta Encoders (speed control): ", speed.getVelocity());

            telemetry.addData("enc/s", speed.getVelocity());
            telemetry.addData("Ultrasonic reading", Double.toString(snsRange.getDistance(DistanceUnit.CM)));

            telemetry.addData("Low Switch", Boolean.toString(lowLimit.getState()));
            telemetry.addData("High Switch", Boolean.toString(highLimit.getState()));

            telemetry.addData("Counter EOPD: ", Double.toString(snsCounterEOPD.getLightDetected()));
            telemetry.addData("Counter on Lift: ", Integer.toString(counter));
            telemetry.addData("Lag (ms): ", lagTracker.elapsed());
            lagTracker.reset();
        } catch (Exception e) {}
    }





    public double deadzone(double power) {
        if(Math.abs(power) < .1) {
            return 0;
        } else {
            return power;
        }
    }

    public void rampShooter(double targetPower) {
        if(RAMPING_UP_SHOOTER) {
            while(shooterCurrentSpeed < targetPower) {
                mtrShooter.setPower(-(shooterCurrentSpeed + .005));

                shooterCurrentSpeed = Math.abs(mtrShooter.getPower());
            }
        } else if(RAMPING_DOWN_SHOOTER) {
            while(shooterCurrentSpeed < targetPower) {
                mtrShooter.setPower(-(shooterCurrentSpeed - .005));

                shooterCurrentSpeed = Math.abs(mtrShooter.getPower());
            }
        }
    }

    public static void updateShooterState(double shooterTargetSpeed, double shooterCurrentSpeed) {
        if(shooterTargetSpeed < shooterCurrentSpeed) {
            RAMPING_DOWN_SHOOTER = true;
            RAMPING_UP_SHOOTER = false;
        } else if(shooterTargetSpeed > shooterCurrentSpeed) {
            RAMPING_UP_SHOOTER = true;
            RAMPING_DOWN_SHOOTER = false;
        }

        if(RAMPING_DOWN_SHOOTER || RAMPING_UP_SHOOTER) {
            SHOOTER_RUNNING = false;
            SHOOTER_STOP = false;
        } else if((shooterCurrentSpeed == shooterTargetSpeed) && (shooterCurrentSpeed > 0)) {
            SHOOTER_RUNNING = true;
            SHOOTER_STOP = false;
            RAMPING_UP_SHOOTER = false;
            RAMPING_DOWN_SHOOTER = false;
        } else {
            SHOOTER_RUNNING = false;
            SHOOTER_STOP = true;
        }
    }

    @Override
    public void stop() {
        speed.disablePid();
        speed.stop();
    }
}
