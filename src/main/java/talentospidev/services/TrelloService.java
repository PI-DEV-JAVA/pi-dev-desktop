package talentospidev.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import talentospidev.models.Activity.Activity;
import talentospidev.models.Project.Project;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class TrelloService {
    
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    
    private static final Gson gson = new Gson();
    private static String apiKey;
    private static String token;
    private static String defaultBoardId;
    private static String defaultListId;
    private static Properties props;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static boolean isInitialized = false;
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        props = new Properties();
        try (InputStream input = TrelloService.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("❌ config.properties not found in resources folder");
                return;
            }
            props.load(input);
            
            apiKey = props.getProperty("trello.api.key");
            token = props.getProperty("trello.api.token");
            defaultBoardId = props.getProperty("trello.board.id");
            defaultListId = props.getProperty("trello.list.id");
            
            if (apiKey != null && token != null && defaultBoardId != null && defaultListId != null) {
                isInitialized = true;
                System.out.println("✅ Trello configuration loaded successfully");
            } else {
                System.err.println("❌ Trello configuration incomplete. Check config.properties");
            }
            
        } catch (IOException e) {
            System.err.println("❌ Failed to load config.properties: " + e.getMessage());
        }
    }
    
    private static boolean checkCredentials() {
        if (!isInitialized) {
            System.err.println("❌ Trello service not initialized. Check config.properties");
            return false;
        }
        return true;
    }
    
    /**
     * Creates a Trello card when a new activity is created
     * @return The Trello card ID if successful, null otherwise
     */
    public static String createActivityCard(Activity activity, String employeeName, String projectName) {
        if (!checkCredentials()) return null;
        
        try {
            String cardName = String.format("Activity #%d: %s", 
                activity.getIdActivity(), 
                activity.getDescription().length() > 40 ? 
                    activity.getDescription().substring(0, 40) + "..." : 
                    activity.getDescription());
            
            String description = String.format("""
                **Activity Details**
                ---
                **ID**: %d
                **Employee**: %s
                **Project**: %s
                **Date**: %s
                **Hours Assigned**: %.1f
                
                **Description**:
                %s
                
                ---
                **Status**: Not Started
                **Created**: %s
                """,
                activity.getIdActivity(),
                employeeName,
                projectName,
                activity.getDate().toString(),
                activity.getHoursWorked(),
                activity.getDescription(),
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );
            
            // Create card JSON
            JsonObject cardJson = new JsonObject();
            cardJson.addProperty("name", cardName);
            cardJson.addProperty("desc", description);
            cardJson.addProperty("idList", defaultListId);
            cardJson.addProperty("due", activity.getDate().toString());
            
            // Make API request to create card
            String url = "https://api.trello.com/1/cards?key=" + apiKey + "&token=" + token;
            
            RequestBody body = RequestBody.create(cardJson.toString(), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    String cardId = json.get("id").getAsString();
                    String cardUrl = json.get("url").getAsString();
                    
                    System.out.println("✅ Trello card created for Activity #" + activity.getIdActivity());
                    System.out.println("   Card URL: " + cardUrl);
                    System.out.println("   Card ID: " + cardId);
                    
                    // Add labels based on hours
                    addLabelsToCard(cardId, activity);
                    
                    // Add checklist for activity steps
                    addActivityChecklist(cardId, activity);
                    
                    // Return the card ID so it can be stored
                    return cardId;
                    
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    System.err.println("❌ Failed to create Trello card: " + response.code() + " - " + errorBody);
                    return null;
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Exception creating Trello card: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Creates a Trello card when a new project is created
     * @return The Trello card ID if successful, null otherwise
     */
    public static String createProjectCard(Project project, String managerName) {
        if (!checkCredentials()) return null;
        
        try {
            String cardName = String.format("📁 Project: %s", project.getName());
            
            String description = String.format("""
                **Project Details**
                ---
                **ID**: %d
                **Manager**: %s
                **Status**: %s
                **Start Date**: %s
                **End Date**: %s
                
                **Description**:
                %s
                
                ---
                **Created**: %s
                """,
                project.getId(),
                managerName,
                project.getStatus(),
                project.getStartDate() != null ? project.getStartDate().toString() : "TBD",
                project.getEndDate() != null ? project.getEndDate().toString() : "TBD",
                project.getDescription() != null ? project.getDescription() : "No description",
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );
            
            // Create card JSON
            JsonObject cardJson = new JsonObject();
            cardJson.addProperty("name", cardName);
            cardJson.addProperty("desc", description);
            cardJson.addProperty("idList", defaultListId);
            
            if (project.getEndDate() != null) {
                cardJson.addProperty("due", project.getEndDate().toString());
            }
            
            // Make API request
            String url = "https://api.trello.com/1/cards?key=" + apiKey + "&token=" + token;
            
            RequestBody body = RequestBody.create(cardJson.toString(), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    String cardId = json.get("id").getAsString();
                    String cardUrl = json.get("url").getAsString();
                    
                    System.out.println("✅ Trello card created for Project: " + project.getName());
                    System.out.println("   Card URL: " + cardUrl);
                    System.out.println("   Card ID: " + cardId);
                    
                    // Add project phases as checklist
                    addProjectPhases(cardId);
                    
                    return cardId;
                    
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    System.err.println("❌ Failed to create Trello project card: " + response.code());
                    System.err.println("   Response: " + errorBody);
                    return null;
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Exception creating project card: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Updates an existing Trello card with activity progress
     */
    public static void updateActivityCard(String cardId, Activity activity, double progressPercent) {
        if (!checkCredentials()) return;
        
        try {
            // First, update the card description with current progress
            String updatedDesc = String.format("""
                **Activity Details**
                ---
                **ID**: %d
                **Employee**: %s
                **Project**: %s
                **Date**: %s
                **Hours Assigned**: %.1f
                **Hours Tracked**: %.1f
                **Progress**: %.0f%%
                
                **Description**:
                %s
                
                ---
                **Last Updated**: %s
                """,
                activity.getIdActivity(),
                "Employee", // You might want to pass employee name
                "Project",  // You might want to pass project name
                activity.getDate().toString(),
                activity.getHoursWorked(),
                activity.getTrackedHours(),
                progressPercent,
                activity.getDescription(),
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );
            
            // Update card description
            String updateUrl = "https://api.trello.com/1/cards/" + cardId + "?key=" + apiKey + "&token=" + token;
            
            JsonObject updateJson = new JsonObject();
            updateJson.addProperty("desc", updatedDesc);
            
            RequestBody updateBody = RequestBody.create(updateJson.toString(), JSON);
            Request updateRequest = new Request.Builder()
                    .url(updateUrl)
                    .put(updateBody)
                    .build();
            
            try (Response response = client.newCall(updateRequest).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("⚠️ Failed to update card description: " + response.code());
                }
            }
            
            // Add comment with progress update
            String comment = String.format("**Progress Update**\n" +
                "Tracked: %.1f / %.1f hours (%.0f%% complete)\n" +
                "Last Updated: %s",
                activity.getTrackedHours(),
                activity.getHoursWorked(),
                progressPercent,
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );
            
            addCommentToCard(cardId, comment);
            
            // Add milestone comments
            if (progressPercent == 25 || progressPercent == 50 || progressPercent == 75 || progressPercent == 100) {
                String milestoneComment = String.format("🎯 **Milestone Reached!** Activity is %.0f%% complete!", 
                    progressPercent);
                addCommentToCard(cardId, milestoneComment);
            }
            
            System.out.println("✅ Trello card updated for Activity #" + activity.getIdActivity());
            
        } catch (Exception e) {
            System.err.println("❌ Failed to update Trello card: " + e.getMessage());
        }
    }
    
    private static void addLabelsToCard(String cardId, Activity activity) {
        try {
            String labelColor = activity.getHoursWorked() > 8 ? "red" : 
                               activity.getHoursWorked() > 4 ? "yellow" : "green";
            String labelName = activity.getHoursWorked() > 8 ? "Large Task" : 
                              activity.getHoursWorked() > 4 ? "Medium Task" : "Small Task";
            
            // First, create the label on the board
            String createLabelUrl = "https://api.trello.com/1/boards/" + defaultBoardId + 
                                   "/labels?key=" + apiKey + "&token=" + token +
                                   "&name=" + labelName + "&color=" + labelColor;
            
            Request createLabelRequest = new Request.Builder()
                    .url(createLabelUrl)
                    .post(RequestBody.create("", JSON))
                    .build();
            
            try (Response response = client.newCall(createLabelRequest).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    String labelId = json.get("id").getAsString();
                    
                    // Add label to card
                    String addLabelUrl = "https://api.trello.com/1/cards/" + cardId + 
                                        "/idLabels?key=" + apiKey + "&token=" + token +
                                        "&value=" + labelId;
                    
                    Request addLabelRequest = new Request.Builder()
                            .url(addLabelUrl)
                            .put(RequestBody.create("", JSON))
                            .build();
                    
                    client.newCall(addLabelRequest).execute().close();
                    System.out.println("   Label added: " + labelName);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to add labels: " + e.getMessage());
        }
    }
    
    private static void addActivityChecklist(String cardId, Activity activity) {
        try {
            // Create checklist
            String createChecklistUrl = "https://api.trello.com/1/cards/" + cardId + 
                                        "/checklists?key=" + apiKey + "&token=" + token +
                                        "&name=Activity Checklist";
            
            Request createChecklistRequest = new Request.Builder()
                    .url(createChecklistUrl)
                    .post(RequestBody.create("", JSON))
                    .build();
            
            try (Response response = client.newCall(createChecklistRequest).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    String checklistId = json.get("id").getAsString();
                    
                    // Add checklist items
                    addChecklistItem(checklistId, "Start work on activity");
                    addChecklistItem(checklistId, "Complete " + (int)activity.getHoursWorked() + " hours of work");
                    addChecklistItem(checklistId, "Upload any required files");
                    addChecklistItem(checklistId, "Submit for review");
                    
                    System.out.println("   Checklist added");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to add checklist: " + e.getMessage());
        }
    }
    
    private static void addProjectPhases(String cardId) {
        try {
            // Create checklist
            String createChecklistUrl = "https://api.trello.com/1/cards/" + cardId + 
                                        "/checklists?key=" + apiKey + "&token=" + token +
                                        "&name=Project Phases";
            
            Request createChecklistRequest = new Request.Builder()
                    .url(createChecklistUrl)
                    .post(RequestBody.create("", JSON))
                    .build();
            
            try (Response response = client.newCall(createChecklistRequest).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    String checklistId = json.get("id").getAsString();
                    
                    // Add project phases
                    addChecklistItem(checklistId, "Planning Phase");
                    addChecklistItem(checklistId, "Design Phase");
                    addChecklistItem(checklistId, "Development Phase");
                    addChecklistItem(checklistId, "Testing Phase");
                    addChecklistItem(checklistId, "Deployment Phase");
                    
                    System.out.println("   Project phases added");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to add project phases: " + e.getMessage());
        }
    }
    
    private static void addChecklistItem(String checklistId, String itemName) {
        try {
            String url = "https://api.trello.com/1/checklists/" + checklistId + 
                        "/checkItems?key=" + apiKey + "&token=" + token +
                        "&name=" + java.net.URLEncoder.encode(itemName, "UTF-8");
            
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create("", JSON))
                    .build();
            
            client.newCall(request).execute().close();
        } catch (Exception e) {
            System.err.println("⚠️ Failed to add checklist item: " + e.getMessage());
        }
    }
    
    private static void addCommentToCard(String cardId, String comment) {
        try {
            String url = "https://api.trello.com/1/cards/" + cardId + 
                        "/actions/comments?key=" + apiKey + "&token=" + token +
                        "&text=" + java.net.URLEncoder.encode(comment, "UTF-8");
            
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create("", JSON))
                    .build();
            
            client.newCall(request).execute().close();
        } catch (Exception e) {
            System.err.println("⚠️ Failed to add comment: " + e.getMessage());
        }
    }
    
    /**
     * Utility method to list all boards and their lists
     * Call this to find your board and list IDs
     */
    public static void listBoards() {
        if (!checkCredentials()) return;
        
        try {
            String url = "https://api.trello.com/1/members/me/boards?key=" + apiKey + "&token=" + token;
            
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonArray boards = JsonParser.parseString(responseBody).getAsJsonArray();
                    
                    System.out.println("\n=== Your Trello Boards ===");
                    for (int i = 0; i < boards.size(); i++) {
                        JsonObject board = boards.get(i).getAsJsonObject();
                        String boardId = board.get("id").getAsString();
                        String boardName = board.get("name").getAsString();
                        String boardUrl = board.get("url").getAsString();
                        
                        System.out.println("\n📋 Board: " + boardName);
                        System.out.println("   ID: " + boardId);
                        System.out.println("   URL: " + boardUrl);
                        
                        // Get lists for this board
                        getBoardLists(boardId);
                    }
                } else {
                    System.err.println("❌ Failed to fetch boards: " + response.code());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Exception listing boards: " + e.getMessage());
        }
    }
    
    private static void getBoardLists(String boardId) {
        try {
            String url = "https://api.trello.com/1/boards/" + boardId + 
                        "/lists?key=" + apiKey + "&token=" + token;
            
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonArray lists = JsonParser.parseString(responseBody).getAsJsonArray();
                    
                    System.out.println("   📌 Lists:");
                    for (int i = 0; i < lists.size(); i++) {
                        JsonObject list = lists.get(i).getAsJsonObject();
                        String listId = list.get("id").getAsString();
                        String listName = list.get("name").getAsString();
                        System.out.println("      - " + listName + " (ID: " + listId + ")");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to fetch lists: " + e.getMessage());
        }
    }
    
    /**
     * Test method to verify Trello connection
     */
    public static boolean testConnection() {
        if (!checkCredentials()) return false;
        
        try {
            String url = "https://api.trello.com/1/members/me?key=" + apiKey + "&token=" + token;
            
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    String username = json.get("fullName").getAsString();
                    System.out.println("✅ Trello connection successful! Logged in as: " + username);
                    return true;
                } else {
                    System.err.println("❌ Trello connection failed: " + response.code());
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Trello connection error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the URL for a Trello card
     */
    public static String getCardUrl(String cardId) {
        return "https://trello.com/c/" + cardId;
    }
}