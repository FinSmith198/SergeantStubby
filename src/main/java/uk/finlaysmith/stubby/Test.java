package uk.finlaysmith.stubby;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.finlaysmith.stubby.Classes.*;

public class Test {

    private static final int START_XP_DIFF = 1000;
    private static final int XP_DIFF_RATE = 500;

    public static void main(String[] args) throws InterruptedException {
        Config.init("DDServerData.db");

        // create the bot instance with certain token
        Bot.init(Config.DISCORD_TOKEN);



    }


}
