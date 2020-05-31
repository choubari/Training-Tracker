package com.choubapp.running;

import android.app.Activity;
import android.content.Context;
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
    private ArrayList<DataSnapshot> mSnapshotList;

    private String teamIdSelected;

    private ChildEventListener mListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.d("onChildadd","un child est ajouté");
            InstantMessage test =(InstantMessage) dataSnapshot.getValue(InstantMessage.class);
            if (test != null && test.getTeamId().equals(teamIdSelected)) {
                mSnapshotList.add(dataSnapshot);
                notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.d("onChanged","un child est modifié");
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            Log.d("onChildRemoved","un child est supprimé");

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.d("onChildmoved","un child a été changé");

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.d("onChildcancelled","un child a ete rejeter");

        }
    };

    ChatListAdapter(Activity activity, DatabaseReference ref, String id){
        Log.d("chatListAdapter","depuis adapter");

        mActivity = activity;
        teamIdSelected = id;
        mDatabaseReference = ref.child("messages");
        mDatabaseReference.addChildEventListener(mListener); // adding listener to database

        mSnapshotList = new ArrayList<>();
    }
    static class ViewHolder{
        TextView authorName;
        TextView body;
        TextView time;
        LinearLayout.LayoutParams params;

    }

    @Override
    public int getCount() {
        Log.d("getcont", "sizeof snapshot");

        return mSnapshotList.size();
    }

    @Override
    public InstantMessage getItem(int position) {
        Log.d("getItem", "J'ai pris l'item numéro "+position);
        DataSnapshot snapshot = mSnapshotList.get(position);
        return snapshot.getValue(InstantMessage.class);
    }

    @Override
    public long getItemId(int position) {
        Log.d("getItemId", "J'ai pris l'item id"+position);
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("getView", "J'ai pris l item id"+position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chat_msg_row, parent, false);

            final ViewHolder holder = new ViewHolder();
            holder.authorName =  convertView.findViewById(R.id.author);
            holder.body =  convertView.findViewById(R.id.message);
            holder.params = (LinearLayout.LayoutParams) holder.authorName.getLayoutParams();
            holder.time =  convertView.findViewById(R.id.Time);

            convertView.setTag(holder);
        }

        final InstantMessage message = getItem(position);
        final ViewHolder holder = (ViewHolder) convertView.getTag();
        // setting the view information
        String author = message.getCoachName();
        holder.authorName.setText(author);

        String msg = message.getMessage();
        holder.body.setText(msg);

        String time = message.getTime();
        holder.time.setText(time);
        return convertView;

    }

    String getDateselected(int position)
    {
        Log.d("getDateselected", "J'ai pris la date de "+position);

        InstantMessage message  = getItem(position);
        Log.d("je retourne", message.getDate());
        return message.getDate();
    }

    void cleaunup(){
        Log.d("cleanup", "J'ai videe");

        mDatabaseReference.removeEventListener(mListener);
    }
}
