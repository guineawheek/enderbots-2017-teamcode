package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.util.Dir;

import static org.firstinspires.ftc.teamcode.Guinea_hwInit.*;


/**
 * Created by Guinea on 11/20/16.
 * This autonomous is intended to be used in conjunction with capable alliance partners who can
 * handle pressing beacons reliably.
 *
 * It waits five seconds at the beginning of the auto period, to avoid interfering with alliance
 * partners, then proceeds to drive 5.4 motor rotations at full speed using full sensor control to
 * land on the center platform.
 */
@Disabled
@Autonomous(name="Shooting auto ball park", group="K9Bot")
public class Guinea_autoBallParkStand extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        Guinea_hwInit.init(hardwareMap);
        waitForStart();
        sleep(5000);
        //shoot(0.6);
        //Guinea_hwInit.moveTo(-6.8, 6.8, -6.8, 6.8, 1);
        moveTo(Dir.forward, 5.4, 1);
        stop();
    }
}
