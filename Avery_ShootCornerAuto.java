package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.util.Dir;

/**
 * This autonomous was created for the purpose of blocking an opposing alliance from getting beacons in autonomous.
 * A full use case and field map of this program can be found in the Software requirements specification of the notebook.
 * Created by Avery on 2/20/17.
 * Code written using costom classes written by head autonomous coder Guinea
 * Info on the classes and methods called are found in AutonomousOpMode.
 */
@Autonomous(name="Spicy Shoot and corner autonomous BOI", group="K9Bot")
public class Avery_ShootCornerAuto extends  BeaconAutoOpMode{   //Init contained in the BeaconOpMode method
    public void runAutonomous() throws InterruptedException {
                                                                    
       if(alliance == ALLIANCE_BLUE){
           mtrShooter.setPower(0.2);
           driveStraightDistance(0.7, Dir.frontright, 2.5);
           turnAbsolute(-35, 0.01, 2);
           shoot();
           sleep(1000);
           turnAbsolute(0, 0.01, 2);
           driveStraightDistance(0.7, Dir.right, 2.5);
           turnAbsolute(-35, 0.01, 2);
           driveStraightDistance(0.5, Dir.right, 1);
       }
        else{
           mtrShooter.setPower(0.2);
           driveStraightDistance(0.7, Dir.frontleft, 2.5);
           turnAbsolute(35, 0.01, 2);
           shoot();
           sleep(1000);
           turnAbsolute(0, 0.01, 2);
           driveStraightDistance(0.7, Dir.left, 2.5);
           turnAbsolute(35, 0.01, 2);
           driveStraightDistance(0.5, Dir.left, 1);

       }

    }
}
