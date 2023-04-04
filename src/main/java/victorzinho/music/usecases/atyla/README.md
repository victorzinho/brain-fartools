# Atyla - Generating harmony from a trip

In 2019 I sailed on board the tall ship [Atyla](https://atyla.org/) from Norway to Denmark. And when I came back I 
decided to turn that into music somehow.

The idea behind this is to place a hexagonal grid over some part of the world (for GIS geeks, the hex grid is generated 
in UTM so if "some part of the world" is big you have to switch between CRSs, overlapping hexagons, etc.
A fun problem to solve). Then define notes for all cells in the grid ([Tonnetz](https://en.wikipedia.org/wiki/Tonnetz)-like)
and take some points that represent a trajectory over that grid to generate your harmony.
Rules for the generated harmony are available in the [javadoc](MusicHarmonyGenerator.java#L47).

Reusability for this one is complex since the data might come in different shapes and sizes (I aggregated the ship's
position from [MarineTraffic](https://www.marinetraffic.com/) and YellowBrick, in different json formats for example).
Plus files can be big and the browser didn't seem the best option at the time I wrote it.

To reuse:
- Define your [grid](Main.java#L16).
- Define your point data ([extending PointData](input/VesselData.java)) with the extra values you might want to use.
  It needs to have at least lon/lat positions and timestamps. If that's the only relevant data you don't need to extend
  anything. 
- Somehow read it into a list of your (potentially extended) point data class.
- Write some [adapter](input/VesselDataInterpolationAdapter.java) for the interpolation to be able to take
  the relevant values and build back point data (couldn't find a nicer option, sorry).
- Define [extra attributes](input/VesselDataCalculations.java) to write as separate parts in the score, if needed.
- Wire it all together, or rewrite the existing [Main](Main.java) class.
- Profit (as MusicXML file).
