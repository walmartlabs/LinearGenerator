package com.walmart.linearroad.generator;

/**
 * Accident
 *
 * Select cars randomly to be part of an accident and create the Accident.
 * The Accident will hold the essential information for an Accident.
 *
 * Created by Sung Kim on 6/28/2016.
 */
public class Accident {
    private Car c1;
    private Car c2;
    private int seg;
    private int dir;
    private int time;
    //private int clearTime;

    /**
     * Pass in two Cars and a time to create an Accident.
     *
     * @param c1    the first Car
     * @param c2    the second Car
     * @param time  the simulation time, in seconds
     * @param seg   the segment of the XWay this Accident occurred in
     * @param dir   the direction of the XWay this Accident occurred in
     */
    public Accident(Car c1, Car c2, int time, int seg, int dir) {
        this.c1 = c1;
        this.c2 = c2;
        this.seg = seg;
        this.dir = dir;
        this.time = time;
        //this.clearTime = -1;
    }

    /**
     *
     * @return the first Car that makes up the Accident
     */
    public Car getC1() {
        return c1;
    }

    /**
     *
     * @return the second Car that makes up the Accident
     */
    public Car getC2() {
        return c2;
    }

    /**
     *
     * @return the Accident segment
     */
    public int getSeg() { return seg; }

    /**
     *
     * @return the direction
     */
    public int getDir() { return dir;}

    /**
     *
     * @return the time the accident was created
     */
    public int getTime() { return time; }
}
