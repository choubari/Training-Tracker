package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
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

import static com.choubapp.running.CoachDashboardActivity.USER_DATA;

public class CoachTrainingActivity extends AppCompatActivity {
    String email;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference Trainings = db.collection("Entrainement");
    TextView countdown;
    Date minDate;  Timestamp started;
    Boolean alreadyStarted = false;
    TextView next;
    Button startButton;
    String NextTrainingID;
    Thread thread;
    boolean running=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        email= intent.getStringExtra(USER_DATA);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_training);
        startButton =findViewById(R.id.startCoachbutton);
        startButton.setVisibility(View.INVISIBLE);
        replaceProgressbar();
        // thread pour actualiser le text du compteur
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted() && running) {
                        Thread.sleep(1000);
                        runOnUiThread(() -> startTimer(minDate));
                    }
                } catch (InterruptedException e) {
                    System.out.println("Exception : " + e);
                }
            }
        };

        thread.start();
    }

    private void replaceProgressbar(){
        ArrayList<Timestamp> Dates = new ArrayList<>();
        List<String> IDs = new ArrayList<>();
// chercher la date la plus proche du prochain entrainement
        Trainings.whereEqualTo("Email Coach", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ProgressBar progressBar;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String trainingName = document.getString("TrainingName");
                    if (trainingName!=null ){
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
                //recuperer la plus proche date et mettre a jour les texts
                progressBar=findViewById(R.id.progressBar2);
                progressBar.setVisibility(View.GONE);
                next=findViewById(R.id.nexttraining);
                if (Dates.isEmpty() && !alreadyStarted)
                {
                    next.setText("Vous n'avez aucun prochain entraînement");
                }
                else { if(alreadyStarted) {
                    next.setText("Votre entraînement est déjà commencé depuis : \n");
                    minDate = started;
                } else{
                        minDate = Collections.min(Dates);
                        int index = Dates.indexOf(minDate);
                        NextTrainingID = IDs.get(index);
                        String[] SplitedDate = minDate.toString().split(" ", 2);
                        next.setText("Votre prochain entraînement sera le : \n" + SplitedDate[0] + " à " + SplitedDate[1] + "\n" + "il vous reste :");
                     }
                }

            }
        });
    }
    private void startTimer(Date end) {
        // demarrer le compteur
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.s");
        try {
            next=findViewById(R.id.nexttraining);
            Date date1 = new Date();
            Date date2 = simpleDateFormat.parse(String.valueOf(end));
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
    }
    public void printDifference(Date startDate, Date endDate){
        // afficher difference entre deux dates en jours, heurs, minutes et secondes
        long different = endDate.getTime() - startDate.getTime();
        if (different<= 1800000 || alreadyStarted) {
            startButton =findViewById(R.id.startCoachbutton);
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
        thread.interrupt();
        Intent intent = new Intent(this, CoachTrainingTime.class);
        intent.putExtra(USER_DATA,email);
        intent.putExtra("TrainingID",NextTrainingID);
        startActivity(intent);
        finish();
        running=false;
    }


    @Override
    public void onBackPressed() {
        running=false;
        super.onBackPressed();
    }
    public void BacktoDashboard(View v) {
        Intent intent = new Intent(this, CoachDashboardActivity.class);
        running=false;
        finish();
        startActivity(intent);
    }
    // clique sur creer entrainement
    public void CreateTraining(View v){
        Intent intent = new Intent(this, CreateTraining.class);
        intent.putExtra(USER_DATA, email);
        finish();
        startActivity(intent);
    }
    // clique sur modifier entrainement
    public void UpdateTraining(View v){
        Intent intent = new Intent(this, UpdateTraining.class);
        intent.putExtra(USER_DATA, email);
        finish();
        startActivity(intent);
    }
}
