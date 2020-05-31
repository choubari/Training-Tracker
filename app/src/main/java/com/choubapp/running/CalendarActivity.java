package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {
    //Menu Calendrier (Membre)
    String TeamID;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CalendarView calendarView;
    List<String> TrainingDates = new ArrayList<>();
    int b=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        TeamID= intent.getStringExtra(DashboardActivity.USER_TEAM);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        setEvent(TeamID);
        calendarView = findViewById(R.id.calendarView);

        //chercher si la date choisie correspond à une date d'entrainement
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDayCalendar = eventDay.getCalendar();
            String day= String.format("%02d", clickedDayCalendar.get(Calendar.DAY_OF_MONTH));
            String month=String.format("%02d",clickedDayCalendar.get(Calendar.MONTH)+1);
            String year=String.valueOf(clickedDayCalendar.get(Calendar.YEAR));
            String selectedDate =day+"-"+month+"-"+year;
            if (TrainingDates.contains(selectedDate)){
                String title ="Entraînement(s) pour : " +selectedDate;
                final String[] message = {""};
                CollectionReference entr = db.collection("Entrainement");
                // on cherche dans la collection Entrainement les entrainements de la même équipe et durant la date séléctionnée
                entr.whereEqualTo("Date", selectedDate).whereEqualTo("TeamID", TeamID)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    // si il existe des entrainements, on crée ce message d'info et l'afficher dans training_info.xml
                                    Log.d("TAG", document.getId() + " => " + document.getData());
                                    message[0] = message[0] + "Entraînement: " + document.get("TrainingName") + "\n"
                                            +  "Description: " +document.get("Description")+ "\n"
                                            + "Date: " + selectedDate + "\n"
                                            + "Heure: " + document.get("HeureDep") + " --> " + document.get("HeureArr") + "\n"
                                            + "Lieu: " + document.get("LieuDep") + " --> " + document.get("LieuArr") + "\n"+ "\n"+ "\n";
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
                        });
            }
        });
    }
    public void setEvent(String id){
        //ajouter une icon sous la date de chaque entrainement dans le calendrier
        List<EventDay> events = new ArrayList<>();
        calendarView = findViewById(R.id.calendarView);
        CollectionReference entrainement = db.collection("Entrainement");
        entrainement.whereEqualTo("TeamID", id)
                .get()
                .addOnCompleteListener(task -> {
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
                });
    }

    public void BacktoDashboard(View v) {
        Intent intent = new Intent(this, DashboardActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //si nous somme dans training_info.xml on revient au calendrier, sinon on revient au dashboard
        if (b == 1 ) {
            b = 0;
            finish();
            startActivity(getIntent());
        } else {
            super.onBackPressed();
        }
    }
}
