package com.walmart.linearroad.generator;

import java.util.*;

/**
 * Parse command line arguments.  Test commit.
 * Created by Sung Kim on 8/8/16.
 */
public class CLParser {
    /**
     *
     */
    private static Set<String> allowedFlags;

    static {
        allowedFlags = new HashSet<>();
        allowedFlags.add("-x"); // Number of expressways.
        allowedFlags.add("-o"); // Output file name.
        allowedFlags.add("-m"); // Multi-thread
        //////////////////////////////////////////////////////////////////
        // HDFS: comment out if not needed to remove hadoop dependencies
        //allowedFlags.add("-h"); // Hadoop output file name.  Should be mutually exclusive with -o.
        // HDFS: END
        //////////////////////////////////////////////////////////////////
    }

    private static Set<String> mutuallyExclusiveFileFlags;

    static {
        mutuallyExclusiveFileFlags = new HashSet<>();
        mutuallyExclusiveFileFlags.add("-o");
        mutuallyExclusiveFileFlags.add("-h");
    }

    /**
     * Ensure all command line flags are valid.
     * Flags must precede arguments.
     * All arguments must have a flag.
     * No standalone flags without an argument.
     *
     * @param args  the command-line arguments
     * @return      a List of Strings with the invalid flags
     */
    private static List<String> checkCommandLineArgs(String[] args) {
        List<String> invalidFlags = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (i % 2 == 0) {
                if (!allowedFlags.contains(args[i])) {
                    invalidFlags.add(args[i]);
                }
            }
        }
        return invalidFlags;
    }

    /**
     * Ensure that mutually exclusive flags are not present.
     *
     * @param flagMap   a Map of the valid flags parsed from the command-line args
     * @return          the List<String> of mutually exclusive flags
     */
    private static List<String> checkMutuallyExclusiveFlags(Map<String, String> flagMap) {
        List<String> exclusiveFlags = new ArrayList<>();

        // DEBUG
        //flagMap.keySet().forEach(System.out::println);
        // DEBUG END

        if (flagMap.keySet().containsAll(mutuallyExclusiveFileFlags)) {
            exclusiveFlags.addAll(mutuallyExclusiveFileFlags);
        }
        return exclusiveFlags;
    }

    /**
     * Simple func to print error messages from flags and exit.
     *
     * @param invalidFlags  the List<String> of invalid flags to print
     */
    private static void printInvalidFlags(List<String> invalidFlags) {
        StringBuilder sb = new StringBuilder();
        System.err.print("Invalid flags: ");
        invalidFlags.forEach((flag) -> {
            sb.append(flag + ", ");
        });
        System.err.print(new StringBuilder(sb.substring(0, (sb.lastIndexOf(",")))));
        System.err.println(" used.");
        System.exit(1);
    }

    /**
     * Simple func to print error messages from mutually exclusive flags and exit.
     *
     * @param mutuallyExclusiveFlags
     */
    private static void printMutuallyExclusiveFlags(List<String> mutuallyExclusiveFlags) {
        StringBuilder sb = new StringBuilder();
        System.err.print("The following flags are mutually exclusive: ");
        mutuallyExclusiveFlags.forEach((flag) -> {
            sb.append(flag + ", ");
        });
        System.err.print(new StringBuilder(sb.substring(0, (sb.lastIndexOf(",")))));
        System.err.println(".");
        System.exit(1);
    }

    /**
     * Look for flags and their associated value.
     * If no value follows a flag throw an error.
     *
     * @param args  the command line argument String[]
     * @return      a Map<String, String> of flags and their values
     */
    public static Map<String, String> parseCommandLine(String[] args) {
        Map<String, String> argMap = new HashMap<>();

        // Only bother with non-default args if the number of command-line args are >= 2.
        if (args.length == 1 && allowedFlags.contains(args[0])) {
            System.err.println(args[0] + " requires an argument.");
            System.exit(1);
        }
        if (args.length == 1 && !allowedFlags.contains(args[0])) {
            System.err.println(args[0] + " is an invalid argument.");
            System.exit(1);
        }
        if (args.length > 1) {
            List<String> invalidFlags = checkCommandLineArgs(args);
            if (invalidFlags.size() > 0) {
                printInvalidFlags(invalidFlags);
            }
        }

        // Parse into Map<String, String>
        for (int i = 0; i < args.length; i += 2) {
            argMap.put(args[i], args[i + 1]);
        }

        // Check for mutually exclusive
        List<String> mutuallyExclusiveFlags = checkMutuallyExclusiveFlags(argMap);
        if (mutuallyExclusiveFlags.size() > 0) {
            printMutuallyExclusiveFlags(mutuallyExclusiveFlags);
        }
        for (String k : argMap.keySet()) {
            System.out.println(argMap.get(k));
        }

        return argMap;
    }

    public static void main(String[] args) {
        Map<String, String> argMap = parseCommandLine(args);
        for (String k : argMap.keySet()) {
            System.out.println(k + "," + argMap.get(k));
        }
    }
    // TODO: Doesn't yet account for java <> -o -h // Where -h becomes the arg for -o.
}
