package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.choubapp.running.CoachDashboardActivity.USER_DATA;
import static com.choubapp.running.DashboardActivity.USER_TEAM;

public class CoachTrainingActivity extends AppCompatActivity {
    String email;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference Trainings = db.collection("Entrainement");
    TextView countdown;
    Date minDate;  Timestamp started;
    Boolean alreadyStarted = false, alreadyFinished=false;
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
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted() && running) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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

        Trainings.whereEqualTo("Email Coach", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
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
                                if(mytime.after(timestampArr)){
                                    alreadyFinished =true;
                                }
                            } catch(Exception e) {
                                System.out.println("Exception :" + e);
                            }
                        }
                    }
                    //look for the closet date
                    //System.out.println(Dates);
                    progressBar=findViewById(R.id.progressBar2);
                    progressBar.setVisibility(View.GONE);
                    next=findViewById(R.id.nexttraining);
                    if (Dates.isEmpty() && alreadyFinished)
                        next.setText("Vous n'avez aucun prochain entraînement");
                    else { if(alreadyStarted) {
                        next.setText("Votre entraînement est déjà commencé depuis : \n");
                        minDate = started;
                        //started
                    } else{
                            minDate = Collections.min(Dates);
                            int index = Dates.indexOf(minDate);
                            NextTrainingID = IDs.get(index);
                            //System.out.println(minDate + " " + NextTrainingID);
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
            startButton =findViewById(R.id.startCoachbutton);
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
        thread.interrupt();
        running=false;
        Intent intent = new Intent(this, CoachTrainingTime.class);
        intent.putExtra(USER_DATA,email);
        intent.putExtra("TrainingID",NextTrainingID);
        startActivity(intent);
        finish();
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
    public void CreateTraining(View v){
        Intent intent = new Intent(this, CreateTraining.class);
        intent.putExtra(USER_DATA, email);
        finish();
        startActivity(intent);
    }
    public void UpdateTraining(View v){
        Intent intent = new Intent(this, UpdateTraining.class);
        intent.putExtra(USER_DATA, email);
        finish();
        startActivity(intent);
    }
}
