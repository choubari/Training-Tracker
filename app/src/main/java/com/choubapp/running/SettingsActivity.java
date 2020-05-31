package com.choubapp.running;
import androidx.annotation.NonNull;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class SettingsActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    static final String LOGIN_EMAIL = "com.choubapp.running.LOGIN_EMAIL";
    ScrollView inputs;
    RelativeLayout loading;
    private ProgressBar mProgressBar ;
    private Uri mImageUri;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference = firebaseStorage.getReference();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    private FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build();
    TextView DisplayName, DisplayUsername,DisplayTeamID,DisplayEmail,DisplayPassword,DisplayDate;
    private String FullName, Username, Email,Password,Date,TeamID;
    String DocID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        DocID= intent.getStringExtra(DashboardActivity.USER_DATA);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loading=findViewById(R.id.loading);
        inputs=findViewById(R.id.settingsView);
        inputs.setVisibility(View.INVISIBLE);

        DisplayName =  findViewById(R.id.edit_fullname);
        DisplayUsername =  findViewById(R.id.edit_username);
        DisplayTeamID =  findViewById(R.id.edit_team_id);
        DisplayEmail =  findViewById(R.id.edit_email);
        DisplayPassword = findViewById(R.id.edit_password);
        DisplayDate =  findViewById(R.id.edit_DatePicker);
        db.setFirestoreSettings(settings);
        // recuperer et afficher les donnees de l'utilisateur
        DocumentReference DocRef = db.collection("member").document(DocID);
        DocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    DisplayName.setText(document.get("FullName").toString());
                    DisplayUsername.setText(document.get("Username").toString());
                    DisplayTeamID.setText(document.get("Team").toString());
                    DisplayEmail.setText(document.get("Email").toString());
                    DisplayPassword.setText(document.get("Password").toString());
                    DisplayDate.setText(document.get("Birth").toString());
                    Log.d("TAG", "DocumentSnapshot data: " + document.getData());

                    loading=findViewById(R.id.loading);
                    inputs=findViewById(R.id.settingsView);
                    loading.setVisibility(View.GONE);
                    inputs.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    public void BacktoDashboard(View v) {
        Intent intent = new Intent(this, DashboardActivity.class);
        Email= DisplayEmail.getText().toString();
        if (!Email.equals("")) intent.putExtra(LOGIN_EMAIL,Email);
        startActivity(intent);
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
    // fenetre pour choisisr date de naissance
    public void BirthDatePicker(View v) throws ParseException {
        final DatePickerDialog[] picker = new DatePickerDialog[1];
        final TextInputEditText eText;
        eText= findViewById(R.id.edit_DatePicker);
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        Context context = SettingsActivity.this;
        if (isBrokenSamsungDevice()) {
            context = new ContextThemeWrapper(SettingsActivity.this, android.R.style.Theme_Holo_Light_Dialog);
        }
        picker[0] = new DatePickerDialog(context, (view, year1, monthOfYear, dayOfMonth) -> {
            eText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1);
            setDate( dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1);
        }, year, month, day);
        picker[0].getDatePicker().setMaxDate(new Date().getTime());
        picker[0].show();
    }
    public void setDate(String date) {
        Date = date;
    }

    // ouvrir la fenetre pour choisir une image
    public  void ImageUpload(View v){
        mProgressBar = findViewById(R.id.progressBar);
        openFileChooser();
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final TextInputEditText uploadbox;
        uploadbox= findViewById(R.id.picture_upload);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK  && data!= null && data.getData() != null) {
            mImageUri = data.getData();
            uploadbox.setText("profilepicture.jpg");
        }
    }
    // envoyer et sauvegarder l'image dans Firebase Storage
    private void uploadFile(View v) {
        if (mImageUri != null) {
             StorageReference fileReference = storageReference.child(firebaseAuth.getUid()).child("ImageProfile").child("Profile Pic"); //User id/Images/Profile Pic.jpg
             fileReference.putFile(mImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> mProgressBar.setProgress(0), 500);

                        Toast.makeText(SettingsActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                        BacktoDashboard(v);
                    })
                    .addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        mProgressBar.setProgress((int) progress);
                    });
        } else {
            BacktoDashboard(v);
        }
    }

// modifier et enregistrer les donnees de l'utilisateurs si les données entrées sont valides
    public  void saveData(View v){
        uploadFile(v);
        attemptSaving();
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }
    private void attemptSaving() {
        // initialiser les erreur à null
        DisplayEmail.setError(null);
        DisplayPassword.setError(null);
        DisplayName.setError(null);
        DisplayUsername.setError(null);
        // récupérer les valeurs des cases
        FullName = DisplayName.getText().toString();
        Username = DisplayUsername.getText().toString();
        TeamID = DisplayTeamID.getText().toString();
        Email = DisplayEmail.getText().toString();
        Password = DisplayPassword.getText().toString();
        Date = DisplayDate.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // verifier la validité du password
        if (TextUtils.isEmpty(Password) || Password.length() < 4) {
            DisplayPassword.setError(getString(R.string.error_invalid_password));
            focusView = DisplayPassword;
            cancel = true;
        }
        //verifier si Username n'est pas null
        if (TextUtils.isEmpty(Username)) {
            DisplayUsername.setError(getString(R.string.error_field_required));
            focusView = DisplayUsername;
            cancel = true;
        }
        // verifier si username ne contient pas d'espace
        if (Username.contains(" ")) {
            DisplayUsername.setError("Ce champ ne doit pas contenir un espace");
            focusView = DisplayUsername;
            cancel = true;
        }

        //verifier si le nom n'est pas null
        if (TextUtils.isEmpty(FullName)) {
            DisplayName.setError(getString(R.string.error_field_required));
            focusView = DisplayName;
            cancel = true;
        }
        // verifier si email est valide
        if (TextUtils.isEmpty(Email)) {
            DisplayEmail.setError(getString(R.string.error_field_required));
            focusView = DisplayEmail;
            cancel = true;
        } else if (!isEmailValid(Email)) {
            DisplayEmail.setError(getString(R.string.error_invalid_email));
            focusView = DisplayEmail;
            cancel = true;
        }
        if (cancel) {
            // il existe une erreur, on l'affiche sur la case correspondante
            focusView.requestFocus();
        } else {
            // on change les données dans le document de cet utilisateur
            db.collection("member").document(DocID).update(
                    "FullName", FullName,
                    "Email", Email,
                    "Username", Username,
                    "Password", Password,
                    "Team", TeamID,
                    "Birth", Date
            );
            Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();
        }
    }

}
