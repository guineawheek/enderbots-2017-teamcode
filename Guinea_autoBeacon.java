package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cColorSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.util.Guinea_EncoderTracker;
import org.firstinspires.ftc.teamcode.util.Guinea_Timer;
import org.firstinspires.ftc.teamcode.util.Dir;
import org.firstinspires.ftc.teamcode.util.TelemetryManager;

import static org.firstinspires.ftc.teamcode.Guinea_hwInit.*;

/**
 * Created by Guinea on 11/25/16.
 *
 * Attempts the beacons.
 */

@Autonomous(name="Beacon Auto (remember alliance switch!)", group="K9Bot")
public class Guinea_autoBeacon extends LinearOpMode {
    // value threshould to be able to count as a "color"
    // used to discriminate against white and beacons of the opposite color
    final static double COLOR_THRESH = 15;

    // used in logging to record what the robot is currently doing on a high level
    static String state = "init";
    static Guinea_Timer autoTime;
    boolean logThreadRunning;
    /*
     * When current encoder count < target
     * It's shorthand to check if an encoder moving counterclockwise has reached
     * a certain number of rotations
     */
    private boolean underTarget(Guinea_EncoderTracker enc, double rotations) {
        return enc.getCurrentPosition() < rotations * DISTANCE_PER_ROT;
    }

    /*
     * When current encoder count > target.
     * It's shorthand to check if an encoder moving clockwise has reached
     * a certain number of rotations
     */
    private boolean overTarget(Guinea_EncoderTracker enc, double rotations) {
        return enc.getCurrentPosition() > rotations * DISTANCE_PER_ROT;
    }

    /**
     * Checks to see if what a beacon color sensor is reading sufficiently red.
     * @param snsColorBeacon the color sensor in question
     * @return true if sufficiently red
     */
    private boolean red(ModernRoboticsI2cColorSensor snsColorBeacon) {
        return (snsColorBeacon.red() - 2) > snsColorBeacon.blue();
    }

    /**
     * Checks to see if what a beacon color sensor is reading sufficiently blue.
     * @param snsColorBeacon the color sensor in question
     * @return true if sufficiently blue
     */
    private boolean blue(ModernRoboticsI2cColorSensor snsColorBeacon) {
        return (snsColorBeacon.blue() - 2) > snsColorBeacon.red();
    }

    /**
     * moves the beacon appendage out and in
     * @throws InterruptedException
     */
    private void togglebeacon() throws InterruptedException {
        svoThwack.setPosition(0.25);
        sleep(600);
        svoThwack.setPosition(0.85);
    }

    /**
     * used internally by thack()
     * @param isRed whether the robot is on the red alliance
     * @param relevantColorSensor the relevant color sensor (determined by thwack())
     * @return the beacon seems to be of the correct color
     */
    private boolean beaconCorrect(boolean isRed, ModernRoboticsI2cColorSensor relevantColorSensor) {
        return (isRed && red(relevantColorSensor)) || (!isRed && blue(relevantColorSensor));
    }

    /**
     * beacon pressing routine
     * @param alliance the alliance color boolean as pulled from the alliance color switch
     * @param firstBeacon whether or not the robot is on the side of the beacon closest to the robot starting position
     * @throws InterruptedException
     */
    private void thwack(boolean alliance, boolean firstBeacon) throws InterruptedException {
        // if we fail, we try three times before giving up and trying the next one
        /*
        So here's the idea
        Find the wrong colored side
        Thwack it until it turns the color we want
        ?
        Profit
         */

        if (!opModeIsActive()) return;
        boolean isRed = (alliance == ALLIANCE_RED);
        double allianceCoeff = (isRed) ? 1 : -1;
        double beaconCoeff = (firstBeacon) ? 1 : -1;

        //double tilt = 0.1 * allianceCoeff * ((firstBeacon) ? -1 : 1);
        // clever hack to compute which color sensor is on the opposite side of where the beacon presser is now
        // so we can check if the beacon changed to our color
        ModernRoboticsI2cColorSensor relevantColorSensor = (allianceCoeff * beaconCoeff == 1) ? snsColorBeaconRight : snsColorBeaconLeft;
        state = "thwack-1";

        // what? the beacon is ALREADY our color?
        // http://i.imgur.com/KoyDO1u.png
        if (beaconCorrect(isRed, relevantColorSensor)) return;

        togglebeacon();
        sleep(300);

        // did we get it on the first try?
        if (beaconCorrect(isRed, relevantColorSensor)) return;
        // guess not. whelp, let's try once more

        state = "thwack-2";

        // move a biit closer
        Guinea_Timer timer = new Guinea_Timer();
        timer.setTarget(250);
        while (!timer.done()) {
            setDrivePower(0.3, 0.3, -0.3, -0.3);
        }
        setDrivePower(0, 0, 0, 0);
        togglebeacon();
    }

    @Override
    public void runOpMode() throws InterruptedException {

        Guinea_hwInit.init(hardwareMap);

        boolean alliance = allianceColor.getState();
        double allianceCoeff = (alliance == ALLIANCE_RED) ? 1 : -1; // 1 if red, -1 otherwise

        //mtrShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //mtrShooter.setMaxSpeed(2230);
        telemetry.setAutoClear(false);
        telemetry.addLine("Are you ready for a MIRACLE?");
        telemetry.update();
        while (snsGyroRaw.isCalibrating());
        telemetry.addLine("The robot is ready! Hail Kamen \uD83D\uDC4C");
        telemetry.update();
        waitForStart();
        telemetry.setAutoClear(true);
        autoTime = new Guinea_Timer();
        autoTime.reset();
        logThreadRunning = true;

        // just the logging thread
        Thread logThread = new Thread() {
            public void run() {
                TelemetryManager telemetryManager = new TelemetryManager(telemetry);
                telemetryManager.pushHeaders("time", "state", "mtrFL", "mtrFR", "mtrBL", "mtrBR",
                              "encFL", "encFR", "encBL", "encBR",
                              "snsGyroRaw", "snsGyro", "snsRange",
                              "snsColorLine",
                              "snsColorBeaconLeft",
                              "snsColorBeaconRight"
                );

                while (opModeIsActive() && !isInterrupted()) {

                    telemetryManager.pushValues(autoTime.elapsed() / 1000d, state,
                            mtrFL.getPower(), mtrFR.getPower(), mtrBL.getPower(), mtrBR.getPower(),
                            encFL.getCurrentPosition(), encFR.getCurrentPosition(), encBL.getCurrentPosition(), encBR.getCurrentPosition(),
                            snsGyroRaw.getIntegratedZValue(), snsGyro.getHeading(), snsRange.getDistance(DistanceUnit.CM),
                            String.format("(%d %d %d %d)", snsColorLine.red(), snsColorLine.green(), snsColorLine.blue(), snsColorLine.alpha()),
                            String.format("(%d %d %d %d)", snsColorBeaconLeft.red(), snsColorBeaconLeft.green(), snsColorBeaconLeft.blue(), snsColorBeaconLeft.alpha()),
                            String.format("(%d %d %d %d)", snsColorBeaconRight.red(), snsColorBeaconRight.green(), snsColorBeaconRight.blue(), snsColorBeaconRight.alpha())
                    );
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e) { break; }
                }
                telemetryManager.close();
                logThreadRunning = false;
            }
        };
        logThread.start();

        // shoot dem balls
        state = "shoot-position";

        Guinea_Timer spoolTimer = new Guinea_Timer();
        spoolTimer.setTarget(1500);
        // temp - may set from speedcalib
        // found via experimentation - don't say it's too high, it's not with lower batteries
        mtrShooter.setPower(0.50);

        resetDrive();
        while (Math.abs(encFL.getRotations()) < 0.83 && opModeIsActive()) {
            driveStraight(0.6, Dir.forward);
        }
        resetDrive();
        while (!spoolTimer.done() && opModeIsActive());
        state = "shoot";
        try {
            // swallow the InterruptedException so we can guarentee the pid thread dies
            //spool();
            shoot();
        } finally {
            shooter.stop();
            resetDrive();
        }

        state = "align-approach";
        while (Math.abs(encFL.getRotations()) < 0.83 && opModeIsActive()) {
            driveStraight(0.6, (alliance == ALLIANCE_RED) ? Dir.left : Dir.right);
        }
        resetDrive();

        // driveStraight diagonally until we come within 32 cm or we overrun to 7 units
        // on blue side, turn around after about 3 motor rotations
        // front is positive
        // back is negative
        if (alliance == ALLIANCE_RED) {
            state = "red-approach";
            //&& underTarget(encFR, 4.5) || overTarget(encBL, -4.5))
            while (snsRange.getDistance(DistanceUnit.CM) > 20 && opModeIsActive()) {
                driveStraight(0.7, Dir.frontleft);
            }
        } else {
            state = "blue-approach";
            /*while (overTarget(encFL, -4) || underTarget(encBR, 4) && opModeIsActive()) {
                driveStraight(0.7, Dir.frontright);
            }
            resetDrive();*/
            while (Math.abs(snsGyroRaw.getIntegratedZValue() - 172) > 4 && opModeIsActive()) {
                double correction = Range.clip(-(snsGyroRaw.getIntegratedZValue() - 172) * 0.005, -1, 1);
                setDrivePower(correction, correction, correction, correction);
            }
            resetDrive();
            while (snsRange.getDistance(DistanceUnit.CM) > 25 && opModeIsActive()) {
                driveStraight(0.7, Dir.backleft);
            }
        }

        // we don't need a realign on blue side because that's already done in the 180 degree turn
        if (alliance == ALLIANCE_RED) {

            resetDrive();
            state = "realign-red";
            double target = (alliance == ALLIANCE_RED) ? 0 : 172;
            while (Math.abs(snsGyroRaw.getIntegratedZValue() - target) > 2 && opModeIsActive()) {
                double correction = Range.clip(-(snsGyroRaw.getIntegratedZValue() - target) * 0.01, -1, 1);
                setDrivePower(correction, correction, correction, correction);
            }
            resetDrive();
            /*while (Math.abs(encFL.getRotations()) < 0.2) {
                driveStraight(0.3, Dir.forward);
            }
            resetDrive();*/
        }
        boolean firstBeacon = false;
        // we are in place; get the first beacon done
        state = "line-1";
        try {
            firstBeacon = doLine(alliance);
        } catch (InterruptedException e) {
            // opModeIsActive = false, let the end code kill our threads
        }

        // back away from the wall
        resetDrive();

        double DIST_FROM_WALL = 12;
        while (snsRange.getDistance(DistanceUnit.CM) < DIST_FROM_WALL && opModeIsActive()) {
            driveStraight(0.6, Dir.right);
        }

        resetDrive();

        state = "line-2-approach";
        // wall follow until we get close to the second beacon

        while (Math.abs(encFL.getRotations()) < (2.3 + ((firstBeacon) ? 0.3 : 0)) && opModeIsActive()) {
            driveStraight(0.75, (alliance == ALLIANCE_RED) ? Dir.forward : Dir.backward);
        }

        // realign ourselves for the second line
        resetDrive();
        state = "realign-2";
        double target = (alliance == ALLIANCE_RED) ? 0 : 172;
        while (Math.abs(snsGyroRaw.getIntegratedZValue() - target) > 3 && opModeIsActive()) {
            double correction = Range.clip(-(snsGyroRaw.getIntegratedZValue() - target) * 0.01, -1, 1);
            setDrivePower(correction, correction, correction, correction);
        }

        // nab the second beacon
        state = "line-2";
        firstBeacon = false;
        try {
            firstBeacon = doLine(alliance);
        } catch (InterruptedException e) {
            // see above for why we swallow the exception
        }


        // if the second robot switch is in the alt position, attempt to quickly rush back and park on the corner vortex
        state = "ramp-land";
        if (autoStartPosition.getState() == ALT_START_POS) {

            resetDrive();
            while (snsRange.getDistance(DistanceUnit.CM) < 18 && opModeIsActive()) {
                driveStraight(1, Dir.right);
            }
            resetDrive();
            while (Math.abs(encFL.getRotations()) < 5.1 + ((firstBeacon) ? 0 : 0.2) && opModeIsActive()) {
                driveStraight(1, (alliance == ALLIANCE_RED) ? Dir.backward : Dir.forward);
            }
            resetDrive();
            while (Math.abs(snsGyro.getHeading()) < 20 && opModeIsActive()) {
                setDrivePower(-1 * allianceCoeff, -1 * allianceCoeff, -1 * allianceCoeff, -1 * allianceCoeff);
            }
            resetDrive();
            while (opModeIsActive() && Math.abs(mtrFL.getCurrentPosition()) < 0.5) {
                driveStraight(1, (alliance == ALLIANCE_RED) ? Dir.backward : Dir.forward);
            }
        }
        // because we want telemetry still up for a while
        telemetry.setAutoClear(false);
        // tell the logThread to kys
        logThread.interrupt();
        while (logThreadRunning);

        while (opModeIsActive()) idle();
    }

    /**
     * beacon approach and attempt routine
     * @param alliance alliance color
     * @throws InterruptedException
     * @return whether the beacon nabbed was the first or second one
     */
    private boolean doLine(boolean alliance) throws InterruptedException {
        double allianceCoeff = (alliance == ALLIANCE_RED) ? 1 : -1;
        //snsGyro.reset();
        resetDrive();

        double lineSideCoeff = allianceCoeff; // 1 if follow left side, -1 if right side
        boolean changeSide = false;

        state = "line-find changeSide = false";
        // look for line
        while (snsColorLine.alpha() < 55 && opModeIsActive()) {
            driveStraight(0.10, (alliance == ALLIANCE_RED ^ changeSide) ? Dir.forward : Dir.backward);
            if (Math.abs(encFL.getRotations()) > 2.5) {
                // we probably overshot, try the other way
                changeSide = true;
                state = "line-find changeSide = true";
            }
        }
        //setDrivePower(0, 0, 0, 0);
        resetDrive();
        /*
        sleep(250);

        // position ourselves over the line
        resetDrive();

        state = "line-pos changeSide = " + Boolean.toString(changeSide);
        while (snsColorLine.alpha() < 50 && opModeIsActive()) {
            // failsafe if we wander too far off
            //if (Math.abs(snsGyro.getHeading()) > 20 && !changeSide) {
            if (Math.abs(encFL.getRotations()) > 0.3) {
                changeSide = true;
                lineSideCoeff *= -1;
                state = "line-pos changeSide = true";
            }
            //setDrivePower(0.1 * lineSideCoeff, 0.1 * lineSideCoeff, 0.1 * lineSideCoeff, 0.1 * lineSideCoeff);
            driveStraight(0.10, (lineSideCoeff > 0) ? Dir.backward : Dir.forward);
        }
        resetDrive();*/
        state = "line-follow";

        // proportionally follow the line in
        // aim for the left edge of the line on the red side
        // or for the right edge of the line on the blue side

        // experimental new linefollower
        while (snsRange.getDistance(DistanceUnit.CM) > 9 && opModeIsActive()) {

            //double y1 = (snsRange.getDistance(DistanceUnit.CM) - 8) * 0.03;
            double y1 = 0.15;
            double x1 = (55 - snsColorLine.alpha()) * lineSideCoeff * 0.0015;
            double x2 = 0; //(snsGyroRaw.getIntegratedZValue() - target) * 0.001;

            double frontLeft  =  x1 + y1 + x2;
            double frontRight = -x1 + y1 - x2;
            double backLeft   =  x1 - y1 - x2;
            double backRight  = -x1 - y1 + x2;
            setDrivePower(frontLeft, frontRight, backLeft, backRight);

            //driveStraight((snsRange.getDistance(DistanceUnit.CM) - 7) * 0.02 + 0.03, Dir.left);
        }
        resetDrive();
        /*
        while (Math.abs(snsGyroRaw.getIntegratedZValue() - target) > 3 && opModeIsActive()) {
            double correction = Range.clip(-(snsGyroRaw.getIntegratedZValue() - target) * 0.01, -1, 1);
            setDrivePower(correction, correction, correction, correction);
        }
        */

        /*
        resetDrive();
        while (snsRange.getDistance(DistanceUnit.CM) > 11 && opModeIsActive()) {
            driveStraight(0.2, Dir.left);
        }
        resetDrive();
        */

        //Guinea_Timer beacon_delay = new Guinea_Timer();
        //beacon_delay.setTarget(100);
        // look for the beacon color side closest to the start position
        // 0.15, 0.42

        // position ourselves in front of the beacon side closest to the robot starting position
        state = "position-first";
        while (Math.abs(encFL.getRotations()) < 0.13 && opModeIsActive()) {//!(red() || blue()) || !beacon_delay.done()) {
            driveStraight(0.20, (alliance == ALLIANCE_RED) ? Dir.backward : Dir.forward);
        }

        // check to see if this is the first side of the beacon checked
        boolean firstBeacon = true;
        if (((alliance == ALLIANCE_BLUE) && blue(snsColorBeaconLeft)) ||
                ((alliance == ALLIANCE_RED) && red(snsColorBeaconRight))) {
            // this beacon side looks like the wrong color, let's move to the other side
            sleep(100);
            state = "position-second";
            resetDrive();
            while (Math.abs(encFL.getRotations()) < 0.26 && opModeIsActive()) {
                driveStraight(0.20, (alliance == ALLIANCE_RED) ? Dir.forward : Dir.backward);
            }
            firstBeacon = false;
        }
        setDrivePower(0, 0, 0, 0);
        resetDrive();

        // try hitting the beacon itself
        thwack(alliance, firstBeacon);
        return firstBeacon;
    }
}
