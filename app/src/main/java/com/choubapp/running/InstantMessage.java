package com.choubapp.running;

public class InstantMessage {
    private String message;
    private String coachName;
    private String teamId;
    private String date;
    private String time;

    InstantMessage(String message, String author, String teamId, String date, String time) {
        this.message = message;
        this.coachName = author;
        this.teamId = teamId;
        this.date = date;
        this.time = time;

    }
    public InstantMessage() {

    }

    public String getMessage() {
        return message;
    }

    public String getCoachName() {
        return coachName;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
