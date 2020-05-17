package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
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
    String email,teamID,CoachMail;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference Trainings = db.collection("Entrainement");
    TextView countdown;
    Date minDate;  Timestamp started;
    Boolean alreadyStarted = false , alreadyFinished =false;
    TextView next;
    Button startButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        teamID= intent.getStringExtra(USER_TEAM);
        email= intent.getStringExtra(USER_DATA);
        getCoachMail(teamID);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        startButton =findViewById(R.id.startMemberbutton);
        startButton.setVisibility(View.INVISIBLE);
        replaceProgressbar();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.s");
                                startTimer(minDate);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        thread.start();

    }
    private void replaceProgressbar(){
        ArrayList<Timestamp> Dates = new ArrayList<>();
        List<String> IDs = new ArrayList<>();

        Trainings.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ProgressBar progressBar;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String trainingName = document.getString("TrainingName");
                        String coachmail = document.getString("Email Coach");
                        if (trainingName!=null && coachmail.equals(CoachMail) ){
                            String mdate = document.get("Date").toString();
                            String mTimeDep = document.get("HeureDep").toString();
                            String mTimeArr = document.get("HeureArr").toString();
                            //String[] Str = mdate.split("-", 2);
                            //String[] Tme = mdate.split(":", 1);
                            try {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
                                Date parsedDateDep = dateFormat.parse(mdate +" "+mTimeDep);
                                Timestamp timestampDep = new Timestamp(parsedDateDep.getTime());
                                Date parsedDateArr = dateFormat.parse(mdate +" "+mTimeArr);
                                Timestamp timestampArr = new Timestamp(parsedDateArr.getTime());
                                Date datee= new Date();
                                Timestamp mytime = new Timestamp(datee.getTime());
                                if(mytime.before(timestampDep)){
                                    Dates.add(timestampDep);
                                    IDs.add(document.getId());
                                }
                                if(mytime.before(timestampArr) && mytime.after(timestampDep)){
                                    alreadyStarted =true;
                                    started = timestampDep;
                                }
                                if(mytime.after(timestampArr) ){
                                    alreadyFinished =true;
                                }
                            } catch(Exception e) {
                                System.out.println("Exception :" + e);
                            }
                        }

                    }
                    //look for the closet date
                    System.out.println(Dates);
                    progressBar=findViewById(R.id.progressBar2);
                    next=findViewById(R.id.nexttraining);
                    progressBar.setVisibility(View.GONE);
                    if (Dates.isEmpty() && alreadyFinished)
                        next.setText("Vous n'avez aucun prochain entraînement");

                    else { if(alreadyStarted) {
                        next.setText("Votre entraînement est déjà commencé depuis : \n");
                        minDate = started;
                        //started
                    } else{
                        minDate = Collections.min(Dates);
                        System.out.println(minDate);
                        String[] SplitedDate = minDate.toString().split(" ", 2);
                        next.setText("Votre prochain entraînement sera le : \n" + SplitedDate[0] + " à " + SplitedDate[1] + "\n" + "il vous reste :");
                        //startTimer(minDate);
                    }
                    }

                }
            }
        });
    }
    private void startTimer(Date end) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.s");

        try {
            next=findViewById(R.id.nexttraining);
            Date date1 = new Date();
            Date date2 = simpleDateFormat.parse(String.valueOf(end));
            if (alreadyStarted) {
                printDifference(date2, date1);
            }
            else printDifference(date1, date2);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void printDifference(Date startDate, Date endDate){

        long different = endDate.getTime() - startDate.getTime();
        if (different<= 1800000 || alreadyStarted) {
            startButton =findViewById(R.id.startMemberbutton);
            startButton.setVisibility(View.VISIBLE);
        }
        //System.out.println("startDate : " + startDate);
        //System.out.println("endDate : "+ endDate);
        //System.out.println("different : " + different);

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

    public void StartTraining(View v){

    }


    public void getCoachMail(String id){
        CollectionReference equipe = db.collection("Equipe");
        equipe.whereEqualTo("ID", teamID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                CoachMail = document.get("Email Coach").toString();
                            }

                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }
    public void BacktoDashboard(View v) {
        Intent intent = new Intent(this, DashboardActivity.class);
        finish();
        startActivity(intent);
    }
}
