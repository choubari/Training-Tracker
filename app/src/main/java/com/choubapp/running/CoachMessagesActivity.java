package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.choubapp.running.CoachDashboardActivity.USER_DATA;

public class CoachMessagesActivity extends AppCompatActivity  {
    // TODO: Add member variables here:
    private String coachname;
    private ListView mChatListView;
    private EditText mInputText;
    private ImageButton mSendButton;
    String email;
    String teamidselected;


    private DatabaseReference mDatabaseReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference Teams = db.collection("Equipe");
    CollectionReference Coach = db.collection("coach");

    private ChatListAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        email= intent.getStringExtra(USER_DATA);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_messages);

        // TODO: Set up the display name and get the Firebase reference
        setupDisplayName();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

                // Link the Views in the layout to the Java code
           mInputText = (EditText) findViewById(R.id.messageInput);
           mSendButton = (ImageButton) findViewById(R.id.sendButton);
            mChatListView = (ListView) findViewById(R.id.chat_list_view);

        // TODO: Send the message when the "enter" button is pressed
        mInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                sendMessage();
                return false;
            }
        });

        // TODO: Add an OnClickListener to the sendButton to send a message
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        LoadSpinnerData();
    }

    // TODO: Load date in Spinner
    public void LoadSpinnerData(){
        Spinner spinner = (Spinner) findViewById(R.id.spinnerMessagesCoach);
        List<String> TeamsList = new ArrayList<>();

        List<String> TeamsIds = new ArrayList<>();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, TeamsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        Teams.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String teamname = document.getString("Nom Equipe");
                        String coachmail = document.getString("Email Coach");
                        String teamid = document.getString("ID");
                        if (teamname!=null && coachmail.equals(email) ){
                            TeamsList.add(teamname);
                            TeamsIds.add(teamid);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int select = parent.getSelectedItemPosition();
                teamidselected = TeamsIds.get(select);

                // TODO: Send Teamidselected To chatlistadapter
                onStop();
                onStart();





            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    // TODO: Retrieve the display name from the Shared Preferences
    private void setupDisplayName(){
        Coach.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if(document.getString("Email").equals(email)) coachname = document.getString("FullName") ;
                    }
                }

            }

        });

    }

    private void sendMessage() {
        //Log.d("FlashChat", " I send somthing" );
        // TODO: Grab the text the user typed in and push the message to Firebase
        String input = mInputText.getText().toString();

        if(!input.equals("")){
            InstantMessage chat = new InstantMessage(input , coachname,teamidselected);
            mDatabaseReference.child("messages").push().setValue(chat);
            mInputText.setText("");
        }
    }

    // TODO: Override the onStart() lifecycle method. Setup the adapter here.

    public void onStart(){
        super.onStart();
        mAdapter = new ChatListAdapter(this , mDatabaseReference,teamidselected);
        mChatListView.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        // TODO: Remove the Firebase event listener on the adapter.
        mAdapter.cleaunup();

    }

    public void BacktoDashboard(View v) {
        Intent intent = new Intent(this, CoachDashboardActivity.class);
        finish();
        startActivity(intent);
    }
}
