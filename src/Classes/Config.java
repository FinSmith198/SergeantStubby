package Classes;

import org.json.simple.JSONObject;

import java.nio.file.LinkOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Config {
    public static final String TEST_GUILD_ID = "821405370014629930";
    public static final String DD_GUILD_ID = "1001456689851146331";
    
    // constants - which coincidentally are not constant
    public static String RCON_KEY;
    public static String DISCORD_TOKEN;
    public static String HLL_SERVER_RCON_URL;
    public static String HLL_SERVER_STATS_URL;
    public static String HLL_STATS_DISCORD_CHANNEL;
    public static String SEEDING_MESSAGE_DESCRIPTION;
    public static Long HLL_STATS_UPDATE_DELAY;
    public static Long XP_PER_MESSAGE;
    public static Long MAX_XP_PER_DAY;
    public static Long START_LEVEL;
    public static Long LEVEL_DIFF;
    public static Long STUBBY_DISCORD_USERID = 1120989978994937897L;


    public static void init(String filePath){
        try {
            Class.forName("org.sqlite.JDBC");
            Connection databaseConnection = DriverManager.getConnection("jdbc:sqlite:DDServerData.db");


            ResultSet resultSet = databaseConnection.createStatement().executeQuery("SELECT * FROM Config INNER JOIN BotData ON (BotData.SNAPSHOT = Config.SNAPSHOT);");
            while (resultSet.next()){
                RCON_KEY = resultSet.getString(2);
                HLL_SERVER_RCON_URL = resultSet.getString(3);
                HLL_SERVER_STATS_URL = resultSet.getString(4);
                SEEDING_MESSAGE_DESCRIPTION = resultSet.getString(5).replaceAll("\\\\n", "\n");
                HLL_STATS_UPDATE_DELAY = resultSet.getLong(6);
                XP_PER_MESSAGE = resultSet.getLong(7);
                MAX_XP_PER_DAY = resultSet.getLong(8);
                START_LEVEL = resultSet.getLong(9);
                LEVEL_DIFF = resultSet.getLong(10);
                DISCORD_TOKEN = resultSet.getString(12);
                HLL_STATS_DISCORD_CHANNEL = resultSet.getString(13);
            }
            databaseConnection.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.fillInStackTrace();
        }
    }
}
