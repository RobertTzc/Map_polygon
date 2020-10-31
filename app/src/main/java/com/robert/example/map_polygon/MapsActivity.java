package com.robert.example.map_polygon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
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

    ArrayList<Marker> markerList = new ArrayList<>();
    ArrayList<Marker> wpMarkerList = new ArrayList<>();
    ArrayList<GePoint> corner = new ArrayList<>();
    ReadFlightParameters path = new ReadFlightParameters();
    int altitude;
    EditText et_altitude;
    TextView tv_info;
    int red = 0,green = 0,blue = 0;
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
        //initialize map frag
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(this);
        tv_info.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.satelite_icon),
                null,null,null);//依次是左、上、右、下，有就是位置，没有就是null
        tv_info.setCompoundDrawablePadding(10);//设置文字与图标间距
        tv_info.setText("GPS information");
        btCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this,MyWindow.class);
                startService(intent);
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

        path.UpdateBounds(corner,corner.get(0),100,0.8);
        List<GePoint> wps = path.getWaypoints();
        List<Boolean> isTurning = path.getIsTurning();
        List<Double> a = path.getAltitudes();
        PolylineOptions generatedPath = new PolylineOptions();
        for (int i = 0;i<wps.size();i++){
            GePoint wp = wps.get(i);
            LatLng latLng = new LatLng(wp.latitude,wp.longtitude);
            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
            Marker marker = null;
            if (isTurning.get(i))
                marker= gMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.turn_icon)));
            else
                marker = gMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_icon)));
            wpMarkerList.add(marker);
            generatedPath.add(latLng);
        }
        pathPoly = gMap.addPolyline(generatedPath);

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setMapType(gMap.MAP_TYPE_SATELLITE);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(38.9129228409671,-92.2959491063508),15));
        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(38.9129228409671,-92.2959491063508));
        Marker marker = gMap.addMarker(markerOptions.draggable(true).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.drone_icon)));
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
