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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

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
        loadprofilepic();
        findViewById(R.id.header).setVisibility(View.GONE);
        findViewById(R.id.big_screen).setVisibility(View.GONE);
        final TextView DisplayName = (TextView) findViewById(R.id.name);
        final TextView DisplayUsername = (TextView) findViewById(R.id.username);
        System.out.println("Email : "+Email);
        db.setFirestoreSettings(settings);
        CollectionReference coach = db.collection("coach");
        coach.whereEqualTo("Email", Email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                DisplayName.setText(document.get("FullName").toString());
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
    private void loadprofilepic(){
        ImageView imageView =findViewById(R.id.person);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        // Get the image stored on Firebase via "User id/ImageProfile/Profile Pic.jpg".
        storageReference.child(firebaseAuth.getUid()).child("ImageProfile").child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).fit().centerInside().into(imageView);
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
        intent.putExtra(USER_DATA, Email);
        startActivity(intent);
    }

    // Executed when Training button pressed
    public void CoachTraining(View v) {
        Intent intent = new Intent(this, CoachTrainingActivity.class);
        intent.putExtra(USER_DATA, Email);
        startActivity(intent);
    }


    // Executed when Messages button pressed
    public void CoachMessages(View v) {
        Intent intent = new Intent(this, CoachMessagesActivity.class);
        intent.putExtra(USER_DATA, Email);
        startActivity(intent);
    }


    // Executed when Tasks button pressed
    public void CoachMemberlist(View v) {
        Intent intent = new Intent(this, CoachMemberlistActivity.class);
        intent.putExtra(USER_DATA, Email);
        startActivity(intent);
    }

    // Executed when Settings button pressed
    public void CoachSettings(View v) {
        Intent intent = new Intent(this, CoachSettingsActivity.class);
        intent.putExtra(USER_DATA, doc_id);
        startActivity(intent);
    }
}