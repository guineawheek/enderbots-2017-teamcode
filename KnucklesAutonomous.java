package org.firstinspires.ftc.teamcode;

import android.os.Environment;

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

import java.io.File;

/**
 * TODO: make into functioning autonomous
 * An autonomous that helps bring weaker alliance partners to victory.
 * <ol>
 *
 * </ol>
 *
 *
 * all for 115 points
 */

@Autonomous(name="Knuckles Autonomous")
public class KnucklesAutonomous extends SanicAutonomous {
    @Override
    protected boolean andKnuckles() {
        // the base class has all the logic handled already, nothing to do here.
        return true;
    }

}
