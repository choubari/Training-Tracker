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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.choubapp.running.CoachDashboardActivity.USER_DATA;

public class CoachTrainingTime extends AppCompatActivity  implements OnMapReadyCallback {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference trackingTrainingDoc ;
    CollectionReference particpdoc;
    Boolean mLocationPermissionGranted=false;
    public static final int ERROR_DIALOG_REQUEST = 9000;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9001;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    private MapView mMapView;
    private GeoPoint userCoordinates, Start = new GeoPoint(0, 0), End=new GeoPoint(0,0);
    private GoogleMap mGoogleMap;
    private LatLngBounds mMapBoundary; Boolean alreadyViewSet=false;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    String email, trainingID;
    ArrayList<GeoPoint> UsersLocations = new ArrayList<>();
    ArrayList<String> UsersNames = new ArrayList<>();
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 5000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        email= intent.getStringExtra(USER_DATA);
        trainingID= intent.getStringExtra("TrainingID");
        trackingTrainingDoc =db.collection("tracking").document(trainingID);
        particpdoc=trackingTrainingDoc.collection("Participants");
        setContentView(R.layout.activity_coach_training_time);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.user_list_map);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

    }

    private void GetStartEndPoints(){
        db.collection("Entrainement").document(trainingID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("TAG", document.getId() + " => " + document.getData());
                            Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            try {
                                List<Address> addressStart = geoCoder.getFromLocationName(document.get("LieuDep").toString(), 1);
                                if (!addressStart.isEmpty()) {
                                    double latitudeStart = addressStart.get(0).getLatitude();
                                    double longitudeStart = addressStart.get(0).getLongitude();
                                    Start = new GeoPoint(latitudeStart, longitudeStart);
                                }
                                List<Address> addressEnd = geoCoder.getFromLocationName(document.get("LieuArr").toString(), 1);
                                if (!addressEnd.isEmpty()) {
                                    double latitudeEnd = addressEnd.get(0).getLatitude();
                                    double longitudeEnd = addressEnd.get(0).getLongitude();
                                    End = new GeoPoint(latitudeEnd, longitudeEnd);
                                }
                                createTrackingofTraining();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }
                }
            });

    }

    private void setCameraView() {
       // GetStartEndPoints();
        if (userCoordinates!= null) {
            double bottomBoundary = userCoordinates.getLatitude() - .01;
            double topBoundary = userCoordinates.getLatitude() + .01;
            double leftBoundary = userCoordinates.getLongitude() - .01;
            double rightBoundary = userCoordinates.getLongitude() + .01;
            mMapBoundary = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
        }
    }

    private void createTrackingofTraining(){
        Map<String, Object> newtrack = new HashMap<>();
        newtrack.put("TrainingID", trainingID);
        newtrack.put("EmailCoach", email);
        newtrack.put("Status", "start");
        newtrack.put("Start", Start);
        newtrack.put("End", End);
        trackingTrainingDoc.set(newtrack);
    }
    public void StopTraining(View view) {
        updateTrainingStatus();
    }
    private void  updateTrainingStatus(){
        trackingTrainingDoc.update("Status", "stop")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot successfully updated!");
                        Toast.makeText(CoachTrainingTime.this, "Vous avez arrêté votre entraînement", Toast.LENGTH_SHORT).show();
                        CoachTrainingTime.super.onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error updating document", e);
                    }
                });
    }



    private void getParticipantsData(){
        GetStartEndPoints();

        getLastKnownLocation();
        UsersLocations.clear();
        UsersNames.clear();
        particpdoc.whereEqualTo("Availability", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                GeoPoint geo = (GeoPoint) document.get("Location");
                                UsersLocations.add(geo);
                                String title = document.get("Fullname").toString();
                                UsersNames.add(title);
                                // marker.hideInfoWindow();
                                // marker.showInfoWindow();
                                //mGoogleMap.clear();
                                //getLastKnownLocation();
                            }
                            updateMapMarkers();
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    private void startUserLocationsRunnable(){
        Log.d("TAG", "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                getParticipantsData();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }
    private void updateMapMarkers(){
        if (mGoogleMap!=null){
            mGoogleMap.clear();
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(Start.getLatitude(), Start.getLongitude())).title("Début").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(End.getLatitude(), End.getLongitude())).title("Fin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            System.out.println("Participants:  " +UsersNames);
            for (int i =0 ; i< UsersLocations.size();i++) {
                System.out.println("Marker setted");
                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(UsersLocations.get(i).getLatitude(), UsersLocations.get(i).getLongitude())).title(UsersNames.get(i)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Arrêter l'Entraînement?");
        builder.setMessage("Voules-vous arrêter cet entraînement pour vous et pour les membres? ");
        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                updateTrainingStatus();
            }
        });
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
               // getLastKnownLocation();
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Pour accéder à l'entraînement, vous devez activer GPS. Voulez-vous l'activer ?")
                .setCancelable(false)
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
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
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            //getChatrooms();
            //getParticipantsData();
            System.out.println("1chatroom");
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
                  //  getParticipantsData();
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
        //getParticipantsData();
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

                        if (!alreadyViewSet) {
                            System.out.println("zoom");
                            setCameraView();
                            alreadyViewSet = true;
                        }
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(mLocationPermissionGranted){
               // getChatrooms();
                startUserLocationsRunnable();
                System.out.println("3chatroom");
                mMapView.onResume();
                getLastKnownLocation();
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
        startUserLocationsRunnable();
        getLastKnownLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //map.addMarker(new MarkerOptions().position(new LatLng(34.2249, -5.7069)).title("Fin").draggable(true) .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker)));
        map.setMyLocationEnabled(true);
        mGoogleMap=map;
        getLastKnownLocation();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        stopLocationUpdates();
        super.onDestroy();
        updateTrainingStatus();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    public void setUserCoordinates(GeoPoint userCoordinates) {
        this.userCoordinates = userCoordinates;
        //setCameraView();
    }

}
