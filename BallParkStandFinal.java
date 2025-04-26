package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.util.Dir;

/**This autonomous was created for the purpose of shooting two balls in the center vortex, knocking the cap ball
 * off of the center platform, and parking on the center structure.
 * A full use case and field map of this program can be found in the Software requirements specification
 * of the engineering notebook.
 * Created by Avery on 2/20/17.
 * Code written using costom classes written by head autonomous coder Guinea
 * Info on the classes and methods called are found in AutonomousOpMode.
 */
@Autonomous(name="Spicy Ball Park and Yoga ball autonomous", group="K9Bot")
public class BallParkStandFinal extends  BeaconAutoOpMode{
    public void runAutonomous() throws InterruptedException {

       if(alliance == ALLIANCE_BLUE){
           mtrShooter.setPower(0.7);
           driveStraightDistance(0.7, Dir.frontright, 2.5);
           turnAbsolute(-35, 0.01, 2);
           shoot();
           sleep(1000);
           turnAbsolute(0, 0.01, 2);
           driveStraightDistance(1, Dir.frontright, 3.3);
           mtrFR.setPower(1);
           mtrBR.setPower(1);
           sleep(200);
           mtrFR.setPower(0);
           mtrBR.setPower(0);
           sleep(200);
           mtrFR.setPower(-1);
           mtrBR.setPower(-1);
           sleep(200);
           mtrFR.setPower(0);
           mtrBR.setPower(0);
       }
        else{
           mtrShooter.setPower(0.7);
           driveStraightDistance(0.7, Dir.frontleft, 2.5);
           turnAbsolute(35, 0.01, 2);
           shoot();
           sleep(1000);
           turnAbsolute(0, 0.01, 2);
           driveStraightDistance(1, Dir.frontleft, 3.3);
           mtrFL.setPower(1);
           mtrBL.setPower(1);
           sleep(200);
           mtrFL.setPower(0);
           mtrBL.setPower(0);
           sleep(200);
           mtrFL.setPower(-1);
           mtrBL.setPower(-1);
           sleep(200);
           mtrFL.setPower(0);
           mtrBL.setPower(0);

       }

    }
}
