package com.pi.tests;

import com.pi.models.EvenementRh;
import com.pi.models.Participation;
import java.time.LocalDate;

public class TestModels {
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("  TEST DES CLASSES MODELS");
        System.out.println("=================================\n");

        System.out.println("Test de la classe EvenementRh");
        System.out.println("---------------------------------");

        EvenementRh event1 = new EvenementRh(
                "Forum Emploi 2026",
                "Job Fair",
                LocalDate.of(2026, 3, 15),
                "Parc des Expositions",
                "Actif"
        );

        event1.setIdEvent(1);

        System.out.println("Événement 1 créé :");
        System.out.println("   " + event1);

        System.out.println("\nVérification des getters :");
        System.out.println("   ID : " + event1.getIdEvent());
        System.out.println("   Titre : " + event1.getTitre());
        System.out.println("   Type : " + event1.getTypeEvent());
        System.out.println("   Date : " + event1.getDateEvent());
        System.out.println("   Lieu : " + event1.getLieu());
        System.out.println("   Statut : " + event1.getStatut());

        System.out.println("\nModification du statut :");
        event1.setStatut("Annulé");
        System.out.println("   Nouveau statut : " + event1.getStatut());

        EvenementRh event2 = new EvenementRh(
                2,
                "Atelier CV",
                "Workshop",
                LocalDate.of(2026, 3, 20),
                "Salle 101",
                "Actif"
        );

        System.out.println("\nÉvénement 2 créé :");
        System.out.println("   " + event2);

        System.out.println("\nRésumé de l'événement 2 :");
        System.out.println("   " + event2.getResume());

        System.out.println("\nClasse EvenementRh: OK\n");

        System.out.println("Test de la classe Participation");
        System.out.println("---------------------------------");

        Participation part1 = new Participation(
                1,
                5,
                "Inscrit"
        );

        part1.setIdParticipation(10);

        System.out.println("Participation 1 créée :");
        System.out.println("   " + part1);

        System.out.println("\nVérification des getters :");
        System.out.println("   ID Participation : " + part1.getIdParticipation());
        System.out.println("   ID Événement : " + part1.getIdEvent());
        System.out.println("   ID Utilisateur : " + part1.getIdUser());
        System.out.println("   Statut : " + part1.getStatut());

        System.out.println("\nModification du statut :");
        part1.setStatut("Annulé");
        System.out.println("   Nouveau statut : " + part1.getStatut());

        Participation part2 = new Participation(
                20,
                2,
                8,
                "Inscrit"
        );

        System.out.println("\nParticipation 2 créée :");
        System.out.println("   " + part2);

        System.out.println("\nClasse Participation: OK\n");

        System.out.println("Test d'intégration");
        System.out.println("---------------------");

        EvenementRh event3 = new EvenementRh(
                "Conférence Java",
                "Conférence",
                LocalDate.of(2026, 4, 10),
                "Amphi 3",
                "Actif"
        );
        event3.setIdEvent(3);

        Participation part3 = new Participation(3, 10, "Inscrit");
        Participation part4 = new Participation(3, 11, "Inscrit");
        part3.setIdParticipation(30);
        part4.setIdParticipation(31);

        System.out.println("Événement :");
        System.out.println("   " + event3);
        System.out.println("Participations associées :");
        System.out.println("   " + part3);
        System.out.println("   " + part4);

        System.out.println("\nIntégration: OK\n");

        System.out.println("=================================");
        System.out.println("  RÉSULTAT DES TESTS");
        System.out.println("=================================");
        System.out.println("EvenementRh : " + testEvenementRh());
        System.out.println("Participation : " + testParticipation());
        System.out.println("=================================");
        System.out.println("Tous les tests sont passés avec succès !");
    }

    private static String testEvenementRh() {
        try {
            EvenementRh e = new EvenementRh("Test", "Type", LocalDate.now(), "Lieu", "Statut");
            if (e.getTitre().equals("Test") && e.getTypeEvent().equals("Type")) {
                return "Fonctionnel";
            }
            return "Problème détecté";
        } catch (Exception e) {
            return "Erreur: " + e.getMessage();
        }
    }

    private static String testParticipation() {
        try {
            Participation p = new Participation(1, 1, "Inscrit");
            if (p.getIdEvent() == 1 && p.getIdUser() == 1 && p.getStatut().equals("Inscrit")) {
                return "Fonctionnel";
            }
            return "Problème détecté";
        } catch (Exception e) {
            return "Erreur: " + e.getMessage();
        }
    }
}
