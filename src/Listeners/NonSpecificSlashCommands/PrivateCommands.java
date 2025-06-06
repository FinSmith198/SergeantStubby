package Listeners.NonSpecificSlashCommands;

import Classes.Bot;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Objects;

public class PrivateCommands extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();

        // give Stubby's version
        switch (command) {
            case "version" : {
                event.deferReply(true).queue();
                event.getHook().sendMessage("Current version is v1.2.6.2").queue();
                break;
            }
            case "terminate" : {
                event.deferReply(true).queue();
                event.getHook().sendMessage("Stopping Stubby...").queue();
                System.exit(0);
                break;
            }
            case "send-message" : {
                event.deferReply(true).queue();
                try {
                    TextChannel text_channel = Bot.jda.getTextChannelById(Objects.requireNonNull(event.getOption("channel-id")).getAsString());
                    String message = event.getChannel().asTextChannel().retrieveMessageById(Objects.requireNonNull(event.getOption("message-id")).getAsString()).complete().getContentRaw();

                    (Objects.requireNonNull(text_channel)).sendMessage(message).queue();
                    event.getHook().sendMessage("Message was sent to **" + text_channel.getName() + "** with contents:\n```\n" + message + "\n```").queue();
                } catch (Exception e) {
                    event.getHook().sendMessage("Message was not sent, perhaps the channel-ID, or message-ID is incorrect?\nMake sure the referenced message is also in the same channel as your use of this command.").queue();
                }
                break;
            }
            case "set-fin-move" : {
                event.deferReply(true).queue();
                boolean active = Bot.setFinMoveListener(Objects.requireNonNull(event.getOption("set")).getAsBoolean());
                event.getHook().sendMessage("Fin Move Restrictor is now set to "+active).queue();
                break;
            }
            case "execute-sql" : {
                event.deferReply().queue();
                StringBuilder response = new StringBuilder("The SQL was Successfully Run");
                try {
                    String sql = Objects.requireNonNull(event.getOption("sql")).getAsString();

                    Class.forName("org.sqlite.JDBC");
                    Connection c = DriverManager.getConnection("jdbc:sqlite:DDServerData.db");


                    // if the command is select, then prepare to return data in a string
                    if (sql.split(" ", 2)[0].equalsIgnoreCase("SELECT")) {


                        ResultSet resultSet = c.createStatement().executeQuery(sql);
                        ResultSetMetaData metaData = resultSet.getMetaData();

                        if (metaData.getColumnCount() != 0) {
                            response.append(", and Returned:\n\n").append("no#");

                            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                                String columnName = metaData.getColumnName(i);
                                response.append(" \\| ").append(discordFormat(columnName));
                            }
                            response.append("\n");

                            // get all rows of the response
                            while (resultSet.next()) {
                                StringBuilder record = new StringBuilder("\n");
                                record.append(resultSet.getRow());
                                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                                    record.append(" \\| ").append(discordFormat(resultSet.getString(i)));
                                }
                                if (response.length() + record.length() >= 2000) {
                                    event.getHook().sendMessage(response.toString()).queue();
                                    response = new StringBuilder();
                                }
                                response.append(record);
                            }
                        }
                    } else {
                        c.createStatement().execute(sql);
                    }

                    event.getHook().sendMessage(response.toString()).queue();

                    c.close();

                } catch (Exception e) {
                    event.getHook().sendMessage("Command Failed.\n" + e.getMessage()).queue();
                }
                break;
            }
        }

    }

    private String discordFormat (String s){
        return s.replaceAll("\\|", "\\\\|")
                .replaceAll("_", "\\\\_")
                .replaceAll("\\*", "\\\\*")
                .replaceAll("~", "\\\\~");
    }

}
