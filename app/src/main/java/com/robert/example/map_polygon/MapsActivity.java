package com.robert.example.map_polygon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.warkiz.tickseekbar.TickSeekBar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.missouri.frame.GePoint;
import edu.missouri.frame.ReadFlightParameters;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback{

    GoogleMap gMap;
    CheckBox checkBox;
    SeekBar seekRed,seekGreen,seekBlue;
    Button btDraw,btClear,btCamera;
    Polygon polygon = null;
    Polyline pathPoly;
    ArrayList<LatLng> latLngList = new ArrayList<>();
    ArrayList<LatLng> wpLatLngList = new ArrayList<>();
    DrawerLayout drawerLayout;
    ArrayList<Marker> markerList = new ArrayList<>();
    ArrayList<Marker> wpMarkerList = new ArrayList<>();
    ArrayList<GePoint> corner = new ArrayList<>();
    ReadFlightParameters path = new ReadFlightParameters();
    int altitude;
    TickSeekBar seekBar;
    EditText et_altitude;
    TextView tv_info;
    DroneStatus droneStatus = new DroneStatus();
    NumberPicker np_shutter,np_ISO,np_aperture,np_exposureCompensation;
    String[] ISOValue = new String[]{"50","100","200","400","800","1600","3200","6400","12800","25600"};
    String[] ShutterValue = new String[]{"1_20000","1_16000","1_12800","1_10000","1_8000","1_6400","1_6000","1_5000","1_4000","1_3200","1_3000","1_2500","1_2000","1_1600","1_1500",
            "1_1250","1_1000","1_800","1_725","1_640","1_500","1_400","1_350","1_320","1_250","1_240","1_200","1_180","1_160","1_125","1_120","1_100","1_90",
            "1_80","1_60","1_50","1_40","1_30","1_25","1_20","1_15","1_12_DOT_5","1_10","1_8","1_6_DOT_25","1_5","1_4","1_3","1_2_DOT_5","1_2",
            "1_1_DOT_67","1_1_DOT_25","1","1_DOT_3","1_DOT_6","2","2_DOT_5","3","3_DOT_2","4","5","6","7","8","9","10","13","15","20","25","3"};
    String[] ApertureValue = new String[]{"F_1_DOT_7","F_1_DOT_8","F_2","F_2_DOT_2","F_2_DOT_5","F_2_DOT_6","F_2_DOT_8","F_3_DOT_2","F_3_DOT_4","F_3_DOT_5","F_4","F_4_DOT_5","F_4_DOT_8","F_5","F_5_DOT_6",
            "F_6_DOT_3","F_6_DOT_8","F_7_DOT_1","F_8","F_9","F_9_DOT_6","F_10","F_11","F_13","F_14","F_16","F_18","F_19","F_20","F_22"};
    String[] ExposureValue = new String[]{"N_5_0","N_4_7","N_4_3","N_4_0","N_3_7","N_3_3","N_3_0","N_2_7","N_2_3","N_2_0","N_1_7","N_1_3","N_1_0","N_0_7","N_0_3","N_0_0",
                                                "P_0_3","P_0_7","P_1_0","P_1_3","P_1_7","P_2_0","P_2_3","P_2_7","P_3_0","P_3_3","P_3_7","P_4_0","P_4_3","P_4_7","P_5_0"};

    int red = 0,green = 0,blue = 0;
    void createDroneInfo(){
        droneStatus.batteryPercentage=20;
        droneStatus.droneHeading = (int)(Math.random()*360)-180;
        droneStatus.droneLatitude = 38.9129228409671;
        droneStatus.droneLongtitude = -92.2959491063508;
        droneStatus.homeLatitude = 38.9139228409671;
        droneStatus.homeLongtitude = -92.2959491063508;
        droneStatus.droneHeight = 100;
        droneStatus.overlapRatio = 20;
        droneStatus.plannedSpeed = 5;
        droneStatus.satelliteCount=15;
        droneStatus.batteryPrecentageRemian = droneStatus.batteryPercentage;
        displayDroneInfo();
    }
    private static DecimalFormat df = new DecimalFormat("0.00");
    void displayDroneInfo(){
        tv_info.setText("Battery_info: "+String.valueOf(droneStatus.batteryPercentage)+"" +
                "\nSatellite count: "+String.valueOf(droneStatus.satelliteCount)+
                "\nSpeed_info: "+String.valueOf(df.format(droneStatus.droneSpeed))+"m/s"+
                "\nSpeed set: "+String.valueOf(droneStatus.plannedSpeed)+"m/s"+
                "\nHeight: "+String.valueOf(df.format(droneStatus.droneHeight))+"m"+
                "\nOverlap set: "+ String.valueOf(droneStatus.overlapRatio)+
                "\nDrone heading : "+ String.valueOf(droneStatus.droneHeading)+
                "\nDrone battery estimation remain： "+String.valueOf(droneStatus.batteryPrecentageRemian)
        );
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Assign var
        checkBox = findViewById(R.id.check_box);
        btDraw =  findViewById(R.id.bt_draw);
        btClear  = findViewById(R.id.bt_clear);
        et_altitude = findViewById(R.id.et_altitude);
        tv_info = findViewById(R.id.tv_info);
        btCamera = findViewById(R.id.bt_camera);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        np_shutter = findViewById(R.id.np_shutter);
        np_aperture = findViewById(R.id.np_Aperture);
        np_exposureCompensation = findViewById(R.id.np_exposure);
        np_ISO = findViewById(R.id.np_ISO);

        np_shutter.setDisplayedValues(ShutterValue);
        np_shutter.setMaxValue(10);
        np_ISO.setDisplayedValues(ISOValue);
        np_ISO.setMaxValue(5);
        np_exposureCompensation.setDisplayedValues(ExposureValue);
        np_exposureCompensation.setMaxValue(10);
        np_aperture.setDisplayedValues(ApertureValue);
        np_aperture.setMaxValue(ApertureValue.length-1);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                //Toast.makeText(MapsActivity.this,"slide",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                //Toast.makeText(MapsActivity.this,"open",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                //Toast.makeText(MapsActivity.this,"close",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        createDroneInfo();

        //initialize map frag
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(this);
        btCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this,MyWindow.class);
                startService(intent);
            }
        });
        findViewById(R.id.btn_open_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);

                Toast.makeText(MapsActivity.this,"show",Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_close_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //get check box state
                if (b)
                {
                    if (polygon == null)
                        return;
                    //Fill
                    generatePath();
                }
                else{
                    //Unfill
                    polygon.setFillColor(Color.TRANSPARENT);
                }
            }
        });
        btDraw.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                latLngList.clear();
                for (Marker m:markerList)
                {
                    latLngList.add(new LatLng(m.getPosition().latitude,m.getPosition().longitude));
                }
                if (polygon !=null)
                    polygon.remove();
                PolygonOptions polygonOptions = new PolygonOptions().addAll(latLngList)
                        .clickable(true);
                polygon = gMap.addPolygon((polygonOptions));
                polygon.setFillColor(Color.argb(100,255,0,30));





            }
        });
        btClear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (polygon!=null)
                    polygon.remove();
                if (pathPoly!=null)
                    pathPoly.remove();
                for (Marker marker:markerList) marker.remove();
                for (Marker marker:wpMarkerList) marker.remove();
                latLngList.clear();
                wpLatLngList.clear();
                markerList.clear();
                wpMarkerList.clear();
                checkBox.setChecked(false);
                corner.clear();
            }
        });


    }
    public void generatePath(){
        for (LatLng p:latLngList){
            corner.add(new GePoint(p.latitude,p.longitude));
        }

        path.UpdateBounds(corner,altitude,droneStatus);
        droneStatus.batteryPrecentageRemian = path.getEnergyPercentRemainingAfterPlan();
        List<GePoint> wps = path.getWaypoints();
        List<Boolean> isTurning = path.getIsTurning();
        List<Double> a = path.getAltitudes();

        //add check if duplicate wp exist
        ArrayList<GePoint> pre_wp = new ArrayList<>();
        List<Integer> toremove = new ArrayList<>();
        for  (int i = 0;i<wps.size();i++){
            if (pre_wp.contains(wps.get(i)))
                toremove.add(i);
            else
                pre_wp.add(wps.get(i));
        }
        for (int i= toremove.size()-1 ; i>=0;i--){
            wps.remove(toremove.get(i));
            isTurning.remove(toremove.get(i));
            a.remove(toremove.get(i));
        }
        PolylineOptions generatedPath = new PolylineOptions();
        for (int i = 0;i<wps.size();i++){
            GePoint wp = wps.get(i);
            LatLng latLng = new LatLng(wp.latitude,wp.longtitude);
            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
            Marker marker = null;
            if (isTurning.get(i))
                marker= gMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.turn_icon)).title(String.valueOf(i+1)+'_'+String.valueOf(altitude)));
            else
                marker = gMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_icon)).alpha(0.5f).title(String.valueOf(i+1)+'_'+String.valueOf(altitude)));
            wpMarkerList.add(marker);
            generatedPath.add(latLng);
        }
        pathPoly = gMap.addPolyline(generatedPath);
        displayDroneInfo();


    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setMapType(gMap.MAP_TYPE_SATELLITE);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(droneStatus.homeLatitude,droneStatus.homeLongtitude),18));
        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(droneStatus.droneLatitude,droneStatus.droneLongtitude)).rotation(droneStatus.droneHeading).anchor(0.5f,0.5f);
        Marker marker = gMap.addMarker(markerOptions.draggable(true).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.drone_icon)));
        MarkerOptions markerOptionsH = new MarkerOptions().position(new LatLng(droneStatus.homeLatitude,droneStatus.homeLongtitude));
        Marker markerH = gMap.addMarker(markerOptionsH.draggable(true).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.drone_home_icon)));

        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //ToastUtil.toast(MapsActivity.this,"Latitude"+marker.getPosition().latitude+"Altitude"+marker.getPosition());
                return false;
            }
        });
        gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                ToastUtil.toast(MapsActivity.this,"Latitude"+marker.getPosition().latitude+"\nlongtitude"+marker.getPosition().longitude);
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                //create Marker
                String alt = et_altitude.getText().toString();
                try {
                    altitude = Integer.parseInt(alt);
                }
                catch (Exception e)
                {
                    altitude = 60;
                }

                markerOptions.title(String.valueOf(markerList.size())+'_'+String.valueOf(altitude));
                Marker marker = gMap.addMarker(markerOptions.draggable(true).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_icon)));
                //add Marker
                markerList.add(marker);

            }
        });
    }



}
