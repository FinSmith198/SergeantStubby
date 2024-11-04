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

    public static void main(String[] args) throws IOException, ParseException {
        // set up the connection
        URL url = new URL("https://stats1.ddclan.org/api/get_players");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // add api key to the connection
        con.setRequestProperty("Authorization","bearer: "+ KEY);
        con.setRequestMethod("GET");

        System.out.println(con);

        // get the response from the request
        InputStream inputStream = con.getInputStream();

        // and parse the JSON
        JSONParser jsonParser = new JSONParser();
        System.out.println((JSONObject) jsonParser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8)));
    }


}
