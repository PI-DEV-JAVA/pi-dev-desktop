package talentospidev.utils;

/**
 * Static context for passing data between views,
 * since SceneUtil does not support navigation parameters.
 */
public class ViewContext {

    private static int selectedOfferId = -1;
    private static int selectedApplicationId = -1;
    private static int selectedActivityId = -1;
    private static int selectedInterviewId = -1;
    private static int selectedFormationId = -1;
    private static int selectedQuizId = -1;
    private static int selectedSeanceId = -1;

    public static int getSelectedInterviewId() { return selectedInterviewId; }
    public static void setSelectedInterviewId(int id) { selectedInterviewId = id; }

    public static int getSelectedSeanceId() { return selectedSeanceId; }
    public static void setSelectedSeanceId(int id) { selectedSeanceId = id; }

    public static int getSelectedOfferId() { return selectedOfferId; }
    public static void setSelectedOfferId(int id) { selectedOfferId = id; }

    public static int getSelectedApplicationId() { return selectedApplicationId; }
    public static void setSelectedApplicationId(int id) { selectedApplicationId = id; }

    public static int getSelectedActivityId() { return selectedActivityId; }
    public static void setSelectedActivityId(int id) { selectedActivityId = id; }

    public static int getSelectedFormationId() { return selectedFormationId; }
    public static void setSelectedFormationId(int id) { selectedFormationId = id; }

    public static int getSelectedQuizId() { return selectedQuizId; }
    public static void setSelectedQuizId(int id) { selectedQuizId = id; }

    public static void clear() {
        selectedOfferId = -1;
        selectedApplicationId = -1;
        selectedActivityId = -1;
        selectedFormationId = -1;
        selectedQuizId = -1;
        selectedSeanceId = -1;
    }
}
