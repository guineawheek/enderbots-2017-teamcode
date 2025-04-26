package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.util.Guinea_Timer;

import static org.firstinspires.ftc.teamcode.Guinea_hwInit.mtrShooter;
import static org.firstinspires.ftc.teamcode.Guinea_hwInit.shooter;

/**
 * Created by guinea on 2/1/17.
 */
@Disabled
@TeleOp(name="Speed Calib Op (don't use)", group="test")
public class SpeedCalibrationOp extends OpMode {
    static final int STATE_WARMUP = 0;
    static final int STATE_MEASURE = 1;
    static final int STATE_COMPUTE = 2;
    static final int STATE_DONE = 3;

    int state;

    boolean firstflag = true;
    static double[] speeds = {0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65};
    static double[] velocities = new double[speeds.length];
    static int[] targets = {1800, 1900, 2100};
    public static double lowPower = 0.55;
    public static double medPower = 0.60;
    public static double highPower = 0.65;
    public static double superHighPower = 1.0;
    int index;

    Guinea_Timer elapsedTime;
    @Override
    public void init() {
        Guinea_hwInit.init(hardwareMap);
        telemetry.setAutoClear(false);
        elapsedTime = new Guinea_Timer();
        state = 0;
        index = 0;
    }

    @Override
    public void loop() {
        if (state == STATE_WARMUP) {
            if (firstflag) {
                telemetry.addLine("Warming up...");
                telemetry.update();
                mtrShooter.setPower(speeds[index]);
                elapsedTime.setTarget(2000);
                firstflag = false;
            } else if (elapsedTime.done()) {
                state++;
                firstflag = true;
            }
        }
        else if (state == STATE_MEASURE) {
            if (firstflag && index < speeds.length) {
                mtrShooter.setPower(speeds[index]);
                elapsedTime.setTarget(4000);
                firstflag = false;
            } else if (elapsedTime.done() && index < speeds.length) {
                velocities[index] = shooter.getAvgVelocity();
                firstflag = true;
                telemetry.addData("power " + Double.toString(speeds[index]) + ":", velocities[index]);
                telemetry.update();
                index++;
            } else if (index >= speeds.length) {
                mtrShooter.setPower(0);
                state++;
            }
        }
        else if (state == STATE_COMPUTE) {
            lowPower = speeds[getMinIndex(targets[0])];
            medPower = speeds[getMinIndex(targets[1])];
            highPower = speeds[getMinIndex(targets[2])];
            telemetry.addData("lowPower: ", lowPower);
            telemetry.addData("medPower: ", medPower);
            telemetry.addData("highPower: ", highPower);
            telemetry.update();
            state++;
        }
    }

    private static int getMinIndex(double target) {
        int minIndex = 0;
        double minDiff = Integer.MAX_VALUE;
        for (int i = 0; i < velocities.length; i++) {
            if (Math.abs(target - velocities[i]) < minDiff) {
                minIndex = i;
                minDiff = Math.abs(target - velocities[i]);
            }
        }

        return minIndex;
    }
}
