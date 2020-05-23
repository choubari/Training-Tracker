package com.choubapp.running;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class MessagesActivity extends AppCompatActivity {
    String teamid;
    private ListView mChatListView;

    private DatabaseReference mDatabaseReference;


    private ChatListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Intent intent = getIntent();
        teamid = intent.getStringExtra(DashboardActivity.USER_TEAM);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mChatListView = (ListView) findViewById(R.id.chat_list_view1);

    }

    public void onStart(){
        super.onStart();
        mAdapter = new ChatListAdapter(this , mDatabaseReference,teamid);
        mChatListView.setAdapter(mAdapter);
    }

    public void onStop() {
        super.onStop();

        // TODO: Remove the Firebase event listener on the adapter.
        mAdapter.cleaunup();

    }



    public void BacktoDashboard(View v) {
        Intent intent = new Intent(this, DashboardActivity.class);
        finish();
        startActivity(intent);
    }

}
