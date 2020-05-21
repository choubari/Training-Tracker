package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.concurrent.ExecutionException;

import static com.squareup.picasso.Picasso.LoadedFrom.NETWORK;

public class DashboardActivity extends AppCompatActivity {
   static final String USER_DATA = "com.choubapp.running.USER_DATA";
   static final String USER_TEAM = "com.choubapp.running.USER_TEAM";
    ImageView profilpic;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build();
    String Email,Team;
    String doc_id ;
    TextView DisplayName ,DisplayUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Email= intent.getStringExtra(MainActivity.LOGIN_EMAIL);
        Email= intent.getStringExtra(SettingsActivity.LOGIN_EMAIL);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        loadprofilepic();
        findViewById(R.id.header).setVisibility(View.GONE);
        findViewById(R.id.big_screen).setVisibility(View.GONE);
        profilpic = findViewById(R.id.person);
        DisplayName = (TextView) findViewById(R.id.name);
        DisplayUsername = (TextView) findViewById(R.id.username);
        db.setFirestoreSettings(settings);
        CollectionReference peopleRef = db.collection("member");
        peopleRef.whereEqualTo("Email", Email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                           // Boolean docfound=false;
                              for (DocumentSnapshot document : task.getResult()) {
                             //     docfound = true;
                                  Log.d("TAG", document.getId() + " => " + document.getData());
                                  DisplayName.setText(document.get("FullName").toString());
                                  DisplayUsername.setText("@" + document.get("Username").toString());
                                  doc_id = document.getId();
                                  Team=document.get("Team").toString();
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (exception instanceof StorageException && ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    Log.d("TAG", "File not exist");
                }
            }
        });
    }

    // Executed when Sign in button pressed
    public void Logout(View v)   {
        // TODO: Logout
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    // Executed when Calendar button pressed
    public void Calendar(View v) {
        Intent intent = new Intent(this, CalendarActivity.class);
        intent.putExtra(USER_TEAM,Team);
        startActivity(intent);
    }


    // Executed when Training button pressed
    public void Training(View v) {
        DisplayName = (TextView) findViewById(R.id.name);
        DisplayUsername = (TextView) findViewById(R.id.username);
        Intent intent = new Intent(this, TrainingActivity.class);
        intent.putExtra(USER_TEAM,Team);
        intent.putExtra(USER_DATA, Email);
        intent.putExtra("userFullName", DisplayName.getText());
        intent.putExtra("username", DisplayUsername.getText());
        startActivity(intent);
    }


    // Executed when Messages button pressed
    public void Messages(View v) {
        Intent intent = new Intent(this, MessagesActivity.class);
        intent.putExtra(USER_TEAM,Team);
        startActivity(intent);
    }


    // Executed when Tasks button pressed
    public void Tasks(View v) {
        Intent intent = new Intent(this, TasksActivity.class);
        intent.putExtra(USER_DATA, Email);
        startActivity(intent);
    }

    // Executed when Settings button pressed
    public void Settings(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(USER_DATA,doc_id);
        startActivity(intent);
    }
}
