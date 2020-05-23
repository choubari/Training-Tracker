package com.choubapp.running;

import java.sql.Timestamp;

public class TrainingTaskItem {
    private String Name;
    private String Time;
    private String TrainingDate;
    public TrainingTaskItem(String name, String time, String trainingDate) {
        Name = name;
        Time = time;
        TrainingDate = trainingDate;
    }
    public String getName() {
        return Name;
    }
    public String getTime() {
        return Time;
    }
    public String getTrainingDate() {
        return TrainingDate;
    }
}