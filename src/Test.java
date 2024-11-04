import Classes.Bot;
import Classes.Config;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

public class Test {

    private static final int START_XP_DIFF = 1000;
    private static final int XP_DIFF_RATE = 500;
    public static final String KEY = "216a386b-b6a9-416e-a6b7-cb914c5cae53";

    public static void main(String[] args) throws InterruptedException {
        Config.init("DDServerData.db");

        // create the bot instance with certain token
        Bot.init(Config.DISCORD_TOKEN);



    }


}
