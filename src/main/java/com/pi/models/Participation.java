package com.pi.models;

public class Participation {
    // Attributs
    private int idParticipation;
    private int idEvent;
    private int idUser;
    private String statut;

    // Constructeurs
    public Participation() {}

    public Participation(int idEvent, int idUser, String statut) {
        this.idEvent = idEvent;
        this.idUser = idUser;
        this.statut = statut;
    }

    public Participation(int idParticipation, int idEvent, int idUser, String statut) {
        this.idParticipation = idParticipation;
        this.idEvent = idEvent;
        this.idUser = idUser;
        this.statut = statut;
    }

    // Getters et Setters
    public int getIdParticipation() {
        return idParticipation;
    }

    public void setIdParticipation(int idParticipation) {
        this.idParticipation = idParticipation;
    }

    public int getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(int idEvent) {
        this.idEvent = idEvent;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Participation{" +
                "id=" + idParticipation +
                ", événement=" + idEvent +
                ", utilisateur=" + idUser +
                ", statut='" + statut + '\'' +
                '}';
    }
}