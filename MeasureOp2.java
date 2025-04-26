package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.logging.Loggable;
import org.firstinspires.ftc.teamcode.util.IniConfig;

/**
 * Created by guinea on 4/10/17.
 * Mostly to debug autonomous programs.
 *
 * Included:
 * Driving
 * Deadzone
 * Power-cutting/slow driving
 * Sensor dumps
 * All shooting systems
 *
 * Not included:
 * beacon thwacker
 * lift code (as it's teleop only)
 */
@TeleOp(name="Sensor measure teleop")
public class MeasureOp2 extends AutonomousOpMode {

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private double deadzone(double x, double dead) {
        if (Math.abs(x) >= Math.abs(dead)) {
            return x;
        } else {
            return 0;
        }
    }

    IniConfig.ConfigSection config;
    @Override
    public void initHook() {
        setFileLogEnabled(false);
        config = robotConfig.getSection("TeleOp");
        mtrFL.setMaxSpeed(2900);
        mtrFR.setMaxSpeed(2900);
        mtrBL.setMaxSpeed(2900);
        mtrBR.setMaxSpeed(2900);
    }
    @Override
    public void runAutonomous() throws InterruptedException {
        logger.addSource("revolver", new Loggable() {
            @Override
            public Object getLogData() {
                return svoFeeder.getPosition();
            }
        });
        while (opModeIsActive()) {

            // driving with deadzone
            double dead = config.getNumber("joystickDeadzone", 0.1);
            double mult = 1;
            if (gamepad1.left_trigger > 0.5)  {
                mult = 1/3d;
            } else if (gamepad1.right_trigger > 0.5) {
                mult = 1/9d;
            }
            omniDrive(-deadzone(gamepad1.left_stick_y, dead) * mult,
                    deadzone(gamepad1.left_stick_x, dead) * mult,
                    deadzone(gamepad1.right_stick_x, dead) * mult);

            // shooting - power
            // the defaults are a compromise which should keep the bevels spinning under most levels of wear
            double lowPower = config.getNumber("shooter.low", 0.2);
            double medPower = config.getNumber("shooter.med", 0.3);
            double highPower = config.getNumber("shooter.high", 0.4);
            double superPower = config.getNumber("shooter.super", 0.55);

            if (gamepad1.dpad_down) {
                mtrShooter.setPower(0);
            } else if (gamepad1.dpad_right) {
                mtrShooter.setPower(lowPower);
            } else if (gamepad1.dpad_left) {
                mtrShooter.setPower(medPower);
            } else if (gamepad1.dpad_up) {
                mtrShooter.setPower(highPower);
            } else if (gamepad1.left_bumper) {
                mtrShooter.setPower(superPower);
            }


            // shooting - collector
            if (gamepad2.x) {
                mtrCollector.setPower(1);
            } else if (gamepad2.y) {
                mtrCollector.setPower(-1);
            } else {
                mtrCollector.setPower(0);
            }

            // shooting - revolver
            double offset = (gamepad1.a) ? 0.001 : 0.013;
            if (gamepad2.left_bumper && !leftPressed) {
                if (svoFeeder.getPosition() > 0.03) {
                    svoFeeder.setPosition(svoFeeder.getPosition() - offset);
                }
            } else if (gamepad2.right_bumper && !rightPressed) {
                if (svoFeeder.getPosition() < 0.981) {
                    svoFeeder.setPosition(svoFeeder.getPosition() + offset);
                }
            } else if (gamepad2.right_trigger > 0.5) {
                svoFeeder.setPosition(0.981);
            }

            leftPressed = gamepad2.left_bumper;
            rightPressed = gamepad2.right_bumper;

            // shooting - flicker
            if (gamepad2.b)  {
                svoFlick.setPosition(0.6);
            } else {
                svoFlick.setPosition(0.1);
            }

            // sensor - reset drivebase
            if (gamepad1.b) {
                resetDrive();
                snsGyroRaw.resetZAxisIntegrator();
            }

            if (gamepad1.x) {
                shoot();
                svoFeeder.setPosition(0.981);
            }
        }
    }
}
