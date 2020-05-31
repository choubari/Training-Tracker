package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.auth.User;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.hardware.*;
import static com.choubapp.running.CoachDashboardActivity.USER_DATA;

public class MemberTrainingTime extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener  {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference trackingTraining, userInfoDoc;
    Boolean mLocationPermissionGranted=false;
    public static final int ERROR_DIALOG_REQUEST = 9000;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9001;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    private MapView mMapView;
   // private @ServerTimestamp Date timestamp;
    private GeoPoint userCoordinates;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    String trainingID,UserUsername, UserFullname,UserEmail;
    private GoogleMap mGoogleMap;
    private LatLngBounds mMapBoundary;
    boolean first=false;
    private int FirstStep, TotalSteps=0;
    private SensorManager sensorManager; private Sensor sensor;
    private TextView count;
    boolean activityRunning;
    private Chronometer chronometer;
    private boolean runningchrono = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        trainingID= intent.getStringExtra("TrainingID");
        UserEmail= intent.getStringExtra("usermail");
        UserFullname=intent.getStringExtra("userFullName");
        UserUsername=intent.getStringExtra("username");
        //initiate doc references
        trackingTraining =db.collection("tracking").document(trainingID);
        userInfoDoc =trackingTraining.collection("Participants").document(UserEmail);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_training_time);
        addToParticipants();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView =  findViewById(R.id.user_list_map);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        count =  findViewById(R.id.stepcount);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);



        chronometer = findViewById(R.id.chronometer);
        chronometer.setFormat("%s");
        chronometer.setBase(SystemClock.elapsedRealtime());


    }
    private void addToParticipants(){
        Map<String, Object> docData = new HashMap<>();
        docData.put("Fullname", UserFullname);
        docData.put("Username", UserUsername);
        docData.put("Email", UserEmail);
        userInfoDoc.set(docData);

    }
    private void GetStartEndPoints(){
        trackingTraining.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d("TAG", document.getId() + " => " + document.getData());
                    GeoPoint Start = (GeoPoint) document.get("Start");
                    GeoPoint End = (GeoPoint) document.get("End");
                    mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(Start.getLatitude(), Start.getLongitude())).title("Début").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(End.getLatitude(), End.getLongitude())).title("Fin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
            } else {
                Log.d("TAG", "Error getting documents: ", task.getException());
            }
        });

    }
    private void setCameraView() {
        if (userCoordinates!= null) {
            // surface du map view est 0.2*0.2=0.4
            double bottomBoundary = userCoordinates.getLatitude() - .01;
            double topBoundary = userCoordinates.getLatitude() + .01;
            double leftBoundary = userCoordinates.getLongitude() - .01;
            double rightBoundary = userCoordinates.getLongitude() + .01;
            mMapBoundary = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
        }
    }

    public void startChronometer() {
        if (!runningchrono) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            runningchrono = true;
        }
    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Pour accéder à l'entraînement, vous devez activer GPS. Voulez-vous l'activer ?")
                .setCancelable(false)
                .setPositiveButton("Oui", (dialog, id) -> {
                    Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            //getChatrooms();
            System.out.println("1chatroom");
            startChronometer();
            getLastKnownLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
        Log.d("TAG", "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d("TAG", "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d("TAG", "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getLastKnownLocation();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAG", "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    System.out.println("2chatroom");
                    getLastKnownLocation();
                    // getChatrooms();
                }
                else{
                    getLocationPermission();
                }
            }
        }
    }
    private void getLastKnownLocation() {
        Log.d("TAG", "getLastKnownLocation: called.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    if (location!=null) {
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        Log.d("TAG", "onComplete: latitude: " + geoPoint.getLatitude());
                        Log.d("TAG", "onComplete: longitude: " + geoPoint.getLongitude());
                        setUserCoordinates(geoPoint);
                        // setTimestamp(null);
                        saveUserLocation();
                        startLocationService();
                    }
                }
            }
        });

    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
            serviceIntent.putExtra("trainingID",trainingID);
//        this.startService(serviceIntent);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                MemberTrainingTime.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }
    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("LocationService".equals(service.service.getClassName())) {
                Log.d("TAG", "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d("TAG", "isLocationServiceRunning: location service is not running.");
        return false;
    }
    private void saveUserLocation(){
        Map<String, Object> TrackData = new HashMap<>();
        TrackData.put("Location", userCoordinates);
        TrackData.put("Availability", true);
        TrackData.put("Steps", TotalSteps);
        userInfoDoc.update(TrackData);
    }

    public void StopTraining(View view) {
        updateToDatabase();
        updateAvailability();
    }
    private void updateAvailability(){
        userInfoDoc.update("Availability", false)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TAG", "DocumentSnapshot successfully updated!");
                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    finish();
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Log.w("TAG", "Error updating document", e));
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sortir");
        builder.setMessage("Voules-vous quitter cet entraînement? ");
        builder.setPositiveButton("Oui", (dialog, id) -> updateAvailability());
        builder.setNegativeButton("Annuler", (dialog, id) -> dialog.dismiss());
        builder.show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        activityRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }
        if(checkMapServices()){
            if(mLocationPermissionGranted){
                // getChatrooms();
                System.out.println("3chatroom");
                getLastKnownLocation();
                mMapView.onResume();
            }
            else{
                getLocationPermission();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
        updateToDatabase();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        map.setMyLocationEnabled(true);
        mGoogleMap=map;
        GetStartEndPoints();
        getLastKnownLocation();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
       // activityRunning = false;
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
        updateToDatabase();
        updateAvailability();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    public void setUserCoordinates(GeoPoint userCoordinates) {
        this.userCoordinates = userCoordinates;
        setCameraView();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        if (values.length > 0 && !first) {                //some values was inside
            FirstStep = (int) values[0];    //the latest value added will be at value[0]
            first=true;
        }

        if (activityRunning) {
            TotalSteps=(int)(event.values[0]-FirstStep);
            count.setText(String.valueOf(TotalSteps));
        }
        updateToDatabase();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void updateToDatabase(){
        int elapsedMillis = (int) (SystemClock.elapsedRealtime() - chronometer.getBase());
        userInfoDoc.update(
                "Steps", TotalSteps,
                "TotalTime", elapsedMillis/1000
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("TAG", "Steps & chrono updated");
            }
        });
    }
}
