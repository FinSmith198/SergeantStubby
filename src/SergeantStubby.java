import Classes.Bot;
import Classes.Config;


public class SergeantStubby {


    public static void main(String[] args) throws Exception {

        // initialise Config data from the database
        Config.init("DDServerData.db");

        // create the bot instance with certain token
        Bot.init(Config.DISCORD_TOKEN);

    }

}
