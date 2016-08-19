package com.walmart.linearroad.generator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Routes will read TrafficCondition to determine how Routes may need to be adjusted.
 * TrafficCondition will take Accidents and number of cars into account.
 * TrafficCondition's are assigned to a three-dimensional Environment.trafficConditions List<List<List<TrafficCondition>>>.
 * The levels of embedding for the List are xway -> segment -> dir.
 * <p>
 * TODO: If there's an accident in a TrafficCondition then:
 *   cars will slow down in the prior segment and in the accident-segment,
 *   a random number of cars will exit, 5%,
 * <p>
 * A TrafficCondition is:
 * 1) The number of cars in a segment
 * 2) Whether there's an accident
 *
 * Created by Sung Kim on 6/28/2016.
 */
public class TrafficCondition {

    /**
     * Required to facilitate checking of cars moving from one segment to another.
     */
    private int id;

    /**
     * XWay of this TrafficCondition.
     * Simply retrieve from a Car.
     */
    //private int xway;

    /**
     * Necessary if speeds and car behaviors will be altered by the presence of an Accident
     */
    //private Accident accident;
    // Look up an accident, do not hold a reference to the Accident.

    /**
     * The Cars in this xway-seg-dir.
     */
    private Set<Car> cars;

    /**
     * Create a new TrafficCondition.
     *
     * @param id
     */
    public TrafficCondition(int id) {//}, int xway) {
        this.id = id;
        //setAccident(null);
        cars = new HashSet<>();
    }

    /**
     *
     * @return  return the id of this TrafficCondition
     */
    public int getId() {
        return id;
    }

    /**
     * Backed by a Set<>.
     *
     * @return  return the number of Cars in this TrafficCondition
     */
    public int getNumCars() {
        return cars.size();
    }

    /**
     *
     * @param c     the Car to be potentially added to the Set of Cars in this TrafficCondition
     */
    public void addCar(Car c) {
        cars.add(c);
    }

    /**
     * We remove a Car when it leaves the 'seg-dir' connected to this TrafficCondition.
     * Again, the seg-dir is represented by the holding List Environment.trafficConditions, which is a 3-d List
     *   as described above.
     *
     * @param c     the Car to be removed from the Set of Cars in this TrafficCondition
     * @return      whether the car was found and removed
     */
    public boolean removeCar(Car c) {
        return cars.remove(c);
    }

    /**
     *
     * @return  an Iterator<Car> over the Set of Cars
     */
    public Iterator<Car> getCarsIterator() {
        return cars.iterator();
    }

    /**
     * We store an Accident to be able to adjust traffic, taking the Accident into account.
     * We simply look up an accident in the current Xway
     *
     * @param accident  the Accident instance to assign to this TrafficCondition
     */
    //public void setAccident(Accident accident) {
    //   this.accident = accident;
    //}

    /**
     * Unless we return the accident related to the XWay we don't return anything
     * @return  this TrafficConditions Accident, if any
     *
     */
    //public Accident getAccident() {
    //return accident;
    //}

    /**
     * Clear an accident.
     *
     * TODO?: Don't we want to remove cars from the simulation? if they were in an accident?
     *   No, it could have simply been a fender bender...
     */
    //public void clearAccident() {
    // Allow the two involved Cars to move again.
    //   cars.forEach(c -> {
    //      if (c.equals(accident.getC1()) || c.equals(accident.getC2())) {
    //         c.startCar();
    //    }
    //});
    //setAccident(null);
    //}

    /**
     * Remember, a TrafficCondition maps to a seg-dir in the simulation.
     *
     * @return  whether there is an active Accident in this TrafficCondition
     */
    public boolean hasAccident() {
        for(Car c : cars) {
            return Environment.xways.get(c.getCurrXway()).hasAccident();
        }
        return false;
    }


    /**
     *
     * @return a String representation of this TrafficCondition.  Simply the number of cars and whether there's an
     *   Accident
     */
    @Override
    public String toString() {
        return "numCars:" + getNumCars() + ", hasAccident: " + hasAccident();
    }

    /**
     * Set equality of TrafficConditions based on its id.
     *
     * @param o     the other TrafficCondition
     * @return      whether the cars are the same object or have the same id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof TrafficCondition) {
            TrafficCondition that = (TrafficCondition) o;
            if (that.id == id) {
                return true;
            }
        }
        return false;
    }

    /**
     * The hash code will simply the TrafficCondition's id.
     *
     * @return  the TrafficConditions's id
     */
    @Override
    public int hashCode() {
        return id;
    }
}
