package com.choubapp.running;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MessagesActivity extends AppCompatActivity {
    String teamid;
    private ListView mChatListView;

    private DatabaseReference mDatabaseReference;


    private ChatListAdapter mAdapter;

    TextView date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Intent intent = getIntent();
        teamid = intent.getStringExtra(DashboardActivity.USER_TEAM);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mChatListView = findViewById(R.id.chat_list_view1);
        date = findViewById(R.id.date1);

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
                        date.setText(mAdapter.getDateselected(firstVisibleItem + visibleItemCount - 1));
                    }
                }

            }
        });

    }
    public void onStart(){
        super.onStart();
        mAdapter = new ChatListAdapter(this , mDatabaseReference,teamid);
        mChatListView.setAdapter(mAdapter);
        ChangeDate();
    }

    public void onStop() {
        super.onStop();
        mAdapter.cleaunup();

    }



    public void BacktoDashboard(View v) {
        Intent intent = new Intent(this, DashboardActivity.class);
        finish();
        startActivity(intent);
    }

}