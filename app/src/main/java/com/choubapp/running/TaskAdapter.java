package com.choubapp.running;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private ArrayList<TrainingTaskItem> mTask;
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public TextView mTime;
        public TextView mName;
        public TextView mDate;
        public TaskViewHolder(View itemView) {
            super(itemView);
            mTime = itemView.findViewById(R.id.training_time);
            mName = itemView.findViewById(R.id.training_name);
            mDate = itemView.findViewById(R.id.training_date);
        }
    }
    public TaskAdapter(ArrayList<TrainingTaskItem> exampleList) {
        mTask = exampleList;
    }
    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.completed_task_item, parent, false);
        TaskViewHolder evh = new TaskViewHolder(v);
        return evh;
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        TrainingTaskItem currentItem = mTask.get(position);
        holder.mTime.setText(currentItem.getTime());
        holder.mName.setText(currentItem.getName());
        holder.mDate.setText(currentItem.getTrainingDate());
    }
    @Override
    public int getItemCount() {
        return mTask.size();
    }
}