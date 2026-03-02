package talentospidev.models.courses;

import java.time.LocalDate;

public class Formation {
    private int id;
    private int recruiterId;
    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String contenu;
    private String difficulte;   // DEBUTANT, INTERMEDIAIRE, AVANCE
    private String categorie;
    private String mode;         // EN_LIGNE, PRESENTIEL, HYBRIDE
    private String lieu;
    private String formateur;
    private String prerequis;
    private int capaciteMax;
    private String statut;       // OUVERTE, EN_COURS, TERMINEE

    public Formation() {}

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRecruiterId() { return recruiterId; }
    public void setRecruiterId(int recruiterId) { this.recruiterId = recruiterId; }

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
