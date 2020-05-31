package com.choubapp.running;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreateTeam extends AppCompatActivity {
    String email;
    TextView TeamName, NewTeam,NewTeamId, CopyID;
    String teamId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        email = intent.getStringExtra(CoachDashboardActivity.USER_DATA);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);
        CopyID=findViewById(R.id.copyID);
        CopyID.setVisibility(View.INVISIBLE);
    }

    public void SaveTeam(View v) {
        NewTeam = findViewById(R.id.JustCreatedTeam);
        NewTeamId = findViewById(R.id.JustCreatedTeamId);
        TeamName = findViewById(R.id.teamname);
        String name = TeamName.getText().toString();
        // generer ID de l'equipe
        teamId= RandomString.getAlphaNumericString(6);
        Map<String, Object> team = new HashMap<>();
        team.put("Nom Equipe", name);
        team.put("Email Coach", email);
        team.put("ID", teamId);
        // enregistrer les donnees de l'equipe dans un nouveau document dans la collection Equipe
        db.collection("Equipe")
                .add(team)
                .addOnSuccessListener(documentReference -> {
                    Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                    NewTeam.setText("Votre équipe a été enregistrée"  );
                    NewTeamId.setText("Son ID est :" +teamId );
                    CopyID.setVisibility(View.VISIBLE);

                })
                .addOnFailureListener(e -> Log.w("TAG", "Error adding document", e));
    }
    // si on clique sur copier, l'ID de l'equipe créée sera copiée dans le presse-papier
    public void  CopyID(View v){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("ID de l'équipe", teamId);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Votre ID est copié dans le presse-papier", Toast.LENGTH_SHORT).show();
    }

    static class RandomString {
        static String getAlphaNumericString(int n) {
            // length is bounded by 256 Character
            byte[] array = new byte[256];
            new Random().nextBytes(array);
            String randomString = new String(array, Charset.forName("UTF-8"));
            // Create a StringBuffer to store the result
            StringBuilder r = new StringBuilder();
            // remove all spacial char
            String AlphaNumericString = randomString.replaceAll("[^A-Za-z0-9]", "");
            // Append first 20 alphanumeric characters
            // from the generated random String into the result
            for (int k = 0; k < AlphaNumericString.length(); k++) {
                if (Character.isLetter(AlphaNumericString.charAt(k))
                        && (n > 0)
                        || Character.isDigit(AlphaNumericString.charAt(k))
                        && (n > 0)) {
                    r.append(AlphaNumericString.charAt(k));
                    n--;
                }
            }
            // return the resultant string
            return r.toString();
        }
    }

}
