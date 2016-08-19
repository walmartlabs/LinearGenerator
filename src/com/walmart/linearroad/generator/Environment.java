package com.walmart.linearroad.generator;

import java.util.*;

/**
 * This is the world, and its constants. Better set in a properties file.
 * The settings below create:
 * ~1 GB per xway files with
 * ~25M records per file
 * ~370K unique cars per xway
 * Each xway takes roughly 5m30s to create.
 *
 * <p>
 * Created by Sung Kim on 6/28/2016.
 */
public class Environment {
    /**
     * The number of expressways in the simulation.  No longer final.
     */
    public static int NUM_XWAYS = 1;

    /**
     * The number of segments in the simulation.
     */
    public static final int NUM_SEGMENTS = 100;

    /**
     * The length, in feet, of a segment.  Defaults to 1 mile with 5280 feet.
     */
    public static final int FEET_PER_SEGMENT = 5280;

    /**
     * Where downtown starts in this expressway.
     * This assumes every expressway has a 'downtown' section.
     * Set to 1/3 from the beginning of 0-based start of an expressway.
     */
    public static final int DOWNTOWN_START = (int) (NUM_SEGMENTS / 3.0);

    /**
     * Where downtown ends in this expressway.
     * This assumes every expressway has a 'downtown' section.
     * Set to 1/3 from the end of a 0-based expressway.
     */
    public static final int DOWNTOWN_END = (int) (NUM_SEGMENTS * 2 / 3.0);

    /**
     * The default entrance lane.
     */
    public static final int ENTRANCE_LANE = 0;

    /**
     * The number of lanes.  Affects how many Cars can travel without adjustments to speed in a segment.
     */
    public static final int NUM_LANES = 5;

    /**
     * The exit lane of an expressway.  This will always be the highest numbered lane: 0-indexed.
     */
    public static final int EXIT_LANE = NUM_LANES - 1;

    /**
     * The max possible speed in the simulation.
     */
    public static final int MAX_SPEED = 100;

    /**
     * The max speed of cars in the entrance lane of an expressway.
     */
    public static final int MAX_SPEED_ENTRANCE = 40;

    /**
     * The start speed of cars when they first appear in the entry lane, in an expressway, whether it be its first
     * appearance in the simulation or for a reentrant car.
     */
    public static final int ENTRY_SPEED = 10;

    /**
     * The max speed of cars in the exit lane of an expressway.
     */
    public static final int MAX_SPEED_EXIT = 10;

    /**
     * The given update interval for the simulation.  Default 1 second/tick.  Updates happen every UPDATE_INTERVAL.
     */
    public static final int UPDATE_INTERVAL = 1;

    /**
     * This represents how often cars emit reports.
     */
    public static final int TIME_INTERVAL = 30;

    /**
     * This represents the likelihood of an exiting Car of coming back onto an expressway later in the simulation.
     */
    public static final double REENTRANT_PERCENT = 0.10;

    /**
     * This is the base number of cars we put into the expressway per UPDATE_INTERVAL.
     */
    public static final int BASE_CARS_PER_UPDATE_INTERVAL_PER_XWAY = 20;

    /**
     * For creating the historical (type 3) file. Starts at day 1 because day 0 is the current day and should be
     * answered by type 2 queries.
     */
    public static final int HISTORICAL_DAY_FIRST = 1;

    /**
     * For creating the historical (type 3) file. 69 represents 10 weeks.
     */
    public static final int HISTORICAL_DAY_LAST = 69;

    /**
     * The target speed of cars traveling in travel lanes.
     */
    public static final int SPEED_LIMIT = 70;

    /**
     * A small adjustment to speed while changing lanes.
     */
    public static final int MAX_LANE_CHANGE_SPEED_DELTA = 5;

    /**
     * How often cars change lanes while traveling.
     */
    public static final double TRAVEL_LANE_CHANGE_PROBABILITY = 0.4;

    /**
     * A little wiggle in speed to provide variety.
     */
    public static final int SPEED_FUDGE = 10;

    /**
     * Acceleration of Cars while entering an expressway.
     * In Feet/sec^2
     */
    public static final int ACCELERATION_ENTRANCE = 10;

    /**
     * Acceleration of Cars while traveling in travel lanes.
     * In Feet/sec^2
     */
    public static final int ACCELERATION_TRAVEL = 5;

    /**
     * How many seconds to run the simulation: from [0 - SIM_LENGTH)
     */
    public static final int SIM_LENGTH = 10800;

    /**
     * How many cars can safely fit into a single lane without any affects on traffic
     */
    public static final int FULL_SPEED_CARS_PER_LANE = 24;

    /**
     * How long to wait from start of simulation, or since last Accident, before allowing an Accident
     * In minutes
     */
    public static final int ACCIDENT_INTERVAL = 10;

    /**
     * How long to wait before potentially clearing an Accident
     * In minutes
     */
    public static final int ACCIDENT_CLEAR_WAIT_TIME = 10;

    /**
     * Offset for where to place Cars in an expressway.  Makes Cars avoid entering at the ends of the expressway.
     * So, for a 100 segment expressway cars going direction 0 will enter anywhere from [0 - 97], and cars going
     * direction 1 will enter anywhere from [99-2], if the ROUTE_BUFFER is 2.
     * <p>
     * Also plays a part in deciding on the exit segment (Route.createExitSegIndex) as the exit lane will be at least
     * ROUTE_BUFFER distance away from the entrance segment.
     */
    public static final int ROUTE_BUFFER = 2;

    /**
     * Should generally be set to >= 0.50 as it is multiplied by Math.random().
     */
    public static double CAR_MULTIPLIER_FACTOR = 0.8;


    /**
     * A general function to create a Route to place into a Car (move to
     */
    public static Route createRoute() {

        // DEBUG
        //System.out.println("createRoute:Environment.NUM_XWAYS: " + Environment.NUM_XWAYS);
        // DEBUG END

        Random rand = new Random();
        Route r;
        int dir = rand.nextInt(2);
        if (dir == 0) {
            r = new Route(rand.nextInt(Environment.NUM_SEGMENTS - Environment.ROUTE_BUFFER), dir, rand.nextInt(Environment.NUM_XWAYS));
        } else {
            r = new Route(rand.nextInt(Environment.NUM_SEGMENTS - Environment.ROUTE_BUFFER) + Environment.ROUTE_BUFFER, dir, rand.nextInt(Environment.NUM_XWAYS));
        }
        return r;
    }

    /**
     * The 3-d List of List of List of TrafficConditions to hold all the traffic conditions during the simulation.
     * Thus a TrafficCondition could also have been called a Segment, or it could have been embedded in a Segment.
     */
    public static List<List<List<TrafficCondition>>> trafficConditions;

    /**
     * The NUM_XWAYS can be changed via a command-line so use a static initialization function vs. a block.
     */
    public static void initializeTrafficConditions() {
        // This is a NUM_XWAYS * NUM_SEGMENTS * NUM_DIRS 3-D array/List of TrafficCondition(s)
        int counter = 0;
        trafficConditions = new ArrayList<>();
        for (int i = 0; i < Environment.NUM_XWAYS; i++) {
            List<List<TrafficCondition>> xway = new ArrayList<>();
            trafficConditions.add(xway);
            for (int j = 0; j < Environment.NUM_SEGMENTS; j++) {
                List<TrafficCondition> seg = new ArrayList<>();
                xway.add(seg);
                for (int k = 0; k < 2; k++) { // 2 being the number of directions
                    TrafficCondition tc = new TrafficCondition(counter++);
                    seg.add(tc);
                }
            }
        }

        // DEBUG
        counter = 0;
        for (int i = 0; i < Environment.NUM_XWAYS; i++) {
            for (int j = 0; j < Environment.NUM_SEGMENTS; j++) {
                for (int k = 0; k < 2; k++) {
                    System.out.println(counter++ + ": " + trafficConditions.get(i).get(j).get(k));
                }
            }
        }
        //System.exit(1);
        // DEBUG END
    }

    /**
     * Use this to track time within the simulation.
     * Not used for anything at the moment that can't be tracked with the loop variable of the main simulation loop.
     */
    public static int simTime = 0;

    /**
     * Our separate list to hold XWay-specific data, which at this moment is only Accident presence and timings.
     */
    public static List<XWay> xways;
    public static void initializeXWays() {
        xways = new ArrayList<>();
        for (int i = 0; i < NUM_XWAYS; i++) {
            xways.add(new XWay(i));
        }
    }

    /**
     * Re-entrant cars: map to time they will re-enter and store the actual Car instances.
     */
    public static Map<Integer, List<Car>> reentrants;
    public static void initializeReentrants() {
        reentrants = new HashMap<>();
    }
    /**
     * Add a car to the reentrants array
     *
     * @param time
     * @param c
     */
    public static void addToReentrants(int time, Car c) {
        synchronized (Environment.reentrants) {
            Environment.reentrants.putIfAbsent(time, new ArrayList<>());
            Environment.reentrants.get(time).add(c);
        }
    }

    // TODO: Most of the settings should, or can, be moved to config or properties file.

    /**
     * A convenience function for converting minutes to seconds
     *
     * @param min
     * @return
     */
    public static int minToSec(int min) {
        return min * 60;
    }


}
