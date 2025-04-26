package org.firstinspires.ftc.teamcode;

import android.os.Environment;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.util.IniConfig;

import java.io.File;

/**
 * Created by guinea on 7/1/17.
 */

@TeleOp(name="Example Config File Opmode")
public class ExampleConfigOpmode extends OpMode {
    IniConfig robotConfig; // the main class for the config file
    IniConfig.ConfigSection firstSection; //specific sections in the config file
    IniConfig.ConfigSection secondSection;

    public void init() {
        // lets us put things to telemetry in init that'll stick
        telemetry.setAutoClear(false);
        // opens a config file on the phone's internal storage
        // most of the time you can assume the underlying path is /sdcard/ftc-config/example.ini
        // it's a good idea to make a directory for your config files for organization
        robotConfig = new IniConfig(new File(Environment.getExternalStorageDirectory().getPath() + "/ftc-config/example.ini"));

        // tries to read the config; if we can't read it, show a panic message on telemetry
        // however, the opmode won't actually crash, it'll just fall back to specified fallback values
        if (!robotConfig.readConfig()) {
            telemetry.addLine("can't read config file! program will still run but failure may be imminent");
            telemetry.addData("Specific error: ", robotConfig.getLastError());
        }
        // reads sections
        firstSection = robotConfig.getSection("First Section");
        secondSection = robotConfig.getSection("Second Section");

        // read a string, if the string value isn't there a blank string is used by default
        // to change the default value just add a second string argument to .get
        telemetry.addData("String value: ", firstSection.get("string value"));
        // example of fallback value
        telemetry.addData("Nonexistant value: ", firstSection.get("non-existant key", "fallback value"));


        // read double values, getd() and getNumber() are identical also works if that's more readable
        telemetry.addData("Double value", secondSection.getd("number_value", 7.8/10));
        //telemetry.addData("Double value", secondSection.getNumber("number_value", 7.8/10));

        // if other-number isn't in the config zero is used by default
        telemetry.addData("Double value 2", secondSection.getNumber("other-number"));

        // read an integer, similar to double value, except getInteger/geti are used instead
        telemetry.addData("Integer value", secondSection.getInteger("integer.value", 42));

        // reading integer in hexadecimal notation, also works
        telemetry.addData("Hex integer value", secondSection.getInteger("hexInteger", 0xdead));

    }

    @Override
    public void loop() {

    }

    @Override
    public void stop() {
        telemetry.setAutoClear(true);
    }
}
