package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        mMapView = findViewById(R.id.user_list_map);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
    }

    private void GetStartEndPoints(){
        // affichier les markeurs vert et rouge qui identifient lieu depart et d'arrivee
        db.collection("Entrainement").document(trainingID).get().addOnCompleteListener(task -> {
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
                            // recuperer les coordonnees geographique du point de depart
                            Start = new GeoPoint(latitudeStart, longitudeStart);
                        }
                        List<Address> addressEnd = geoCoder.getFromLocationName(document.get("LieuArr").toString(), 1);
                        if (!addressEnd.isEmpty()) {
                            double latitudeEnd = addressEnd.get(0).getLatitude();
                            double longitudeEnd = addressEnd.get(0).getLongitude();
                            // recuperer les coordonnees geographique du point d'arrivee
                            End = new GeoPoint(latitudeEnd, longitudeEnd);
                        }
                        // creer un document dans la collection tracking
                        createTrackingofTraining();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            } else {
                Log.d("TAG", "Error getting documents: ", task.getException());
            }
        });

    }

    private void setCameraView() {
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
        //creer un document dans la collection tracking
        Map<String, Object> newtrack = new HashMap<>();
        newtrack.put("TrainingID", trainingID);
        newtrack.put("EmailCoach", email);
        newtrack.put("Status", "start");
        newtrack.put("Start", Start);
        newtrack.put("End", End);
        trackingTrainingDoc.set(newtrack);
    }
    // arreter l 'entrainement
    public void StopTraining(View view) {
        updateTrainingStatus();
    }
    private void  updateTrainingStatus(){
        // mise a jour du status
        trackingTrainingDoc.update("Status", "stop")
                .addOnSuccessListener(aVoid -> {
                    Log.d("TAG", "DocumentSnapshot successfully updated!");
                    Toast.makeText(CoachTrainingTime.this, "Vous avez arrêté votre entraînement", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), CoachDashboardActivity.class);
                    finish();
                    startActivity(intent);
                    //CoachTrainingTime.super.onBackPressed();
                })
                .addOnFailureListener(e -> Log.w("TAG", "Error updating document", e));
    }

    // afficher dans la carte les markeurs des participants
    private void getParticipantsData(){
        GetStartEndPoints();
        getLastKnownLocation();
        UsersLocations.clear();
        UsersNames.clear();
        particpdoc.whereEqualTo("Availability", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Log.d("TAG", document.getId() + " => " + document.getData());
                            GeoPoint geo = (GeoPoint) document.get("Location");
                            UsersLocations.add(geo);
                            String title = document.get("Fullname").toString();
                            UsersNames.add(title);
                        }
                        // mise a jour des markeurs dans la carte
                        updateMapMarkers();
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void startUserLocationsRunnable(){
        Log.d("TAG", "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = () -> {
            // mise a jour des markeurs de participants chaque 5 secondes
            getParticipantsData();
            mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }
    private void updateMapMarkers(){
        // mise à jour des markeurs dans la carte
        if (mGoogleMap!=null){
            // on supprimer les markeurs precedents pour eviter la duplication
            mGoogleMap.clear();
            // markeur vert pour le lieu de départ avec titre "Début"
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(Start.getLatitude(), Start.getLongitude())).title("Début").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            // markeur vert pour le lieu d'arrivée avec titre "Fin"
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(End.getLatitude(), End.getLongitude())).title("Fin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            for (int i =0 ; i< UsersLocations.size();i++) {
                // markeur bleu pour les participants, lorsqu'on clique dedans, le nom du joueur s'affiche
                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(UsersLocations.get(i).getLatitude(), UsersLocations.get(i).getLongitude())).title(UsersNames.get(i)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
        }
    }

    @Override
    public void onBackPressed() {
        // afficher une fenetre pour confirmer l'arret de l'entrainement lorsqu'on clique sur la boutton de revenir en arrière
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Arrêter l'Entraînement?");
        builder.setMessage("Voules-vous arrêter cet entraînement pour vous et pour les membres? ");
        builder.setPositiveButton("Oui", (dialog, id) -> updateTrainingStatus());
        builder.setNegativeButton("Annuler", (dialog, id) -> dialog.dismiss());
        builder.show();
    }

    // verifier si le service Maps du playServices existe dans le telephone
    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }
    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }
    public boolean isServicesOK(){
        Log.d("TAG", "isServicesOK: checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if(available == ConnectionResult.SUCCESS){
            Log.d("TAG", "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d("TAG", "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    // fenetre pour activer GPS
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

    // demander acces à la localisation
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getLastKnownLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }
    // si GPS est activé on recupere la derniere localisation de l'utilisateur, sinon on demande la permission d'acces à la location
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAG", "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    getLastKnownLocation();
                }
                else{
                    getLocationPermission();
                }
            }
        }
    }
    // derniere location assurée par le service de localisation
    private void getLastKnownLocation() {
        Log.d("TAG", "getLastKnownLocation: called.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Location location = task.getResult();
                if (location!=null) {
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Log.d("TAG", "onComplete: latitude: " + geoPoint.getLatitude());
                    Log.d("TAG", "onComplete: longitude: " + geoPoint.getLongitude());
                    setUserCoordinates(geoPoint);
                    if (!alreadyViewSet) {
                        setCameraView(); // zoom sur la position
                        alreadyViewSet = true;
                    }
                }
            }
        });
    }

    // si l'utilisateur verouille puis déverouille son telephone, on persiste l'affichage de la carte géographique et on verifie l'activation du GPS
    @Override
    protected void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(mLocationPermissionGranted){
                startUserLocationsRunnable();
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
        map.setMyLocationEnabled(true); // afficher le point bleu sur ma location
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
    }

}
