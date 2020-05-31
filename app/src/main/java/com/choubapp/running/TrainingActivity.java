package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.choubapp.running.DashboardActivity.USER_DATA;
import static com.choubapp.running.DashboardActivity.USER_TEAM;

public class TrainingActivity extends AppCompatActivity {
    String email,teamID,CoachMail,NextTrainingID, UserUsername, UserFullname;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference Trainings = db.collection("Entrainement");
    DocumentReference TrainingTracking ;
    TextView countdown;
    Date minDate;  Timestamp started;
    Boolean alreadyStarted = false , running=true;
    TextView next;
    Button startButton;
    ArrayList<Timestamp> Dates = new ArrayList<>();
    List<String> IDs = new ArrayList<>();
    ProgressBar progressBar;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        teamID= intent.getStringExtra(USER_TEAM);
        email= intent.getStringExtra(USER_DATA);
        UserFullname=intent.getStringExtra("userFullName");
        UserUsername=intent.getStringExtra("username");
        getCoachMail();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        startButton =findViewById(R.id.startMemberbutton);
        startButton.setVisibility(View.INVISIBLE);
        replaceProgressbar();
    }
    private void replaceProgressbar(){
// chercher la date la plus proche du prochain entrainement
        Trainings.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String trainingName = document.getString("TrainingName");
                    String team_id = document.getString("TeamID");
                    if (trainingName!=null && team_id.equals(teamID) ){
                        String mdate = document.get("Date").toString();
                        String mTimeDep = document.get("HeureDep").toString();
                        String mTimeArr = document.get("HeureArr").toString();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                        Date parsedDateDep=null;
                        Date parsedDateArr=null;
                        try {
                            parsedDateDep = dateFormat.parse(mdate +" "+mTimeDep);
                            Timestamp timestampDep = new Timestamp(parsedDateDep.getTime());
                            parsedDateArr = dateFormat.parse(mdate +" "+mTimeArr);
                            Timestamp timestampArr = new Timestamp(parsedDateArr.getTime());
                            Date datee= new Date();
                            Timestamp mytime = new Timestamp(datee.getTime());
                            if(mytime.before(timestampDep)){
                                Dates.add(timestampDep);
                                IDs.add(document.getId());
                            }
                            if(mytime.before(timestampArr) && mytime.after(timestampDep)){
                                alreadyStarted =true;
                                NextTrainingID = document.getId();
                                started = timestampDep;
                            }
                        } catch(Exception e) {
                            System.out.println("Exception :" + e);
                        }
                    }
                }

            }else{Log.d("TAG", "Error getting document: "+task.getException());}
            Handler handler = new Handler();
            handler.postDelayed(() -> updateText(), 2000);
        });
    }
    private void updateText(){
        // mise a jour du text
        progressBar=findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);
        //mise à jour du text selon l'existance du proche entrainement
        next=findViewById(R.id.nexttraining);
        if (Dates.isEmpty() && !alreadyStarted) {
             next.setText("Vous n'avez aucun prochain entraînement");
        }else {
            if(alreadyStarted) {
                next.setText("Votre entraînement est déjà commencé depuis : \n");
                minDate = started;
                startTimer(minDate);
            }else{
                minDate = Collections.min(Dates);
                int index = Dates.indexOf(minDate);
                NextTrainingID = IDs.get(index);
                String[] SplitedDate = minDate.toString().split(" ", 2);
                next.setText("Votre prochain entraînement sera le : \n" + SplitedDate[0] + " à " + SplitedDate[1] + "\n" + "il vous reste :");
                startTimer(minDate);

            }
        }
    }
    private void startTimer(Date end) {
        // thread pour demarrer le compteur
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted() && running) {
                        Thread.sleep(1000);
                        runOnUiThread(() -> {
                            SimpleDateFormat simpleDateFormat =
                                    new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.s");
                            Date date2=null;
                            try {
                                next=findViewById(R.id.nexttraining);
                                Date date1 = new Date();

                                date2 = simpleDateFormat.parse(String.valueOf(end));
                                if (alreadyStarted) {
                                    if (date2 != null) {
                                        printDifference(date2, date1);
                                    }
                                }
                                else if (date2 != null) {
                                    printDifference(date1, date2);
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        thread.start();

    }
    public void printDifference(Date startDate, Date endDate){
        // afficher difference entre deux dates en jours, heurs, minutes et secondes
        long different = endDate.getTime() - startDate.getTime();
        if (different<= 1800000 || alreadyStarted) {
            startButton =findViewById(R.id.startMemberbutton);
            startButton.setVisibility(View.VISIBLE);
        }
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;
        countdown=findViewById(R.id.countdown);
        if (elapsedDays!=0) {
            countdown.setText(elapsedDays + " jours, " + elapsedHours + " heures, " + elapsedMinutes + " minutes, " + elapsedSeconds + " seconds");
        }else
            countdown.setText(elapsedHours+" heures, "+elapsedMinutes+" minutes, "+elapsedSeconds+" seconds");

    }

// clique sur le boutton commencer l'entrainment
    public void StartTraining(View v){
        TrainingTracking = db.collection("tracking").document(NextTrainingID);
        TrainingTracking.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d("TAG", "Document exists!");
                    String trainingStatus = document.get("Status").toString();
                    checkTrainingStatus(trainingStatus);
                } else {
                    Log.d("TAG", "Document does not exist!");
                    Toast.makeText(getApplicationContext(), "Votre entraînement n'est encore démarré par votre Coach, Veuillez Patienter ..", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d("TAG", "Failed with: ", task.getException());
            }
        });
    }

    private void checkTrainingStatus(String status){
    // verifier si le coach a démarré l'entrainement
        if (status.equals("start")) {
            thread.interrupt();
            Intent intent = new Intent(this, MemberTrainingTime.class);
            intent.putExtra("TrainingID",NextTrainingID);
            intent.putExtra("usermail",email);
            intent.putExtra("userFullName",UserFullname);
            intent.putExtra("username",UserUsername);
            startActivity(intent);
            finish();
            running=false;
        }else{
            if (status.equals("stop"))
                Toast.makeText(this, "Votre entraînement est déjà arrêté par votre Coach", Toast.LENGTH_SHORT).show();
        }
    }
    // recupere l eamil du coach
    public void getCoachMail(){
        CollectionReference equipe = db.collection("Equipe");
        equipe.whereEqualTo("ID", teamID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Log.d("TAG", document.getId() + " => " + document.getData());
                            CoachMail = document.get("Email Coach").toString();
                        }

                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }
                });

    }
    @Override
    public void onBackPressed() {
        running=false;
        super.onBackPressed();
    }
    public void BacktoDashboard(View v) {
        Intent intent = new Intent(this, DashboardActivity.class);
        running=false;
        finish();
        startActivity(intent);
    }
}
