# Raster - Generating music lines from a path over a raster

From 2018 to 2022 I worked at [xarvio](https://xarvio.com) where part of my work was to process inputs from agricultural
machinery to produce nice maps. And obviously I wanted to make that into music as well :)

This one is about taking a raster and some set of points representing a way in that raster to produce a music line
by classifying the values of the raster in the given points (highly customizable in terms of number of lines, 
available pitch classes and pitch class sets for each line, classification methods, etc.). Maybe the easiest way to
reproduce is some digital elevation model and the GPS points of your latest bike trip.

Again, reusability for this one is complex, so it will need a few steps.

To reuse:
- Find your raster (or rasters) and read them into GridCoverage2D with geotools.
- Find your vector points and read them into SimpleFeatureCollection with geotools.
- Define the way to generate the music lines [for each raster](Main.java#L28). Things to decide:
  - The overall available pitches to obtain from the pitch raster.
  - Whether to restrict them to a single pitch class set, a collection of pitch class sets (a single pitch class set
    will be chosen for each position based on a second raster that will have to be provided); or just let any pitch from
    the overall set mentioned above to appear on the score at any point.
  - The classification method for each of the coverages (pitch class and pitch class set). Note: The classification 
    of values takes into account only the values of the raster(s) where they intersect with the vector points.
    Considering the whole raster (or a ROI around the points) would probably not benefit the output and will make it
    slower and more complex.
- Profit (as MusicXML file).

Note: it works with pitch classes; i.e., the octaves are arbitrary. I found that degree of freedom important to make
something interesting while eventually working on the music itself with the score.

Note: it took me a bit to make generic code for this that could be reasonably readable and reusable.
Time that I didn't spend testing, so it might not work exactly as explained above :)