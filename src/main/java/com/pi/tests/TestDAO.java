package com.pi.tests;

import com.pi.dao.EvenementRhDAO;
import com.pi.dao.ParticipationDAO;
import com.pi.models.EvenementRh;
import com.pi.models.Participation;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class TestDAO {
    public static void main(String[] args) {
        EvenementRhDAO eventDAO = new EvenementRhDAO();
        ParticipationDAO partDAO = new ParticipationDAO();
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=================================");
            System.out.println("  TEST DES CLASSES DAO");
            System.out.println("=================================\n");

            System.out.println("1. Test : Ajouter un evenement");
            System.out.println("---------------------------------");
            EvenementRh event1 = new EvenementRh(
                    "Forum Emploi 2026",
                    "Job Fair",
                    LocalDate.of(2026, 3, 15),
                    "Parc des Expositions",
                    "Actif"
            );
            eventDAO.ajouter(event1);
            System.out.println();

            System.out.println("2. Test : Ajouter un deuxieme evenement");
            System.out.println("---------------------------------");
            EvenementRh event2 = new EvenementRh(
                    "Atelier CV",
                    "Workshop",
                    LocalDate.of(2026, 3, 20),
                    "Salle 101",
                    "Actif"
            );
            eventDAO.ajouter(event2);
            System.out.println();

            System.out.println("3. Test : Afficher tous les evenements");
            System.out.println("---------------------------------");
            List<EvenementRh> listeEvents = eventDAO.afficherTous();
            if (listeEvents.isEmpty()) {
                System.out.println("   Aucun evenement trouve");
            } else {
                for (EvenementRh e : listeEvents) {
                    System.out.println("   " + e);
                }
            }
            System.out.println();

            if (!listeEvents.isEmpty()) {
                int premierId = listeEvents.get(0).getIdEvent();
                System.out.println("4. Test : Rechercher evenement par ID (ID = " + premierId + ")");
                System.out.println("---------------------------------");
                EvenementRh trouve = eventDAO.getById(premierId);
                if (trouve != null) {
                    System.out.println("   Trouve : " + trouve);
                } else {
                    System.out.println("   Aucun evenement trouve avec l'ID " + premierId);
                }
                System.out.println();
            }

            if (!listeEvents.isEmpty()) {
                int idAModifier = listeEvents.get(0).getIdEvent();
                System.out.println("5. Test : Modifier l'evenement ID = " + idAModifier);
                System.out.println("---------------------------------");
                EvenementRh aModifier = eventDAO.getById(idAModifier);
                if (aModifier != null) {
                    System.out.println("   Avant modification : " + aModifier);
                    aModifier.setStatut("Annule");
                    aModifier.setLieu("Lieu modifie");
                    eventDAO.modifier(aModifier);

                    EvenementRh apresModif = eventDAO.getById(idAModifier);
                    System.out.println("   Apres modification : " + apresModif);
                }
                System.out.println();
            }

            System.out.println("6. Test : Rechercher par type 'Job Fair'");
            System.out.println("---------------------------------");
            List<EvenementRh> jobFairs = eventDAO.rechercherParType("Job Fair");
            if (jobFairs.isEmpty()) {
                System.out.println("   Aucun evenement de type 'Job Fair' trouve");
            } else {
                for (EvenementRh e : jobFairs) {
                    System.out.println("   " + e);
                }
            }
            System.out.println();

            System.out.println("7. Test : Rechercher par statut 'Actif'");
            System.out.println("---------------------------------");
            List<EvenementRh> actifs = eventDAO.rechercherParStatut("Actif");
            if (actifs.isEmpty()) {
                System.out.println("   Aucun evenement actif trouve");
            } else {
                for (EvenementRh e : actifs) {
                    System.out.println("   " + e);
                }
            }
            System.out.println();

            System.out.println("8. Test : Evenements a venir");
            System.out.println("---------------------------------");
            List<EvenementRh> aVenir = eventDAO.getEvenementsAVenir();
            if (aVenir.isEmpty()) {
                System.out.println("   Aucun evenement a venir");
            } else {
                for (EvenementRh e : aVenir) {
                    System.out.println("   " + e);
                }
            }
            System.out.println();

            if (!listeEvents.isEmpty()) {
                int idEvent = listeEvents.get(0).getIdEvent();
                System.out.println("9. Test : Ajouter une participation a l'evenement ID = " + idEvent);
                System.out.println("---------------------------------");
                Participation part1 = new Participation(idEvent, 1, "Inscrit");
                partDAO.ajouter(part1);

                Participation part2 = new Participation(idEvent, 2, "Inscrit");
                partDAO.ajouter(part2);
                System.out.println();
            }

            System.out.println("10. Test : Afficher toutes les participations");
            System.out.println("---------------------------------");
            List<Participation> listeParts = partDAO.afficherToutes();
            if (listeParts.isEmpty()) {
                System.out.println("   Aucune participation trouvee");
            } else {
                for (Participation p : listeParts) {
                    System.out.println("   " + p);
                }
            }
            System.out.println();

            if (!listeEvents.isEmpty()) {
                int idEvent = listeEvents.get(0).getIdEvent();
                System.out.println("11. Test : Participations pour l'evenement ID = " + idEvent);
                System.out.println("---------------------------------");
                List<Participation> partsEvent = partDAO.getByEvent(idEvent);
                if (partsEvent.isEmpty()) {
                    System.out.println("   Aucune participation pour cet evenement");
                } else {
                    for (Participation p : partsEvent) {
                        System.out.println("   " + p);
                    }
                }
                System.out.println();
            }

            if (!listeEvents.isEmpty()) {
                int idEvent = listeEvents.get(0).getIdEvent();
                System.out.println("12. Test : Compter les participants pour l'evenement ID = " + idEvent);
                System.out.println("---------------------------------");
                int nbParticipants = partDAO.compterParticipants(idEvent);
                System.out.println("   Nombre de participants inscrits : " + nbParticipants);
                System.out.println();
            }

            if (!listeEvents.isEmpty()) {
                int idEvent = listeEvents.get(0).getIdEvent();
                System.out.println("13. Test : Verifier inscription (utilisateur 1 a evenement " + idEvent + ")");
                System.out.println("---------------------------------");
                boolean inscrit = partDAO.estInscrit(idEvent, 1);
                System.out.println("   Utilisateur 1 inscrit ? " + (inscrit ? "Oui" : "Non"));
                System.out.println();
            }

            if (!listeParts.isEmpty()) {
                int idPart = listeParts.get(0).getIdParticipation();
                System.out.println("14. Test : Modifier statut participation ID = " + idPart);
                System.out.println("---------------------------------");
                System.out.println("   Avant modification : " + partDAO.getByEvent(listeEvents.get(0).getIdEvent()).get(0));
                partDAO.modifierStatut(idPart, "Annule");
                System.out.println("   Apres modification : " + partDAO.getByEvent(listeEvents.get(0).getIdEvent()).get(0));
                System.out.println();
            }

            if (!listeParts.isEmpty()) {
                int idPart = listeParts.get(listeParts.size() - 1).getIdParticipation();
                System.out.println("15. Test : Supprimer la participation ID = " + idPart);
                System.out.println("---------------------------------");
                partDAO.supprimer(idPart);
                System.out.println("   Verification :");
                List<Participation> apresSuppression = partDAO.afficherToutes();
                System.out.println("   Nombre de participations restantes : " + apresSuppression.size());
                System.out.println();
            }

            System.out.println("16. Test : Nettoyage des donnees de test");
            System.out.println("---------------------------------");
            System.out.print("   Voulez-vous supprimer toutes les donnees de test ? (o/n) : ");
            String reponse = scanner.nextLine();
            if (reponse.equalsIgnoreCase("o")) {
                if (!listeEvents.isEmpty()) {
                    for (EvenementRh e : listeEvents) {
                        partDAO.supprimerParEvent(e.getIdEvent());
                        eventDAO.supprimer(e.getIdEvent());
                    }
                    System.out.println("   Toutes les donnees de test ont ete supprimees");
                }
            } else {
                System.out.println("   Nettoyage ignore");
            }

            System.out.println("\n=================================");
            System.out.println("  TESTS TERMINES AVEC SUCCES");
            System.out.println("=================================");

        } catch (SQLException e) {
            System.out.println("\nERREUR LORS DES TESTS");
            System.out.println("   Message : " + e.getMessage());
            System.out.println("   Code d'erreur : " + e.getErrorCode());
            System.out.println("   Etat SQL : " + e.getSQLState());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}