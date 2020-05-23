package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class TasksActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference Member = db.collection("member");
    CollectionReference Trainings = db.collection("Entrainement");
    CollectionReference Trackings = db.collection("tracking");
    String ID;
    RelativeLayout loading;
    RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    HashMap<Timestamp,String> EndedTrainings ;
    Map<Timestamp, String> map ;
    ArrayList<Timestamp> completedDates = new ArrayList<>();
    ArrayList<String> completedDocID = new ArrayList<>();
    ArrayList<Long> durations = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        getTeamID();
    }

    private void getTeamID() {
        Member.whereEqualTo("Email", firebaseAuth.getCurrentUser().getEmail()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                ID = document.get("Team").toString();
                                getPreviousTeamTrainings(ID);
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                            Toast.makeText(TasksActivity.this, "Vous devrez être membre 'une équipe et completer au moins un entraînement", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void getPreviousTeamTrainings(String Team){
        EndedTrainings = new HashMap<>();
        Trainings.whereEqualTo("TeamID",Team).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String trainingName = document.getString("TrainingName");
                        if (trainingName!=null){
                            String mdate = document.get("Date").toString();
                            String mTimeDep = document.get("HeureDep").toString();
                            String mTimeArr = document.get("HeureArr").toString();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                            Date parsedDateDep=null;
                            Date parsedDateArr=null;
                            String DateDep = mdate +" "+mTimeDep;
                            String DateArr = mdate +" "+mTimeArr;
                            try {
                                parsedDateDep =(Date) dateFormat.parse(DateDep);
                                Timestamp timestampDep = new Timestamp(parsedDateDep.getTime());
                                parsedDateArr =(Date) dateFormat.parse(DateArr);
                                Timestamp timestampArr = new Timestamp(parsedDateArr.getTime());
                                Date datee= new Date();
                                Timestamp mytime = new Timestamp(datee.getTime());
                                if(mytime.after(timestampArr)){
                                    EndedTrainings.put(timestampArr,document.getId());
                                    // System.out.println("hashmap ended trainings"+EndedTrainings);
                                }

                            } catch(Exception e) {
                                e.printStackTrace();
                                System.out.println("Exception :" + e);
                            }
                        }
                    }getParticipantTrackingData();
                }
            }
        });
    }

    private void getParticipantTrackingData(){
        map = new TreeMap<Timestamp, String>(EndedTrainings);
        System.out.println(map);
        if (map.size()==0){
            loading=findViewById(R.id.loading);
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Vous n'avez complété aucun entraînement", Toast.LENGTH_SHORT).show();
        }
        for (int i=0 ; i<map.size(); i++){
            Timestamp key = (Timestamp) map.keySet().toArray()[i];
            String docID= map.get(key);
            int finalI = i;
            Trackings.document(docID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            System.out.println("training found");
                            Log.d("TAG", "Document exists!");
                            Trackings.document(docID).collection("Participants").document(firebaseAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        DocumentSnapshot doc = task.getResult();
                                        if (doc.exists()){
                                            completedDates.add(key);
                                            //completedDocID.add(name);
                                            durations.add(doc.getLong("TotalTime")/60);
                                            System.out.println("Time "+doc.getLong("TotalTime")/60);
                                        }
                                    }
                                    if (finalI == (map.size()-1)) setRecyclerView();
                                }
                            });

                        } else {
                            Log.d("TAG", "Document does not exist!");
                        }
                    } else {
                        Log.d("TAG", "Failed with: ", task.getException());
                    }
                }
            });
        }
    }

    private void setRecyclerView(){
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
        //mAdapter.notifyDataSetChanged();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        System.out.println("adapter set");
        mRecyclerView.setAdapter(mAdapter);
    }


    public void BacktoDashboard(View v) {
        Intent intent = new Intent(this, DashboardActivity.class);
        finish();
        startActivity(intent);
    }
}
