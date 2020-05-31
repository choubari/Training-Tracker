package com.choubapp.running;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class MembreAdapter extends FirestoreRecyclerAdapter<Membre, MembreAdapter.MembreHolder> {

    MembreAdapter(@NonNull FirestoreRecyclerOptions<Membre> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MembreHolder holder, int position, @NonNull Membre model) {
        holder.textViewFullName.setText(model.getFullName());
        holder.textViewUsername.setText("@" + model.getUsername());
        holder.textViewEmail.setText(String.valueOf(model.getEmail()));
        holder.textViewBirth.setText(String.valueOf(model.getBirth()));
    }

    @NonNull
    @Override
    public MembreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.team_member_item, parent, false);
        return new MembreHolder(v);
    }

    static class MembreHolder extends RecyclerView.ViewHolder {
        TextView textViewFullName;
        TextView textViewUsername;
        TextView textViewEmail;
        TextView textViewBirth;

        MembreHolder(View itemView) {
            super(itemView);
            textViewFullName = itemView.findViewById(R.id.text_view_name);
            textViewUsername = itemView.findViewById(R.id.text_view_username);
            textViewEmail = itemView.findViewById(R.id.text_view_email);
            textViewBirth = itemView.findViewById(R.id.text_view_birth);
        }
    }

}