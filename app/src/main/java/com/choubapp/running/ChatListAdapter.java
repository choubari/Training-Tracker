package com.choubapp.running;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class ChatListAdapter extends BaseAdapter {
    private Activity mActivity;
    private DatabaseReference mDatabaseReference;
    private String mDisplayName;
    private ArrayList<DataSnapshot> mSnapshotList;

    String teamIdSelected;

    private ArrayList<InstantMessage> Messages;


    private ChildEventListener mListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            // Oblige de database that we will show just teamMesaage
            InstantMessage test =(InstantMessage) dataSnapshot.getValue(InstantMessage.class);
            if (test.getTeamId().equals(teamIdSelected)){
            mSnapshotList.add(dataSnapshot);
            notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public ChatListAdapter(Activity activity, DatabaseReference ref, String mDisplayName, String id){
        mActivity = activity;
        this.mDisplayName = mDisplayName;
        Log.v("soufiane" , id +"\n"+ mDisplayName);
        teamIdSelected = id;
        mDatabaseReference = ref.child("messages");
        mDatabaseReference.addChildEventListener(mListener); // adding listenner to db

        mSnapshotList = new ArrayList<>();
    }
    static class ViewHolder{
        TextView authorName;
        TextView body;
        LinearLayout.LayoutParams params;

    }

    @Override
    public int getCount() {

        return mSnapshotList.size();
    }

    @Override
    public InstantMessage getItem(int position) {
        DataSnapshot snapshot = mSnapshotList.get(position);
        return snapshot.getValue(InstantMessage.class);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chat_msg_row, parent, false);

                final ViewHolder holder = new ViewHolder();
                holder.authorName = (TextView) convertView.findViewById(R.id.author);
                holder.body = (TextView) convertView.findViewById(R.id.message);
                holder.params = (LinearLayout.LayoutParams) holder.authorName.getLayoutParams();
                convertView.setTag(holder);
            }

            final InstantMessage message = getItem(position);
            final ViewHolder holder = (ViewHolder) convertView.getTag();


            // setting the view information
            String author = message.getCoachName();
            holder.authorName.setText(author);

            String msg = message.getMessage();
            holder.body.setText(msg);

            return convertView;

    }



    public void cleaunup(){
     mDatabaseReference.removeEventListener(mListener);
    }
}
