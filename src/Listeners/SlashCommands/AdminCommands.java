package Listeners.SlashCommands;

import Classes.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class AdminCommands extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();

        // set the automatic promotion message in DD general
        if (command.equals("set-dd-promotion-message")) {
            event.deferReply(true).queue();
            String s;
            if (!Bot.getTagRestrictorStatus()){
                s = "The dog-tag management is currently inactive, please turn it on to start the auto promotion message";
            } else {
                boolean choice = Objects.requireNonNull(event.getOption("set")).getAsBoolean();
                s = "The automatic message posted to DD general when members are promoted is no longer active";
                if (Bot.setPromotionMessageStatus(choice)){
                    s = "The automatic message posted to DD general when members are promoted is now active";
                }
            }
            event.getHook().sendMessage(s).queue();
        }

        // set management and restriction of DD/DDR dog-tags
        else if (command.equals("set-dogtags")) {
            event.deferReply(true).queue();
            boolean active = Bot.setTagRestrictor(Objects.requireNonNull(event.getOption("set")).getAsBoolean());
            String s = "Sgt Stubby is no longer Restricting/Managing DD/DDR Tags in the Server...";
            if (active) s = "Sgt Stubby is now Restricting/Managing DD/DDR Tags in the Server.";

            event.getHook().sendMessage(s).queue();
        }

        else if (command.equals("reset-seeding-leaderboard")) {
            event.deferReply(true).queue();
            String response = "Leaderboard was reset, all players' time has been reset to 0!\nYou may need to wait for the Leaderboard to Update.";
            try {
                if (!Objects.requireNonNull(event.getOption("confirmation")).getAsBoolean()) {
                    throw new Exception("Confirmation was Set to False. Command was Aborted.");
                }
                Class.forName("org.sqlite.JDBC");
                Connection c = DriverManager.getConnection("jdbc:sqlite:DDServerData.db");
                c.createStatement().execute("UPDATE SteamPlayers SET time_seeded = 0;");
            } catch (Exception e) {
                e.printStackTrace();
                response = "Leaderboard was not reset.\n"+e.getMessage();
            }
            event.getHook().sendMessage(response).queue();

        }




    }


}
