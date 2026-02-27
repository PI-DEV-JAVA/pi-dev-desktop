package talentospidev.utils;

/**
 * Static context for passing data between views,
 * since SceneUtil does not support navigation parameters.
 */
public class ViewContext {

    private static int selectedOfferId = -1;
    private static int selectedApplicationId = -1;
    private static int selectedActivityId = -1;

    public static int getSelectedOfferId() {
        return selectedOfferId;
    }

    public static void setSelectedOfferId(int id) {
        selectedOfferId = id;
    }

    public static int getSelectedApplicationId() {
        return selectedApplicationId;
    }

    public static void setSelectedApplicationId(int id) {
        selectedApplicationId = id;
    }

    public static int getSelectedActivityId() {
        return selectedActivityId;
    }

    public static void setSelectedActivityId(int id) {
        selectedActivityId = id;
    }

    public static void clear() {
        selectedOfferId = -1;
        selectedApplicationId = -1;
        selectedActivityId = -1;
    }
}
