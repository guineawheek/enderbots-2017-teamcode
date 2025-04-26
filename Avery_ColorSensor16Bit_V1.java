package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchImpl;

/**
 * This class is created to read the full 16 bit reading from the modern robotics color sensor.
 * One can use this class to read the normalized reading from the color sensor.
 * This is usefull in cases where the 8bit reading will not be accurate enough for the desired task.
 * This program reads from whatever I2c adress is specified for the color sensor, then it
 * reads from the 2 registers to get the LSB and MSB values. Then it combines the two 8-bit values from the
 * 2 registers into one 16 bit value.
 *
 * Created by Avery on 2/25/17.
 */

public class Avery_ColorSensor16Bit_V1 {
    I2cDevice Sense;
    I2cDeviceSynch Sensor;
    private final int OFFSET = 0x00;


    /** This is the color sensor constructor
     */

    public Avery_ColorSensor16Bit_V1(HardwareMap hardwareMap, String name, int i2cAddress) {
        Sense = hardwareMap.i2cDevice.get(name);
        Sensor = new I2cDeviceSynchImpl(Sense, I2cAddr.create8bit(i2cAddress), false);
        Sensor.setReadWindow(new I2cDeviceSynch.ReadWindow(OFFSET, 26, I2cDeviceSynch.ReadMode.REPEAT));
        Sensor.engage();
    }

    public void engage() {
        if (!Sensor.isEngaged()) {
            Sensor.engage();
        }
    }
    public void disengage() {
        if (Sensor.isEngaged()) {
            Sensor.disengage();
        }
    }
    public void close() {
        disengage();
    }

    public byte[] raw() {
        return Sensor.read(0, 26);
    }
    //This is the first method, which returns the normalized values from the color sensor.
    //The values will be returned in an array of red, green, and blue.

    public int[] Avery_ReadColor16(){

        //Sensor.read(0x16 - OFFSET, 8); //<------ This reads the register for the LSB value  and then
                              //        the next register down to get the MSB value.
        byte[] Values16 = new byte[8];
        Values16 = Sensor.read(0x16 - OFFSET, 8);

        // These snippets below read the 2 registers for the
        // normalized reading of each color, and then shifts over and combines them.
        byte Red1 = Values16[0];
        byte Red2 = Values16[1];
        int Red16 = ((Red2 & 0xff) << 8 ) + (Red1 & 0xff);

        byte Green1 = Values16[2];
        byte Green2 = Values16[3];
        int Green16 = ((Green2 & 0xff) << 8 ) + (Green1 & 0xff);

        byte Blue1 = Values16[4];
        byte Blue2 = Values16[5];
        int Blue16 = ((Blue2 & 0xff) << 8 ) + (Blue1 & 0xff);

        byte White1 = Values16[6];
        byte White2 = Values16[7];
        int White16 = ((White2 & 0xff) << 8 ) + (White1 & 0xff);

        int[] RGBW16 = {Red16, Green16, Blue16, White16};//Puts all of the 16 bit values in an array
        return RGBW16;      //Gives the array to the user
    }


    public int[] Avery_ReadColor8(){

        //Sensor.read(0x05, 4);               //Reads all of the neccesary registers for the readings
        byte[] Values8 = new byte[4];       //Creates a byte array for the values
        Values8 = Sensor.read(0x05 - OFFSET, 4);     //Puts the values read into the byte array


        byte Red8B = Values8[0] ;
        int Red8 = Red8B & 0xff;
        byte Green8B = Values8[1];
        int Green8 = Green8B & 0xff;
        byte Blue8B = Values8[2];
        int Blue8 = Blue8B & 0xff;
        byte White8B = Values8[3];
        int White8 = White8B & 0xff;

        int[] RGBW8 = {Red8, Green8, Blue8, White8};

        return RGBW8;
    }
    public void Avery_CalibrateColor(){

    }
}