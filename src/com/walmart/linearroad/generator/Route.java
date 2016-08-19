package com.walmart.linearroad.generator;

import java.util.Random;

/**
 * The route a Car takes.
 * <p>
 * A car will be assigned a route.
 * The route will determine where a car gets on, gets off, and its desired destination.
 * More details can be added to take into account a more complex simulation world.
 * <p>
 * Created by Sung Kim on 6/28/2016.
 */
public class Route {
    private int entranceSegIndex; // 0-99 with Environment.NUM_SEGMENTS == 100
    private int exitSegIndex; // 0-99 - Same.  This will be generated, based on direction and entranceSegIndex.
    private int dir; // The direction
    private int xway;

    // A true simulator would allow a car to change expressways right away, i.e. 405 to the 5
    public Route(int entranceSegIndex, int dir, int xway) {
        this.entranceSegIndex = entranceSegIndex;
        this.dir = dir;
        this.xway = xway;
        exitSegIndex = createExitSegIndex();
    }

    /**
     * This does the meat of the calculations to determine a desired exit.
     * If the center is 'downtown' then you'll have more cars exiting there.  Future, 'full-world' simulations can
     * take into account time-of-day to have flows heavier FROM downtown to 'suburbs' at appropriate times.
     * The segments closer to the xway boundaries/edges, i.e. closer to 0 and 99 if there are 100 segments, are the
     * suburbs.
     * And then, more cars can enter from the downtown after 5:00 pm and exit in a suburb.
     *
     * @return  the exit segment number for this Route and the Car it will be embedded in
     */
    private int createExitSegIndex() {
        Random r = new Random();
        int exitSeg = -1;

        if (dir == 0) {
            exitSeg = r.nextInt(Environment.NUM_SEGMENTS - entranceSegIndex) + (entranceSegIndex + Environment.ROUTE_BUFFER);
            if (exitSeg > Environment.NUM_SEGMENTS - 1) exitSeg = Environment.NUM_SEGMENTS - 1; // No longer necessary.
        } else {
            exitSeg = r.nextInt(Environment.NUM_SEGMENTS - (Environment.NUM_SEGMENTS - entranceSegIndex) + 1) - Environment.ROUTE_BUFFER;
            if (exitSeg < 0) exitSeg = 0; // No longer necessary.
        }

        // Greater than 1/3 and less than 2/3 is 'downtown' and increases the odds of a car exiting in an segment that's in 'downtown'
        if (!isInDowntown() && isBeforeDowntown()) {
            //int chance = r.nextInt(100); // 0-99
            //if (chance < 80) { // Exit in downtown , 75% chance, 0-74
            if (Math.random() < .8) {
                exitSeg = r.nextInt(Environment.DOWNTOWN_END - Environment.DOWNTOWN_START) + Environment.DOWNTOWN_START;
            }
        }
        return exitSeg;
    }

    /**
     * What constitutes 'downtown' can be whichever segments are desired. At the moment it will be more
     * formulaic as the middle 1/3 of the available segments.
     * >= (middle third) <= is part of downtown.
     *
     * @return  whether this segment is part of 'downtown'
     */
    private boolean isInDowntown() {
        if (entranceSegIndex >= Environment.DOWNTOWN_START &&
                entranceSegIndex <= Environment.DOWNTOWN_END) {
            return true;
        }
        return false;
    }

    /**
     * We only want to consider increasing the odds of getting off in downtown if we're 'before' downtown per our dir.
     *
     * @return
     */
    private boolean isBeforeDowntown() {
        if (dir == 0 && entranceSegIndex < Environment.DOWNTOWN_START) return true;
        if (dir == 1 && entranceSegIndex > Environment.DOWNTOWN_END) return true;
        return false;
    }


    /**
     * @return  the direction of the Route
     */
    public int getDir() {
        return dir;
    }

    /**
     * @return  the xway of the Route
     */
    public int getXway() {
        return xway;
    }

    /**
     * @return  the entrance segment of the Route
     */
    public int getEntranceSegIndex() {
        return entranceSegIndex;
    }

    /**
     * @return  the exit segment of the Route
     */
    public int getExitSegIndex() {
        return exitSegIndex;
    }


    /**
     * Test Route
     * Created by Sung Kim on 6/28/2016.
     */
    public static void main(String[] args) {
        // Test Route
        Route r = new Route(0, 0, 0);
        assert r.isBeforeDowntown() : "Route seg 0, dir 0 isBeforeDowntown failed when before downtown.  Dir 0.";
        r = new Route(99, 0, 0);
        assert !r.isBeforeDowntown() : "Route seg 99, dir 0 !isBeforeDowntown failed when actually after downtown.  Dir 0";
        r = new Route(99, 1, 0);
        assert r.isBeforeDowntown() : "Route seg 99, dir 1 isBeforeDowntown failed when actually after downtown.  Dir 1";
        r = new Route(0, 1, 0);
        assert !r.isBeforeDowntown() : "Route seg 0, dir 1 !isBeforeDowntown failed when actually after downtown.  Dir 1";
        r = new Route(33, 0, 0);
        assert !r.isBeforeDowntown() : "Route seg 33, dir 0 !isBeforeDowntown failed when actually is in downtown.  Dir 0";
        assert r.isInDowntown() : "Route seg 33, dir 0 isInDowntown() failed.";
        r = new Route(66, 0, 0);
        assert !r.isBeforeDowntown() : "Route seg 66, dir 0 !isBeforeDowntown failed when actually is in downtown.  Dir 0";
        assert r.isInDowntown() : "Route seg 66, dir 0 isInDowntown() failed.";
        r = new Route(33, 1, 0);
        assert !r.isBeforeDowntown() : "Route !isBeforeDowntown failed when actually is in downtown.  Dir 1";
        assert r.isInDowntown() : "Route seg 66, dir 1 isInDowntown() failed.";
        r = new Route(66, 1, 0);
        assert !r.isBeforeDowntown() : "Route !isBeforeDowntown failed when actually is in downtown.  Dir 1";
        assert r.isInDowntown() : "Route seg 66, dir 1 isInDowntown() failed.";
        r = new Route(32, 0, 0);
        assert r.isBeforeDowntown() : "Route seg 32, dir 0 isBeforeDowntown failed.";
        assert !r.isInDowntown() : "Route seg 32, dir 0 !isInDowntown failed!";
        r = new Route(67, 0, 0);
        assert !r.isBeforeDowntown() : "Route seg 67, dir 0 !isBeforeDowntown failed.";
        assert !r.isInDowntown() : "Route seg 67, dir 0 !isInDowntown failed!";
        r = new Route(32, 1, 0);
        assert !r.isBeforeDowntown() : "Route seg 32, dir 1 isBeforeDowntown failed.";
        assert !r.isInDowntown() : "Route seg 32, dir 1 !isInDowntown failed!";
        r = new Route(67, 1, 0);
        assert r.isBeforeDowntown() : "Route seg 67, dir 1 isBeforeDowntown failed.";
        assert !r.isInDowntown() : "Route seg 67, dir 0 !isInDowntown failed!";
        r = new Route(Environment.NUM_SEGMENTS - 1, 0, 0);
        assert r.createExitSegIndex() == Environment.NUM_SEGMENTS - 1 : "Route seg (n_segs-1), dir 0 does not have exit seg index of (n_segs-1)";
        r = new Route(0, 1, 0);
        assert r.createExitSegIndex() == 0 : "Route seg 0, dir 1 does not have exit seg index of 0";

        for (int i = 0; i < Environment.NUM_SEGMENTS; i++) {
            int dir = (Math.random() < 0.5 ? 0 : 1);
            r = new Route(i, dir, 0);
            System.out.println(i + "," + r.isInDowntown() + "," + dir + "," + r.createExitSegIndex());
        }
    }
}
