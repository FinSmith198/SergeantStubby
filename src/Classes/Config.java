package Classes;


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
    public static Long ERROR_MESSAGE_CHANNEL;
    public static String APPLICATION_TICKET_LOGGING_CHANNEL;
    public static String APPLICATION_RECRUIT_GOOGLE_FORM_URL;
    public static String WELCOME_CHANNEL = "1397584180627640404";


    public static void init(String filePath){
        try {
            Class.forName("org.sqlite.JDBC");
            Connection databaseConnection = DriverManager.getConnection("jdbc:sqlite:"+filePath);


            ResultSet resultSet = databaseConnection.createStatement().executeQuery("SELECT * FROM Config INNER JOIN BotData ON (BotData.SNAPSHOT = Config.SNAPSHOT);");
            while (resultSet.next()){
                RCON_KEY = resultSet.getString("rcon_key");
                HLL_SERVER_RCON_URL = resultSet.getString("hll_rcon_url");
                HLL_SERVER_STATS_URL = resultSet.getString("hll_stats_url");
                SEEDING_MESSAGE_DESCRIPTION = resultSet.getString("seeding_message_description").replaceAll("\\\\n", "\n");
                HLL_STATS_UPDATE_DELAY = resultSet.getLong("hll_stats_update_delay");
                XP_PER_MESSAGE = resultSet.getLong("xp_per_message");
                MAX_XP_PER_DAY = resultSet.getLong("max_xp_per_day");
                START_LEVEL = resultSet.getLong("xp_start_level");
                LEVEL_DIFF = resultSet.getLong("xp_level_diff");
                DISCORD_TOKEN = resultSet.getString("TOKEN");
                HLL_STATS_DISCORD_CHANNEL = resultSet.getString("seeding_stats_text_channel");
                ERROR_MESSAGE_CHANNEL = resultSet.getLong("error_message_channel");
                APPLICATION_TICKET_LOGGING_CHANNEL = resultSet.getString("application_ticket_log_channel");
                APPLICATION_RECRUIT_GOOGLE_FORM_URL = resultSet.getString("application_recruit_google_form_url");
            }
            resultSet.close();
            databaseConnection.close();

        } catch (ClassNotFoundException | SQLException e) {
            Bot.sendErrorMessage(e);
        }
    }
}
