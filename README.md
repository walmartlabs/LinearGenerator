# LinearGenerator
Reworked data generator for LinearRoad streaming benchmark that no longer needs mitsim or any database.

To use the new generator compile the files and run: `java com.walmart.linearroad.generator.LinearGen [-o <output file>] [-x <number of xways>] [-m <dummy value to activate multi-threading>]`

The default settings in Environment.java create:
* ~1 GB per xway files with
* ~25M records per file
* ~370K unique cars per xway

Each xway takes roughly 5m30s to create.
