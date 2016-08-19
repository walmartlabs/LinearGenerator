package com.walmart.linearroad.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * The main class that sets up the environment, creates the Cars, and moves everything along.
 * <p>
 * Created by Sung Kim on 6/28/2016.
 */
public class LinearGen {

    /**
     * Determine how many cars should/can be introduced into the simulation at given seconds based on
     * how long the simulation is, smoothing out the number of cars to match a sin wave from 0 - 180 degrees.
     * We normalize based on how many segments are made from 180 degrees and the number seconds.
     *
     * @param time the simulation time, in seconds.  I.e. 3 hours will be 10800 seconds.  The total simulation time is
     *             taken from Environment.SIM_LENGTH
     * @return the multiplier for the Environment.BASE_CARS_PER_UPDATE_INTERVAL_PER_XWAY
     */
    private static double getCarMultiplier(int time) {
        double step = 180.0 / Environment.SIM_LENGTH;
        return Math.sin(Math.toRadians(time * step)) + 1 * (Environment.CAR_MULTIPLIER_FACTOR + Math.random());
    }

    /**
     * Each expressway should have an accident roughly every 20 minutes, either direction.
     * The accident should clear in 10-20 minutes (600-1200 seconds).
     * <p>
     * Stop one Car and then find a candidate car in the same xway+seg+dir by investigating the TrafficCondition
     * in which the Car is traveling.
     * <p>
     * Checking for a car to see if it could be part of an Accident created Accidents at exactly the times it could.
     * So, run per tick of the clock per XWay, not per Car per tick.
     * The same Accident is redundantly stored in a TrafficCondition and an XWay.
     * This is silly.  Why redundantly store the Accident?  Just have traffic conditions refer to accidents in the Xways.
     *
     * @param cars     the Cars List to investigate to see if an accident can be created with it and its xway-seg-dir
     * @param currTime the current world time
     */
    private static void checkToCreateOrClearAccident(List<Car> cars, int currTime) {
        for (XWay xway : Environment.xways) {
            checkToCreateOrClearAccidentMT(xway.getXWayNumber(), cars, currTime);
            /*
            if (xway.hasAccident()) {
                // Remember, the Accident reference in the TrafficCondition is only for adjusting traffic patterns.
                //if (currAccDuration > Environment.minToSec(Environment.ACCIDENT_CLEAR_WAIT_TIME) + (Math.random() * Environment.minToSec(10))) {
                if (currTime > xway.getClearAccidentTime()) {
                    Accident acc = xway.getAccident();

                    // DEBUG
                    System.out.println("Accident cleared. " + xway.getXWayNumber() + "," + currTime + "," + acc.getC1().getId() + "," + acc.getC2().getId());
                    // DEBUG END

                    clearAccident(xway.getXWayNumber(), currTime);
                    // Loop through all xways to see if there is an Accident.
                }
            }

            // See if it's been long enough since the last Accident to potentially add an accident.
            if (currTime > xway.getNextAccidentTime() && !xway.hasAccident()) {

                // Loop through the cars in the simulation that is in this XWay
                // to see if one fits the bill to create an Accident.
                for (Iterator<Car> iter = cars.iterator(); iter.hasNext(); ) {
                    Car c = iter.next();

                    if (c.hasBeenInAccident()) continue;

                    int xwayIndex = c.getCurrXway();
                    if (xwayIndex != xway.getXWayNumber()) continue;
                    int seg = c.getCurrSegment();
                    int dir = c.getCurrDir();

                    // Try creating an Accident.
                    Accident acc = createAccident(xwayIndex, seg, dir, c);

                    // DEBUG
                    //System.out.println("Could have had accident. " + c.getId() + "," + c.getCurrTime() + "," + wiggle + "," + timeSinceAccidentForThisXway);
                    // DEBUG END

                    if (acc != null) {  // Remember, there's no guarantee a suitable second car will be found.
                        // If an Accident is created/creatable set it in the appropriate TrafficCondition AND XWay.
                        xway.turnOnAccident(acc);

                        // DEBUG
                        System.out.println("Accident created. " + xway.getXWayNumber() + "," + currTime + "," + acc.getC1().getId() + "," + acc.getC2().getId());
                        System.out.println("Accident info: " + seg + "," + dir + "," + acc.getC1().getCurrPosition() + "," + acc.getC2().getCurrPosition());
                        // DEBUG END

                        // Stop at one Accident for each expressway
                        break;
                    }
                }
            }*/
        }
    }

    // MT
    private static void checkToCreateOrClearAccidentMT(int x, List<Car> cars, int currTime) {
        XWay xway = Environment.xways.get(x);
        if (xway.hasAccident()) {
            // Remember, the Accident reference in the TrafficCondition is only for adjusting traffic patterns.
            //if (currAccDuration > Environment.minToSec(Environment.ACCIDENT_CLEAR_WAIT_TIME) + (Math.random() * Environment.minToSec(10))) {
            if (currTime > xway.getClearAccidentTime()) {
                Accident acc = xway.getAccident();

                // DEBUG
                System.out.println("Accident cleared. " + xway.getXWayNumber() + "," + currTime + "," + acc.getC1().getId() + "," + acc.getC2().getId());
                // DEBUG END

                clearAccident(xway.getXWayNumber(), currTime);
                // Loop through all xways to see if there is an Accident.
            }
        }

        // See if it's been long enough since the last Accident to potentially add an accident.
        if (currTime > xway.getNextAccidentTime() && !xway.hasAccident()) {

            // Loop through the cars in the simulation that is in this XWay
            // to see if one fits the bill to create an Accident.
            for (Iterator<Car> iter = cars.iterator(); iter.hasNext(); ) {
                Car c = iter.next();

                if (c.hasBeenInAccident()) continue;

                int xwayIndex = c.getCurrXway();
                if (xwayIndex != xway.getXWayNumber()) continue;
                int seg = c.getCurrSegment();
                int dir = c.getCurrDir();

                // Try creating an Accident.
                Accident acc = createAccident(xwayIndex, seg, dir, c);

                // DEBUG
                //System.out.println("Could have had accident. " + c.getId() + "," + c.getCurrTime() + "," + wiggle + "," + timeSinceAccidentForThisXway);
                // DEBUG END

                if (acc != null) {  // Remember, there's no guarantee a suitable second car will be found.
                    // If an Accident is created/creatable set it in the appropriate TrafficCondition AND XWay.
                    xway.turnOnAccident(acc);

                    // DEBUG
                    System.out.println("Accident created. " + xway.getXWayNumber() + "," + currTime + "," + acc.getC1().getId() + "," + acc.getC2().getId());
                    System.out.println("Accident info: " + seg + "," + dir + "," + acc.getC1().getCurrPosition() + "," + acc.getC2().getCurrPosition());
                    // DEBUG END

                    // Stop at one Accident for each expressway
                    break;
                }
            }
        }
    }
    // MT END

    /**
     * Set the current car as an Accident car.  (A 'Stopped' Car.)
     * Find another car with this xway+seg+dir to stop at the same place the current car is stopped.
     * Update the XWay.
     * Update the associated TrafficCondition.
     * <p>
     * Passing in the xway, seg, dir is redundant since the information is all found in a car anyways.
     * But, for some obsessive compulsive reason I'm passing them in anyways.
     *
     * @param xway the current expressway
     * @param seg  the current segment
     * @param dir  the current direction
     * @param c    the Car that will be the "first" car in the Accident
     */
    private static Accident createAccident(int xway, int seg, int dir, Car c) {
        Accident newAccident = null;
        if (!isInTravelLane(c)) {
            return null;
        }
        Car c2 = findCarWithin1000FeetOfCurrCar(xway, seg, dir, c);
        if (c2 != null) {  // If there are no other cars that fit the bill no Accident can be created.
            c.stopCar();
            c2.stopCar();
            // Move c2, magically, to c's position and lane
            c2.targetStoppedCar(c);

            // Create a new Accident
            newAccident = new Accident(c, c2, c.getCurrTime(), seg, dir);
        }
        return newAccident;
    }

    /**
     * Find a car in the same xway+seg+dir, which is the same TrafficCondition.
     * Because a TrafficCondition holds an unordered Set of cars, this is inefficient at the moment.
     *
     * @param c
     * @return
     */
    /*
    public static Car findCarInSegmentBeforeCurrCar(int xway, int seg, int dir, Car c) {
        TrafficCondition tc = Environment.trafficConditions.get(xway).get(seg).get(dir);
        Iterator<Car> cars = tc.getCarsIterator();
        Car tempCar;
        while (cars.hasNext()) {
            tempCar = cars.next();
            // Just pick the first one that fits the bill
            // In a travel lane
            // Before the first car in position (based on dir)
            if (carIsBeforeCar(c, tempCar) && isInTravelLane(tempCar)) {
                return tempCar;
            }
        }
        return null;
    }*/

    /**
     * Find a car in the same xway+seg+dir, which is in the same TrafficCondition and within 1000 feet of the first.
     * And maybe this is better adjusted to have an argument rather than a set distance.
     * Because a TrafficCondition holds an unordered Set of cars, this is inefficient at the moment.
     *
     * @param c the Car to check
     * @return the Car that is behind and within 1000 feet of the first Car
     */
    private static Car findCarWithin1000FeetOfCurrCar(int xway, int seg, int dir, Car c) {
        TrafficCondition tc = Environment.trafficConditions.get(xway).get(seg).get(dir);
        Iterator<Car> cars = tc.getCarsIterator();
        Car tempCar;
        while (cars.hasNext()) {
            // Just pick the first one that fits the bill
            tempCar = cars.next();
            if (c == tempCar) continue;
            // Is in a travel lane and
            // is within 1000 feet of the first car.
            if (carIsWithin1000FtOfCar(c, tempCar) && isInTravelLane(tempCar)) {
                return tempCar;
            }
        }
        return null;
    }


    /**
     * Helper function to check if one car is before another based on direction
     * Assumes that the two cars are in the same direction because they are in the same TrafficCondition.
     *
     * @param car1
     * @param car2
     * @return
     */
    /*
    public static boolean carIsBeforeCar(Car car1, Car car2) {
        // Pull the direction from either car.
        int dir = car1.getCurrDir();
        if (dir == 0) {
            if (car2.getCurrPosition() < car1.getCurrPosition()) {
                return true;
            }
        } else {
            if (car2.getCurrPosition() > car1.getCurrPosition()) {
                return true;
            }
        }
        return false;
    }*/

    /**
     * Check to see if Car2 is within 1000 feet of Car1.
     * Car1 has to be ahead of the Car2.
     *
     * @param car1 the first car
     * @param car2 the second car
     * @return they are within 1000, non-inclusive
     */
    private static boolean carIsWithin1000FtOfCar(Car car1, Car car2) {
        if (car1.getCurrDir() == 0) {
            if (car1.getCurrPosition() - car2.getCurrPosition() < 1000) {
                return true;
            }
        } else {
            if (car2.getCurrPosition() - car1.getCurrPosition() < 1000) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ensure the car is not in the entrance or exit lane.
     *
     * @param c the Car
     * @return whether the Car is in a travel lane
     */
    private static boolean isInTravelLane(Car c) {
        int currentLane = c.getCurrLane();
        if (currentLane > 0 && currentLane < Environment.NUM_LANES - 1) {
            return true;
        }
        return false;
    }

    /**
     * Clearing an Accident essentially equates to allowing the two accident cars to move again.
     * Set the two accident cars so that they can start moving again--set the appropriate flags.
     *
     * @param xway     the xway
     * @param currTime the current simulation time
     */
    private static void clearAccident(int xway, int currTime) {
        Environment.xways.get(xway).clearAccident(currTime);
    }

    /**
     * A separate function to do possible additional processing. None at the moment.
     *
     * @param iter
     * @param c
     */
    private static void removeCarFromActiveCars(Iterator<Car> iter, Car c) {
        iter.remove();
    }

    /**
     * Create the toll file using the maxCarId.
     * Use all the type 3's created during data file creation to fix the toll file while it's being created.
     *
     * @param maxCarId the max car id
     * @param type3map a map of type 3 notifications to accurately map xways in the newly created toll file
     * @param fileName the name of the data output file that will have ".tolls.dat" appended to it
     * @param flagMap  the map of command-line args, currently used to write to Hadoop directly if -h flag is present
     */
    private static void createTollFile(int maxCarId, String fileName, Map<String, Integer> type3map, Map<String, String> flagMap) {
        int carId, day, toll, xway;
        Random rand = new Random();
        PrintWriter writer = null;
        // Hadoop?
        if (flagMap != null && flagMap.containsKey("-h")) {
            //////////////////////////////////////////////////////////////////
            // HDFS: comment out if not needed to remove hadoop dependencies
            /*String hdfsHost = flagMap.get("-h").substring(0, flagMap.get("-h").indexOf("/", 8));
            System.out.println(hdfsHost);
            writer = HdfsWriter.getHdfsWriter(hdfsHost, flagMap.get("-h") + ".tolls.dat");*/
            // HDFS: END
            //////////////////////////////////////////////////////////////////
        } else {
            try {
                writer = new PrintWriter(fileName + ".tolls.dat");
            } catch (FileNotFoundException e) {
                System.err.println("There was a problem creating the output file.");
                System.err.println(e);
            }
        }
        maxCarId++; // we want to include the max carId in the result set
        for (carId = 0; carId < maxCarId; carId++) {
            for (day = 1; day < Environment.HISTORICAL_DAY_LAST + 1; day++) {
                toll = rand.nextInt(90) + 10;
                xway = type3map.getOrDefault(carId + "-" + day, rand.nextInt(Environment.NUM_XWAYS));
                writer.println(carId + "," + day + "," + xway + "," + toll);
            }
        }
        writer.close();
    }

    /**
     * Use the default path/name for the data file or use a user-supplied path/name.
     *
     * @param argMap parsed and Mapped command-line arguments
     * @return the filename for the output file
     */
    private static String getOutfileName(Map<String, String> argMap) {
        if (argMap != null && argMap.containsKey("-o")) {
            return argMap.get("-o");
        } else {
            return "car.dat";
        }
    }

    /**
     * Pass in the parsed and Mapped command-line to get the PrintWriter, writing to the argument to -o or -h.
     *
     * @param argMap parsed and Mapped command-line arguments
     * @return a PrinterWriter pointing to a file based on the argument to -o or -h
     */
    private static PrintWriter getWriter(Map<String, String> argMap) {
        String defaultOutfile = "car.dat";
        PrintWriter writer = null;
        if (argMap != null) {
            if (argMap.containsKey("-o")) {
                try {
                    writer = new PrintWriter(argMap.get("-o"));
                } catch (FileNotFoundException e) {
                    System.err.println("File " + argMap.get("-o") + " not found.  Using the default of ./car.dat");
                }
            }
            //////////////////////////////////////////////////////////////////
            // HDFS: comment out if not needed to remove hadoop dependencies
            /*if (argMap.containsKey("-h")) {
                // DEBUG
                String hdfsHost = argMap.get("-h").substring(0, argMap.get("-h").indexOf("/", 8));
                System.out.println(hdfsHost);
                // DEBUG END
                writer = HdfsWriter.getHdfsWriter(hdfsHost, argMap.get("-h"));
            }*/
            // HDFS: END
            //////////////////////////////////////////////////////////////////
        }
        // Just create the default out file 'car.dat' if possible and quit if it can't be created.
        if (writer == null) {
            try {
                writer = new PrintWriter(defaultOutfile);
            } catch (FileNotFoundException e) {
                System.err.println("File car.dat could not be created.  Exiting.");
                System.exit(1);
            }
        }
        return writer;
    }

    /**
     * Override the default number of expressways.
     *
     * @param argMap
     */
    private static void setNumberOfExpressways(Map<String, String> argMap) {
        // Override default number of expressways?
        if (argMap != null && argMap.containsKey("-x")) {
            Environment.NUM_XWAYS = Integer.parseInt(argMap.get("-x"));
            // DEBUG
            System.out.println("Environment.NUM_XWAYS: " + Environment.NUM_XWAYS);
            // DEBUG END
        }
    }

    // MT

    private static class XWayRunnable implements Runnable {
        List<Car> cars;
        int t; // currTime
        int xway;
        Map<String, Integer> type3map;
        PrintWriter writer;


        public XWayRunnable(int xway, int t, List<Car> cars, PrintWriter writer, Map<String, Integer> type3map) {
            this.xway = xway;
            this.cars = cars;
            this.t = t;
            this.type3map = type3map;
            this.writer = writer;
        }

        @Override
        public void run() {
            Iterator<Car> iter = cars.iterator();
            Car cr;
            while (iter.hasNext()) {
                cr = iter.next();

                if (((cr.getCurrTime() - cr.getStartTime()) % Environment.TIME_INTERVAL == 0)) {
                    // Emit pre-update to handle new Car creation and exits.
                    // In other words, emit happens for creation or the previous change.
                    String notification = cr.emit();
                    if (notification.contains("\n3")) {
                        String[] tokens = notification.trim().split("\n3")[1].trim().split(",");
                        synchronized (type3map) {
                            type3map.put(cr.getId() + "-" + tokens[14], cr.getCurrXway());
                        }
                    }
                    synchronized (writer) {
                        writer.print(notification);
                    }

                    // Take the time here to remove any cars that should be removed.
                    if (cr.isRemoveable()) {
                        removeCarFromActiveCars(iter, cr);
                    }
                }
                // Check if you want to create an Accident with this Car in its xway-seg-dir-position
                //checkToCreateOrClearAccident(cr);

                cr.moveCar();
                cr.updateTime(Environment.UPDATE_INTERVAL);
            }

            // Check to see if you can create an Accident
            checkToCreateOrClearAccidentMT(xway, cars, t);
        }
    }
    // MT END

    /**
     * Run the simulation to build the cars file.
     *
     * @param args
     */
    public static void main(String[] args) {
        //
        long startTime = System.nanoTime();

        // MT
        boolean MT = false;

        // Parse command-line args, if any.
        // Allow for command-line arguments for an outfile file but also to override some default settings
        // Only bother with non-default args if the number of command-line args are >= 2.
        Map<String, String> argMap = CLParser.parseCommandLine(args);
        if (argMap.get("-o") != null) {
            MT = true;
        }

        // Process command-line args if any.
        String outfileName = getOutfileName(argMap);
        PrintWriter writer = getWriter(argMap);

        setNumberOfExpressways(argMap);

        // Initialize static Environment variables.
        Environment.initializeTrafficConditions();
        Environment.initializeXWays();
        Environment.initializeReentrants();


        // The Map to hold type 3's.  Key: carId-day ; Value: xway.  Necessary to have accurate xways in the historical
        // toll file.
        Map<String, Integer> type3map = new HashMap<>();

        // Random generator to use throughout the main simulation.
        Random rand = new Random();

        // We do removals from arbitrary positions, so we use a LinkedList, not an ArrayList.
        List<Car> cars = new LinkedList<>();
        Route r;
        Car c;

        // For MT
        // We can create the data structure, but won't do anything with it if MT is not true.
        Map<Integer, List<Car>> xwayCars = new HashMap<>();
        for (int i = 0; i < Environment.NUM_XWAYS; i++) {
            xwayCars.put(i, new LinkedList<>());
        }
        // For MT END

        // Keep track of last assigned car id
        int carId = 0;

        // The iterator to iterate through cars in the simulation, once for each 'tick' or second.
        Iterator<Car> iter;
        // ---------------------------------------
        // THE MAIN SIMULATION LOOP
        // ---------------------------------------
        // For each second of the simulation insert BASE_CARS_PER_UPDATE_INTERVAL_PER_XWAY
        for (int i = 0; i < Environment.SIM_LENGTH; i++) {
            // DEBUG
            //System.out.println("carMultiplier: " + getCarMultiplier(i));
            // END - DEBUG

            // Insert a random number of cars up to BASE_CARS_PER_UPDATE_INTERVAL_PER_XWAY.  The multiplier follows a SIN curve from
            //   0 - 180, with amplitude 1
            for (int j = 0; j < getCarMultiplier(i) * Environment.BASE_CARS_PER_UPDATE_INTERVAL_PER_XWAY * Environment.NUM_XWAYS; j++) {
                // The range is effectively 2-97 so we don't get cars entering where they have no place to go but exit.
                r = Environment.createRoute();
                c = new Car(carId++, r, i);
                // MT
                if (MT) {
                    xwayCars.get(r.getXway()).add(c);
                } else {
                    cars.add(c);
                }
                // MT END
                // The car will emit below on creation time because emission happens before update and movement.
            }

            // Insert any classifying re-entrant cars
            if (Environment.reentrants.containsKey(i)) {
                // If there's a key, there's also going to be a List of Cars as the value
                List<Car> reCars = Environment.reentrants.get(i);
                // Reset the cars and put them in new xways, segs, and directions
                for (Car rc : reCars) {
                    // MT
                    if (MT) {
                        Car nc = rc.reEnter(i);
                        xwayCars.get(nc.getCurrXway()).add(nc);
                    } else {
                        cars.add(rc.reEnter(i));
                    }
                    // MT END
                }
                Environment.reentrants.remove(i);
            }

            // Insert a random shuffle to add some non-uniformity to the reports.

            // Target for multi-threading

            // Update all cars
            // MT
            if (MT) {
                List<Thread> threads = new ArrayList<>();
                for (int x : xwayCars.keySet()) {
                    List<Car> listOfCars = xwayCars.get(x);
                    //Thread t = new Thread(new XWayRunnable(x, i, listOfCars, writerMT, type3mapMT));
                    Thread t = new Thread(new XWayRunnable(x, i, listOfCars, writer, type3map));
                    t.start();
                    threads.add(t);
                }
                for (Thread t : threads) {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        System.err.println(e);
                        System.err.println("Error occurred while thread joining.");
                        System.exit(1);
                    }
                } // MT END
            } else {
                iter = cars.iterator();
                Car cr;
                while (iter.hasNext()) {
                    cr = iter.next();

                    if (((cr.getCurrTime() - cr.getStartTime()) % Environment.TIME_INTERVAL == 0)) {
                        // Emit pre-update to handle new Car creation and exits.
                        // In other words, emit happens for creation or the previous change.
                        String notification = cr.emit();
                        if (notification.contains("\n3")) {
                            String[] tokens = notification.trim().split("\n3")[1].trim().split(",");
                            type3map.put(cr.getId() + "-" + tokens[14], cr.getCurrXway());
                        }
                        writer.print(notification);

                        // Take the time here to remove any cars that should be removed.
                        if (cr.isRemoveable()) {
                            removeCarFromActiveCars(iter, cr);
                        }
                    }
                    // Check if you want to create an Accident with this Car in its xway-seg-dir-position
                    //checkToCreateOrClearAccident(cr);

                    cr.moveCar();
                    cr.updateTime(Environment.UPDATE_INTERVAL);
                }

                // Check to see if you can create an Accident
                checkToCreateOrClearAccident(cars, i);
            }


            // Update all xway times
            //for (int j = 0; j < Environment.xways.size(); j++) {
            //    Environment.xways.get(j).incrTime();
            //}
            // real	10m55.576s
            // user	11m7.693s
            // sys	0m11.224s
            //for (XWay xway : Environment.xways) {
            //  xway.incrTime();
            //}
            // real	10m50.421s (2xway)
            // user	11m3.918s
            // sys	0m10.840s
            // Using forEach and lambda's _can_ be faster, but it isn't always the case. Java >=8.
            //Environment.xways.forEach(xway -> xway.incrTime());
            // real	10m59.189s (2xway)
            // user	11m8.998s
            // sys	0m11.411s
            // Using the for each loop, available since Java 1.5 was marginally faster.

            // Update the world sim time
            Environment.simTime++;
        }
        writer.close();
        System.out.println("All notification data created.");
        System.out.printf("Time to create %d xways: %f\n", Environment.NUM_XWAYS, (System.nanoTime() - startTime) / 1000000000.0);

        // Use the max carId to create the toll file here.
        System.out.println("Creating the toll file ...");
        long tollStartTime = System.nanoTime();
        createTollFile(carId, outfileName, type3map, argMap);
        System.out.printf("Time to create toll data: %f\n", (System.nanoTime() - tollStartTime) / 1000000000.0);

        System.out.printf("Total time to create all data: %f\n", (System.nanoTime() - startTime) / 1000000000.0);

        System.out.println("---REPORT---");
        System.out.println("Number of cars created: " + carId);
        System.out.println("Number of queries created: " + Car.getQid());
        System.out.println("Number of type 0's created: " + Car.getT0s());
        System.out.println("Number of type 2's created: " + Car.getT2s());
        System.out.println("Number of type 3's created: " + Car.getT3s());
        System.out.println("Total Number of notifications created: " + Car.getTAll());
        System.out.println("---REPORT END---");
    }
}
