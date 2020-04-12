package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CoachDashboardActivity extends AppCompatActivity {
    static final String USER_DATA = "com.choubapp.running.USER_DATA";
    //private final String IMAGE_URL = "PictureUploads/1585506259357.jpg";
    // StorageReference picref =FirebaseStorage.getInstance().getReference("PictureUploads");
    //StorageReference pathReference = picref.child("1585506259357.jpg");
    ImageView profilpic;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build();
    String Email;
    String doc_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Email = intent.getStringExtra(MainActivity.LOGIN_EMAIL);
        Email = intent.getStringExtra(CoachSettingsActivity.LOGIN_EMAIL);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_dashboard);
        findViewById(R.id.header).setVisibility(View.GONE);
        findViewById(R.id.big_screen).setVisibility(View.GONE);
        profilpic = findViewById(R.id.person);
        // String url="https://firebasestorage.googleapis.com/v0/b/trackingtraining-ff626.appspot.com/o/PictureUploads%2F1585506259357.jpg";
        //Glide.with(DashboardActivity.this).load(url).centerCrop().into(profilpic);
        //GlideApp.with(this /* context */).load(pathReference).into(profilpic);
        final TextView DisplayName = (TextView) findViewById(R.id.name);
        final TextView DisplayUsername = (TextView) findViewById(R.id.username);
        db.setFirestoreSettings(settings);
        CollectionReference peopleRef = db.collection("coach");
        peopleRef.whereEqualTo("Email", Email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                DisplayName.setText(document.get("Nom Complet").toString());
                                DisplayUsername.setText("@" + document.get("Username").toString());
                                doc_id = document.getId();
                                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                                findViewById(R.id.header).setVisibility(View.VISIBLE);
                                findViewById(R.id.big_screen).setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }

                });
    }

    // Executed when Sign in button pressed
    public void Logout(View v) {
        // TODO: Logout
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    // Executed when Calendar button pressed
    public void CoachReports(View v) {
        Intent intent = new Intent(CoachDashboardActivity.this, CoachReportsActivity.class);
        startActivity(intent);
    }

    // Executed when Training button pressed
    public void CoachTraining(View v) {
        Intent intent = new Intent(this, CoachTrainingActivity.class);
        startActivity(intent);
    }


    // Executed when Messages button pressed
    public void CoachMessages(View v) {
        Intent intent = new Intent(this, CoachMessagesActivity.class);
        startActivity(intent);
    }


    // Executed when Tasks button pressed
    public void CoachMemberlist(View v) {
        Intent intent = new Intent(this, CoachMemberlistActivity.class);
        startActivity(intent);
    }

    // Executed when Settings button pressed
    public void CoachSettings(View v) {
        Intent intent = new Intent(this, CoachSettingsActivity.class);
        intent.putExtra(USER_DATA, doc_id);
        startActivity(intent);
    }
}