package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.util.Guinea_AdjustedGyro;
import org.firstinspires.ftc.teamcode.util.Guinea_EncoderTracker;
import org.firstinspires.ftc.teamcode.util.SpeedController;

/**
 * Created by Avery on 10/6/2016.
 */
@Disabled
@TeleOp(name="Velocity TeleOp Drone", group="K9bot")

public class TeleOpVelocity5484_DroneDrive extends OpMode {

    // debug constant to enable/disable velocity at compile time
    // merged branches to master should NEVER have this set to false.
    public static final boolean VELOCITY = false;

    DcMotor mtrFL;
    DcMotor mtrFR;
    DcMotor mtrBL;
    DcMotor mtrBR;

    DcMotor mtrShooter;
    DcMotor mtrCollector;
    SpeedController speed;
    Guinea_EncoderTracker encShooter;
    Guinea_EncoderTracker encSlide;
    Guinea_AdjustedGyro snsGyro;

    DcMotor mtrSlide;

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

    protected ModernRoboticsI2cGyro snsGyroRaw;
    ColorSensor snsColorBeacon;
    ModernRoboticsI2cRangeSensor snsRange;

    Servo svoFlick;
    Servo svoFeeder;
    Servo svoBeacon;
    Servo svoLift;

    Phin_DroneDrive drive = new Phin_DroneDrive();

    float hsvValues1[] = {0F, 0F, 0F};
    float hsvValues2[] = {0F, 0F, 0F};

    @Override
    public void init() {

        mtrFL = hardwareMap.dcMotor.get("mtrFL");
        mtrFR = hardwareMap.dcMotor.get("mtrFR");
        mtrBL = hardwareMap.dcMotor.get("mtrBL");
        mtrBR = hardwareMap.dcMotor.get("mtrBR");


        svoFlick = hardwareMap.servo.get("svoFlick");
        svoFeeder = hardwareMap.servo.get("svoFeeder");
        svoBeacon = hardwareMap.servo.get("svoBeacon");
        svoLift = hardwareMap.servo.get("svoLift");


        mtrCollector = hardwareMap.dcMotor.get("mtrCollector");
        mtrShooter = hardwareMap.dcMotor.get("mtrShooter");
        mtrSlide = hardwareMap.dcMotor.get("mtrSlide");


        encShooter = new Guinea_EncoderTracker(mtrShooter);
        encShooter.reset();
        speed = new SpeedController(mtrShooter, encShooter, 1000 * 9999);
        speed.start();
        if (VELOCITY) speed.enablePid();

        snsGyroRaw = (ModernRoboticsI2cGyro) hardwareMap.gyroSensor.get("snsGyro");
        snsRange = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "snsRange");
        snsColorBeacon = hardwareMap.colorSensor.get("snsColorBeaconLeft");

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

        snsGyroRaw.calibrate();
        while(snsGyroRaw.isCalibrating());

        snsGyro = new Guinea_AdjustedGyro(snsGyroRaw);

        snsGyro.reset();



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

        snsColorBeacon.enableLed(false);

        //mtrShooter.setDirection(DcMotorSimple.Direction.REVERSE);
        mtrCollector.setDirection(DcMotorSimple.Direction.REVERSE);

        svoFeeder.setPosition(.986);
        svoBeacon.setPosition(.85);
        svoLift.setPosition(.99);
        svoFlick.setPosition(.1);
    }

    @Override
    public void loop() {
        double startLoop = System.currentTimeMillis();
        double startClicks = mtrShooter.getCurrentPosition();

        //this is code for the first player

        double x1 = gamepad1.left_stick_x;
        double y1 = -gamepad1.left_stick_y;
        double x2 = gamepad1.right_stick_x;

        drive.setMotorPowers(x1, x2, y1, snsGyro.getHeading());

        shooterCurrentSpeed = Math.abs(mtrShooter.getPower());

        // Code for the drive train
        if(deadzone(x1) == 0 && deadzone(y1) == 0 && deadzone(x2) == 0) {
            MOVING = false;
            DT_STOP = true;
        } else {
            MOVING = true;
            DT_STOP = false;
        }

        if(gamepad1.left_trigger > 0.5) {
            SLOW_SPEED = true;
        } else {
            SLOW_SPEED = false;
        }

        if(gamepad1.right_trigger > 0.5) {
            SUPER_SLOW_SPEED = true;
        } else {
            SUPER_SLOW_SPEED = false;
        }


        //This button resests the gyro position
        if(gamepad1.back) {
            snsGyroRaw.resetZAxisIntegrator();
            snsGyro.reset();
        }




        //this section is code for second player







        //this is code for the speed of the wheels, controlled by the first player since its the one positioning the robot.
        if (VELOCITY) {
            if (gamepad2.dpad_up) {
                speed.setPower(SpeedController.AUTONOMOUS_POWER);
                //mtrShooter.setPower(.55);
            } else if (gamepad2.dpad_left) {
                speed.setPower(SpeedController.HIGH_POWER);
                //mtrShooter.setPower(.45);
            } else if (gamepad2.dpad_right) {
                speed.setPower(SpeedController.MED_POWER);
                //mtrShooter.setPower(.35);
            } else if (gamepad2.dpad_down) {
                speed.setPower(SpeedController.NO_POWER);
                //mtrShooter.setPower(0);
            }
        } else {
            if(gamepad2.dpad_up) {
                mtrShooter.setPower(SpeedCalibrationOp.highPower);
            } else if(gamepad2.dpad_left) {
                mtrShooter.setPower(SpeedCalibrationOp.medPower);
            } else if(gamepad2.dpad_right) {
                mtrShooter.setPower(SpeedCalibrationOp.lowPower);
            } else if(gamepad2.dpad_down) {
                mtrShooter.setPower(0);
            } else if(gamepad2.left_trigger > .5) {
                mtrShooter.setPower(SpeedCalibrationOp.superHighPower);
            }
        }




        //Beacon servo code
        if (gamepad2.a){
            svoBeacon.setPosition(.25);
        }
        else{
            svoBeacon.setPosition(0.85);
        }



        //Collector code, controlled by the second player for intake
        if(gamepad2.x) {
            COLLECTOR_RUNNING = true;
            COLLECTOR_BACKWARDS = false;
            COLLECTOR_STOP = false;
        } else if(gamepad2.y) {
            COLLECTOR_RUNNING = false;
            COLLECTOR_BACKWARDS = true;
            COLLECTOR_STOP = false;
        } else {
            COLLECTOR_RUNNING = false;
            COLLECTOR_BACKWARDS = false;
            COLLECTOR_STOP = true;
        }



        //code for the cap ball lasso
        if(gamepad1.right_bumper) {
            FORK_OUT = true;
            FORK_IN = false;
        } else {
            FORK_OUT = false;
            FORK_IN = true;
        }

        //code for the Linear slide
        if (gamepad1.y && (encSlide.getCurrentPosition() > -100000 || gamepad1.b)) {
            LIFT_UP = true;
            LIFT_DOWN = false;
            LIFT_STOP = false;
        }
        else if(gamepad1.x && (encSlide.getCurrentPosition() <= 0 || gamepad1.a)) {
            LIFT_UP = false;
            LIFT_DOWN = true;
            LIFT_STOP = false;
        }
        else{
            LIFT_UP = false;
            LIFT_DOWN = false;
            LIFT_STOP = true;
        }


        //code for the firing pin, controlled by the second player
        if(gamepad2.b) {
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


        //Code for the revolver, controlled by the second player
        if(gamepad2.left_bumper) {
            FEED_BALL = true;
            FEED_REVERSE = false;
            FEED_STOP = false;
            FEED_BACK = false;
        } else if(gamepad2.right_bumper) {
            FEED_BALL = false;
            FEED_REVERSE = false;
            FEED_STOP = false;
            FEED_BACK = true;
        } else if (gamepad2.right_trigger > 0.5) {
            FEED_BALL = false;
            FEED_REVERSE = true;
            FEED_STOP = false;
            FEED_BACK = false;
        }
        else {
            FEED_BALL = false;
            FEED_REVERSE = false;
            FEED_STOP = true;
            FEED_BACK = false;
        }








        //now we enter the section showing the actual movement of motors instead of the logic behind it



        if (LIFT_UP){
            mtrSlide.setPower(0.7);
        }
        else if(LIFT_DOWN){
            mtrSlide.setPower(-0.7);
        }
        else if(LIFT_STOP){
            mtrSlide.setPower(0);
        }
        else{
            mtrSlide.setPower(0);
        }


        if (FORK_OUT) {
            svoLift.setPosition(.7);
        } else {
            svoLift.setPosition(1);
        }

        if(FEED_BACK && !FEED_WATCH1) {
            FEED_WATCH1 = true;
        } else if(!FEED_BACK && FEED_WATCH1 && svoFeeder.getPosition() < 0.986) {
            FEED_WATCH1 = false;
            svoFeeder.setPosition(svoFeeder.getPosition() + .026);
        }

        if(FEED_BALL && !FEED_WATCH) {
            FEED_WATCH = true;
        } else if(!FEED_BALL && FEED_WATCH && svoFeeder.getPosition() > 0.03) {
            FEED_WATCH = false;
            svoFeeder.setPosition(svoFeeder.getPosition() - .026);
        }

        if(FEED_REVERSE) {
            svoFeeder.setPosition(.986);
        }







        if(COLLECTOR_RUNNING){
            mtrCollector.setPower(1);
        }
        else if (COLLECTOR_BACKWARDS){
            mtrCollector.setPower(-1);
        }
        else if (COLLECTOR_STOP){
            mtrCollector.setPower(0);
        }






        if(FLICK_BALL){
            svoFlick.setPosition(0.6);
        }
        else if(FLICK_STOP){
            svoFlick.setPosition(0.1);
        }


        if(gamepad1.back) {
            snsGyro.reset();
        }


        if(DT_STOP) {
            mtrFL.setPower(0);
            mtrFR.setPower(0);
            mtrBL.setPower(0);
            mtrBR.setPower(0);
        }
        else if(MOVING) {
            if(SLOW_SPEED) {
                drive.fl = drive.fl / 3;
                drive.fr = drive.fr / 3;
                drive.bl = drive.bl / 3;
                drive.br = drive.br / 3;
            } else if(SUPER_SLOW_SPEED) {
                drive.fl = drive.fl / 9;
                drive.fr = drive.fr / 9;
                drive.bl = drive.bl / 9;
                drive.br = drive.br / 9;
            } else {
                drive.fl = drive.fl;
                drive.fr = drive.fr;
                drive.bl = drive.bl;
                drive.br = drive.br;
            }

            mtrFL.setPower(Range.clip(-drive.fl, -1, 1));
            mtrFR.setPower(Range.clip(drive.fr, -1, 1));
            mtrBL.setPower(Range.clip(drive.bl, -1, 1));
            mtrBR.setPower(Range.clip(-drive.br, -1, 1));
        }







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
