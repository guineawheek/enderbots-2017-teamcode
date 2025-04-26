package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cColorSensor;

import org.firstinspires.ftc.teamcode.util.Guinea_Timer;
import org.firstinspires.ftc.teamcode.util.Dir;

/**
 * A class that extends {@link AutonomousOpMode} with functions relevant to beacon autonomouses, as
 * not to clutter the original base class.
 */

public abstract class BeaconAutoOpMode extends AutonomousOpMode {

    /**
     * The beacon approach and attempt routine, except it assumes that gyro control should be used
     * to approach the line.
     *
     * @param facingForward whether the robot is driving forward or backward to approach the line
     * @return whether or not the side of the  beacon closest to the point which the function began
     *         while approaching the line was pressed. Returns false if the farther side was pressed.
     * @throws InterruptedException if the opmode is stopped while the function is sleeping.
     */
    protected boolean thwackBeacon(boolean facingForward) throws InterruptedException {
        return thwackBeacon(facingForward, true);
    }

    /**
     * Drives the robot at a slow pace either forwards or backwards until it finds a line.
     * @param facingForward whether the robot is driving forward to approach the line or backward
     * @param driveStraightApproach sets whether the robot should use gyro control to approach the line
     * @param overShootThreshold the maximum number of motor rotations the robot should go before it's
     *                           determined that it has probably gone too far and should try looking
     *                           in the other direction
     */
    protected void driveToLine(boolean facingForward, boolean driveStraightApproach, double overShootThreshold) {
        boolean changeSide = false;

        state = "line-find";
        // look for line
        resetDrive();
        while (snsColorLine.alpha() < 55 && opModeIsActive()) {
            if (driveStraightApproach) {
                driveStraight(0.10, (facingForward ^ changeSide) ? Dir.forward : Dir.backward);
            } else {
                double power = (facingForward ^ changeSide) ? 0.1 : -0.1;
                setDrivePower(-power, power, -power, power);
            }
            if (encDistanceGt(overShootThreshold)) {
                // we probably overshot, try the other way
                changeSide = true;
                state = "line-find changeSide";
            }
        }
    }
    /**
     * The beacon approach and attempt routine.
     *
     * <p>Assumes that the robot is more or less perpendicular to a line in front of a beacon, and is
     * just behind the line.</p>
     * @param facingForward whether the robot in its approach towards the line is facing forward
     * @param driveStraightApproach whether the robot should use gyro control to approach the line.
     * @throws InterruptedException if the {@link com.qualcomm.robotcore.eventloop.opmode.LinearOpMode} gets stopped during operation
     * @return whether the beacon side nabbed was the first or second side. Which side that is is relative to the approach orientation of the bot.
     */
    protected boolean thwackBeacon(boolean facingForward, boolean driveStraightApproach) throws InterruptedException {

        double lineSideCoeff = facingForward ? 1 : -1; // 1 if follow right side, -1 if left side

        driveToLine(facingForward, driveStraightApproach, 2.5);

        // proportionally follow the line in
        // aim for the left edge of the line on the red side
        // or for the right edge of the line on the blue side

        // experimental new linefollower
        // this loop is unrolled unless there comes reason for line following outside of beacons

        resetDrive();
        //resetEncoders();
        state = "line-follow";
        while (rangeDistanceGt(10) && opModeIsActive()) {
            omniDrive((snsColorLine.alpha() - 55) * lineSideCoeff * 0.0012, -0.15, 0);
        }

        if (rangeDistanceLt(7)) {
            omniDrive(0, 0.1, 0);
            sleep(50);
            resetDrive();
        }
        // look for the beacon color side closest to the start position
        // 0.15, 0.42
        snsGyroRaw.resetZAxisIntegrator();
        // position ourselves in front of the beacon side closest to the robot starting position
        state = "position-first";
        driveStraightDistance(0.2, facingForward ? Dir.backward : Dir.forward, 0.15);

        ModernRoboticsI2cColorSensor relevantColorSensor = facingForward ? snsColorBeaconRight : snsColorBeaconLeft;
        // check to see if this is the first side of the beacon checked
        boolean firstBeacon = true;
        if ((isBlueAlliance() && blue(relevantColorSensor)) ||
                (isRedAlliance() && red(relevantColorSensor))) {
            // this beacon side looks like the wrong color, let's move to the other side
            sleep(100);
            //snsGyroRaw.resetZAxisIntegrator();
            state = "position-second";
            driveStraightDistance(0.2, facingForward ? Dir.forward : Dir.backward, 0.30);
            firstBeacon = false;

            // set the relevant color sensor to the other side, now that we've moved
            relevantColorSensor = facingForward ? snsColorBeaconLeft : snsColorBeaconRight;
        }

        // fix for autonomous exiting early
        if (!opModeIsActive()) return true;

        // try hitting the beacon itself
        // if we fail, we try twice before giving up and trying the next one
        /*
            So here's the idea
            Find the wrong colored side
            Thwack it until it turns the color we want
            ?
            Profit
         */

        state = "thwack-1";

        // what? the beacon is ALREADY our color?
        /*
            > "My job here is done."
            > "But you didn't do anything!"
            > [capes away]
         */
        if (beaconCorrect(isRedAlliance(), relevantColorSensor)) return firstBeacon;

        togglebeacon();
        sleep(300);

        // did we get it on the first try?
        if (beaconCorrect(isRedAlliance(), relevantColorSensor)) return firstBeacon;
        // guess not. whelp, let's try once more

        state = "thwack-2";

        // move a biit closer
        if (rangeDistanceGt(8)) {
            driveStraightRangeGt(0.3, Dir.left, 8);
        } else {
            Guinea_Timer timer = new Guinea_Timer();
            timer.setTarget(250);
            while (!timer.done()) {
                setDrivePower(0.3, 0.3, -0.3, -0.3);
            }
            setDrivePower(0, 0, 0, 0);
        }

        // "Hey, it's the discount double-check"
        //  - Phineas
        togglebeacon();

        return firstBeacon;
    }

    // Could the following three functions have been inline? Yes.
    // Would it be worth the reduced readability? No way Jose
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
     * Moves the beacon hitting appendage out and in
     * @throws InterruptedException if the opmode is stopped before completion
     */
    private void togglebeacon() throws InterruptedException {
        svoThwack.setPosition(0.25);
        sleep(600);
        svoThwack.setPosition(0.85);
    }

    /**
     * Verifies the side of the beacon the robot is looking at is indeed the correct color, given the
     * color sensor to check against
     * @param isRed whether the robot is on the red alliance
     * @param relevantColorSensor the color sensor currently being used to determine correctness
     * @return the beacon seems to be of the correct color
     */
    private boolean beaconCorrect(boolean isRed, ModernRoboticsI2cColorSensor relevantColorSensor) {
        return (isRed && red(relevantColorSensor)) || (!isRed && blue(relevantColorSensor));
    }


}
