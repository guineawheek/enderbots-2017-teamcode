package org.firstinspires.ftc.teamcode.commands;

import org.firstinspires.ftc.teamcode.util.Guinea_Timer;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.command.Command;
import org.firstinspires.ftc.teamcode.command.CommandBasedOp;

/**
 * Created by guinea on 2/13/17.
 */

public class Sleep extends Command {
    Guinea_Timer timer;
    long ms;

    public Sleep(long ms) {
        this.ms = ms;
    }

    @Override
    public void init(Robot robot, CommandBasedOp opModeContext) {
         timer.setTarget(ms);
    }

    @Override
    public void run() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isFinished() {
        return timer.done();
    }
}
