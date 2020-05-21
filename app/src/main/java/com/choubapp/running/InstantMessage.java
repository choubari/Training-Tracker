package com.choubapp.running;

public class InstantMessage {
    private String message;
    private String coachName;
    private String teamId;

   public InstantMessage(String message, String author , String teamId){
       this.message = message;
       this.coachName = author;
       this.teamId = teamId;
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
}
