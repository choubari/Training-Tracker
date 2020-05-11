package com.choubapp.running;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
//import android.support.annotation.NonNull;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import  java.text.SimpleDateFormat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {

    // TODO: Add member variables here:
    private String mDate;
    // UI references.
    private TextInputEditText mFullNameView;
    private TextInputEditText mEmailView;
    private TextInputEditText mUsernameView;
    private TextInputEditText mPasswordView;
    private TextInputEditText mConfirmPasswordView;
    private TextInputEditText eText;
    // Firebase instance variables
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
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.register_form_finished || id == EditorInfo.IME_NULL) {
                    attemptRegistration();
                    pushtoRightCollection();
                    return true;
                }
                return false;
            }
        });

        // TODO: Get hold of an instance of FirebaseAuth
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
    public void BirthDatePicker(View v) throws ParseException {
        final DatePickerDialog[] picker = new DatePickerDialog[1];
        final TextInputEditText eText;
        eText= findViewById(R.id.editText1);
        //eText.setInputType(InputType.TYPE_NULL);
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        Context context = RegisterActivity.this;
        if (isBrokenSamsungDevice()) {
            context = new ContextThemeWrapper(RegisterActivity.this, android.R.style.Theme_Holo_Light_Dialog);
        }
        // date picker dialog  DatePickerDialog.OnDateSetListener listener
        picker[0] = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    eText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    setDate( dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    }
                }, year, month, day);
        picker[0].getDatePicker().setMaxDate(new Date().getTime());
        picker[0].show();
    }

    private void pushtoRightCollection(){
        String fullname = mFullNameView.getText().toString();
        String email = mEmailView.getText().toString().toLowerCase();
        String password = mPasswordView.getText().toString();
        String username = mUsernameView.getText().toString();
        // Create a new user
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
        if (SelectedUser.equals("Membre")) {
            user.put("Team", "");
            // Add a new document with a generated ID
            db.collection("member")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("TAG", "Error adding document", e);
                        }
                    });
        }
        else{
            db.collection("coach")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("TAG", "Error adding document", e);
                        }
                    });
        }
    }

    // Executed when Sign Up button is pressed.
    public void signUp(View v) {
        attemptRegistration();
    }

    private void attemptRegistration() {

        // Reset errors displayed in the form.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mFullNameView.setError(null);
        mUsernameView.setError(null);
        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String fullname = mFullNameView.getText().toString();
        String username = mUsernameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        //Check for Username not null
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }
        if (username.toString().contains(" ")){
            mUsernameView.setError("Ce champ ne doit pas contenir un espace");
            focusView = mUsernameView;
            cancel = true;
        }

        //Check for FullName not null
        if (TextUtils.isEmpty(fullname)) {
            mFullNameView.setError(getString(R.string.error_field_required));
            focusView = mFullNameView;
            cancel = true;
        }
        // Check for a valid email address.
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
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //TODO: add user to collection
            //pushtoRightCollection();
            // TODO: Call create FirebaseUser() here
            createFirebaseUser();
        }
    }

    private boolean isEmailValid(String email) {
        // You can add more checking logic here.
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Add own logic to check for a valid password (minimum 6 characters)
        String ConfirmPassword =mConfirmPasswordView.getText().toString();
        return ConfirmPassword.equals(password) && password.length()>4;
    }
    public void LoginButton(View v) {
        Intent intent = new Intent(this, com.choubapp.running.MainActivity.class);
        startActivity(intent);
    }
    // TODO: Create a Firebase user
    private  void  createFirebaseUser (){
        String email = mEmailView.getText().toString().toLowerCase();
        String password = mPasswordView.getText().toString();
        Toast.makeText(this,"Registration in Progress...",Toast.LENGTH_SHORT).show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("running : " ,"create user" + task.isSuccessful());
                if (!task.isSuccessful()) {
                    Log.d("running :"," user creation failed");
                    showErrorDialog("Registration Attempt Failed!");
                } else{
                    pushtoRightCollection();
                    Intent intent =new Intent(RegisterActivity.this,MainActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        });
    }

    // TODO: Save the display name to Shared Preferences


    // TODO: Create an alert dialog to show in case registration failed
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
