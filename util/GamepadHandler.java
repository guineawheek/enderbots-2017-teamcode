package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.hardware.Gamepad;

/**
 * Created by guinea on 2/9/17.
 * @deprecated no use for it?
 */

public class GamepadHandler {
    private Gamepad gamepad;
    private boolean dpad_up_edge;
    private boolean dpad_down_edge;
    private boolean dpad_right_edge;
    private boolean dpad_left_edge;
    private boolean a_edge;
    private boolean b_up_edge;
    private boolean x_edge;
    private boolean y_edge;
    private boolean guide_edge;
    private boolean start_edge;
    private boolean back_edge;
    private boolean right_bumper_edge;
    private boolean left_bumper_edge;
    private boolean right_stick_bumper_edge;
    private boolean left_stick_bumper_edge;

    public GamepadHandler(Gamepad gamepad) {
        this.gamepad = gamepad;
    }

    public void update() {
        /*
        if button true and edge false then buttonDown
        if button true and edge true
        if button true and edge true then nothing
         */
    }

}
