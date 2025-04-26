package org.firstinspires.ftc.teamcode.util;

/**
 * A class that averages a set of floating point values in an updatable ring buffer.
 * <p>This means that this class will only average the last <i>length</i> values it was updated with.</p>
 * <p>Created by guinea on 2/1/17.</p>
 * @author Guinea
 */

public class CircleBufferAvg {
    private double[] samples;
    private int length;
    private int index;

    /**
     * The constructor.
     * <p>Note that the length of the ring buffer cannot be changed after initialization.</p>
     * @param length the number of values that the ring buffer will have
     */
    public CircleBufferAvg(int length) {
        this.length = length;
        samples = new double[length];
        index = 0;
    }

    /**
     * Gets the length of the ring buffer.
     *
     * @return an int representing the length of the underlying ring buffer
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the average of all the values currently in the ring buffer.
     * <p>If the number of values is less then the length of the ring buffer, note that it will also
     * average those values with zeros representing the missing values.</p>
     * @return the average, as a double
     */
    public double getAverage() {
        double sum = 0;
        for (int i = 0; i < length; i++) {
            sum += samples[i];
        }
        return sum / (double) length;
    }

    /**
     * Updates the ring buffer with new values.
     * <p>As a ring buffer, note that it will overwrite the oldest value if there are enough values
     * to completely populate the buffer.</p>
     * @param value
     */
    public void update(double value) {
        samples[index] = value;
        index++;
        if (index >= length) index = 0;

    }
}
