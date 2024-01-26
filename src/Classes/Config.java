package Classes;

import org.json.simple.JSONObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Config {
    private static Config config_instance = null;
    public JSONObject config;

    private Config(String filePath){
        try {
            Class.forName("org.sqlite.JDBC");
            Connection databaseConnection = DriverManager.getConnection("jdbc:sqlite:DDServerData.db");


            ResultSet resultSet = databaseConnection.createStatement().executeQuery("SELECT * FROM Config INNER JOIN BotData ON (BotData.SNAPSHOT = Config.SNAPSHOT);");
            config = new JSONObject();
            while (resultSet.next()){
                config.put("RCON_KEY", resultSet.getString(2));
                config.put("HLL_SERVER_RCON_URL", resultSet.getString(3));
                config.put("HLL_SERVER_STATS_URL", resultSet.getString(4));
                config.put("SEEDING_MESSAGE_DESCRIPTION", resultSet.getString(5).replaceAll("\\\\n", "\n"));
                config.put("HLL_STATS_UPDATE_DELAY", resultSet.getLong(6));
                config.put("XP_PER_MESSAGE", resultSet.getLong(7));
                config.put("MAX_XP_PER_DAY", resultSet.getLong(8));
                config.put("START_LEVEL", resultSet.getLong(9));
                config.put("LEVEL_DIFF", resultSet.getLong(10));
                config.put("DISCORD_TOKEN", resultSet.getString(12));
                config.put("HLL_STATS_DISCORD_CHANNEL", resultSet.getString(13));
            }
            databaseConnection.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.fillInStackTrace();
        }
    }

    private Config() {

    }

    public static Config getInstance(String filePath){
        if (config_instance == null)
            config_instance = new Config(filePath);
        return config_instance;
    }

    public static Config getInstance(){
        if (config_instance == null)
            config_instance = new Config();
        return config_instance;
    }




}
