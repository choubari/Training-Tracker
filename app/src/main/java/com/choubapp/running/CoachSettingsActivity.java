package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class CoachSettingsActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    static final String LOGIN_EMAIL = "com.choubapp.running.LOGIN_EMAIL";
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseReference;
    private StorageTask mUploadTask;
    private ProgressBar mProgressBar;
    private Uri mImageUri;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build();
    TextView DisplayName, DisplayUsername,DisplayEmail,DisplayPassword,DisplayDate;
    private String FullName, Username, Email,Password,Date;
    String DocID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        DocID= intent.getStringExtra(CoachDashboardActivity.USER_DATA);
        System.out.println("Coach Setting"+DocID);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_settings);
        DisplayName = (TextView) findViewById(R.id.edit_fullname);
        DisplayUsername = (TextView) findViewById(R.id.edit_username);
        DisplayEmail = (TextView) findViewById(R.id.edit_email);
        DisplayPassword = (TextView) findViewById(R.id.edit_password);
        DisplayDate = (TextView) findViewById(R.id.edit_DatePicker);
        db.setFirestoreSettings(settings);
        DocumentReference DocRef = db.collection("coach").document(DocID);
        DocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        DisplayName.setText(document.get("FullName").toString());
                        DisplayUsername.setText(document.get("Username").toString());
                        DisplayEmail.setText(document.get("Email").toString());
                        DisplayPassword.setText(document.get("Password").toString());
                        DisplayDate.setText(document.get("Birth").toString());
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                    }
                }
            }
        });
    }

    public void BacktoDashboard(View v) {
        Intent intent = new Intent(this, CoachDashboardActivity.class);
        Email= DisplayEmail.getText().toString();
        if (Email!="") intent.putExtra(LOGIN_EMAIL,Email);
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
    public void BirthDatePicker(View v) throws ParseException {
        final DatePickerDialog[] picker = new DatePickerDialog[1];
        final TextInputEditText eText;
        eText= findViewById(R.id.edit_DatePicker);
        //eText.setInputType(InputType.TYPE_NULL);
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        Context context = CoachSettingsActivity.this;
        if (isBrokenSamsungDevice()) {
            context = new ContextThemeWrapper(CoachSettingsActivity.this, android.R.style.Theme_Holo_Light_Dialog);
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


    public class Upload {
        private String mImageUrl;
        public Upload() {
            //empty constructor needed
        }
        public Upload(String imageUrl) {
            mImageUrl = imageUrl;
        }
        public String getImageUrl() {
            return mImageUrl;
        }
        public void setImageUrl(String imageUrl) {
            mImageUrl = imageUrl;
        }
    }
    public  void ImageUpload(View v){
        mStorageRef = FirebaseStorage.getInstance().getReference("PictureUploads");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("PictureUploads");
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
            uploadbox.setText("image.jpg");
        }
    }
    //get image extension
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void uploadFile() {
        if (mImageUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                }
                            }, 500);

                            Toast.makeText(CoachSettingsActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                            CoachSettingsActivity.Upload upload = new CoachSettingsActivity.Upload(taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                            String uploadId = mDatabaseReference.push().getKey();
                            mDatabaseReference.child(uploadId).setValue(upload);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CoachSettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
    public void  savepicture( View v){
        if (mUploadTask != null && mUploadTask.isInProgress()) {
            Toast.makeText(CoachSettingsActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
        } else {
            uploadFile();
        }
    }
    public  void saveData(View v){
        attemptSaving(v);
    }

    public void setDate(String date) {
        Date = date;
    }
    private boolean isEmailValid(String email) {
        // You can add more checking logic here.
        return email.contains("@");
    }
    private void attemptSaving(View v) {
        // Reset errors displayed in the form.
        DisplayEmail.setError(null);
        DisplayPassword.setError(null);
        DisplayName.setError(null);
        DisplayUsername.setError(null);
        // Store values at the time of the login attempt.
        FullName = DisplayName.getText().toString();
        Username = DisplayUsername.getText().toString();
        Email = DisplayEmail.getText().toString();
        Password = DisplayPassword.getText().toString();
        Date = DisplayDate.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(Password) || Password.length() < 4) {
            DisplayPassword.setError(getString(R.string.error_invalid_password));
            focusView = DisplayPassword;
            cancel = true;
        }
        //Check for Username not null
        if (TextUtils.isEmpty(Username)) {
            DisplayUsername.setError(getString(R.string.error_field_required));
            focusView = DisplayUsername;
            cancel = true;
        }
        if (Username.toString().contains(" ")) {
            DisplayUsername.setError("Ce champ ne doit pas contenir un espace");
            focusView = DisplayUsername;
            cancel = true;
        }

        //Check for FullName not null
        if (TextUtils.isEmpty(FullName)) {
            DisplayName.setError(getString(R.string.error_field_required));
            focusView = DisplayName;
            cancel = true;
        }
        // Check for a valid email address.
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
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            db.collection("coach").document(DocID).update(
                    "FullName", FullName,
                    "Email", Email,
                    "Username", Username,
                    "Password", Password,
                    "Birth", Date
            );
            Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();
            BacktoDashboard(v);

        }
    }


}
