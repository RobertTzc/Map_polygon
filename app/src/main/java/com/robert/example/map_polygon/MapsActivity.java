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
    EditText et_altitude;
    TextView tv_info;
    DroneStatus droneStatus = new DroneStatus();
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
