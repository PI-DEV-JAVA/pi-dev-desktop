package com.pi.models;

import java.time.LocalDate;

public class EvenementRh {
    private int idEvent;
    private String titre;
    private String typeEvent;
    private LocalDate dateEvent;
    private String lieu;
    private String statut;

    public EvenementRh() {}

    public EvenementRh(String titre, String typeEvent, LocalDate dateEvent, String lieu, String statut) {
        this.titre = titre;
        this.typeEvent = typeEvent;
        this.dateEvent = dateEvent;
        this.lieu = lieu;
        this.statut = statut;
    }

    public EvenementRh(int idEvent, String titre, String typeEvent, LocalDate dateEvent, String lieu, String statut) {
        this.idEvent = idEvent;
        this.titre = titre;
        this.typeEvent = typeEvent;
        this.dateEvent = dateEvent;
        this.lieu = lieu;
        this.statut = statut;
    }

    public int getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(int idEvent) {
        this.idEvent = idEvent;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getTypeEvent() {
        return typeEvent;
    }

    public void setTypeEvent(String typeEvent) {
        this.typeEvent = typeEvent;
    }

    public LocalDate getDateEvent() {
        return dateEvent;
    }

    public void setDateEvent(LocalDate dateEvent) {
        this.dateEvent = dateEvent;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getResume() {
        return titre + " (" + typeEvent + ") - " + dateEvent;
    }

    public boolean estAVenir() {
        return dateEvent.isAfter(LocalDate.now());
    }

    public boolean estActif() {
        return "Actif".equalsIgnoreCase(statut);
    }

    @Override
    public String toString() {
        return "Événement{" +
                "id=" + idEvent +
                ", titre='" + titre + '\'' +
                ", type='" + typeEvent + '\'' +
                ", date=" + dateEvent +
                ", lieu='" + lieu + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}