package org.firstinspires.ftc.teamcode.command;

import org.firstinspires.ftc.teamcode.Robot;

/**
 * Created by guinea on 2/13/17.
 */

public abstract class Command {
    public abstract void init(Robot robot, CommandBasedOp opModeContext);
    public abstract void run();
    public abstract void stop();
    public abstract boolean isFinished();
}
