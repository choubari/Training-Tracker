package com.choubapp.running;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {
    static final String LOGIN_KEY = "LoginData";
    static final String LOGIN_EMAIL = "com.choubapp.running.LOGIN_EMAIL";

    //static final String PASSWORD_KEY = "Password";
    String Lemail , Lpassword, Lcheckbox;
    private FirebaseAuth mAuth;
    SharedPreferences prefs;
    SharedPreferences.Editor mEditor;

    // UI references.
    private TextInputEditText mEmailView;
    private TextInputEditText mPasswordView;
    private CheckBox mCheckBox;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: Grab an instance of FirebaseAuth
        mAuth= FirebaseAuth.getInstance();

        mEmailView =  findViewById(R.id.login_email);
        mPasswordView =  findViewById(R.id.login_password);
        mCheckBox= findViewById(R.id.checkBox);
        //todo: fix this, cant modify mEmailView and mPasswordView
        showLoginData();
        //attemptLogin();
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
    }

    // Executed when Sign in button pressed
    public void signInExistingUser(View v)   {
        // TODO: Call attemptLogin() here
        attemptLogin();
    }

    // Executed when Register button pressed
    public void registerNewUser(View v) {
        Intent intent = new Intent(this, com.choubapp.running.RegisterActivity.class);
        startActivity(intent);
    }

    // TODO: Complete the attemptLogin() method
    private void attemptLogin() {

        final String email =mEmailView.getText().toString().toLowerCase();
        String password = mPasswordView.getText().toString();

        if (email.equals("")||password.equals("")) return;
        Toast.makeText(this,"Login in Progress...",Toast.LENGTH_SHORT).show();
        // TODO: Use FirebaseAuth to sign in with email & password
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("running :","SignIn"+task.isSuccessful());
                if (!task.isSuccessful()) {
                    Log.d("running :", "Problem signIn"+task.getException() );
                    showErrorDialog("Problem while signin in");}
                else {
                    saveLoginData();
                    GoToCoachOrMember(email);
                }
            }
        });
    }

    private void GoToCoachOrMember(String email){
        db.setFirestoreSettings(settings);
        CollectionReference peopleRef = db.collection("member");
        peopleRef.whereEqualTo("Email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Boolean docfound=false;
                            for (DocumentSnapshot document : task.getResult()) {
                                docfound = true;
                                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                intent.putExtra(LOGIN_EMAIL, email);
                                finish();
                                startActivity(intent);
                            }
                            if (!docfound) {
                                Intent intent = new Intent(MainActivity.this, CoachDashboardActivity.class);
                                intent.putExtra(LOGIN_EMAIL, email);
                                finish();
                                startActivity(intent);
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    private void saveLoginData(){
        prefs = getSharedPreferences(LOGIN_KEY,0);
        mEditor = prefs.edit();
        if (mCheckBox.isChecked()){
        mEditor.putString("Email",mEmailView.getText().toString());
        mEditor.commit();
        mEditor.putString("Password",mPasswordView.getText().toString());
        mEditor.commit();
        mEditor.putString("Checkbox","true");
        mEditor.commit();
        }
                 else {
                        mEditor.putString("Email","");
                        mEditor.commit();
                        mEditor.putString("Password","");
                        mEditor.commit();
                        mEditor.putString("Checkbox","false");
                    mEditor.commit();}

        //prefs.edit().putString(getString).apply();
    }
    private void showLoginData(){

        prefs = getSharedPreferences(LOGIN_KEY,0);
        //Lemail = prefs.getString(MainActivity.EMAIL_KEY,null);
        Lemail = prefs.getString("Email",null);
        //System.out.println(Lemail);
        //Lpassword = prefs.getString(MainActivity.PASSWORD_KEY,null);
        Lpassword = prefs.getString("Password",null);
        Lcheckbox = prefs.getString("Checkbox","false");
        mEmailView.setText(Lemail);
        mPasswordView.setText(Lpassword);
        //mCheckBox.setChecked(true);
        if (Lcheckbox.equals("true")) mCheckBox.setChecked(true);
        else mCheckBox.setChecked(false);
        //if (Lemail==null || Lpassword==null) return false;
        //return true;
    }
    // TODO: Show error on screen with an alert dialog
    private void showErrorDialog (String message) {
        new AlertDialog.Builder(this)
                .setTitle("Oops")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }



}