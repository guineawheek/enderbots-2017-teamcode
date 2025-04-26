package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.util.Guinea_Timer;
import org.firstinspires.ftc.teamcode.util.Dir;
import org.firstinspires.ftc.teamcode.util.SpeedController;

import static org.firstinspires.ftc.teamcode.Guinea_hwInit.driveStraight;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.encFL;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.resetDrive;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.shoot;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.shooter;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.snsGyroRaw;

/**
 * Created by guinea on 1/22/17.
 *
 * This was a hacked together way to test how the robot would drive forward to shoot in the standard
 * autonomous mode.
 */
@Disabled
@TeleOp(name="shoot-position", group="asdf")
public class autoAltTest extends LinearOpMode {
    public void runOpMode() throws InterruptedException {

        Guinea_hwInit.init(hardwareMap);
        //mtrShooter.setMaxSpeed(2900);
        //shooter.enablePid();
        telemetry.setAutoClear(false);
        telemetry.addLine("Are you ready for a MIRACLE?");
        telemetry.update();
        while (snsGyroRaw.isCalibrating());
        telemetry.addLine("the Robot is ready! bless Kamen \uD83D\uDC4C");
        telemetry.update();
        waitForStart();

        telemetry.setAutoClear(true);
        resetDrive();
        /*double target = (allianceColor.getState() == ALLIANCE_RED) ? 0 : 172;
        double lineSideCoeff = (allianceColor.getState() == ALLIANCE_RED) ? 1 : -1;
        // experimental new linefollower
        while (opModeIsActive()) {
            double y1 = 0.2;
            double x1 = (65 - snsColorLine.alpha()) * lineSideCoeff * 0.003;
            double x2 = 0; //(snsGyroRaw.getIntegratedZValue() - target) * 0.0005;

            double frontLeft  =  x1 + y1 + x2;
            double frontRight = -x1 + y1 - x2;
            double backLeft   =  x1 - y1 - x2;
            double backRight  = -x1 - y1 + x2;
            setDrivePower(frontLeft, frontRight, backLeft, backRight);

            telemetry.addData("alpha:", snsColorLine.alpha());
            telemetry.addData("gyro_raw:", snsGyroRaw.getIntegratedZValue());
            telemetry.addData("x1:", x1);
            telemetry.addData("y1:", y1);
            telemetry.addData("x2:", x2);
            telemetry.update();
            idle();
        }
        if (true) return;*/

        shooter.enablePid();
        shooter.setPower(SpeedController.AUTONOMOUS_POWER);
        Guinea_Timer spoolTimer = new Guinea_Timer();
        spoolTimer.setTarget(1500);
        while (Math.abs(encFL.getRotations()) < 0.83 && opModeIsActive()) {
            driveStraight(0.7, Dir.forward);
        }
        resetDrive();
        while (!spoolTimer.done() && opModeIsActive());

        shoot();
        shooter.stop();

        /*while (Math.abs(encFL.getRotations()) < 0.83 && opModeIsActive()) {
            driveStraight(0.7, Guinea_hwInit.Dir.right);
        }
        resetDrive();*/
    }
}
