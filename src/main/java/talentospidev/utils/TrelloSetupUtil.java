package talentospidev.utils;

import talentospidev.services.TrelloService;

public class TrelloSetupUtil {
    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("🔧 Trello Setup Utility");
        System.out.println("=====================================");
        
        // Test connection first
        System.out.println("\n📡 Testing Trello connection...");
        boolean connected = TrelloService.testConnection();
        
        if (connected) {
            System.out.println("\n📋 Fetching your boards and lists...");
            TrelloService.listBoards();
            
            System.out.println("\n=====================================");
            System.out.println("📝 Add these to your config.properties:");
            System.out.println("trello.board.id=your_board_id_here");
            System.out.println("trello.list.id=your_list_id_here");
            System.out.println("=====================================");
        } else {
            System.out.println("\n❌ Failed to connect to Trello. Check your API key and token.");
        }
    }
}