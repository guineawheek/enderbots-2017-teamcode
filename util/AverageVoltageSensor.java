package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.teamcode.logging.Loggable;

import java.util.LinkedList;

/**
 * Created by guinea on 2/20/17.
 */

public class AverageVoltageSensor implements Loggable {
    HardwareMap hardwareMap;
    public AverageVoltageSensor(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
    }

    public double getAverageVoltage() {
        LinkedList<Double> values = new LinkedList<>();
        double sum = 0;
        for (VoltageSensor voltageSensor : hardwareMap.voltageSensor) {
            values.add(voltageSensor.getVoltage()) ;
        }
        for (double value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    @Override
    public Object getLogData() {
        return getAverageVoltage();
    }
}
