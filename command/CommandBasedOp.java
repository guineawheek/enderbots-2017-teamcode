package org.firstinspires.ftc.teamcode.command;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.Robot;

/**
 * Created by guinea on 2/13/17.
 */

public abstract class CommandBasedOp extends OpMode {

    CommandScheduler initCommandScheduler;
    CommandScheduler loopCommandScheduler;
    public Robot robot;

    /**
     * Add commands for initialization
     * @param scheduler
     */
    public abstract void setupInitCommands(CommandScheduler scheduler);
    public abstract void setupLoopCommands(CommandScheduler scheduler);

    @Override
    public void init() {
        robot = new Robot(hardwareMap);
        initCommandScheduler = new CommandScheduler(this);
        setupInitCommands(initCommandScheduler);
        loopCommandScheduler = new CommandScheduler(this);
        setupLoopCommands(loopCommandScheduler);
    }
    @Override
    public void init_loop() {
        initCommandScheduler.run();
    }

    @Override
    public void start() {
        initCommandScheduler.stop();
    }

    @Override
    public void loop() {
        loopCommandScheduler.run();
    }

    @Override
    public void stop() {
        loopCommandScheduler.stop();
        initCommandScheduler.stop();
    }

}
