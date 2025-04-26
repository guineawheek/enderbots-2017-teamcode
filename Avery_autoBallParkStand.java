package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.util.Guinea_Timer;
import org.firstinspires.ftc.teamcode.util.Dir;

import static org.firstinspires.ftc.teamcode.Guinea_hwInit.ALLIANCE_BLUE;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.ALLIANCE_RED;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.allianceColor;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.driveStraight;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.encFL;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.moveTo;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.mtrBL;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.mtrBR;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.mtrFL;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.mtrFR;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.mtrShooter;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.resetDrive;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.setDrivePower;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.shoot;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.snsGyro;

/**
 * Created by Avery on 1/11/2017.
 */
@Autonomous (name="Yoga ball, & park Auto", group="K9Bot")
public class Avery_autoBallParkStand extends LinearOpMode{
    @Override
    public void runOpMode() throws InterruptedException {

        Guinea_hwInit.init(hardwareMap);
        double allianceCoeff = (allianceColor.getState() == ALLIANCE_RED) ? 0.2 : -0.2;
        waitForStart();
        sleep(18000);
        Guinea_Timer timer = new Guinea_Timer();
        timer.setTarget(1500);
        mtrShooter.setPower(0.45);
        boolean alliance = allianceColor.getState();

        resetDrive();
        while (Math.abs(encFL.getRotations()) < 2.3 && opModeIsActive()) {
            driveStraight(0.7, Dir.forward);
        }
        resetDrive();
        //moveTo(Guinea_hwInit.Dir.forward, 2.3, 1);
        while (!timer.done() && opModeIsActive());
        //spool();
        shoot();
        sleep(1000);
        while (Math.abs(encFL.getRotations()) < 2 && opModeIsActive()) {
            driveStraight(0.7, Dir.forward);
        }
        resetDrive();
        //moveTo(Guinea_hwInit.Dir.forward, 2, 1);

        while (Math.abs(snsGyro.getHeading()) < 45 && opModeIsActive()) {
            setDrivePower(allianceCoeff, allianceCoeff, allianceCoeff, allianceCoeff);
        }
        resetDrive();
        while (Math.abs(encFL.getRotations()) < 2 && opModeIsActive()) {
            driveStraight(0.7, Dir.forward);
        }
       // moveTo(Guinea_hwInit.Dir.forward, 2, 1);
        if (true) return;
        if (alliance == ALLIANCE_BLUE) {
            mtrBR.setPower(0.3);
            mtrFR.setPower(0.3);
            mtrBL.setPower(0.3);
            mtrFL.setPower(0.3);
            while (snsGyro.getHeading() > 310 && snsGyro.getHeading() < 320);
            mtrFL.setPower(0);
            mtrFR.setPower(0);
            mtrBL.setPower(0);
            mtrBR.setPower(0);
            sleep(500);
            mtrBR.setPower(0.7);
            mtrFL.setPower(-0.7);

            //mtrFR.setPower(0);
            //mtrBR.setPower(0);
        } else {
            mtrFL.setPower(-0.7);
            mtrBL.setPower(-0.7);
        }
        sleep(700);
        setDrivePower(0,0,0,0);
    }
}
