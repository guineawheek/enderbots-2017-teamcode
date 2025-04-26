package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Avery_ColorSensor16Bit_V1;

import java.util.Arrays;

/**
 * Created by guinea on 3/18/17.
 */

public class RevolverWatcherThread extends Thread {
    Avery_ColorSensor16Bit_V1 snsColorBall;
    boolean enabled;
    boolean watchDogError;
    String cachedData;
    Guinea_Timer watchdog;
    public RevolverWatcherThread(HardwareMap hardwareMap) {
        snsColorBall = new Avery_ColorSensor16Bit_V1(hardwareMap, "snsColorRevolver", 0x38);
        enabled = false;
        cachedData = "";
        watchdog = new Guinea_Timer(250);
    }
    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                if (enabled) {
                    if (watchDogError = watchdog.done()) {
                        cachedData = "error: hanging";
                        return;
                    }

                    cachedData = Arrays.toString(snsColorBall.Avery_ReadColor16());
                    watchdog.reset();
                }
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    return;
                }
            }
        } catch (Exception e) {}
    }

    public boolean hanging() {
        return watchDogError;
    }

    public String getData() {
        return cachedData;
    }

    public void enable() {
        enabled = true;
        watchdog.reset();
    }

    public void disable() {
        enabled = false;
    }
}
