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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.choubapp.running.CoachDashboardActivity.USER_DATA;

public class UpdateTraining extends AppCompatActivity {
    String email;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference Trainings = db.collection("Entrainement");
    CollectionReference Teams = db.collection("Equipe");
    ScrollView Data;
    Boolean b ;
    String currentTraining,LoadedTeam;
    Spinner sp;
    TextView Name, Desc, date, LieuDep, LieuArr, TimeDep,TimeArr;
    String docID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        email= intent.getStringExtra(USER_DATA);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_training);
        Data=findViewById(R.id.trainingINFO);
        Data.setVisibility(View.INVISIBLE);
        Spinner spinner = findViewById(R.id.spinnertrainings);
        LoadSpinnerData(spinner,"TrainingName",Trainings);
    }
    public void LoadSpinnerData(Spinner spinner, String field, CollectionReference trainings){
        List<String> TrainingsList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, TrainingsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        trainings.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String trainingName = document.getString(field);
                    String coachmail = document.getString("Email Coach");
                    if (trainingName!=null && coachmail.equals(email) ){
                        TrainingsList.add(trainingName);
                    }
                    if (field.equals("Nom Equipe")) sp.setSelection(((ArrayAdapter<String>)sp.getAdapter()).getPosition(LoadedTeam));

                }
                adapter.notifyDataSetChanged();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentTraining= parent.getSelectedItem().toString();
                RetreiveTrainingData(currentTraining);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    // afficher les données de l'entrainement dans les cases correspondantes
    public void RetreiveTrainingData(String tr){
        b=false;
        Data.setVisibility(View.VISIBLE);
        Name=findViewById(R.id.trainingname);
        Desc=findViewById(R.id.trainingdesc);
        date=findViewById(R.id.trainingdate);
        LieuDep=findViewById(R.id.depart);
        LieuArr=findViewById(R.id.arrive);
        TimeDep=findViewById(R.id.heuredep);
        TimeArr=findViewById(R.id.heurearr);
        Trainings.whereEqualTo("TrainingName", tr)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Log.d("TAG", document.getId() + " => " + document.getData());
                            docID= document.getId();
                            Name.setText(document.get("TrainingName").toString());
                            Desc.setText(document.get("Description").toString());
                            date.setText(document.get("Date").toString());
                            LieuDep.setText(document.get("LieuDep").toString());
                            LieuArr.setText(document.get("LieuArr").toString());
                            TimeDep.setText(document.get("HeureDep").toString());
                            TimeArr.setText(document.get("HeureArr").toString());
                            LoadedTeam=document.get("Team").toString();
                            System.out.println(b);
                            sp=findViewById(R.id.pickteam);
                            if (!b){
                                selectTeam(sp);
                                b=true;
                            }
                        }
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }
                });
    }
    // action si le coach clique sur spinner, il affiche les noms de ses équipes
    public void selectTeam(View v){
        sp=findViewById(R.id.pickteam);
        LoadSpinnerData(sp, "Nom Equipe",Teams);
    }
    // enregistrer les données modifiées
    public void SaveEdited(View v) {
        db.collection("Entrainement").document(docID).update(
                "TrainingName",Name.getText().toString(),
                "Description",Desc.getText().toString(),
                "Date", date.getText().toString(),
                "LieuDep",LieuDep.getText().toString(),
                "LieuArr",LieuArr.getText().toString(),
                "HeureDep",TimeDep.getText().toString(),
                "Team",sp.getSelectedItem().toString(),
                "HeureArr",TimeArr.getText().toString()
        );
        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();
        BacktoDashboard(v);

    }
    // clique sur le boutton de suppression de l'entrainement
    public void deleteTraining(View v) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer l'Entraînement")
                .setMessage("Voulez-vous supprimer cet entraînement? (action irreversible)")
                .setPositiveButton("Oui", (dialog, which) -> db.collection("Entrainement").document(docID)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("TAG", "DocumentSnapshot successfully deleted!");
                            BacktoDashboard(v);
                        })
                        .addOnFailureListener(e -> Log.w("TAG", "Error deleting document", e)))
                .setNegativeButton("Non", null)
                .show();
    }
    // fenetre pour choisir la date
    public void DatePicker(View v) throws ParseException {
        final DatePickerDialog[] picker = new DatePickerDialog[1];
        date= findViewById(R.id.trainingdate);
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        picker[0] = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> date.setText(String.format("%02d", dayOfMonth) + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + year1), year, month, day);
        picker[0].getDatePicker().setMinDate(new Date().getTime());
        picker[0].show();
    }
    // fenetre pour choisir l'heure de départ
    public void TimePickerDep(View v){
        TimeDep= findViewById(R.id.heuredep);
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> TimeDep.setText(String.format("%02d", selectedHour )+ ":" + String.format("%02d", selectedMinute)), hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }
    // fenetre pour choisir l'heure d'arrivée
    public void TimePickerArr(View v){
        TimeArr= findViewById(R.id.heurearr);
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> TimeArr.setText( String.format("%02d", selectedHour )+ ":" + String.format("%02d", selectedMinute)), hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }
    public void BacktoDashboard(View v) {
        Intent intent = new Intent(this, CoachDashboardActivity.class);
        finish();
        startActivity(intent);
    }

}
