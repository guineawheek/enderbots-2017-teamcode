package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchImpl;

/**
 * Created by guinea on 2/21/17.
 */

public class ModernRoboticsI2cFullColorSensor {
    public static final int COMMAND_ACTIVE = 0x00;
    public static final int COMMAND_PASSIVE = 0x01;
    public static final int COMMAND_50Hz = 0x35;
    public static final int COMMAND_60Hz = 0x36;
    public static final int COMMAND_BLACK_CALIB = 0x42;
    public static final int COMMAND_WHITE_CALIB = 0x43;


    I2cDevice snsColor;
    I2cDeviceSynch snsColorHandler;
    public ModernRoboticsI2cFullColorSensor(HardwareMap hardwareMap, String name, int addr) {

        snsColor = hardwareMap.i2cDevice.get("cc");
        snsColorHandler = new I2cDeviceSynchImpl(snsColor, I2cAddr.create8bit(addr), false);
        snsColorHandler.engage();

    }

    public int readI2cUInt8(int addr) {
        return snsColorHandler.read8(addr) & 0xff;
    }

    public int readI2cUInt16(int addr) {
        byte[] buffer = snsColorHandler.read(addr, 2);
        return ((buffer[1] & 0xff) << 8) & (buffer[0] & 0xff);
    }

    public int getFirmwareRevision() {
        return readI2cUInt8(0x00);
    }
    public int getManufacturerCode() {
        return readI2cUInt8(0x01);
    }
    public int getSensorIDCode() {
        return readI2cUInt8(0x02);
    }
    public int getCommand() {
        return readI2cUInt8(0x03);
    }
    public int colorNumber() {
        return readI2cUInt8(0x04);
    }
    public int red() {
        return readI2cUInt8(0x05);
    }
    public int green() {
        return readI2cUInt8(0x06);
    }
    public int blue() {
        return readI2cUInt8(0x07);
    }
    public int alpha() {
        return readI2cUInt8(0x08);
    }

    public int colorNumberIndex() {
        return readI2cUInt8(0x09);
    }
    public int redIndex() {
        return readI2cUInt8(0x0a);
    }
    public int greenIndex() {
        return readI2cUInt8(0x0b);
    }
    public int blueIndex() {
        return readI2cUInt8(0x0c);
    }

    public int red16() { return readI2cUInt16(0x0e); }
    public int green16() { return readI2cUInt16(0x10); }
    public int blue16() { return readI2cUInt16(0x12); }
    public int alpha16() { return readI2cUInt16(0x14); }

    public int red16Norm() { return readI2cUInt16(0x16); }
    public int green16Norm() { return readI2cUInt16(0x18); }
    public int blue16Norm() { return readI2cUInt16(0x1a); }
    public int alpha16Norm() { return readI2cUInt16(0x1c); }

    public void writeCommand(int command) {
        snsColorHandler.write8(0x03, command);
    }

    public void calibrateBlack() {
        writeCommand(COMMAND_BLACK_CALIB);
    }

    public void calibrateWhite() {
        writeCommand(COMMAND_WHITE_CALIB);
    }

    public boolean isCalibrating() {
        int command = getCommand();
        return ((command == COMMAND_BLACK_CALIB || command == COMMAND_WHITE_CALIB));
    }

    //colorCreader.write8(3, 0);    //Set the mode of the color sensor to Active
        //.read(addr, len)
        //.read8(addr)


}
