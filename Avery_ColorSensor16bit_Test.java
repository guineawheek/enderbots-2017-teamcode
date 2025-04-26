package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.teamcode.util.Guinea_Timer;

/**
 * This is a class I created to test my colorsensor class and make sure that it is doing everything I am trying to accomplish
 * which is to simply give the user a 16 bit color reading that they can use in any of their code.
 * Created by Avery on 2/27/17.
 */


@TeleOp(name="Color sensor test", group="K9bot")
public class Avery_ColorSensor16bit_Test extends OpMode {

    TouchSensor snsTouch;
    Avery_ColorSensor16Bit_V1 colorSensor;
    ColorSensor snsColorBeaconLeft;
    Guinea_Timer lag;


    public void init (){
    // snsTouch = hardwareMap.touchSensor.get("snsTouch");
        // snsColorRevolver, 0x3e
    colorSensor = new Avery_ColorSensor16Bit_V1(hardwareMap , "snsColorRevolver", 0x38);
        //snsColorBeaconLeft = hardwareMap.colorSensor.get("snsColorBeaconLeft");
        lag = new Guinea_Timer();
    }
    @Override
    public void loop (){
        if (true) {//snsTouch.isPressed()){
            int[] Values16 = colorSensor.Avery_ReadColor16();
            //int[] Values8 = colorSensor.Avery_ReadColor8();

            telemetry.addData("16 bit data readings RGBW: ", String.format("%d %d %d %d", Values16[0], Values16[1], Values16[2], Values16[3]));
            //telemetry.addData("8 bit data readings RGBW: ", String.format("%d %d %d %d", Values8[0], Values8[1], Values8[2], Values8[3]));

            /*byte[] raw = colorSensor.raw();
            for (int i = 0; i < raw.length; i++) {
                int value = raw[i] & 0xff;
                telemetry.addData("0x" + Integer.toHexString(i), value);
            }*/
            if (gamepad1.x) {
                colorSensor.disengage();
            } else if (gamepad1.y) {
                colorSensor.engage();
            }
            telemetry.addData("Lag (ms): ", lag.elapsed());
            lag.reset();
        }
    }
}

