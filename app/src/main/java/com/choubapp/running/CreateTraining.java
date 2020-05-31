package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.choubapp.running.CoachDashboardActivity.USER_DATA;

public class CreateTraining extends AppCompatActivity {
    private TextView LieuDep,LieuArr,TrainingName, Description;
    private TextInputEditText eDate,eTimeDep, eTimeArr;
    String team,teamID;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference Teams = db.collection("Equipe");
    String email;
    List<String> TeamsList = new ArrayList<>();
    List<String> TeamIDsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        email= intent.getStringExtra(USER_DATA);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_training);
        LoadSpinnerTeams();
    }
    public void LoadSpinnerTeams(){
        // charger les noms des équipe dans le spinner
        Spinner spinner =  findViewById(R.id.pickteam);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, TeamsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        Teams.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String teamname = document.getString("Nom Equipe");
                    String team_id = document.getString("ID");
                    String coachmail = document.getString("Email Coach");
                    if (teamname!=null && coachmail.equals(email) ){
                        TeamIDsList.add(team_id);
                        TeamsList.add(teamname);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                team= parent.getSelectedItem().toString();
                teamID= TeamIDsList.get(TeamsList.indexOf(team));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    // fenetre pour choisir la date de l'entrainement
    public void DatePicker(View v) throws ParseException {
        final DatePickerDialog[] picker = new DatePickerDialog[1];
        eDate= findViewById(R.id.trainingdate);
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        picker[0] = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> eDate.setText(String.format("%02d", dayOfMonth) + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + year1), year, month, day);
        picker[0].getDatePicker().setMinDate(new Date().getTime());
        picker[0].show();
    }
    // fenetre pour choisir heure de depart
    public void TimePickerDep(View v){
        eTimeDep= findViewById(R.id.heuredep);
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> eTimeDep.setText(String.format("%02d", selectedHour )+ ":" + String.format("%02d", selectedMinute)), hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }
    // fenetre pour choisir heure d'arrivée
    public void TimePickerArr(View v){
        eTimeArr= findViewById(R.id.heurearr);
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> eTimeArr.setText( String.format("%02d", selectedHour )+ ":" + String.format("%02d", selectedMinute)), hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }
   // enregistrer l'entrainement
    public void SaveTraining(View v){
        LieuDep =findViewById(R.id.depart);
        LieuArr=findViewById(R.id.arrive);
        TrainingName=findViewById(R.id.trainingname);
        Description=findViewById(R.id.trainingdesc);
        Map<String, Object> training = new HashMap<>();
        training.put("TrainingName", TrainingName.getText().toString());
        training.put("Description", Description.getText().toString());
        training.put("Team", team);
        training.put("TeamID", teamID);
        training.put("Email Coach", email);
        training.put("Date", eDate.getText().toString());
        training.put("HeureDep", eTimeDep.getText().toString());
        training.put("HeureArr", eTimeArr.getText().toString());
        training.put("LieuDep", LieuDep.getText().toString());
        training.put("LieuArr", LieuArr.getText().toString());
        db.collection("Entrainement")
                .add(training)
                .addOnSuccessListener(documentReference -> {
                    Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                    new AlertDialog.Builder(CreateTraining.this)
                            .setTitle("Succes !")
                            .setMessage("Votre entraînement a été créé")
                            .setPositiveButton("Ok", (dialog, which) -> onBackPressed())
                            .show();
                })
                .addOnFailureListener(e -> Log.w("TAG", "Error adding document", e));
    }
}
