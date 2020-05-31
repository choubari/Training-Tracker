package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

import static com.choubapp.running.CoachDashboardActivity.USER_DATA;

public class CoachMemberlistActivity extends AppCompatActivity {
    String email;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference Teams = db.collection("Equipe");
    private MembreAdapter adapter;
    TextView showID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        email= intent.getStringExtra(USER_DATA);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_memberlist);
        LoadSpinnerData();
    }


    public void LoadSpinnerData(){
        // afficher les équipes du coach dans un spinner
        Spinner spinner =  findViewById(R.id.spinner);
        List<String> TeamsList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, TeamsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        Teams.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String teamname = document.getString("Nom Equipe");
                    String coachmail = document.getString("Email Coach");
                    if (teamname!=null && coachmail.equals(email) ){
                        TeamsList.add(teamname);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
        // si une équipe est séléctionnée, on affiche la liste de cette équipe
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String team = parent.getSelectedItem().toString();
                SetRecyclerbyID(team);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
   // rechercher ID de l'équipe selectionnée et appeler la methode setUpRecyclerView
    private void SetRecyclerbyID(String team){
        showID=findViewById(R.id.ShowID);
        final String[] Id = new String[1];
        CollectionReference equipe = db.collection("Equipe");
        equipe.whereEqualTo("Nom Equipe", team)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Log.d("TAG", document.getId() + " => " + document.getData());
                            Id[0]=document.get("ID").toString();
                            showID.setText(Id[0]);
                            setUpRecyclerView(Id[0]);
                        }
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }
                });
    }
    // afficher la liste des membres de l'équipe sélectionnée dans un recyclerview
    private void setUpRecyclerView(String teamid) {
        CollectionReference Members = db.collection("member");
        Query query = Members.whereEqualTo("Team",teamid);
        FirestoreRecyclerOptions<Membre> options = new FirestoreRecyclerOptions.Builder<Membre>()
                .setQuery(query, Membre.class)
                .build();
        adapter = new MembreAdapter(options);
        adapter.startListening();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    public void BacktoDashboard(View v) {
        Intent intent = new Intent(this, CoachDashboardActivity.class);
        finish();
        startActivity(intent);
    }
    // si le coach clique sur le titre créer une équipe
    public void CreateTeam(View v){
        Intent intent = new Intent(this, CreateTeam.class);
        intent.putExtra(USER_DATA, email);
        finish();
        startActivity(intent);
    }
    // copier ID de l'équipe en cliquant sur l'ID affiché en gras
    public void copyID(View v){
        showID=findViewById(R.id.ShowID);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("ID de l'équipe", showID.getText());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Votre ID est copié dans le presse-papier", Toast.LENGTH_SHORT).show();

    }
}
