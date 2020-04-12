package com.choubapp.running;

import android.app.DatePickerDialog;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class Membre {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build();
    Map<String, Object> UserMap;
    private String FullName;
    private String Username;
    private String Email;
    private String Password;
    private String Date;
    private String Gender="";
    private String CoachUsername="";

    public Membre(Map<String, Object> userMap) {
        this.UserMap = userMap;
        this.Username=userMap.get("Username").toString();
        this.FullName=userMap.get("Nom Complet").toString();
        this.Email=userMap.get("Email").toString();
        this.Username=userMap.get("Username").toString();
        this.Password=userMap.get("Password").toString();
        this.Date=userMap.get("Date de Naissance").toString();
    }

    @Override
    public String toString() {
        return ' ' +
                "FullName='" + FullName + '\'' +
                ", Username='" + Username + '\'' +
                ", Email='" + Email + '\'' +
                ", Password='" + Password + '\'' +
                ", Date='" + Date + '\'' +
                ' ';
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gendre) {
        Gender = gendre;
    }

    public String getCoachUsername() {
        return CoachUsername;
    }

    public void setCoachUsername(String coachUsername) {
        CoachUsername = coachUsername;
    }
}
