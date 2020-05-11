package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {
    String TeamID;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CalendarView calendarView;
    List<String> TrainingDates = new ArrayList<>();
    String name ;
    int b=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        TeamID= intent.getStringExtra(DashboardActivity.USER_TEAM);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        getTeamName(TeamID);
        calendarView = (CalendarView) findViewById(R.id.calendarView);
        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clickedDayCalendar = eventDay.getCalendar();
                String day= String.valueOf(clickedDayCalendar.get(Calendar.DAY_OF_MONTH));
                String month=String.valueOf(clickedDayCalendar.get(Calendar.MONTH)+1);
                String year=String.valueOf(clickedDayCalendar.get(Calendar.YEAR));
                String selectedDate =day+"-"+month+"-"+year;
                if (TrainingDates.contains(selectedDate)){
                    String title ="Entraînement(s) pour : " +selectedDate;
                    final String[] message = {""};
                    CollectionReference equipe = db.collection("Entrainement");
                    equipe.whereEqualTo("Date", selectedDate).whereEqualTo("Team", name)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @SuppressLint("ResourceType")
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (DocumentSnapshot document : task.getResult()) {
                                            Log.d("TAG", document.getId() + " => " + document.getData());
                                            message[0] = message[0] + "Entraînement: " + document.get("TrainingName") + "\n"
                                                    +  "Description: " +document.get("Description")+ "\n"
                                                    + "Date: " + selectedDate + "\n"
                                                    + "Heure: " + document.get("HeureDep") + " --> " + document.get("HeureArr") + "\n"
                                                    + "Lieu: " + document.get("LieuDep") + " --> " + document.get("LieuArr") + "\n"+ "\n"+ "\n";
                                            System.out.println(message[0]);
                                        }
                                        setContentView(R.layout.training_info);
                                        b=1;
                                        TextView tit=findViewById(R.id.info_title);
                                        TextView msg=findViewById(R.id.info_message);
                                        tit.setText(title);
                                        msg.setText(message[0]);
                                    } else {
                                        Log.d("TAG", "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                }
            }
        });
    }

    public void getTeamName(String id){

        CollectionReference equipe = db.collection("Equipe");
        equipe.whereEqualTo("ID", id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                name =document.get("Nom Equipe").toString();
                            }
                            setEvents(name);
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void setEvents(String name){
        List<EventDay> events = new ArrayList<>();
        calendarView = (CalendarView) findViewById(R.id.calendarView);
        CollectionReference equipe = db.collection("Entrainement");
        equipe.whereEqualTo("Team", name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                String DateStr =document.get("Date").toString();
                                TrainingDates.add(DateStr);
                                String[] arrOfStr = DateStr.split("-");
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Integer.parseInt(arrOfStr[2]), Integer.parseInt(arrOfStr[1])-1, Integer.parseInt(arrOfStr[0]));
                                events.add(new EventDay(calendar, R.drawable.emp));

                            }
                            calendarView.setEvents(events);
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
    @Override
    public void onBackPressed() {

        if (b == 1 ) {
            b = 0;
            finish();
            startActivity(getIntent());
        } else {
            super.onBackPressed();
        }
    }
}
