package com.choubapp.running;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import java.text.ParseException;
import java.util.Date;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {
    private String mDate;
    private TextInputEditText mFullNameView;
    private TextInputEditText mEmailView;
    private TextInputEditText mUsernameView;
    private TextInputEditText mPasswordView;
    private TextInputEditText mConfirmPasswordView;
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mFullNameView =  findViewById(R.id.register_full_name);
        mEmailView =  findViewById(R.id.register_email);
        mPasswordView =  findViewById(R.id.register_password);
        mConfirmPasswordView = findViewById(R.id.register_confirm_password);
        mUsernameView = findViewById(R.id.register_username);

        // Keyboard sign in action
        mConfirmPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == R.integer.register_form_finished || id == EditorInfo.IME_NULL) {
                attemptRegistration();
                pushtoRightCollection();
                return true;
            }
            return false;
        });
        mAuth = FirebaseAuth.getInstance();
    }



    private static boolean isBrokenSamsungDevice() {
        return (Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && isBetweenAndroidVersions(
                Build.VERSION_CODES.LOLLIPOP,
                Build.VERSION_CODES.LOLLIPOP_MR1));
    }
    private static boolean isBetweenAndroidVersions(int min, int max) {
        return Build.VERSION.SDK_INT >= min && Build.VERSION.SDK_INT <= max;
    }
    // fenetre pour afficher date de naissance
    public void BirthDatePicker(View v) throws ParseException {
        final DatePickerDialog[] picker = new DatePickerDialog[1];
        final TextInputEditText eText;
        eText= findViewById(R.id.editText1);
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        Context context = RegisterActivity.this;
        if (isBrokenSamsungDevice()) {
            context = new ContextThemeWrapper(RegisterActivity.this, android.R.style.Theme_Holo_Light_Dialog);
        }
        picker[0] = new DatePickerDialog(context, (view, year1, monthOfYear, dayOfMonth) -> {
            eText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1);
            setDate( dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1);
            }, year, month, day);
        picker[0].getDatePicker().setMaxDate(new Date().getTime());
        picker[0].show();
    }

    private void pushtoRightCollection(){
        String fullname = mFullNameView.getText().toString();
        String email = mEmailView.getText().toString().toLowerCase();
        String password = mPasswordView.getText().toString();
        String username = mUsernameView.getText().toString();
        // creer un nouveau utilisateur
        Map<String, Object> user = new HashMap<>();
        user.put("FullName", fullname);
        user.put("Email", email);
        user.put("Password", password);
        user.put("Username", username);
        user.put("Birth",mDate);
        RadioGroup Member_Coach= findViewById(R.id.member_coach);
        int selectedid=Member_Coach.getCheckedRadioButtonId();
        RadioButton UserSelected=findViewById(selectedid);
        String SelectedUser= UserSelected.getText().toString();
        // si l'utilisateur coche la case membre:
        if (SelectedUser.equals("Membre")) {
            user.put("Team", "");
            // ajouter un nouveau document à la collection member
            db.collection("member")
                    .add(user)
                    .addOnSuccessListener(documentReference -> Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId()))
                    .addOnFailureListener(e -> Log.w("TAG", "Error adding document", e));
        }
        else{
            // si l'utilisateur coche qu'il est coach
            // ajouter un nouveau document à la collection coach
            db.collection("coach")
                    .add(user)
                    .addOnSuccessListener(documentReference -> Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId()))
                    .addOnFailureListener(e -> Log.w("TAG", "Error adding document", e));
        }
    }

    // si l'utilisateur clique sur creer mon compte
    public void signUp(View v) {
        attemptRegistration();
    }
    // verifier si les données entrées sont valides
    private void attemptRegistration() {
        //initialiser les erreurs à null
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mFullNameView.setError(null);
        mUsernameView.setError(null);
        // récupérer les donnees entrées dans les cases
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String fullname = mFullNameView.getText().toString();
        String username = mUsernameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //verifier si le password entré est valide
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        //verifier si username est non null
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }
        // verifier si username contient un espace
        if (username.contains(" ")){
            mUsernameView.setError("Ce champ ne doit pas contenir un espace");
            focusView = mUsernameView;
            cancel = true;
        }

        //verifier si le nom n'est pas nul
        if (TextUtils.isEmpty(fullname)) {
            mFullNameView.setError(getString(R.string.error_field_required));
            focusView = mFullNameView;
            cancel = true;
        }
        // verifier si l'email est valide
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // il existe une erreur, on l'affiche sur la case correspondante
            focusView.requestFocus();
        } else {
            // toutes les conditions sont valides, on crée un utilisateur dans firebase
            createFirebaseUser();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        String ConfirmPassword =mConfirmPasswordView.getText().toString();
        return ConfirmPassword.equals(password) && password.length()>4;
    }
    public void LoginButton(View v) {
        Intent intent = new Intent(this, com.choubapp.running.MainActivity.class);
        startActivity(intent);
    }
    //creer un utilisateur sur Firebase
    private  void  createFirebaseUser (){
        String email = mEmailView.getText().toString().toLowerCase();
        String password = mPasswordView.getText().toString();
        Toast.makeText(this,"Registration in Progress...",Toast.LENGTH_SHORT).show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            Log.d("running : " ,"create user" + task.isSuccessful());
            if (!task.isSuccessful()) {
                Log.d("running :"," user creation failed");
                showErrorDialog("Registration Attempt Failed!");
            } else{
                // ajouter les donnees de cet utilisateur dans la bonne collection
                pushtoRightCollection();
                Intent intent =new Intent(RegisterActivity.this,MainActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }
    // fenetre d'erreur d'inscription
    private void showErrorDialog (String message) {
    new AlertDialog.Builder(this)
            .setTitle("Oops")
            .setMessage(message)
            .setPositiveButton(android.R.string.ok,null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    public void setDate(String date) {
        mDate = date;
    }
}
