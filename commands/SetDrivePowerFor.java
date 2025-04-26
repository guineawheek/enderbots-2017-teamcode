package org.firstinspires.ftc.teamcode.commands;

import org.firstinspires.ftc.teamcode.util.Guinea_Timer;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.command.Command;
import org.firstinspires.ftc.teamcode.command.CommandBasedOp;

/**
 * Created by guinea on 2/13/17.
 */

public class SetDrivePowerFor extends Command {
    double fl, fr, bl, br;
    long ms;
    Guinea_Timer timer;
    Robot robot;
    public SetDrivePowerFor(double fl, double fr, double bl, double br, long ms) {
        this.fl = fl;
        this.fr = fr;
        this.bl = bl;
        this.br = br;
        this.ms = ms;
    }

    @Override
    public void init(Robot robot, CommandBasedOp opModeContext) {
        robot.setDrivePower(fl, fr, bl, br);
        timer.setTarget(ms);
        this.robot = robot;
    }

    @Override
    public void run() {

    }

    @Override
    public void stop() {
        robot.setDrivePower(0, 0, 0, 0);
    }

    @Override
    public boolean isFinished() {
        return timer.done();
    }
}
