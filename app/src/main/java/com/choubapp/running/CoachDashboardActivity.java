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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


public class CoachDashboardActivity extends AppCompatActivity {
    static final String USER_DATA = "com.choubapp.running.USER_DATA";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build();
    String Email;
    String doc_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        // récuperer email du login ou du menu paramètres
        Email = intent.getStringExtra(MainActivity.LOGIN_EMAIL);
        Email = intent.getStringExtra(CoachSettingsActivity.LOGIN_EMAIL);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_dashboard);

        loadprofilepic();
        findViewById(R.id.header).setVisibility(View.GONE);
        findViewById(R.id.big_screen).setVisibility(View.GONE);
        final TextView DisplayName =  findViewById(R.id.name);
        final TextView DisplayUsername =  findViewById(R.id.username);
        db.setFirestoreSettings(settings);
        // afficher nom, username du caoch
        CollectionReference coach = db.collection("coach");
        coach.whereEqualTo("Email", Email)
                .get()
                .addOnCompleteListener(task -> {
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
                });
    }
    private void loadprofilepic(){
        // afficher image de profile si elle existe
        ImageView imageView =findViewById(R.id.person);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        // récuperer l'image enregistrée de Firebase Storage via "User id/ImageProfile/Profile Pic.jpg".
        storageReference.child(firebaseAuth.getUid()).child("ImageProfile").child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).fit().centerInside().into(imageView);
            }
        });
    }


    // si le boutton se déconnecter est cliqué
    public void Logout(View v) {
        // TODO: Logout
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    // si le menu calendrier est choisi
    public void CoachReports(View v) {
        Intent intent = new Intent(CoachDashboardActivity.this, CoachReportsActivity.class);
        intent.putExtra(USER_DATA, Email);
        startActivity(intent);
    }

    // si le menu entrainement est choisi
    public void CoachTraining(View v) {
        Intent intent = new Intent(this, CoachTrainingActivity.class);
        intent.putExtra(USER_DATA, Email);
        startActivity(intent);
    }


    // si le menu messages est choisi
    public void CoachMessages(View v) {
        Intent intent = new Intent(this, CoachMessagesActivity.class);
        intent.putExtra(USER_DATA, Email);
        startActivity(intent);
    }


    // si le menu membres est choisi
    public void CoachMemberlist(View v) {
        Intent intent = new Intent(this, CoachMemberlistActivity.class);
        intent.putExtra(USER_DATA, Email);
        startActivity(intent);
    }

    // si le menu parametres est choisi
    public void CoachSettings(View v) {
        Intent intent = new Intent(this, CoachSettingsActivity.class);
        intent.putExtra(USER_DATA, doc_id);
        startActivity(intent);
    }
}