package com.pi.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Validator {

    public static boolean estNombreValide(String str) {
        if (str == null || str.trim().isEmpty()) return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean estDateValide(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return false;
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean estChaineNonVide(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static boolean estStatutEvenementValide(String statut) {
        return statut != null &&
                (statut.equalsIgnoreCase("Actif") ||
                        statut.equalsIgnoreCase("Annule"));
    }

    public static boolean estStatutParticipationValide(String statut) {
        return statut != null &&
                (statut.equalsIgnoreCase("Inscrit") ||
                        statut.equalsIgnoreCase("Annule"));
    }

    public static boolean estTypeEvenementValide(String type) {
        return type != null &&
                (type.equalsIgnoreCase("Job Fair") ||
                        type.equalsIgnoreCase("Workshop") ||
                        type.equalsIgnoreCase("Conference"));
    }
}