package com.choubapp.running;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.choubapp.running.CoachDashboardActivity.USER_DATA;

public class CoachMessagesActivity extends AppCompatActivity  {
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

    TextView date;

    private ChatListAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        email= intent.getStringExtra(USER_DATA);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_messages);

        setupDisplayName();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mInputText = findViewById(R.id.messageInput);
        mSendButton = findViewById(R.id.sendButton);
        mChatListView = findViewById(R.id.chat_list_view);

        date = findViewById(R.id.date);
        // envoyer message si la touche "enter" du clavier est cliquée
        mInputText.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return false;
        });

        // envoyer un message si le boutton d'envoi est cliqué
        mSendButton.setOnClickListener(v -> sendMessage());
        LoadSpinnerData();
    }

    public void LoadSpinnerData(){
        // spinner des équipes
        Spinner spinner = findViewById(R.id.spinnerMessagesCoach);
        List<String> TeamsList = new ArrayList<>();
        List<String> TeamsIds = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, TeamsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        Teams.get().addOnCompleteListener(task -> {
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
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int select = parent.getSelectedItemPosition();
                teamidselected = TeamsIds.get(select);
                date.setText("");
                onStop();
                onStart();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    // Récupérer le nom du coach (l'emmetteur du message)
    private void setupDisplayName(){
        Coach.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(document.getString("Email").equals(email)) coachname = document.getString("FullName") ;
                }
            }
        });

    }

    private void sendMessage() {
        //envoyer le message à Firebase
        String input = mInputText.getText().toString();
        if(!input.equals("")){
            InstantMessage chat = new InstantMessage(input, coachname, teamidselected, new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()), new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
            mDatabaseReference.child("messages").push().setValue(chat);
            mInputText.setText("");
        }
    }

    // Setup adapter au début.
    public void onStart(){
        super.onStart();
        mAdapter = new ChatListAdapter(this , mDatabaseReference,teamidselected);
        mChatListView.setAdapter(mAdapter);
        ChangeDate();
    }

    public void ChangeDate() {
        mChatListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount != 0) {

                    if (mAdapter.getDateselected(firstVisibleItem + visibleItemCount - 1).equals(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()))) {
                        date.setText("Today");
                    } else {
                        //changer le text de la Date
                        date.setText(mAdapter.getDateselected(firstVisibleItem + visibleItemCount - 1));
                    }
                }

            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        //supprimer Firebase event listener de l'adapter.
        mAdapter.cleaunup();

    }

    public void BacktoDashboard(View v) {
        Intent intent = new Intent(this, CoachDashboardActivity.class);
        finish();
        startActivity(intent);
    }
}