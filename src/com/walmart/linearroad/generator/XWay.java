package com.walmart.linearroad.generator;

/**
 * The XWay
 * This ends up as a Convenience container, primarily for tracking Accidents.
 *
 * Created by Sung Kim on 6/28/2016.
 */
public class XWay {

    /**
     * The XWay number.
     */
    private int id;

    /**
     * The time since the last Accident.  Necessary to track how often/when to make this XWay eligible for another
     *   Accident.
     */
    private int timeSinceLastAccident;

    /**
     * How long this XWay has had its current Accident.  Necessary to track when to clear an Accident.
     */
    private int timeWithAccident;

    /**
     * Whether the XWay has an Accident or not.
     * There will only be one accident per XWay at any given time.
     */
    //private boolean hasAccident;

    /**
     * The Accident associated with this XWay.
     */
    private Accident accident;

    /**
     * The time of the next Accident, with some wiggle room.
     */
    private int nextAccidentTime;

    /**
     * The time to clear the accident, with some wiggle room.
     */
    private int clearAccidentTime;

    /**
     *
     * @param xway  number externally assigned.  There are no checks for previous existence
     */
    public XWay(int xwayNumber) {
        id = xwayNumber;
        //clearAccident(0);
        accident = null;
        nextAccidentTime = createNextAccidentTime(Environment.ACCIDENT_INTERVAL);
    }

    /**
     *
     * @param base  the number of minutes to wait before being a candidate for an Accident
     * @return      the time around which to create the Accident
     */
    private int createNextAccidentTime(int base) {
        return (int)((Environment.minToSec(base) + (Math.random() < 0.5 ? (Math.random() * Environment.minToSec(5)) : -(Math.random() * Environment.minToSec(5)))));
    }

    /**
     *
     * @param base  the number of minutes to wait before clearing an Accident
     * @return      the time around which to clear the Accident
     */
    private int createAccidentClearTime(int base) {
        return (int)((Environment.minToSec(base) + (Math.random() < 0.5 ? (Math.random() * Environment.minToSec(5)) : -(Math.random() * Environment.minToSec(5)))));
    }

    /**
     * @return      the time set for the next Accident
     */
    public int getNextAccidentTime() {
        return nextAccidentTime;
    }

    public int getClearAccidentTime() {
        return clearAccidentTime;
    }

    /**
     *
     * @return      return the XWay number
     */
    public int getXWayNumber() {
        return id;
    }

    /**
     *
     * @return      the Accident, if it exists, otherwise <code>null</code>
     */
    public Accident getAccident() {
        return accident;
    }
    /**
     * Set this XWay as having an Accident.
     */
    public void turnOnAccident(Accident acc) {
        //hasAccident = true;
        accident = acc;
        clearAccidentTime = acc.getTime() + createAccidentClearTime(Environment.ACCIDENT_CLEAR_WAIT_TIME);
        // DEBUG
        System.out.println("Accident to-be-cleared time at " + clearAccidentTime);
        // DEBUG END
        //timeWithAccident = 0;
    }

    /**
     * Increment the time of this XWay with or without an Accident.
     */
    public void incrTime() {
        //incrTimeSinceLastAccident();
        //incrTimeWithAccident();
    }

    /**
     * Increment the time with an Accident.
     */
    /*private void incrTimeWithAccident() {
        if (hasAccident()) {
            timeWithAccident++;
        }
    }*/

    /**
     * Increment the time without an Accident.
     */
    /*private void incrTimeSinceLastAccident() {
        if (!hasAccident()) {
            timeSinceLastAccident++;
        }
    }*/

    /**
     * @return Return the amount of time since this XWay has had an Accident. 0 if no Accident exists.
     */
    public int getTimeWithAccident() {
        return timeWithAccident;
    }

    /**
     *
     * @return Return the amount of time since the last Accident was cleared.
     */
    public int getTimeSinceLastAccident() {
        return timeSinceLastAccident;
    }

    /**
     *
     * @return      returns whether this Car has an Accident
     */
    public boolean hasAccident() {
        return accident != null;
    }

    /**
     * Clear the Accident in this XWay.
     * Clearing Accidents must be done in both the XWay and the TrafficCondition.
     *
     * @param currTime  the current time, which is needed to add a random time for the _next_ accident
     */
    public void clearAccident(int currTime) {
        accident.getC1().startCar();
        accident.getC2().startCar();

        //resetTimeSinceLastAccident();
        // We don't base the next Accident on the sim time but instead the time since the accident was last cleared.
        nextAccidentTime = currTime + createNextAccidentTime(Environment.ACCIDENT_INTERVAL);
        accident = null;

        // DEBUG
        System.out.println("xway: " + id + ", nextAccidentTime at " + nextAccidentTime);
        // DEBUG END
    }

    /**
     * Reset how long it's been since the last Accident.
     */
    public void resetTimeSinceLastAccident() {
        //timeSinceLastAccident = 0;
    }

    /**
     * Set equality of XWay based on its id.
     *
     * @param o     the other XWay
     * @return      whether the cars are the same object or have the same id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof XWay) {
            XWay that = (XWay) o;
            if (that.id == id) {
                return true;
            }
        }
        return false;
    }

    /**
     * The hash code will simply the TrafficCondition's id.
     *
     * @return  the XWay's id
     */
    @Override
    public int hashCode() {
        return id;
    }
}
