package com.robert.example.map_polygon;

public class DroneStatus {
        public int batteryPercentage = 100;//[0,100] in percentage
        public int batteryPrecentageRemian = 100;

        public double droneLatitude= 38.9129228409671;
        public double droneLongtitude= -92.2959491063508;
        public double homeLatitude = 38.9129228409671;
        public double homeLongtitude = -92.2959491063508;

        public float droneHeading = 0;
        public float droneHeight=30;// in meters, note this is current height, not the path planning height
        public double droneSpeed=0; //in meter/seconds, note this is current speed, not the path planning speed
        public int plannedSpeed=5; //in meter/seconds, this is the speed set for path coverage
        public int overlapRatio=50; //[0,100] in percentage
        public int prePlannedSpeed = 15;

        public float cameraFOV = 66.0f;
        public int satelliteCount=0;

}
