package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class TasksActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference Member = db.collection("member");
    CollectionReference Trainings = db.collection("Entrainement");
    CollectionReference Trackings = db.collection("tracking");
    String ID; // id de l'equipe
    RelativeLayout loading; // layout contenant progressbar
    RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    HashMap<Timestamp,String> EndedTrainings ; // contient les entraînements passés de l'équipe
    Map<Timestamp, String> map ; // contient le Hashmap EndedTrainings triés
    ArrayList<Timestamp> completedDates = new ArrayList<>(); // contient les entraînements complétés par par ce membre
    ArrayList<Long> durations = new ArrayList<>(); // contient les durées parcourues dans chaque entraînement accompli par ce membre


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        getTeamID();
    }
// recuperer id de l'equipe de ce membre
    private void getTeamID() {
        Member.whereEqualTo("Email", firebaseAuth.getCurrentUser().getEmail()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            ID = document.get("Team").toString();
                            getPreviousTeamTrainings(ID);
                        }
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                        Toast.makeText(TasksActivity.this, "Vous devrez être membre 'une équipe et completer au moins un entraînement", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // recuperer les entrainements precedents de son equipe
    private void getPreviousTeamTrainings(String Team){
        EndedTrainings = new HashMap<>();
        Trainings.whereEqualTo("TeamID",Team).get().addOnCompleteListener((OnCompleteListener<QuerySnapshot>) task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    String trainingName = document.getString("TrainingName");
                    if (trainingName!=null){
                        String mdate = document.get("Date").toString();
                        String mTimeArr = document.get("HeureArr").toString();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                        Date parsedDateArr=null;
                        String DateArr = mdate +" "+mTimeArr;
                        try {
                            parsedDateArr = dateFormat.parse(DateArr);
                            Timestamp timestampArr = new Timestamp(parsedDateArr.getTime());
                            Date datee= new Date();
                            Timestamp mytime = new Timestamp(datee.getTime());
                            if(mytime.after(timestampArr)){
                                EndedTrainings.put(timestampArr,document.getId());
                            }

                        } catch(Exception e) {
                            e.printStackTrace();
                            System.out.println("Exception :" + e);
                        }
                    }
                }getParticipantTrackingData();
            }
        });
    }

    private void getParticipantTrackingData(){
        // trier ces entrainements par la date
        map = new TreeMap<Timestamp, String>(EndedTrainings);
        if (map.size()==0){
            loading=findViewById(R.id.loading);
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Vous n'avez complété aucun entraînement", Toast.LENGTH_SHORT).show();
        }
        // recuperer les entrainements accomplies avec les durées parcourues
        for (int i=0 ; i<map.size(); i++){
            Timestamp key = (Timestamp) map.keySet().toArray()[i];
            String docID= map.get(key);
            int finalI = i;
            Trackings.document(docID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "Document exists!");
                        Trackings.document(docID).collection("Participants").document(firebaseAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot doc = task.getResult();
                                    if (doc.exists()){
                                        completedDates.add(key);
                                        durations.add(doc.getLong("TotalTime")/60);
                                    }
                                    if (finalI == (map.size()-1)) {
                                        Handler handler = new Handler();
                                        handler.postDelayed(() -> setRecyclerView(), 1000);
                                    }
                                }
                            }
                        });

                    } else {
                        Log.d("TAG", "Document does not exist!");
                    }
                } else {
                    Log.d("TAG", "Failed with: ", task.getException());
                }
            });
        }
    }

    private void setRecyclerView(){
        // afficher les entrainements complétés avec leurs durées dans Recyclerview
        int i ;
        loading=findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        ArrayList<TrainingTaskItem> MemberTasks = new ArrayList<>();
        for (i=0 ; i< completedDates.size();i++) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(completedDates.get(i));
            String convertedDate = cal.get(Calendar.DAY_OF_MONTH)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.YEAR);
            MemberTasks.add(new TrainingTaskItem("Entraînement "+(i+1), durations.get(i).toString()+ " min", convertedDate ));
        }

        mRecyclerView = findViewById(R.id.tasksRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new TaskAdapter(MemberTasks);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setAdapter(mAdapter);
    }


    public void BacktoDashboard(View v) {
        Intent intent = new Intent(this, DashboardActivity.class);
        finish();
        startActivity(intent);
    }
}
