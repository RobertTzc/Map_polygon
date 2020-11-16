package edu.missouri.drone.variable_height;

import org.apache.commons.math3.special.Erf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import edu.missouri.drone.Drone;
import edu.missouri.drone.static_height.JiaoDrone;
import edu.missouri.drone.static_height.PlowDrone;
import edu.missouri.frame.Area;
import edu.missouri.frame.Detectable;
import edu.missouri.frame.Option;
import edu.missouri.frame.QueueFuntions;
import edu.missouri.geom.Line;
import edu.missouri.geom.Point;
import edu.missouri.geom.Polygon;

public class ImprovedDirectDrone extends Drone {

    List<Point> EnergyPoints = new ArrayList<>();


    Queue<Polygon> regions = new LinkedList<>();
    double thoroughness = 1.0;

    public ImprovedDirectDrone(Area area) {
        super(area);
        regions.addAll(new JiaoDrone(area).decompose(getPolygon(), 500));
    }

    public void route() {
        List<Point> done = new ArrayList<>();

        double overviewAltitude = Option.cruiseAltitude;
        setHeading(getPolygon().widthLine().measure() + Math.PI/2.0);

        Queue<Point> currentPoints = new LinkedList<>(Drone.subdivide(PlowDrone.plan(getPolygon(), getLocation())));

        while(! currentPoints.isEmpty()) {
            Point p = currentPoints.remove();
            p = new Point(p.x(), p.y(), overviewAltitude);
            moveTo(p);
            Capture c = scan();

            List<Point> toDo = new ArrayList<>();
            for(Detectable d: c.detectables) {
                if (done.contains(d)) continue;
                done.add(d);
                double k = altitudeNeeded(d);
                if (k >= overviewAltitude) continue;
                Point q = scanArea(new Point(d.x(), d.y(), k-10)).closest(getLocation());
                Point p2 = new Point(q.x(), q.y(), k);
                toDo.add(p2);
            }

            for(Point q: heuristicTSP(toDo, getLocation(), currentPoints.isEmpty()? null : currentPoints.peek())) {
                moveTo(q);
                scan();
            }
        }
    }

    public Map<Point, Boolean> routes() {
        List<Point> done = new ArrayList<>();

        double overviewAltitude = Option.cruiseAltitude;
        setHeading(getPolygon().widthLine().measure() + Math.PI/2.0);
        List<Point> wayToPoints= PlowDrone.plan(getPolygon(),getLocation());
        Queue<Point> currentPoints = new LinkedList<>(Drone.subdivide(PlowDrone.plan(getPolygon(), getLocation())));

        predecideWayPoints = QueueFuntions.pointQueueToList(currentPoints);


        while(! currentPoints.isEmpty()) {
            Point p = currentPoints.remove();
            p = new Point(p.x(), p.y(), overviewAltitude);
            moveTo(p);
            Capture c = scan();

            List<Point> toDo = new ArrayList<>();
            for(Detectable d: c.detectables) {
                if (done.contains(d)) continue;
                done.add(d);
                double k = altitudeNeeded(d);
                if (k >= overviewAltitude) continue;
                Point q = scanArea(new Point(d.x(), d.y(), k-10)).closest(getLocation());
                Point p2 = new Point(q.x(), q.y(), k);
                toDo.add(p2);
            }

            for(Point q: heuristicTSP(toDo, getLocation(), currentPoints.isEmpty()? null : currentPoints.peek())) {
                moveTo(q);
                scan();
            }
        }

        Map<Point,Boolean> maps = new LinkedHashMap<>();

        for(Point data : predecideWayPoints){
            boolean isTurning = false;
            for (Point point : wayToPoints){
                if (data.x() == point.x() && data.y() == point.y()){
                    isTurning = true;
                }
            }
            maps.put(new Point(data.x(),data.y()),isTurning);
        }
        return maps;

    }

    private double altitudeNeeded(Detectable d) {
        double k = d.detectedFrom() * Math.abs(d.confidence() - Option.confidenceThreshold) * (1 / thoroughness);
        return Math.max(k, Option.minCruiseAltitude);
    }
/***
    @Override
    public void visualize(Graphics g) {

    }
***/
    public List<Point> heuristicTSP(List<? extends Point> points, Point start, Point end) {

        if(end == null) end = getTargetEnd();

        final Point finalEnd = end;
        Collections.sort(points, new Comparator<Point>() {
            @Override
            public int compare(Point point, Point t1) {
                return (int) (point.distance(finalEnd) - t1.distance(finalEnd));
            }
        });

        List<Point> result = new ArrayList<>();
        result.add(start);
        result.add(end);


        for(Point p: points) {
            int bestIndex = 1;
            double bestLength = Double.MAX_VALUE;
            for(int j = 1; j < result.size()-1; j++) {
                result.add(j, p);
                double d = pathLength(result);
                if(d < bestLength) {
                    bestLength = d;
                    bestIndex = j;
                }
                result.remove(p);
            }
            result.add(bestIndex, p);
        }
        result.remove(start);
        result.remove(end);
        return result;
    }


    public void reroute(Area area, double energyBudget) {
        thoroughness = 1.0;
        reroute(area);
    }
    public List<Integer> getWayBackPoints(Point p){
        Point LeftBasicPoint = getPolygon().getLeftBasicPoint();
        Point RightBasicPoint = getPolygon().getRightBasicPoint();
        Point[] points = getPolygon().toPoints();
        int pIndex = getPolygon().indexOf(p);
//        System.out.println(pIndex);
        int LeftBasicIndex = getPolygon().indexOf(LeftBasicPoint);
        int RightBasicIndex = getPolygon().indexOf(RightBasicPoint);
        int BasicIndex;
        BasicIndex = LeftBasicIndex;
//        System.out.println(BasicIndex);
        if(pIndex>BasicIndex){

            int step1 = pIndex-BasicIndex;
            int step2 = points.length - pIndex + BasicIndex;
            List<Integer> path1 = new ArrayList<>();
            for (int i =0;i<step1 +1;i++){
                path1.add( pIndex -i);
            }
            List<Integer> path2 = new ArrayList<>();

            int j = 0;
            for (int i =pIndex;i<points.length;i++){
                path2.add(pIndex +j) ;
                j++;
            }
            for (int i =0;i<=BasicIndex;i++){
                path2.add(i);
                j++;
            }

            boolean containRightPoint = false;
            if(path1.contains(RightBasicIndex)){
                containRightPoint = true;
            }
            return containRightPoint?path2:path1;
        }

        int step1 = BasicIndex - pIndex;
        List<Integer> path1 = new ArrayList<>();
        for (int i =0;i<step1 +1;i++){
            path1.add(pIndex + i) ;
        }
        List<Integer> path2 = new ArrayList<>();

        int j = 0;
        for (int i =pIndex;i>-1;i--){
            path2.add(i);
            j++;
        }
        for (int i =points.length-1;i>=BasicIndex;i--){
            path2.add(i);
            j++;
        }

        boolean containRightPoint = false;
        if(path1.contains(RightBasicIndex)){
            containRightPoint = true;
        }

        return containRightPoint?path2:path1;
    }

    private double pathLength(List<Point> points) {
        double sum = 0;
        for(Line l: Line.arrayFromPoints(points.toArray(new Point[0]))) {
            sum += l.length();
        }
        return sum;
    }

    public int getOptimalSpeed(int planSpeed,double energyBudget) {
        Map<Integer, Double> maps = new LinkedHashMap<>();
        for (int i = 0; i < Drone.MAX_SPEED + 1; i++) {
            maps.put(i, energyUsed(i));
        }
        Iterator<Map.Entry<Integer, Double>> entries = maps.entrySet().iterator();
        int optimalSpeed = planSpeed;
        double minimumEnergy = 10000000000000000.0;
        while (entries.hasNext()) {
            Map.Entry<Integer, Double> entry = entries.next();
            if (entry.getValue() < minimumEnergy) {
                minimumEnergy = entry.getValue();
                optimalSpeed = entry.getKey();
            }
        }
        if (energyBudget< minimumEnergy){
            optimalSpeed = 0;
        }
        return optimalSpeed;
    }

    // Energy calculations
    // credit to DiFranco and Buttazzo
    public double energyUsed(int speed) {
        int cruiseSpeed = speed;
        List<Point> currentPoints = new ArrayList<>(PlowDrone.plan(getPolygon(), getLocation()));
        Point[] finalPoints = new Point[currentPoints.size()];
        for (int i=0;i<currentPoints.size();i++){
            finalPoints[i] = currentPoints.get(i);
        }
        Line[] lines = Line.arrayFromPoints(finalPoints);
        double speedIn, speedOut;
        double result = 0.0;
        for(int i = 0; i < lines.length; i++) {
            // We could probably use some linear algebra to avoid the trig functions here,
            // but I don't trust myself to do that.
            speedIn = 0.0;
            speedOut = 0.0;
            result += legEnergy(lines[i], speedIn , cruiseSpeed, speedOut );
        }
        return result;
    }

    public static double legEnergy(Line path, double startSpeed, int cruiseSpeed, double endSpeed) {
        double dist = path.length2D();

        // These calculations are made for a drone with max speed 15 m/s
        // The Mavic is quite a bit bigger so we're going to (naively) scale things up

        double borderDist = accDist(startSpeed, cruiseSpeed) + decDist(cruiseSpeed, endSpeed);
        while(borderDist > dist && cruiseSpeed > 0) {
            cruiseSpeed -= 0.5;
            borderDist = accDist(startSpeed, cruiseSpeed) + decDist(cruiseSpeed, endSpeed);
        }

        // at distances around 1 meter we start having some problems
        if(cruiseSpeed < 0) {
//            System.err.println("Distance " + dist + " too short to calculate energy cost");
            return EFFICIENCY_FACTOR * cruiseEnergy(dist, cruiseSpeed);
        }

        double result =  EFFICIENCY_FACTOR * accEnergy(startSpeed, cruiseSpeed)
                + EFFICIENCY_FACTOR * decEnergy(cruiseSpeed,endSpeed)
                + EFFICIENCY_FACTOR * cruiseEnergy(dist - borderDist, cruiseSpeed);
//        System.out.println("start speed:"+startSpeed);
//        System.out.println("crusie speed:"+cruiseSpeed);
//        System.out.println("end speed:"+endSpeed);


        if(path.dz() < 0) result += 210 * path.dz() / DESCENT_SPEED * EFFICIENCY_FACTOR;
        if(path.dz() > 0) result += 250 * path.dz() / ASCENT_SPEED  * EFFICIENCY_FACTOR;
        return result;
    }

    private static double accEnergy(double vIn, double vOut) {
        double tIn  = accTime(vIn);
        double tOut = accTime(vOut);
        return (Math.pow(tOut, 2.35)/2.35 + 220*tOut)
                - (Math.pow(tIn, 2.35)/2.35 + 220*tIn);
    }
    private static double accTime(double v) {
        return -4 * Math.log(16.138/(1.138+v) - 1) + 11;
    }
    private static double accDist(double vIn, double vOut) {
        double tIn  = accTime(vIn);
        double tOut = accTime(vOut);
        return (64.552*Math.log(15.6426+Math.pow(Math.E, 0.25*tOut)) - 1.138*tOut)
                - (64.552*Math.log(15.6426+Math.pow(Math.E, 0.25*tIn )) - 1.138*tIn);
    }

    public static double decEnergy(double vIn, double vOut) {
        double tIn = decTime(vIn);
        double tOut = decTime(vOut);
        return (313.769*tOut-24.349*Math.pow(tOut, 2)+2.53967*Math.pow(tOut, 3)-0.1105*Math.pow(tOut, 4)+0.0017*Math.pow(tOut, 5))
                - (313.769*tIn -24.349*Math.pow(tIn , 2)+2.53967*Math.pow(tIn , 3)-0.1105*Math.pow(tIn , 4)+0.0017*Math.pow(tIn , 5));
    }
    private static double decTime(double v) {
        return Math.sqrt(Math.log((v + 0.1)/15.1)/-0.02);
    }
    private static double decDist(double vIn, double vOut) {
        double tIn = decTime(vIn);
        double tOut = decTime(vOut);
        return ((-0.1*tOut)+94.6252* Erf.erf(0.141421 * tOut))-((-0.1*tIn)+94.6252* Erf.erf(0.141421*tIn));
    }

    public static double cruiseEnergy(double dist, double speed) {
        return (Math.pow(.25*speed, 4) - 8*Math.pow(.25*speed, 2) + 220) * dist/speed;
    }

}
