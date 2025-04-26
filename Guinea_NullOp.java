package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

/**
 * Does absolutely nothing lol
 * Intended to be used if running other autonomouses would prove to be a disadvantage.
 * Created by guinea on 11/2/16.
 */
@Autonomous(name="Null Op", group="K9Bot")
public class Guinea_NullOp extends OpMode {
    public void init() {

    }
    public void loop() {
        telemetry.addData("Chance of success", "99.9989523%");
    }
}
