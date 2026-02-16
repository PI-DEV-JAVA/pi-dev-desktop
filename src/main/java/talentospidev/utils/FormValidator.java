package talentospidev.utils;

import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.Period;

/**
 * Reusable utility for inline field validation with visual feedback.
 * Attaches focus-lost listeners that validate fields and show/hide error
 * labels.
 */
public class FormValidator {

    /**
     * Marks an input as valid — green border + remove error message.
     */
    public static void markValid(Control field, Label errorLabel) {
        field.getStyleClass().removeAll("input-error", "input-valid");
        field.getStyleClass().add("input-valid");
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Marks an input as invalid — red border + show error message.
     */
    public static void markError(Control field, Label errorLabel, String message) {
        field.getStyleClass().removeAll("input-error", "input-valid");
        field.getStyleClass().add("input-error");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Resets a field to its default neutral state.
     */
    public static void markNeutral(Control field, Label errorLabel) {
        field.getStyleClass().removeAll("input-error", "input-valid");
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Attach a required-field validator to a TextField on focus-lost.
     */
    public static void requireNotEmpty(TextField field, Label errorLabel, String fieldName) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.getStyleClass().add("error-label");

        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) { // focus lost
                if (field.getText() == null || field.getText().trim().isEmpty()) {
                    markError(field, errorLabel, fieldName + " is required.");
                } else {
                    markValid(field, errorLabel);
                }
            }
        });
    }

    /**
     * Attach email format validator on focus-lost.
     */
    public static void requireEmail(TextField field, Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.getStyleClass().add("error-label");

        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                String text = field.getText();
                if (text == null || text.trim().isEmpty()) {
                    markError(field, errorLabel, "Email is required.");
                } else if (!text.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                    markError(field, errorLabel, "Please enter a valid email.");
                } else {
                    markValid(field, errorLabel);
                }
            }
        });
    }

    /**
     * Attach password strength validator on focus-lost.
     */
    public static void requirePassword(PasswordField field, Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.getStyleClass().add("error-label");

        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                String pw = field.getText();
                if (pw == null || pw.isEmpty()) {
                    markError(field, errorLabel, "Password is required.");
                } else if (pw.length() < 6) {
                    markError(field, errorLabel, "Minimum 6 characters.");
                } else if (!pw.matches(".*[A-Z].*")) {
                    markError(field, errorLabel, "Must contain an uppercase letter.");
                } else if (!pw.matches(".*[0-9].*")) {
                    markError(field, errorLabel, "Must contain a digit.");
                } else {
                    markValid(field, errorLabel);
                }
            }
        });
    }

    /**
     * Attach confirm-password validator on focus-lost.
     */
    public static void requireConfirmPassword(PasswordField confirmField, PasswordField passwordField,
            Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.getStyleClass().add("error-label");

        confirmField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                String confirm = confirmField.getText();
                String password = passwordField.getText();
                if (confirm == null || confirm.isEmpty()) {
                    markError(confirmField, errorLabel, "Please confirm your password.");
                } else if (!confirm.equals(password)) {
                    markError(confirmField, errorLabel, "Passwords do not match.");
                } else {
                    markValid(confirmField, errorLabel);
                }
            }
        });
    }

    /**
     * Attach age >= 18 validator on focus-lost for a DatePicker.
     */
    public static void requireAge18(DatePicker picker, Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.getStyleClass().add("error-label");

        picker.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                LocalDate date = picker.getValue();
                if (date == null) {
                    markError(picker, errorLabel, "Birth date is required.");
                } else {
                    int age = Period.between(date, LocalDate.now()).getYears();
                    if (age < 18) {
                        markError(picker, errorLabel, "You must be at least 18.");
                    } else {
                        markValid(picker, errorLabel);
                    }
                }
            }
        });

        // Also validate when value changes (user picks a date)
        picker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                markError(picker, errorLabel, "Birth date is required.");
            } else {
                int age = Period.between(newVal, LocalDate.now()).getYears();
                if (age < 18) {
                    markError(picker, errorLabel, "You must be at least 18.");
                } else {
                    markValid(picker, errorLabel);
                }
            }
        });
    }

    /**
     * Check if a field currently has the valid style class.
     */
    public static boolean isValid(Control field) {
        return field.getStyleClass().contains("input-valid");
    }

    /**
     * Check if a field has an error or is still neutral (not validated yet).
     */
    public static boolean hasError(Control field) {
        return field.getStyleClass().contains("input-error");
    }
}
