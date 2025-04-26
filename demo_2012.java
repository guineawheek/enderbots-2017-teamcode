package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.LegacyModule;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.Range;

/**
 * Created by Avery on 1/26/17.
 */
@Disabled
@TeleOp(name="DemoButt teleop", group="K9bot")

public class demo_2012 extends OpMode {

    DcMotor mtrL1;
    DcMotor mtrL2;
    DcMotor mtrR1;
    DcMotor mtrR2;
    DcMotor mtrRot;
    DcMotor mtrExt;

    Servo svoGrabber;

    @Override
    public void init() {

        mtrRot = hardwareMap.dcMotor.get("mtrRot");
        mtrExt = hardwareMap.dcMotor.get("mtrExt");
        mtrL1 = hardwareMap.dcMotor.get("mtrL1");
        mtrL2 = hardwareMap.dcMotor.get("mtrL2");
        mtrR1 = hardwareMap.dcMotor.get("mtrR1");
        mtrR2 = hardwareMap.dcMotor.get("mtrR2");

        svoGrabber = hardwareMap.servo.get("svoGrabber");

        svoGrabber.setPosition(0);
        /*
         * According to all known laws of aviation, there is no way a bee should be able to fly.
         * Its wings are too small to get its fat little body off the ground. The bee, of course,
         * files anyway because bees don't care what humans think is possible.
         *
         * Yello, black. Yellow, black. Yellow, black. Yello, black.
         *
         * Oh, black and yello!
         * Let's shake it up a little.
         *
         * Barry! Breakfast is ready! Ooming! Hang on a second. Hello?
         *
         * - Barry?
         * - Adam?
         * - Can you believe this is ahppening?
         * - I can't I'll pick you up.
         *
         * Looking sharp.
         *
         * User the stairs. Your father paied good money for those.
         *
         * Sorry. I'm excited.
         *
         * Here's the graduate. We're very proud of you son.
         *
         * A perfect report card, all B's.
         *
         * Very proud.
         *
         * Ma! I got a thing going here.
         *
         *
         */
    }

    @Override
    public void loop() {

        double mult = (gamepad1.left_bumper) ? 0.5 : 1;
        double left = Range.clip(-gamepad1.left_stick_y, -1, 1) * mult;
        double right = Range.clip(gamepad1.right_stick_y, -1, 1) * mult;

        /*for(int i = 0; i < 2; i++) {
            motors[i].setPower(left);
            motors[i + 2].setPower(right);
        }*/
        mtrL1.setPower(left);
        mtrL2.setPower(left);
        mtrR1.setPower(right);
        mtrR2.setPower(right);

        if (gamepad2.a) {
            svoGrabber.setPosition(1);
        }
        else if (gamepad2.b) {
            svoGrabber.setPosition(0);
        }
        else {
            svoGrabber.setPosition(svoGrabber.getPosition());
            }
        if (gamepad2.left_stick_y > 0.5){
            mtrRot.setPower(0.25);
        } else if (gamepad2.left_stick_y < -0.5){
            mtrRot.setPower(-0.25);
        }
        else {
            mtrRot.setPower(0);
        }
        if (gamepad2.right_stick_y > 0.5){
            mtrExt.setPower(0.25);
        } else if (gamepad2.right_stick_y < -0.5){
            mtrExt.setPower(-0.25);
        } else{
            mtrExt.setPower(0);
        }




    }
}
