package com.choubapp.running;


public class TrainingTaskItem {
    private String Name;
    private String Time;
    private String TrainingDate;
    TrainingTaskItem(String name, String time, String trainingDate) {
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
    String getTrainingDate() {
        return TrainingDate;
    }
}