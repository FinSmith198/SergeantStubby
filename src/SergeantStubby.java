import Classes.Bot;
import Classes.Config;


public class SergeantStubby {


    public static void main(String[] args) throws Exception {

        System.out.println("test");


        // create the bot instance with certain token
        Bot.getInstance((String) Config.getInstance("DDServerData.db").config.get("DISCORD_TOKEN"));

    }

}
