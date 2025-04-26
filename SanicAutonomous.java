package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsAnalogOpticalDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.util.CircleBufferAvg;
import org.firstinspires.ftc.teamcode.util.Guinea_Timer;
import org.firstinspires.ftc.teamcode.util.Dir;
import org.firstinspires.ftc.teamcode.util.IniConfig;
import org.firstinspires.ftc.teamcode.util.SpeedController;

/**
 * A new autonomous that aims to rush to the far beacon to prevent interference and avoid defense.
 * <ol>
 *
 *     <li>rush the beacon farthest from the robot's starting position</li>
 *     <li>immidiately rush the beacon closest to the corner vortex</li>
 *     <li>back away from the beacon, turn, and shoot</li>
 *     <li>land on the center vortex, also pushing off the cap ball</li>
 * </ol>
 *
 *
 * all for 100 points
 * <pre>
 *                   ▄▄▄▄▄
 *          ▀▀▀██████▄▄▄       _______________
 *        ▄▄▄▄▄  █████████▄  /                 \
 *       ▀▀▀▀█████▌ ▀▐▄ ▀▐█ |   Gotta go fast!  |
 *     ▀▀█████▄▄ ▀██████▄██ | _________________/
 *     ▀▄▄▄▄▄  ▀▀█▄▀█════█▀ |/
 *          ▀▀▀▄  ▀▀███ ▀       ▄▄
 *       ▄███▀▀██▄████████▄ ▄▀▀▀▀▀▀█▌
 *     ██▀▄▄▄██▀▄███▀ ▀▀████      ▄██
 *  ▄▀▀▀▄██▄▀▀▌████▒▒▒▒▒▒███     ▌▄▄▀
 *  ▌    ▐▀████▐███▒▒▒▒▒▐██▌
 *  ▀▄▄▄▄▀   ▀▀████▒▒▒▒▄██▀
 *            ▀▀█████████▀
 *          ▄▄██▀██████▀█
 *        ▄██▀     ▀▀▀  █
 *       ▄█             ▐▌
 *   ▄▄▄▄█▌              ▀█▄▄▄▄▀▀▄
 *  ▌     ▐                ▀▀▄▄▄▀
 *   ▀▀▄▄▀
 *
 * </pre>
 */

@Autonomous(name="Sanic Autonomous")
public class SanicAutonomous extends BeaconAutoOpMode {
    IniConfig.ConfigSection config;
    Avery_ColorSensor16Bit_V1 snsColorRevolver;

    private void knucklesCollect() {
       if (!andKnuckles()) return;
        mtrCollector.setPower(-0.7);
        boolean collected = false;
        while (opModeIsActive() && !collected) {
            int[] values = snsColorRevolver.Avery_ReadColor16();
            int threshold = config.getInteger("ballCollected");
            collected = (values[0] > threshold || values[2] > threshold);
        }
        mtrCollector.setPower(0);
        snsColorRevolver.close();

        state = "wall-approach";
        driveStraightDistance(0.7, redblue(Dir.backleft, Dir.backright), 2.0);
        turnAbsolute(redblue(90, 90), 0.0075, 3);
        snsGyroRaw.resetZAxisIntegrator();
        snsGyro.reset();
    }

    protected double rbconf(String name, double fallbackRed, double fallbackBlue) {
        return config.getNumber(name + redblue(".red", ".blue"), redblue(fallbackRed, fallbackBlue));
    }
    protected double getConf(String name, double fallback) {
        return config.getNumber(name, fallback);
    }

    /**
     * mostly there so that a knuckles autonomous can override this and return true
     * @return whether this autonomous has its third ball system enabled
     */
    protected boolean andKnuckles() {
        return false;
    }

    @Override
    public void initHook() {
        // override the max pid speed settings with a much faster counts per second cap
        // this allows the drivebase to go faster
        mtrFL.setMaxSpeed(2900);
        mtrFR.setMaxSpeed(2900);
        mtrBL.setMaxSpeed(2900);
        mtrBR.setMaxSpeed(2900);
        snsGyroRaw.resetZAxisIntegrator();
        // set the shooter into velocity control mode
        /*mtrShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        mtrShooter.setMaxSpeed(3200);*/

        // set the robot to try and maintain its heading it had at its starting position when driving unless told otherwise.
        setGyroResetWithDrive(false);

        config = robotConfig.getSection("SanicAutonomous");
        config.setPermissive(true);
        snsColorRevolver = new Avery_ColorSensor16Bit_V1(hardwareMap, "snsColorRevolver", 0x38);
    }

    @Override
    public void runAutonomous() throws InterruptedException {
        // run the third ball collection routine if enabled
        knucklesCollect();
        state = "wall-approach";

        /* due to differences in how the initial starting position is aligned, the precise
         * encoder and range sensor values for the far beacon approach are different on the red and blue
         * sides.
         * the function redblue(red_value, blue_value) allows for having one function except the values are different
         * the first argument is the value used on the red side, while the second argument is the blue side value.
         *
         * This allows the logic of the program to be consistent on both sides.
         */

        /* in general, all movement functions use the gyroscope to ensure the robot maintains a straight heading.
         * The first argument of this function specifies the motor power. We set it close to 1 for maximum speed
         * but not 1 because proportional control still needs some heading to compensate.
         *
         * The second argument specifies the robot's driving direction. Note how it's different on red and blue sides
         * as the robot's collector side faces towards the wall on blue side.
         *
         * The third argument specifies the range sensor reading threshold in cm at which  the robot
         * should stop driving in this direction.
         */
        driveStraightRangeGt(0.95, redblue(Dir.frontleft, Dir.backleft), rbconf("wallApproach", 25, 25));

        state = "far-line-approach"; // approach the far beacon line

        /*
         * This function is similar to the previous function , with the first two arguments being pretty much the same.
         *
         * However, this function is different as it only makes sense running in the forward and backward directions as
         * it along with trying to drive straight also tries to drive parallel with the wall the range sensor is facing.
         *
         * The third argument here specifices the maximum number of motor rotations before the robot stops.
         * The "WithGuard" portion of the function name also means that the robot will also stop if the line-following color
         * sensor reads a line to prevent overruning the line.
         *
         * Finally the last argument specifices how far the robot should try to follow the wall from, in centimeters.
         */
        // 3.9?
        wallFollowWithGuard(0.90, redblue(Dir.forward, Dir.backward), rbconf("farApproach.dist", 3.30, 3.30), rbconf("farApproach.wall", 22, 18));

        // giving the gyro a short rest helps it work properly
        sleep(100);

        // grab the first beacon - this approaches the beacon line slowly, line follows it in until
        // the range sensor says the robot is close enough, then handles beacon pressing.

        // the boolean argument specifies if the robot is facing forward or backward when approaching the beacon line.
        boolean farBeacon = thwackBeacon(isRedAlliance());
        double beaconComp = (farBeacon) ? 0 : 0.3;

        state = "wall-escape"; // move the robot away from the wall so we don't slam into the wall or the near beacon
        // in this case the 3rd argument is just how many rotations the robot should move
        driveStraightDistance(0.9, redblue(Dir.backright, Dir.frontright), redblue(0.2, 0.4));

        // approach the near beacon, in the opposition direction we approached the far beacon, compensating for which side
        // of the beacon we happened to press
        state = "near-line-approach";
        wallFollowWithGuard(0.9, redblue(Dir.backward, Dir.forward), rbconf("nearApproach.dist", 2.4, 2.1) + beaconComp, rbconf("nearApproach.wall", 14, 18));
        //driveStraightDistance(0.9, redblue(Dir.backleft, Dir.frontleft), 0.4);
        sleep(100);

        // grab the near beacon, and calculate if the robot needs to move forward a bit to align itself with the center vortex
        farBeacon = thwackBeacon(isBlueAlliance());
        beaconComp = (farBeacon ^ isBlueAlliance()) ? 0 : getConf("shootPos.comp", 0.30);

        // drive away from the wall
        state = "shoot-approach";
        driveStraightDistance(0.6, Dir.right, getConf("shootPos.wall", 0.7));
        // realign ourselves to be more centered on the vortex, if we need to.
        driveStraightDistance(0.3, Dir.forward, beaconComp);

        if (!opModeIsActive()) return;
        // set the shooter motor to spool up it's proportionally-controlled shooting system
        /*shooter.enablePid();
        shooter.setPower(SpeedController.AUTONOMOUS_POWER);*/
        mtrShooter.setPower(getConf("shooter.power", 0.23));

        /* turn the robot towards the center vortex.
         * the first argument is the degrees, relative to the robot's starting heading.
         * Unlike most systems, positive is counter-clockwise.
         *
         * The second argument is the proportional gain to be used, as it is usually higher for smaller turns
         * but smaller for larger turns.
         *
         * The third argument is the tolerance plus or minus the target heading in degrees at which
         * the robot will say its alignment is just fine.
         */

        state = "shoot-turn";
        turnAbsolute(getConf("turn.degrees", -87), getConf("turn.gain", 0.008), 2);
        state = "shoot";

        // call our new turned heading zero degrees, so the center platform landing can work
        snsGyroRaw.resetZAxisIntegrator();

        // run the shooting routine of two balls
        spoolWait();
        shoot(andKnuckles());

        // finally, drive the robot in its last seconds to land on the center platform and knock off the cap ball.
        if (autoMode.getState() == NORMAL_MODE) {
            driveStraightDistance(0.9, redblue(Dir.frontleft, Dir.frontright), 2.3);
            driveStraightDistance(0.9, Dir.forward, 1.3);
            //driveStraightDistance(0.9, redblue(Dir.right, Dir.left), 1);
        } else {
            driveStraightDistance(0.9, redblue(Dir.frontright, Dir.frontleft), 1.8);
            turnAbsolute(redblue(-45, 45), 0.01, 3);
            snsGyroRaw.resetZAxisIntegrator();
            driveStraightDistance(0.8, redblue(Dir.right, Dir.left), 1.4);
            // TODO: add corner ramp code
        }
    }
}
