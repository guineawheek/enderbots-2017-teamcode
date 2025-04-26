package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.util.Dir;

/**
 * Created by guinea on 2/13/17.
 *
 * A reimplementation of the original beacon autonomous in the form of AutonomousOpMode.
 */

@Autonomous(name="Regular beacon autonomous (experimental)")
public class autoBeaconNormal extends BeaconAutoOpMode {
    @Override
    public void runAutonomous() throws InterruptedException {
        // drive forward and shoot
        // we drive forward to increase accuracy
        // in hindsight readjusting shooter angle was a mistake
        state = "shoot-position";
        spool();
        driveStraightDistance(0.6, Dir.forward, 0.83);
        spoolWait();
        state = "shoot";
        shoot();

        // move ourselves closer to the beacons themselves.
        state = "align-approach";
        driveStraightDistance(0.6, isRedAlliance() ? Dir.left : Dir.right, 0.83);

        // for red, approach the wall, then use the gyro to re-center ourselves.
        // for blue, do the 180 degree turn, then approach the wall.
        if (isRedAlliance()) {
            state = "red-approach";
            driveStraightRangeGt(0.7, Dir.frontleft, 20);
            state = "realign-red";
            turnAbsolute(0, 0.01, 2);

        } else {
            state = "blue-approach";
            turnAbsolute(172, 0.005, 4);
            driveStraightRangeGt(0.7, Dir.backleft, 25);
        }

        // run the beacon processing routine. We face forward on the red side.
        state = "line-1";
        boolean firstBeacon = thwackBeacon(isRedAlliance());

        // drive away from the wall using the range sensor
        driveStraightRangeLt(0.6, Dir.right, 12);

        // drive forward until we are near the second beacon line
        state = "line-2-approach";
        driveStraightDistance(0.75, isRedAlliance() ? Dir.forward : Dir.backward, 2.3 + (firstBeacon ? 0.3 : 0));

        // on red, realign ourselves.
        state = "realign-2";
        turnAbsolute(isRedAlliance() ? 0 : 172, 0.01, 3);

        // process the second beacon.
        state = "line-2";
        firstBeacon = thwackBeacon(isRedAlliance());

        state = "ramp-land";
        if (autoMode.getState() == ALT_MODE) {
            // land on the corner vortex
            // it's bad for the drivetrain
            /*
            driveStraightRangeLt(1, Dir.right, 18);
            driveStraightDistance(1, isRedAlliance() ? Dir.backward : Dir.forward, 5.1 + (firstBeacon ? 0 : 0.2));
            //turnRelative(isRedAlliance() ? -20 : 20, 1, 5);
            driveStraightDistance(1, isRedAlliance() ? Dir.backleft : Dir.frontleft, 0.5);
            */
        } else {
            // land on the center vortex
            // TODO: go for cap ball/center platform
            driveStraightDistance(1, isRedAlliance() ? Dir.backright : Dir.frontright, 0.95);
            driveStraightDistance(1, isRedAlliance() ? Dir.right : Dir.left, 0.67);
            driveStraightDistance(1, isRedAlliance() ? Dir.backward : Dir.forward, 0.4);
        }

    }
}
