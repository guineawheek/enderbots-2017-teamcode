package org.firstinspires.ftc.teamcode.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity;

/**
 * Created by Guinea on 12/18/16.
 * A base class for all sensor classes pulling data from the phone's onboard sensors.
 *
 * TODO: still wanted?
 */

public abstract class Guinea_PhoneSensor implements SensorEventListener {
    protected SensorManager sensorManager;
    protected Sensor phoneSensor;
    protected float[] values;

    /**
     * Provides a default constructor.
     * Requires the hardwareMap in order to get the current Android application context of the ftc_app
     * @param hardwareMap an OpMode's hardwareMap
     */
    public Guinea_PhoneSensor(HardwareMap hardwareMap) {

        this.sensorManager = (SensorManager) (((FtcRobotControllerActivity) hardwareMap.appContext).getSystemService(Context.SENSOR_SERVICE));
        this.phoneSensor = sensorManager.getDefaultSensor(getSensorType());
        register();
        values = new float[0];
    }

    public void register() {
        // make this faster?!
        sensorManager.registerListener(this, phoneSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    /**
     * override this, but also don't forget to call super.onSensorChanged() first
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        values = event.values;
        onNewData();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Should be a Sensor.TYPE_* constant.
     * Used by the underlying constructor you should call in your constructor.
     * @return
     */
    public abstract int getSensorType();
    protected abstract void onNewData();

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            result.append(String.format("values[%d] == %f\n", i, values[i]));
        }
        return result.toString();
    }

}
