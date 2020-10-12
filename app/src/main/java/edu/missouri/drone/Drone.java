 package edu.missouri.drone;

 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;

 import edu.missouri.frame.Area;
 import edu.missouri.frame.Detectable;
 import edu.missouri.frame.Option;
 import edu.missouri.geom.Line;
 import edu.missouri.geom.Point;
 import edu.missouri.geom.Polygon;

 @SuppressWarnings("unused")
 public abstract class Drone {

     // From the DJI spec sheet on the Mavic Pro:
     public static final double ASPECT_RATIO             = 3.0/4.0;                           // ratio
     public static final double FOV_WIDTH                = Math.toRadians(78.8);              // radians
     public static final double FOV_HEIGHT               = 1.0/ASPECT_RATIO * FOV_WIDTH;      // radians
     public static final double MAX_TRAVEL_DISTANCE      = 12874.8;                           // meters
     public static final double ASCENT_SPEED             = 5.0;                               // meters per second
     public static final double DESCENT_SPEED            = 3.0;                               // meters per second
     public static final double MAX_SPEED                = 40;                                // meters per second
     public static final double MAX_FLIGHT_TIME          = 1620;                              // seconds
     public static final double REALISTIC_FLIGHT_TIME    = 1260;                              // seconds
     public static final double TOTAL_ENERGY             = 157183.2;                          // joules

     // The energy model we're using is for a different drone (the Iris), so we have to fit it to the Mavic Pro.
     // The Mavic is a much nicer drone - weighs half as much with higher quality motors and
     // This one is calculated such that the total travel distance given by DJI
     // costs about the total energy of the drone at the optimal speed (15.5 m/s, apparently)
     public static final double EFFICIENCY_FACTOR  = 0.4;


     private Point location;

     private double heading;
     public Area area;
     private List<Capture> captures;
     public List<Point> route;
     public List<Point> predecideWayPoints;
     public List<Paths> paths = new ArrayList<>();
     public List<Turning> turnings = new ArrayList<>();

     public Drone(Area area) {
         route = new ArrayList<>();
         reroute(area, Option.energyBudget);
     }

     protected class Capture {

         public List<Detectable> detectables;
         public Polygon polygon;
         public double height;

         public Capture(Polygon p, List<Detectable> d, double h) {
             this.polygon = p;
             this.detectables = d;
             this.height = h;
         }
     }


     protected Polygon getPolygon() {
         return area.toPolygon();
     }
     protected Point getTargetEnd() {
         return area.getEnd();
     }
     protected Point getLocation() {
         return location;
     }
     public double getHeading() {
         return heading;
     }
     public void setHeading(double heading) {
         this.heading = heading;
     }
     protected List<Capture> getCaptures() {
         return captures;
     }

     protected Capture scan() {
         // these are backwards for reasons
         double h = 2 * getLocation().z() * Math.tan(FOV_WIDTH/2);
         double w = 2 * getLocation().z() * Math.tan(FOV_HEIGHT/2);
         Polygon p = new Polygon(
                 new Point(getLocation().x() + w/2, getLocation().y() + h/2),
                 new Point(getLocation().x() + w/2, getLocation().y() - h/2),
                 new Point(getLocation().x() - w/2, getLocation().y() - h/2),
                 new Point(getLocation().x() - w/2, getLocation().y() + h/2)
         ).rotate(heading);
         List<Detectable> detected = area.getDetectables(p, getLocation().z());
         Capture c = new Capture(p, detected, getLocation().z());
         captures.add(c);
         return c;
     }

     protected Capture scan(double angle) {
         // these are backwards for reasons
         double h = 2 * getLocation().z() * Math.tan(FOV_WIDTH/2);
         double w = 2 * getLocation().z() * Math.tan(FOV_HEIGHT/2);
         setHeading(angle);
         Polygon p = new Polygon(
                 new Point(getLocation().x() + w/2, getLocation().y() + h/2),
                 new Point(getLocation().x() + w/2, getLocation().y() - h/2),
                 new Point(getLocation().x() - w/2, getLocation().y() - h/2),
                 new Point(getLocation().x() - w/2, getLocation().y() + h/2)
         ).rotate(heading);
         List<Detectable> detected = area.getDetectables(p, getLocation().z());
         Capture c = new Capture(p, detected, getLocation().z());
         captures.add(c);

         return c;
     }

     protected Polygon scanArea(Point p) {
         double h = 2 * p.z() * Math.tan(FOV_WIDTH/2);
         double w = 2 * p.z() * Math.tan(FOV_HEIGHT/2);
         return new Polygon(
                 new Point(p.x() + w/2, p.y() + h/2),
                 new Point(p.x() + w/2, p.y() - h/2),
                 new Point(p.x() - w/2, p.y() - h/2),
                 new Point(p.x() - w/2, p.y() + h/2)
         );
     }

     public void moveTo(Point p) {
         route.add(p);
         this.location = p;
     }

     public void reroute(Area area) {
         route = new ArrayList<>();
         this.area = area;
         moveTo(area.getStart());
         this.setHeading(0.0);
         this.captures = new CopyOnWriteArrayList<>();
         route();
         route.add(0, area.getStart());
         moveTo(area.getEnd());
     }

     public void reroute() {
         route = new ArrayList<>();
         moveTo(area.getStart());
         this.setHeading(0.0);
         this.captures = new CopyOnWriteArrayList<>();
         route();
         route.add(0, area.getStart());
     }

     public void reroute(Area area, double energyBudget) {
         reroute(area);
     }

     public abstract void route();
     /***
     public abstract void visualize(Graphics g);
***/
     public static List<Point> subdivide(List<Point> points) {
         List<Point> result = new ArrayList<>();
         for(Line l: Line.arrayFromPoints(points.toArray(new Point[0]))) result.addAll(l.toSubpoints(new Option().defaultImageHeight()));
         result.add(points.get(points.size()-1));
         return result;
     }

     @Override
     public String toString() {
         return this.getClass().getSimpleName();
     }

     // Metrics:
 }
