package talentos.pidev.models;

import java.time.LocalDate;

public class Formation {

    private int id;
    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String contenu;
    private String difficulte;
    private String categorie;
    private String mode;
    private String lieu;
    private String formateur;
    private String prerequis;
    private int capaciteMax;
    private String statut;

    // Constructor vide
    public Formation() {}

    // Constructor sans id (pour ajout)
    public Formation(String nom, String description, LocalDate dateDebut, LocalDate dateFin,
                     String contenu, String difficulte, String categorie,
                     String mode, String lieu, String formateur,
                     String prerequis, int capaciteMax, String statut) {
        this.nom = nom;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.contenu = contenu;
        this.difficulte = difficulte;
        this.categorie = categorie;
        this.mode = mode;
        this.lieu = lieu;
        this.formateur = formateur;
        this.prerequis = prerequis;
        this.capaciteMax = capaciteMax;
        this.statut = statut;
    }

    // Getters & Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getDifficulte() { return difficulte; }
    public void setDifficulte(String difficulte) { this.difficulte = difficulte; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getFormateur() { return formateur; }
    public void setFormateur(String formateur) { this.formateur = formateur; }

    public String getPrerequis() { return prerequis; }
    public void setPrerequis(String prerequis) { this.prerequis = prerequis; }

    public int getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(int capaciteMax) { this.capaciteMax = capaciteMax; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
