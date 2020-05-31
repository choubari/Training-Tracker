package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


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
        DisplayName =  findViewById(R.id.name);
        DisplayUsername = findViewById(R.id.username);
        db.setFirestoreSettings(settings);
        // afficher nom et username du membre
        CollectionReference peopleRef = db.collection("member");
        peopleRef.whereEqualTo("Email", Email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                          for (DocumentSnapshot document : task.getResult()) {
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
                });
    }
    private void loadprofilepic(){
        ImageView imageView =findViewById(R.id.person);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        // récuperer l'image enregistrée de Firebase Storage via "User id/ImageProfile/Profile Pic.jpg".
        storageReference.child(firebaseAuth.getUid()).child("ImageProfile").child("Profile Pic").getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).fit().centerInside().into(imageView)).addOnFailureListener(exception -> {
            if (exception instanceof StorageException && ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                Log.d("TAG", "File not exist");
            }
        });
    }

    // Se déconnecter
    public void Logout(View v)   {
        // TODO: Logout
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    // menu calendrier
    public void Calendar(View v) {
        Intent intent = new Intent(this, CalendarActivity.class);
        intent.putExtra(USER_TEAM,Team);
        startActivity(intent);
    }


    // menu entraînement
    public void Training(View v) {
        DisplayName = findViewById(R.id.name);
        DisplayUsername =  findViewById(R.id.username);
        Intent intent = new Intent(this, TrainingActivity.class);
        intent.putExtra(USER_TEAM,Team);
        intent.putExtra(USER_DATA, Email);
        intent.putExtra("userFullName", DisplayName.getText());
        intent.putExtra("username", DisplayUsername.getText());
        startActivity(intent);
    }


    // menu messages
    public void Messages(View v) {
        Intent intent = new Intent(this, MessagesActivity.class);
        intent.putExtra(USER_TEAM,Team);

        startActivity(intent);
    }


    // menu tâches accomplies
    public void Tasks(View v) {
        Intent intent = new Intent(this, TasksActivity.class);
        intent.putExtra(USER_DATA, Email);
        startActivity(intent);
    }

    // menu parametres
    public void Settings(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(USER_DATA,doc_id);
        startActivity(intent);
    }
}
