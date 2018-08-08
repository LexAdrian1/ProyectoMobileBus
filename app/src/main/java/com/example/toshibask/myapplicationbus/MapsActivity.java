package com.example.toshibask.myapplicationbus;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.nfc.cardemulation.HostNfcFService;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.toshibask.myapplicationbus.Remote.IGoogleApi;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private List<LatLng> polylineList;
    private Marker marker;
    private float v;
    private double lat, lng;
    private Handler handler;
    private LatLng startPosition, endPosition;
    private int index, next;
    private Button btnGo;
    private EditText edtPlace;
    private String destination;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyline;
    private LatLng myLocation;
    IGoogleApi mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        polylineList = new ArrayList<>();
        mapFragment.getMapAsync(MapsActivity.this);

        mService = Common.getGoogleApi();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera
        final LatLng epn = new LatLng(-0.210370,  -78.489120);
        mMap.addMarker(new MarkerOptions().position(epn).title("Escuela Politecnica Nacional"));
        final LatLng quicentro = new LatLng(-0.17611094405541, -78.48099753793139);
        mMap.addMarker(new MarkerOptions().position(quicentro).title("Quicentro Shopping"));
        final LatLng cato = new LatLng(-0.-0.208739772610497,  -78.49152624607086);
        mMap.addMarker(new MarkerOptions().position(cato).title("Av. 12 de Octubre y Catolica"));
        final LatLng gaso = new LatLng(-0.17472941284265034,  -78.48798629641385);
        mMap.addMarker(new MarkerOptions().position(epn).title("Gasolinera 10 de Agosto"));


        mMap.moveCamera(CameraUpdateFactory.newLatLng(epn));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(googleMap.getCameraPosition().target)
        .zoom(12)
                .bearing(30)
                .tilt(45)
                .build()));
        String requestUrl = null;

            try{
                requestUrl = "https://maps.googleapis.com/maps/api/directions/json?"+
                "mode=driving&"+
                "transit_routing_preference=less_driving&"+
                "origin="+quicentro.latitude+","+quicentro.longitude+"&"+
                "destination="+cato.latitude+","+cato.longitude+"&"+
                "key="+getResources().getString(R.string.google_directions_key);
                Log.d("URL",requestUrl);//Print url to review by Chrome
                mService.getDataFromGoogleApi(requestUrl)
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {

                       try{
                                    JSONObject jsonObject = new JSONObject(response.body().toString());
                                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                    for(int i=0;i<jsonArray.length();i++){
                                        JSONObject route = jsonArray.getJSONObject(i);
                                        JSONObject poly = route.getJSONObject("overview_polyline");
                                        String polyline = poly.getString("points");
                                        polylineList = decodePoly(polyline);

                                    }
                                    //Adjusting Limites
                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                    for(LatLng latLng:polylineList)
                                        builder.include(latLng);
                                    LatLngBounds bounds = builder.build();
                                    CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
                                    mMap.animateCamera(mCameraUpdate);

                                    polylineOptions = new PolylineOptions();
                                    polylineOptions.color(Color.GRAY);
                                    polylineOptions.width(5);
                                    polylineOptions.startCap(new SquareCap());
                                    polylineOptions.endCap(new SquareCap());
                                    polylineOptions.jointType(JointType.ROUND);
                                    polylineOptions.addAll(polylineList);
                                    greyPolyline = mMap.addPolyline(polylineOptions);

                                    blackPolylineOptions = new PolylineOptions();
                                    blackPolylineOptions.color(Color.BLACK);
                                    blackPolylineOptions.width(5);
                                    blackPolylineOptions.startCap(new SquareCap());
                                    blackPolylineOptions.endCap(new SquareCap());
                                    blackPolylineOptions.jointType(JointType.ROUND);
                                    blackPolylineOptions.addAll(polylineList);
                                    blackPolyline = mMap.addPolyline(blackPolylineOptions);

                                    mMap.addMarker(new MarkerOptions().position(polylineList.get(polylineList.size()-1)));
                                    //Animacion
                                    final ValueAnimator polylineAnimator = ValueAnimator.ofInt(0,100);
                                    polylineAnimator.setDuration(2000); // 2 seg
                                    polylineAnimator.setInterpolator(new LinearInterpolator());
                                    polylineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                            List<LatLng> points = greyPolyline.getPoints();
                                            int percentValue = (int)valueAnimator.getAnimatedValue();
                                            int size = points.size();
                                            int newPoints = (int) (size * (percentValue / 100.0f));
                                            List<LatLng> p = points.subList(0,newPoints);
                                            blackPolyline.setPoints(p);
                                        }
                                    });
                                    polylineAnimator.start();
                                    //Añadir marca carro
                                    marker = mMap.addMarker(new MarkerOptions().position(epn)
                                            .flat(true)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                                    //Moviento Carro
                                    handler = new Handler();
                                    index = -1;
                                    next = 1;
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (index < polylineList.size()-1) {
                                                index++;
                                                next = index + 1;
                                            }
                                            if (index < polylineList.size() - 1) {
                                                startPosition = polylineList.get(index);
                                                endPosition = polylineList.get(next);
                                            }

                                            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
                                            valueAnimator.setDuration(3000); //
                                            valueAnimator.setInterpolator(new LinearInterpolator());
                                            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                                @Override
                                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                                    v = valueAnimator.getAnimatedFraction();
                                                    lng = v*endPosition.longitude+(1-v)
                                                            *startPosition.longitude;
                                                    lat = v*endPosition.latitude+(1-v)
                                                            *startPosition.latitude;
                                                    LatLng newPos = new LatLng(lat,lng);
                                                    marker.setPosition(newPos);
                                                    marker.setAnchor(0.5f,0.5f);
                                                    marker.setRotation(getBearing(startPosition,newPos));
                                                    /*mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                                            .target(newPos)
                                                    .zoom(15.5f)
                                                    .build()));*/
                                                }
                                            });
                                            valueAnimator.start();
                                            handler.postDelayed(this,1000);
                                        }
                                    },4000);//Tiempo De Movimiento


                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                Toast.makeText(MapsActivity.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            catch (Exception e){
                e.printStackTrace();
            }


        try{
            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+epn.latitude+","+epn.longitude+"&"+
                    "destination="+gaso.latitude+","+gaso.longitude+"&"+
                    "key="+getResources().getString(R.string.google_directions_key);
            Log.d("URL",requestUrl);//Print url to review by Chrome
            mService.getDataFromGoogleApi(requestUrl)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            try{
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                for(int i=0;i<jsonArray.length();i++){
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polylineList = decodePoly(polyline);

                                }
                                //Adjusting Limites
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                for(LatLng latLng:polylineList)
                                    builder.include(latLng);
                                LatLngBounds bounds = builder.build();
                                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
                                mMap.animateCamera(mCameraUpdate);

                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.RED);
                                polylineOptions.width(7);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polylineList);
                                greyPolyline = mMap.addPolyline(polylineOptions);

                                mMap.addMarker(new MarkerOptions().position(polylineList.get(polylineList.size()-1)));

                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(MapsActivity.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    private float getBearing(LatLng startPosition, LatLng newPos) {
        double lat = Math.abs(startPosition.latitude - newPos.latitude);
        double lng = Math.abs(startPosition.longitude - newPos.longitude);
        if(startPosition.latitude < newPos.latitude && startPosition.longitude < newPos.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat)));
        else if(startPosition.latitude >= newPos.latitude && startPosition.longitude < newPos.longitude)
            return (float) ((90-Math.toDegrees(Math.atan(lng/lat)))+90);
        else if(startPosition.latitude >= newPos.latitude && startPosition.longitude >= newPos.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat))+180);
        else if(startPosition.latitude < newPos.latitude && startPosition.longitude >= newPos.longitude)
            return (float) (90-Math.toDegrees(Math.atan(lng/lat))+270);
        return -1;
    }

    private List<LatLng> decodePoly(String encoded) {
        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
        }
