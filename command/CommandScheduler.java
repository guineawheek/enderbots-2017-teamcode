package org.firstinspires.ftc.teamcode.command;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by guinea on 2/13/17.
 */

public class CommandScheduler {

    private LinkedList<Command> seqCommands;
    private LinkedList<Command> backCommands;
    private LinkedList<Command> runningBackCommands;
    private Command currentCommand;
    private CommandBasedOp context;

    public CommandScheduler(CommandBasedOp context) {
        this.context = context;
        clear();
    }

    public void addSequential(Command command) {
        seqCommands.add(command);
    }
    public void addSequential(Command... commands) {
        for (Command command : commands) addSequential(command);
    }

    public void addBackground(Command command) {
        backCommands.add(command);
    }
    public void addBackground(Command... commands) {
        for (Command command : commands) addBackground(command);
    }

    public void clear() {
        seqCommands = new LinkedList<>();
        backCommands = new LinkedList<>();
        runningBackCommands = new LinkedList<>();
        currentCommand = new NullCommand();
    }


    public void run() {
        if (currentCommand.isFinished() && !seqCommands.isEmpty()) {
            currentCommand.stop();
            currentCommand = seqCommands.removeFirst();
            currentCommand.init(context.robot, context);
        } else if (seqCommands.isEmpty()) {
            // at least give it something to wait on
            currentCommand = new NullCommand();
        } else {
            currentCommand.run();
        }

        // make sure the init of background tasks runs at least once
        for (Iterator<Command> iterator = backCommands.iterator(); iterator.hasNext();) {
            Command command = iterator.next();
            if (!runningBackCommands.contains(command)) {
                command.init(context.robot, context);
                runningBackCommands.add(command);
                iterator.remove();
            }
        }

        for (Iterator<Command> iterator = runningBackCommands.iterator(); iterator.hasNext();) {
            Command command = iterator.next();
            if (command.isFinished()) {
                command.stop();
                iterator.remove();
            } else {
                command.run();
            }
        }


    }

    public void stop() {
        currentCommand.stop();
        currentCommand = new NullCommand();
        for (Command command : runningBackCommands) {
            command.stop();
        }
        clear();
    }
}
