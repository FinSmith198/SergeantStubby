package Listeners;

import Classes.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HLListener extends ListenerAdapter {

    private final ScheduledExecutorService statScheduler = Executors.newScheduledThreadPool(1);
    private TextChannel seedingChannel;
    private EmbedBuilder seedEmbedBuilder = new EmbedBuilder();
    private String leaderboard_ID;

    private JSONArray playerStats;
    private JSONArray oldPlayerStats = new JSONArray();


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equals("add-full-vip")) {
            event.deferReply(true).queue();
            String reply;
            try {
                String steam_id = Objects.requireNonNull(event.getOption("steam-id")).getAsString();
                String name = Objects.requireNonNull(event.getOption("name")).getAsString();

                if (steam_id.length() != 17) throw new NumberFormatException("Invalid SteamID; Steam ID Must be 17 digits long");

                String parameters = String.format("{\"steam_id_64\": \"%s\", \"name\": \"%s\", \"forward\": false}", steam_id, name);

                JSONObject returned = Objects.requireNonNull(sendRequest("do_add_vip", parameters));
                if (returned.get("failed").equals(true)) throw new Exception("Server did not accept the command. Maybe make sure it's active, or try new data");
                reply = String.format("Player '**%s**' with id '**%s**'\nhas successfully been added to vip list", name, steam_id);
            } catch (Exception e){
                reply = "Player was NOT added to the vip list, something went wrong.\nError Message: "+e.getMessage();
            }

            event.getHook().sendMessage(reply).queue();

        }

        if (command.equals("add-temp-vip")) {
            event.deferReply(true).queue();
            String reply;
            try {
                String steam_id = Objects.requireNonNull(event.getOption("steam-id")).getAsString();
                String name = Objects.requireNonNull(event.getOption("name")).getAsString();
                long hours_multiplier = Objects.requireNonNull(event.getOption("time-unit")).getAsLong();
                long hours = hours_multiplier * Objects.requireNonNull(event.getOption("duration")).getAsLong();


                if (steam_id.length() != 17) throw new NumberFormatException("Invalid SteamID; Steam ID Must be 17 digits long");
                if (hours <= 0) throw new NumberFormatException("Invalid Duration; Please enter a Valid number above 0");

                LocalDateTime ldt = LocalDateTime.now().plusHours(hours);
                ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(ldt);
                ldt = ldt.minusSeconds(offset.getTotalSeconds());
                DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss+00:00", Locale.ENGLISH);
                String expiry_date = format1.format(ldt);

                String parameters = String.format("{\"steam_id_64\": \"%s\", \"name\": \"%s\", \"forward\": false, \"expiration\": \"%s\"}", steam_id, name, expiry_date);

                JSONObject returned = Objects.requireNonNull(sendRequest("do_add_vip", parameters));
                if (returned.get("failed").equals(true)) throw new Exception("Server did not accept the command. Maybe make sure it's active, or try new data");
                reply = String.format("Player '**%s**' with id '**%s**'\nhas successfully been added to vip list. \nIt will expire at _%s_", name, steam_id, expiry_date);
            } catch (Exception e){
                reply = "Player was NOT added to the vip list, something went wrong.\nError Message: "+e.getMessage();
            }

            event.getHook().sendMessage(reply).queue();

        }

        if (command.equals("add-group-vip")) {
            event.deferReply(true).queue();
            String reply;
            int i = 0;
            int player_num = 0;
            try {
                long hours_multiplier = Objects.requireNonNull(event.getOption("time-unit")).getAsLong();
                long hours = hours_multiplier * Objects.requireNonNull(event.getOption("duration")).getAsLong();
                player_num = Objects.requireNonNull(event.getOption("number-players")).getAsInt();

                // prepare to make list of players, if chosen num of players > actual num of players, add every player
                if (player_num < 0 || player_num > 100) throw new NumberFormatException("Invalid Number of Players; Must be from 0 to 100");
                if (player_num > playerStats.size()) player_num = playerStats.size();

                // make list of all the first n players on the server
                List<JSONObject> sortedPlayerStats = new ArrayList<>();
                for (Object playerStat : playerStats) sortedPlayerStats.add((JSONObject) playerStat);

                sortedPlayerStats.sort((a, b) -> {
                    long valA = 0;
                    long valB = 0;

                    try {
                        valA = (long) ((JSONObject) a.get("profile")).get("total_playtime_seconds");
                        valB = (long) ((JSONObject) b.get("profile")).get("total_playtime_seconds");
                    } catch (Exception e) {
                        //do something
                    }

                    return Long.compare(valA, valB);
                    //if you want to change the sort order, simply use the following:
                    //return -valA.compareTo(valB);
                });



                if (hours_multiplier == -1){
                    // indefinite vip
                    for (i = 0; i < player_num; i++){
                        JSONObject player = sortedPlayerStats.get(i);

                        // do not add player to vip list, if already vip
                        String steam_id = player.get("steam_id_64").toString();
                        String name = player.get("name").toString();

                        String parameters = String.format("{\"steam_id_64\": \"%s\", \"name\": \"%s\", \"forward\": false}", steam_id, name);

                        JSONObject returned = Objects.requireNonNull(sendRequest("do_add_vip", parameters));
                        if (returned.get("failed").equals(true)) throw new Exception("Server did not accept the command. Maybe make sure it's active, or try new data");
                    }
                    reply = String.format("%s Players, of %s Total Players\nwere successfully added to vip list. \nIt will never expire", player_num, sortedPlayerStats.size());
                }
                else {
                    // not indefinite
                    if (hours <= 0) throw new NumberFormatException("Invalid Duration; Please enter a Valid number above 0");

                    // set time and get expiry date of vip
                    LocalDateTime ldt = LocalDateTime.now().plusHours(hours);
                    ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(ldt);
                    ldt = ldt.minusSeconds(offset.getTotalSeconds());
                    DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss+00:00", Locale.ENGLISH);
                    String expiry_date = format1.format(ldt);

                    // loop through the first n players, add them to vip
                    for (i = 0; i < player_num; i++){
                        JSONObject player = sortedPlayerStats.get(i);

                        if ((boolean) player.get("is_vip")) continue;

                        String steam_id = player.get("steam_id_64").toString();
                        String name = player.get("name").toString();


                        String parameters = String.format("{\"steam_id_64\": \"%s\", \"name\": \"%s\", \"forward\": false, \"expiration\": \"%s\"}", steam_id, name, expiry_date);

                        JSONObject returned = Objects.requireNonNull(sendRequest("do_add_vip", parameters));
                        if (returned.get("failed").equals(true)) throw new Exception("RCON Server did not accept the command. Maybe make sure the server is active, or try new data");
                    }


                    reply = String.format("%s Players, of %s Total Players\nwere successfully added to vip list. \nIt will expire at _%s_", player_num, sortedPlayerStats.size(), expiry_date);
                }


            } catch (Exception e){
                reply = "Added "+i+" out of "+player_num+" to the vip list, something went wrong.\nError Message: "+e.getMessage();
            }

            event.getHook().sendMessage(reply).queue();

        }

    }


    @Override
    public void onReady(@NotNull ReadyEvent event){

        final long delay = (Long) Config.getInstance().config.get("HLL_STATS_UPDATE_DELAY");


        seedingChannel = event.getJDA().getTextChannelById((String) Config.getInstance().config.get("HLL_STATS_DISCORD_CHANNEL"));
        seedEmbedBuilder.setTitle("Top 50 Seeders (Hours Seeded):");
        seedEmbedBuilder.setColor(Color.GREEN);
        seedEmbedBuilder.setDescription(String.format((String) (Config.getInstance().config.get("SEEDING_MESSAGE_DESCRIPTION")), delay));
        seedEmbedBuilder.setFooter("-Sgt. Stubby");

        try {
            MessageHistory history = MessageHistory.getHistoryFromBeginning(seedingChannel).complete();
            List<Message> mess = history.getRetrievedHistory();
            for (Message message : mess){
                if (message != null && message.getAuthor().isBot()){
                    leaderboard_ID = message.getId();
                    break;
                }
            }
            if (leaderboard_ID == null)
                throw new Exception ("No Leaderboard Found");
        } catch (Exception e){
            seedingChannel.sendMessageEmbeds(seedEmbedBuilder.build()).queue();
        }


        setStatScheduler(delay);
    }

    private void updateStatsMessage() {
        if (leaderboard_ID == null) leaderboard_ID = seedingChannel.getLatestMessageId();


        seedEmbedBuilder.clearFields();

        // formats a list of seeders' username, and time seeded in hours
        // List seeders: element example: "[DD] Fin198: 3.7"
        List<String> seeders = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:DDServerData.db");
            ResultSet resultSet = c.createStatement().executeQuery("SELECT username, time_seeded FROM SteamPlayers WHERE time_seeded != 0 ORDER BY time_seeded DESC LIMIT 50;");
            while (resultSet.next()){
                String username = resultSet.getString(1);
                double seconds_seeded = resultSet.getDouble(2);

                double time_seeded = round((seconds_seeded / 3600.0), 1);
                String seeder = String.format("%s: %s", username, time_seeded);
                seeders.add(seeder);
            }
            c.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }


        StringBuilder top25 = new StringBuilder("_ _");
        for (int i = 0; i<25; i++){
            String seeder = (i < seeders.size()) ? discordFormat(seeders.get(i)) : "_blank_";
            top25.append(String.format("\n%s) %s", i+1, seeder));
        }
        StringBuilder top26to50 = new StringBuilder("_ _");
        for (int i = 25; i<50; i++){
            String seeder = (i < seeders.size()) ? discordFormat(seeders.get(i)) : "_blank_";
            top26to50.append(String.format("\n%s) %s", i+1, seeder));
        }

        seedEmbedBuilder.addField("Top 1-25:", top25.toString(), true);
        seedEmbedBuilder.addField("Top 26-50:", top26to50.toString(), true);

        try{
            seedingChannel.editMessageEmbedsById(leaderboard_ID, seedEmbedBuilder.build()).queue();
        } catch (Exception ignored){
        }

    }

    private void setStatScheduler(long delay){
        // scheduler that checks every (60 sec) time period if a person who 'was' on the server is still present
        // if they are still there, then add 60 sec to their seeding time


        statScheduler.scheduleAtFixedRate(() -> {

                    try {
                        updatePlayerStats();
                        // update people's usernames and seeding time if server is not already seeded

                        // only true if server is up, but with nobody on it, or server is down. so, there's no point doing rest of the function
                        if (playerStats.isEmpty()) {
                            oldPlayerStats = new JSONArray();
                            return;
                        }

                        List<String> oldIDs = new ArrayList<>();
                        for (Object old_player : oldPlayerStats) {
                            oldIDs.add((String) ((JSONObject) old_player).get("steam_id_64"));
                        }


                        Class.forName("org.sqlite.JDBC");
                        Connection c = DriverManager.getConnection("jdbc:sqlite:DDServerData.db");

                        // iterator to link past player stats to the current player stats : current will never have less than previous, as it only gets added to (unless new session occurs)
                        // int i = -1;
                        for (Object obj : playerStats) {
                            JSONObject player_stat = (JSONObject) obj;

                            // check if the returned value is valid
                            if (player_stat.get("profile") == null) continue;
                            if (!player_stat.get("steam_id_64").toString().replaceAll("[0-9]+", "").isEmpty()) continue;

                            String formatted_name = player_stat.get("name").toString().replaceAll("'", "").replaceAll("\"", "");

                            // only add seeding time if player has joined in last check, and seeding is in motion. do not count 1 player as seeding
                            if (oldIDs.contains((String) player_stat.get("steam_id_64")) && (playerStats.size() < 50 && playerStats.size() > 1))
                            {
                                // i++;
                                // long current_playtime = (long) ((JSONObject) player_stat.get("profile")).get("current_playtime_seconds");
                                // long old_playtime = (long) (((JSONObject) ((JSONObject) oldPlayerStats.get(i)).get("profile")).get("current_playtime_seconds"));
                                // long seed_to_add = (current_playtime  - old_playtime);

                                // if for some reason it doesn't equal about the delay, just add the delay
                                // if (round(seed_to_add, 0) != delay) seed_to_add = delay;

                                c.createStatement().execute("UPDATE SteamPlayers SET (username, time_seeded) = ('"+formatted_name+"', time_seeded + "+ delay +") WHERE STEAM_ID = " + player_stat.get("steam_id_64") + ";");

                            }
                            // if not, seeding time is not to be updated, so just either attempt to insert the player, or if already present, update their record
                            else {
                                try {
                                    c.createStatement().execute("INSERT INTO SteamPlayers (STEAM_ID, username) VALUES (" + player_stat.get("steam_id_64") + ", '" + formatted_name + "');");
                                } catch (SQLException e) {
                                    try {
                                        c.createStatement().execute("UPDATE SteamPlayers SET (username) = ('" + formatted_name + "') WHERE STEAM_ID = " + player_stat.get("steam_id_64") + ";");
                                    } catch (SQLException o){
                                        System.out.println(o.getMessage());
                                    }
                                }
                            }
                        }
                        c.close();

                        updateStatsMessage();
                        oldPlayerStats = playerStats;

                    } catch (Exception e){
                        e.printStackTrace();
                    }


                },
                5,
                TimeUnit.SECONDS.toSeconds(delay),
                TimeUnit.SECONDS);
    }

    private void updatePlayerStats(){
        JSONObject all_stats = sendRequest("get_players");

        playerStats = ((all_stats == null) ? new JSONArray() : (JSONArray) (all_stats.get("result")));
    }

    private JSONObject sendRequest(String function){
        try {
            URL url = new URL(Config.getInstance().config.get("HLL_SERVER_STATS_URL") + function);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestProperty("Authorization","bearer: "+ Config.getInstance().config.get("RCON_KEY"));
            con.setRequestMethod("GET");

            InputStream inputStream = con.getInputStream();

            JSONParser jsonParser = new JSONParser();
            return (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        } catch (IOException ignored) {

        } catch (ParseException e){
            System.out.println(e.getMessage());
        }
        return null;
    }


    // same as sendRequest, but includes parameters
    private JSONObject sendRequest(String function, String parameter_data) {
        try {
            URL url = new URL(Config.getInstance().config.get("HLL_SERVER_STATS_URL") + function);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("Authorization","bearer: "+ Config.getInstance().config.get("RCON_KEY"));
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            byte[] input = parameter_data.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);


            InputStream inputStream = connection.getInputStream();

            JSONParser jsonParser = new JSONParser();
            return (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        } catch (IOException ignored) {

        } catch (ParseException e){
            System.out.println(e.getMessage());
        }
        return null;
    }


    private double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    private String discordFormat (String s){
        return s.replaceAll("\\|", "\\\\|")
                .replaceAll("_", "\\\\_")
                .replaceAll("\\*", "\\\\*")
                .replaceAll("~", "\\\\~");
    }

}
