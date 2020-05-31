package com.choubapp.running;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class LocationService extends Service {
    private String trainingID;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference trackingTraining ;
    DocumentReference userInfoDoc;
    private GeoPoint previous=null;
    private static final String TAG = "LocationService";
    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private final static long FASTEST_INTERVAL = 5000; /* 5 sec */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        trainingID = intent.getStringExtra("trainingID");
        System.out.println("from service "+trainingID);
        trackingTraining =db.collection("tracking").document(trainingID);
        userInfoDoc =trackingTraining.collection("Participants").document(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_NOT_STICKY;
    }

    private void getLocation() {

        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);


        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }
        Log.d(TAG, "getLocation: getting location information.");
        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Log.d(TAG, "onLocationResult: got location result.");
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            userInfoDoc.get().addOnCompleteListener(task -> {
                                Boolean av = (Boolean) task.getResult().get("Availability");
                                saveUserLocation(geoPoint , av);
                            });
                            saveDistance(geoPoint);

                        }
                    }
                },
                Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }

    private void saveUserLocation(final GeoPoint geo , Boolean av){
        // enregistrer la localisation du membre lorsqu'il est encore participant dans l'entrainement
        try{
            if (av) {
                userInfoDoc.update("Location", geo).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: \ninserted user location into database." +
                                "\n latitude: " + geo.getLatitude() +
                                "\n longitude: " + geo.getLongitude());
                    }
                });
            } else stopSelf();
        }catch (NullPointerException e){
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: "  + e.getMessage() );
            stopSelf();
        }
    }
    private void saveDistance(GeoPoint geo){
        if (previous!=null) {
            long distance = calculateDistance(previous, geo);
            if (distance != 0) {
                System.out.println("distance in meters :" + distance);
                userInfoDoc.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            if (document.get("Distance") != null) {
                                long previousDistance = (long) document.get("Distance");
                                // mise a jour du distance
                                userInfoDoc.update("Distance", distance + previousDistance);
                            } else {
                                // enregistrer la distance
                                userInfoDoc.update("Distance", distance);
                                Log.d(TAG, "previous distance does not exist");
                            }
                        }
                    }
                });
            }
        }
        previous = geo;
    }
    private long calculateDistance(GeoPoint prv , GeoPoint nxt) {
        // callculer distance entre deux points geographiques
        double lat1 = prv.getLatitude();
        double lng1 = prv.getLatitude();
        double lat2 = nxt.getLatitude();
        double lng2 = nxt.getLatitude();

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Math.round(6371000 * c);
    }



}