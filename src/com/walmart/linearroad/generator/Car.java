package com.walmart.linearroad.generator;

import java.util.*;

/**
 * A Car that will travel the expressways.
 * <p>
 * Created by Sung Kim on 6/28/2016.
 */
public class Car {

    /**
     * A global qid (Query Id) counter.
     */
    private static long qid = 0;

    /**
     * Global counters for emitted notifications.
     */
    private static long t0s = 0;
    private static long t2s = 0;
    private static long t3s = 0;
    private static long tAll = 0;

    /**
     * The CarId
     */
    private int id;

    /**
     * The current position, in feet, where feet ranges from 0 - 5280 * Environment.NUM_SEGMENTS
     */
    private double position;

    /**
     * currSpeed and lastSpeed are used to determine the distance traveled
     * currSpeed is what is emitted if emit() is called
     * Updated by updateSpeed()
     */
    private int currSpeed;
    /**
     * currSpeed and lastSpeed are used to determine the distance traveled
     * Updated by updateSpeed()
     */
    private int lastSpeed;

    /**
     * The current time
     */
    private int currTime;

    /**
     * The time this Car began its run.
     * If reentrant, it will be the time it re-entered the expressway.
     */
    private int startTime;

    /**
     * NOT USED: at the moment.
     */
    private int lastTime;

    /**
     * The Route is a logical way to separate Car state from where the Car is trying to go.
     */
    private Route route;

    /**
     * The car's current lane.
     */
    private int currLane;

    /**
     * Whether this Car can be removed from the simulation 'world.'
     * Currently this is only due to moving to the exit lane.
     */
    private boolean canRemove;

    /**
     * Whether this Car is an Accident Car, i.e. is involved in an accident.
     */
    private boolean isAccidentCar;

    /**
     * When creating an Accident, this is the Car to target, its lane and position.
     * The targeted car will be the one that was stopped first by the simulation.
     */
    private Car targetCar;

    /**
     * Go ahead and prevent a Car from being in an Accident twice.
     * Set once this car has been in an accident.
     */
    private boolean hasBeenInAccident;

    /**
     * Create a Car with an <code>id</code>, a <code>Route</code>, and a <code>startTime</code>.
     * Route creation is external to enable custom Routes if necessary.
     *
     * @param id        must be unique for the simulation
     * @param route     see Route
     * @param startTime simulation time when the car enters the simulation
     */
    public Car(int id, Route route, int startTime) {
        this.id = id;

        resetCar(route, startTime);

        // DEBUG
        //System.out.println(Environment.trafficConditions.get(route.getXway()).get(getSegFromPosition()).get(route.getDir()).getNumCars());
        // END DEBUG
    }


    /**
     * Set the proper state for a reentrant Car with a new random Route.
     *
     * @param startTime
     * @return this Car with its state properly reset
     */
    public Car reEnter(int startTime) {
        Route newRoute = Environment.createRoute();

        resetCar(newRoute, startTime);

        // DEBUG
        //System.out.println("Hi, I'm car " + id + " and I'm reentering at time " + startTime + ".");
        // DEBUG END

        return this;
    }


    /**
     * Whether creating a new Car or restarting an existing Car (reenter) just give it a new Route and a start time.
     *
     * @param route     the new Route
     * @param startTime the start time
     */
    private void resetCar(Route route, int startTime) {
        this.route = route;
        this.currTime = startTime;
        this.startTime = startTime;
        this.currSpeed = Environment.ENTRY_SPEED; // This is the given Environment speed while entering on-ramp.
        lastSpeed = 0; // Of course the lastSpeed is zero when new, or reentrant.
        this.position = segToBeginningPosition(route.getEntranceSegIndex());
        currLane = 0; // All new cars start in the entrance lane.
        canRemove = false;

        // This section is wrong.  It shouldn't be here.
        // Add this Car to the Set of Cars in this given TrafficCondition
        // DEBUG
        //Environment.trafficConditions.get(route.getXway());
        // DEBUG END
        // Having references to Environment.trafficConditions leads to tight coupling with the simulation as a whole.
        // It is possible to separate them, but at this point the value of separation isn't useful enough.
        Environment.trafficConditions.get(route.getXway()).get(getSegFromPosition()).get(route.getDir()).addCar(this);

        isAccidentCar = false;
        targetCar = null;
        hasBeenInAccident = false;
    }


    /**
     * When a car is created it is placed at the beginning of the segment if going right.
     * I.e. Dir 0, Seg 0 -> Position 0, Seg 1 -> Position 5280.
     * If a car is going left, it is placed at the end of the segment, in position-feet.
     * I.e. Dir 1, Seg 0 -> Position 5279, Seg 1 -> Position 10559.
     *
     * @param seg the segment from which to get a beginning position
     * @return the position, in feet, of a Car's starting point
     */
    private int segToBeginningPosition(int seg) {
        if (route.getDir() == 0) {
            return seg * Environment.FEET_PER_SEGMENT;
        } else {
            return seg * Environment.FEET_PER_SEGMENT + Environment.FEET_PER_SEGMENT - 1;
        }
    }


    /**
     * Simple calculation to get segment from current position.
     *
     * @return the calculated segment from a given position
     */
    private int getSegFromPosition() {
        return (int) Math.floor(position / Environment.FEET_PER_SEGMENT);
    }


    /**
     * This is our default method to handle speed to-be-used for distance.
     * Simply use the average of the curr and last speeds.
     * More complexity can be added to take acceleration into account.
     *
     * @return the average speed, as a double
     */
    private double getAvgSpeed() {
        return (currSpeed + lastSpeed) / 2.0;
    }


    /**
     * The speed parameter is a double because that's what getAvgSpeed returns.
     *
     * @param speed the speed, as a double, to get how many feet were traveled
     * @return how many feet are traveled at the given speed in one second
     */
    private double convertMphToFeetPerSec(double speed) {
        return speed / 3600 * 5280;
    }


    /**
     * This is just a function of speed, converted to feet per second, times seconds.
     *
     * @param seconds how many seconds of travel at the given speed function of the car
     * @return how much the car moves in the given seconds
     */
    private double getDistanceTraveled(int seconds) {
        return convertMphToFeetPerSec(getAvgSpeed()) * seconds;
    }


    /**
     * Change lanes.
     * Lane changes will be random while traveling but must be in the travel lanes:
     * The travel lanes are: 1 to Environment.EXIT_LANE - 1
     * Allow for a speed up or speed down while changing lanes.  Randomly chosen update speeds will be accompany
     * a lane change.
     */
    private void changeLane() {
        int speedDelta = (int) (Math.random() * Environment.MAX_LANE_CHANGE_SPEED_DELTA);
        currSpeed += (Math.random() < 0.5) ? speedDelta : -speedDelta;
        currLane = new Random().nextInt(Environment.NUM_LANES - 2) + 1; // I.e. 5 lanes, [0-3) + 1 -> 1-3 travel lanes
    }

    /**
     * Set the lane and handle any attendant processing desired.
     * When the car hits the exit segment, based on its Route, then move to the exit lane:
     * Environment.EXIT_LANE.
     *
     * @param lane the lane the Car will move to
     */
    private void setLane(int lane) {
        currLane = lane;
        if (currLane == Environment.EXIT_LANE) {
            exitXWay();
        }
    }


    /**
     * Clean up the car.  But also decide if this car will be a candidate for re-entrance later.
     * If the car won't be returning then it should be eligible for garbage collection.
     */
    private void exitXWay() {
        if (!isRemoveable()) {
            setRemovable(true);
            // Put the car into the reentrants list at a random time in the future
            if (willBeReentrant()) {
                int newTime = currTime + (new Random().nextInt(1000) + 1000); // Add arbitrary delay.
                Environment.addToReentrants(newTime, this);

            }
            removeFromTrafficCondition();
        }
    }


    /**
     * Set the canRemove status if the Car can be removed from the simulation ...
     * unless it's reentrant.
     */
    private void setRemovable(boolean remove) {
        canRemove = remove;
        // If the car is reentrant then this must be set to false.
        // UPDATE: canRemove is used to decide whether a car can be removed from a traffic condition as well as the
        // working car set in the main simulation.  But, the car can, and should, leave both.  It will be kept in a
        // separate List/Map for reentrance when the right time comes along.
    }


    /**
     * Does this car have its canRemove flag set?
     *
     * @return whether this car can be removed from the simulation
     */
    public boolean isRemoveable() {
        return canRemove;
    }


    /**
     * Determine if this Car will be eligible for reentrance.
     *
     * @return whether this Car will be eligible to be put into the reentrants list
     */
    private boolean willBeReentrant() {
        if (Math.random() < Environment.REENTRANT_PERCENT) return true;
        return false;
    }


    /**
     * Return a day between two values for type 3 queries
     *
     * @return a random int between the Environment first and last days for historical, type 3, queries
     */
    private int getHistoricalDay() {
        Random r = new Random();
        return r.nextInt(Environment.HISTORICAL_DAY_LAST - Environment.HISTORICAL_DAY_FIRST) + Environment.HISTORICAL_DAY_FIRST;
    }


    /**
     * Adjust the max speed down on travel lanes based on road conditions.
     * TODO: this should be more continues than in steps.
     *
     * @return an integer representing how much to adjust a cars speed down based on traffic congestion
     */
    private int getTCMaxSpeedAdjustment() {
        int numCars = Environment.trafficConditions.get(route.getXway()).get(getSegFromPosition()).get(route.getDir()).getNumCars();
        int safeCars = Environment.FULL_SPEED_CARS_PER_LANE * (Environment.NUM_LANES - 2);
        int step = 5 * (Environment.NUM_LANES - 2);  // Extra cars per lane * num travel lanes
        if (numCars > safeCars && numCars <= safeCars + step) {
            return 10;
        } else if (numCars > safeCars + step && numCars <= safeCars * step * 2) {
            return 20;
        } else if (numCars > safeCars + step * 2 && numCars <= safeCars * step * 3) {
            return 30;
        } else if (numCars > safeCars + step * 3 && numCars <= safeCars * step * 4) {
            return 40;
        } else if (numCars > safeCars + step * 4) {
            return 50;
        }
        return 0;
    }


    /**
     * The speed is increased:
     * 1) Up to 70 mph if it's not, if the TrafficCondition allows
     * 2) Must slow down around an accident
     * 3) Must generally flow with traffic based on TrafficCondition
     * i.e. based on the number of travel lanes and the number of cars currently in the segment
     * determines the max speed of an area
     * 4) (TODO) You want a random set of speedy cars, seeking to go Environment.MAX_SPEED
     * <p>
     * This will also be 'world' controlled since the Cars are not autonomous threads.
     * <strike>This also spares us from having to have a TrafficCondition associated with each Car, although it would
     * simply be a reference to the same TrafficCondition for all cars in a xway-dir.</strike>
     * <p>
     * 5) (TODO) Cars should slow down on the other side of an accident as well
     * 6) If the car is in the entry lane the max speed is forty and it will speed up, if possible, up to 40
     * 7) If the car is in the exit lane it will disappear after its first appearance in the exit lane, with
     * a speed of 10.
     * <p>
     * This update will be run every UPDATE_INTERVAL.  Default is 1 second.
     * <p>
     */
    private void updateSpeed() {
        lastSpeed = currSpeed;
        int fudge = 0;  // Insert some "real-world" variety in speeds. 50-50 chance of a little faster/slower.

        // Entrance lane
        if (isAccidentCar) {
            currSpeed = 0;
        } else if (currLane == Environment.ENTRANCE_LANE && currSpeed < Environment.MAX_SPEED_ENTRANCE) {
            currSpeed += acceleration(Environment.ENTRANCE_LANE) * Environment.UPDATE_INTERVAL;
            if (currSpeed > Environment.MAX_SPEED_ENTRANCE - getTCMaxSpeedAdjustment()) {
                fudge = new Random().nextInt(Environment.SPEED_FUDGE / 2);
                currSpeed = Environment.MAX_SPEED_ENTRANCE - getTCMaxSpeedAdjustment() + (Math.random() < 0.5 ? fudge : -fudge);
                if (currSpeed < 0) currSpeed = 0;
            }

            // Travel lanes
            // We don't just max out speed but provide a wiggle
        } else if (currLane > Environment.ENTRANCE_LANE && currLane < Environment.EXIT_LANE) {
            // We only change travel lanes while traveling in the travel lanes
            if (Math.random() < Environment.TRAVEL_LANE_CHANGE_PROBABILITY) {
                changeLane();  // This belongs in 'moveCar()'
            } else {
                // The 1 is the lane index and is arbitrary--anything non-entrance and non-exit would work.
                currSpeed += acceleration(1) * Environment.UPDATE_INTERVAL;
                if (currSpeed > Environment.SPEED_LIMIT - getTCMaxSpeedAdjustment()) {
                    fudge = new Random().nextInt(Environment.SPEED_FUDGE);
                    currSpeed = Environment.SPEED_LIMIT - getTCMaxSpeedAdjustment() + (Math.random() < 0.5 ? fudge : -fudge);
                }
            }
            // Exit lane
        } else if (currLane == Environment.EXIT_LANE) {
            currSpeed = Environment.MAX_SPEED_EXIT;
        }
    }


    /**
     * Determine how much a Car's velocity should change based on the Environment.UPDATE_INTERVAL
     * and a given acceleration for the type of lane.
     *
     * @param laneType entrance lane, Travel lane
     * @return the amount of acceleration
     */
    private int acceleration(int laneType) {
        if (laneType == Environment.ENTRANCE_LANE) {
            return (int) (Math.random() * Environment.ACCELERATION_ENTRANCE);
        } else {
            return (int) (Math.random() * Environment.ACCELERATION_TRAVEL);
        }
    }


    /**
     * Make sure a position doesn't go negative and also doesn't go beyond the segment limit.
     * Remember, 0-indexed.
     */
    private void normalizePosition() {
        if (position < 0) position = 0;
        if (position > Environment.FEET_PER_SEGMENT * (Environment.NUM_SEGMENTS - 1))
            position = Environment.FEET_PER_SEGMENT * (Environment.NUM_SEGMENTS - 1);
        // DEBUG
        if (getSegFromPosition() >= Environment.NUM_SEGMENTS)
            System.out.println(currTime + ": " + id + ": " + position);
        // DEBUG END
    }


    /**
     * Return the id of the car.
     *
     * @return This Car's id.
     */
    public int getId() {
        return id;
    }


    /**
     * Move the car forward x feet based on speed.
     * The environment/context/executing function needs to call this function on each car.  I.e. the cars are not
     * threads that update themselves.
     * At the moment cars can overlap.  We may want to add dimensions to cars and prevent them from overlapping and
     * track distance from one car to the car in front of it,  as well as make sure cars can't change lanes
     * if there's a car in the way.
     */
    public void moveCar() {
        // We don't move if we're an accident car
        if (isAccidentCar) {
            return;
        }

        // We home in on the first accident car and then stop and also become an accident car
        if (targetCar != null) {
            currLane = targetCar.currLane;
            currSpeed = Environment.MAX_SPEED;  // Just go ramming speed for now.
            position = getDistanceTraveled(Environment.UPDATE_INTERVAL);
            if (position > targetCar.position) {
                position = targetCar.position;
                stopCar();
            }
            return;
        }

        int dir = route.getDir();
        double delta = getDistanceTraveled(Environment.UPDATE_INTERVAL); // Update the car location every sec. emit
        TrafficCondition tc_prev = Environment.trafficConditions.get(route.getXway()).get(getSegFromPosition()).get(route.getDir());

        // every UPDATE_INTERVAL
        position += dir == 0 ? delta : -delta;
        normalizePosition();

        // Get the new segment based on the new position
        int seg = getSegFromPosition();
        // See if you're in the same segment
        TrafficCondition tc_curr = Environment.trafficConditions.get(route.getXway()).get(seg).get(route.getDir());

        // DEBUG
        //System.out.println(route.getXway() + "," + seg + "," + route.getDir() + ": " + tc_curr.getNumCars());
        // END DEBUG

        // Update the TrafficCondition.
        // Check if the car has moved from one segment to another.
        if (tc_curr.getId() != tc_prev.getId()) {
            // Subtract from the previous and add to the current
            //tc_curr.incrNumCars();
            tc_curr.addCar(this);
            //tc_prev.decrNumCars();
            tc_prev.removeCar(this);
        }

        // Check if this car needs to exit and take appropriate exit, or non-exit, actions.
        if (seg == route.getExitSegIndex() && !isRemoveable()) {
            setLane(Environment.EXIT_LANE);
            // Check if the car has moved from entrance lane to a travel lane.
        } else if ((dir == 0 && seg == route.getEntranceSegIndex() + 1) ||
                (dir == 1 && seg == route.getEntranceSegIndex() - 1)) {
            setLane(Environment.ENTRANCE_LANE + 1);
        }

        // Update the speed after moving the car
        updateSpeed();
    }

    /**
     * Emit the current information, or position report, for a Car.
     * A report is as follows:
     * type, time, carid, speed, xway, lane, dir, seg, pos, qid, sinit, send, dow, tod, day
     * Cars emit upon request.
     *
     * @return a String with the above fields
     */
    public String emit() {
        StringBuilder info = new StringBuilder();
        // Type 0
        info.append(String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d\n",
                0, currTime, id, currSpeed, route.getXway(), currLane, route.getDir(), getSegFromPosition(), (int) position, -1, -1, -1, -1, -1, -1));
        t0s++;
        // Check chances for also emitting an "Other" report, and then what type each will have.
        if (Math.random() < 0.01) {
            double typeChance = Math.random();
            if (typeChance < 0.5) { // Type 2
                // qid is a global counter attached to the Car class at the moment.
                info.append(String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d\n",
                        2, currTime, id, currSpeed, route.getXway(), currLane, route.getDir(), getSegFromPosition(), (int) position, qid++, -1, -1, -1, -1, -1));
                t2s++;
                tAll++;
            } else if (typeChance >= 0.5 && typeChance <= 0.6) { // Type 3
                info.append(String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d\n",
                        3, currTime, id, currSpeed, route.getXway(), currLane, route.getDir(), getSegFromPosition(), (int) position, qid++, -1, -1, -1, -1, getHistoricalDay()));
                t3s++;
                tAll++;
            } else if (typeChance >= 0.6) { // Type 4
                // Do nothing for now
            }
        }
        tAll++;

        return info.toString();
    }

    /**
     * Add a delta to the current time of a car.
     *
     * @param delta amount of time in seconds to update Car's time.  May not always be an UPDATE_INTERVAL
     */
    public void updateTime(int delta) {
        currTime += delta;
    }


    /**
     * When a car exits an expressway we need to remove it from its corresponding TrafficCondition as well.
     * Another function tightly-coupled with the Environment.trafficConditions.  Does have to be but realize that it is.
     */
    public void removeFromTrafficCondition() {
        // DEBUG
        //System.out.println("Removing " + id + " from simulation at time " + currTime + ".");
        //System.out.println(Environment.trafficConditions.get(getCurrXway()).get(getSegFromPosition()).get(getCurrDir()).getNumCars());
        // DEBUG END

        Environment.trafficConditions.get(getCurrXway()).get(getSegFromPosition()).get(getCurrDir()).removeCar(this);

        // DEBUG
        //System.out.println(Environment.trafficConditions.get(getCurrXway()).get(getSegFromPosition()).get(getCurrDir()).getNumCars());
        // DEBUG END
    }

    /**
     * @return the Car's startTime
     */
    public int getStartTime() {
        return startTime;
    }

    /**
     * @return the Car's currTime
     */
    public int getCurrTime() {
        return currTime;
    }

    /**
     * @return the Car's current xway
     */
    public int getCurrXway() {
        return route.getXway();
    }

    /**
     * @return the Car's current lane
     */
    public int getCurrLane() {
        return currLane;
    }

    /**
     * Place this car at the same lane and position as the target car.
     * Used for accident creation
     *
     * @param cr the target car
     */
    public void targetStoppedCar(Car cr) {
        this.currLane = cr.currLane;
        this.position = cr.position;
    }

    /**
     * position is a double but we will return it as an int and base equalities as an int.
     *
     * @return the current position of the Car as an int
     */
    public int getCurrPosition() {
        return (int) position;
    }

    /**
     * Get the segment.
     *
     * @return the Car's current segment based on its position
     */
    public int getCurrSegment() {
        return getSegFromPosition();
    }

    /**
     * Get the direction of travel.
     *
     * @return the Car's current direction of travel
     */
    public int getCurrDir() {
        return route.getDir();
    }


    /**
     * Not just stop a car but make it an accident car.  No reason to 'officially' stop a car otherwise.
     * // OR, makeAccidentCar()...
     */
    public void stopCar() {
        currSpeed = 0;
        lastSpeed = 0; // The last speed is necessary to to prevent car movement once the car is stopped.
        isAccidentCar = true;
        hasBeenInAccident = true;

        // DEBUG
        System.out.println("Car " + id + " is stopping at time " + currTime + " and position " + position);
        // DEBUG END
    }

    /**
     * Remove a car as an accident car and restart it on its journey.  We don't forcibly remove the car,
     * although we can.
     */
    public void startCar() {
        isAccidentCar = false;
        targetCar = null;

        // DEBUG
        System.out.println("Car " + id + " is starting at time " + currTime);
        // DEBUG END
    }

    /**
     * We don't want a car to be in an accident twice.
     *
     * @return
     */
    public boolean hasBeenInAccident() {
        return hasBeenInAccident;
    }

    /*
    The following are global/Car-class level variables to track total numbers of notifications.
     */
    /**
     * Get the global qid.
     *
     * @return the current qid of the cars
     */
    public static long getQid() {
        return qid;
    }

    /**
     * @return the number of type 0 notifications emitted
     */
    public static long getT0s() {
        return t0s;
    }

    /**
     * @return the number of type 2 notifications emitted
     */
    public static long getT2s() {
        return t2s;
    }

    /**
     * @return the number of type 3 notifications emitted
     */
    public static long getT3s() {
        return t3s;
    }

    /**
     * @return the number of all notifications emitted
     */
    public static long getTAll() {
        return tAll;
    }

    /**
     * We set equality of Cars as the object itself or a matching id.
     *
     * @param o the other Car
     * @return whether the cars are the same object or have the same id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Car) {
            Car that = (Car) o;
            if (that.id == id) {
                return true;
            }
        }
        return false;
    }


    /**
     * The hash code will simply the car's id.
     *
     * @return the car's id
     */
    @Override
    public int hashCode() {
        return id;
    }


    /**
     * A set of tests for the Car class.
     *
     * @param args
     */
    public static void main(String[] args) {
        // Necessary to initialize trafficConditions because Car makes references to it. (Yes, tightly coupled.)
        Environment.initializeTrafficConditions();
        Environment.initializeReentrants();

        Route r = new Route(0, 0, 0);
        Car c = new Car(1, r, 0);
        assert c.segToBeginningPosition(0) == 0 : "segToBeginningPosition, seg 0, dir 0, failed";
        r = new Route(0, 1, 0);
        c = new Car(1, r, 0);
        assert c.segToBeginningPosition(0) == Environment.FEET_PER_SEGMENT - 1 : "segToBeginningPosition, seg 0, dir 1, failed";
        r = new Route(0, 0, 0);
        c = new Car(1, r, 0);
        assert c.getAvgSpeed() == 5 : "getAvgSpeed failed";
        System.out.println(c.convertMphToFeetPerSec(5));
        assert c.convertMphToFeetPerSec(5) == 7.333333333333334 : "convertMphToFeetPerSec failed";
        System.out.println(c.getDistanceTraveled(30));
        assert (int) c.getDistanceTraveled(30) == 220 : "getDistanceTraveled failed";
        assert c.getSegFromPosition() == 0 : "getSegFromPosition, pos 0, failed";
        c.position = 5280;
        assert c.getSegFromPosition() == 1 : "getSegFromPosition, pos 5280, failed";
        c.position = Environment.FEET_PER_SEGMENT * 33 + 55;
        assert c.getSegFromPosition() == 33 : "getSegFromPosition, seg 33, failed";
        c.position = Environment.FEET_PER_SEGMENT * 33 - 55;
        assert c.getSegFromPosition() == 32 : "getSegFromPosition, seg 32, failed";

        // Test how often something with a probability of 1% happens.
        int count = 0;
        for (int i = 0; i < 500; i++) {
            double j = Math.random();
            if (j < 0.01) {
                System.out.println(count++ + ": " + j);
            }
        }

        // Ensure days for t3 historical tolls are within the given day range.
        for (int i = 0; i < 1000; i++) {
            int hday = c.getHistoricalDay();
            //System.out.println(hday);
            assert hday >= Environment.HISTORICAL_DAY_FIRST && hday <= Environment.HISTORICAL_DAY_LAST : "Bad historical day";
        }

        // Test a single car movement, no time update
        System.out.println("---Test a single car, no time---");
        r = new Route(0, 0, 0);
        c = new Car(1, r, 0);
        System.out.print(c.emit());
        while (c.getSegFromPosition() <= c.route.getExitSegIndex()) {
            c.moveCar();
            System.out.print(c.emit());
        }

        // Test 10 cars moving, no time update
        System.out.println("---Test 10 cars, no time---");
        int numTestCars = 10;
        Random rand = new Random();
        List<Car> cars = new ArrayList<>();
        for (int i = 0; i < numTestCars; i++) {
            r = new Route(rand.nextInt(Environment.NUM_SEGMENTS), rand.nextInt(2), rand.nextInt(Environment.NUM_XWAYS));
            c = new Car(i, r, 0);
            cars.add(c);
            System.out.print(c.emit());
        }
        // --
        boolean carsStillRunning = true;
        while (carsStillRunning) {
            carsStillRunning = false;
            for (Car cr : cars) {
                //if (cr.getSegFromPosition() <= cr.route.getExitSegIndex()) {
                if (!cr.canRemove) {
                    cr.moveCar();
                    System.out.print(cr.emit());
                    carsStillRunning = true;
                }
            }
        }

        System.out.println("---Test for SIM_LENGTH---");
        cars = new ArrayList<>();
        int carId = 0;
        Iterator<Car> iter;
        // For each second of the simulation insert BASE_CARS_PER_UPDATE_INTERVAL_PER_XWAY
        //for (int i = 0; i < Environment.SIM_LENGTH; i++) {
        for (int i = 0; i < 100; i++) {
            // Introduce new cars at this time
            for (int j = 0; j < (Math.random() * Environment.BASE_CARS_PER_UPDATE_INTERVAL_PER_XWAY); j++) {
                r = new Route(rand.nextInt(Environment.NUM_SEGMENTS), rand.nextInt(2), rand.nextInt(Environment.NUM_XWAYS));
                c = new Car(carId++, r, i);
                cars.add(c);
            }
            // Update and emit from all cars if their TIME_INTERVAL marks are hit
            iter = cars.iterator();
            while (iter.hasNext()) {
                Car cr = iter.next();
                if (((cr.currTime - cr.startTime) % Environment.TIME_INTERVAL) == 0) {

                    // DEBUG - for speed changes
                    if (cr.getId() == 1) {
                        System.out.print(cr.emit());
                    }
                    if (cr.canRemove) {
                        iter.remove();
                    }
                    // DEBUG END
                }

                cr.moveCar();
                cr.updateTime(Environment.UPDATE_INTERVAL);
            }
        }
    }
}
