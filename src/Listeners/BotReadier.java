package Listeners;

import Classes.Bot;
import Classes.Config;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BotReadier extends ListenerAdapter {

    // log command usages
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        // log all commands to bot spam log in test guild server
        Objects.requireNonNull(Bot.jda.getTextChannelById("821410427111866409")).sendMessage(Objects.requireNonNull(event.getMember()).getEffectiveName() +" used "+event.getCommandString()).queue();
    }

    // add commands to guilds, and guild members to database
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event){
        // for guild-specific commands

        System.out.println(event.getGuild().getName()+" guild ready");
        List<CommandData> commandData = new ArrayList<>();


        // only test server
        if (event.getGuild().getId().equals(Config.TEST_GUILD_ID)){
            commandData.add(Commands.slash("terminate", "Stops stubby in its tracks"));
            commandData.add(Commands.slash("version", "Gives the current version of Stubby that is running"));

            commandData.add(Commands.slash("send-message", "sends a message is specified text channel as Stubby").addOptions(
                            new OptionData(OptionType.STRING, "channel-id", "the ID of the channel to send a message to", true),
                            new OptionData(OptionType.STRING, "message-id", "the ID of the message to replicate. Command must be used in same channel", true)
                    )
                    .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
            );

            commandData.add(Commands.slash("set-fin-move", "Sets the fin move restrictor to restrict moving Fin.").addOptions(
                            new OptionData(OptionType.BOOLEAN, "set", "true/false", true)
                            )
                            .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
            );


            commandData.add(Commands.slash("execute-sql", "sends an SQL Query to Stubby's Database, and Returns and Results.").addOptions(
                            new OptionData(OptionType.STRING, "sql", "the SQL of the query to be ran, make sure it's formatted correctly", true)
                            )
                            .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
            );

            event.getGuild().updateCommands().addCommands(commandData).queue();
        }

        // get all members currently in guild, and check if in database, if not, add them
        try {
            ResultSet member_info;
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:DDServerData.db");
            member_info = c.createStatement().executeQuery("SELECT ID from Members;");
            // collect the IDs of users from database into a list
            List<String> storedIDs = new ArrayList<>();
            while (member_info.next()){
                storedIDs.add(member_info.getString(1));
            }
            c.close();

            List<Member> members = event.getGuild().getMembers();
            List<Member> missing_members = new ArrayList<>();
            // check if the users are same with the ones in the guild, if not, add them
            for (Member guild_member : members){
                if (!storedIDs.contains(guild_member.getId()))
                    missing_members.add(guild_member);
            }
            Bot.addMembersToDatabase(missing_members);

        } catch (ClassNotFoundException | SQLException e) {
            Bot.sendErrorMessage(e);
        }
    }

    // global commands
    @Override
    public void onReady(@NotNull ReadyEvent event){
        // for all common commands

        System.out.println("bot ready!");
        List<CommandData> commandData = new ArrayList<>();

        // fun/useful commands
        commandData.add(Commands.slash("set-activity", "Sets Stubby's current discord activity")
                .addOptions(
                        new OptionData(OptionType.STRING, "type", "The Type of activity to be shown", true)
                                .addChoice("Playing", "playing")
                                .addChoice("Watching", "watching")
                                .addChoice("Listening", "listening to"),
                        new OptionData(OptionType.STRING, "text", "The text that the activity shows, e.g. Listening to {text}", true)
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE, Permission.MANAGE_ROLES))
        );

        commandData.add(Commands.slash("ping", "tests Sgt. Stubby's Reaction time"));
        commandData.add(Commands.slash("sl-roulette", "Selects a random person in your Voice Chat to be the Squad Leader for next game"));


        // dog-tags
        commandData.add(Commands.slash("set-dogtags", "Turns On/Off the management of DD and DDR tags in a user's nickname, based off server roles")
                .addOptions(
                        new OptionData(OptionType.BOOLEAN, "set", "True means Stubby will automatically manage DD and DDR tags, False means not", true)
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
        );
        commandData.add(Commands.slash("set-dd-promotion-message", "Turns On/Off the auto-message that posts when member -> DDR, and DDR -> DD. On Delay")
                .addOptions(
                        new OptionData(OptionType.BOOLEAN, "set", "True means Stubby will post a message welcoming the new members of DD in DD_General, False means not", true)
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
        );


        // XP and levelling
        commandData.add(Commands.slash("level", "Get Your Current Level from Chatting in our Discord"));
        commandData.add(Commands.slash("set-member-levelling", "Turns On/Off the levelling of the given member. set:{true/false} member:{@member_ping}")
                .addOptions(
                        new OptionData(OptionType.BOOLEAN, "set", "True means Stubby will let the member collect xp and gain levels, False means they won't", true),
                        new OptionData(OptionType.MENTIONABLE, "member", "Set this to the server ping for a certain discord member, e.g. '@Stener[DD]'.", true)
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
        );


        // HLL Commands
        commandData.add(Commands.slash("reset-seeding-leaderboard", "Resets all of the Players Seeding time to 0 in the leaderboard, Current Scores will not be saved.")
                .addOptions(
                        new OptionData(OptionType.BOOLEAN, "confirmation", "True: Yes, i want to reset the scores to 0. False: No, I want to Keep the Current Scores.", true)
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
        );
        commandData.add(Commands.slash("add-full-vip", "Adds a Permanent VIP to the DD Server with given steam_id and name.")
                .addOptions(
                        new OptionData(OptionType.STRING, "name", "The New Members' Username (does not need to be accurate, just for reference).", true),
                        new OptionData(OptionType.STRING, "steam-id", "The New Members' Steam ID. This needs to be accurate and is the 17-digit number on their profile.", true)
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
        );
        commandData.add(
                Commands.slash("add-temp-vip", "Adds a Temporary VIP to the DD Server with given name, steam_id, and time.")
                .addOptions(
                        new OptionData(OptionType.STRING, "name", "The New Members' Username (does not need to be accurate, just for reference).", true),
                        new OptionData(OptionType.STRING, "steam-id", "The New Members' Steam ID. This needs to be accurate and is the 17-digit number on their profile.", true),
                        new OptionData(OptionType.INTEGER, "time-unit", "Eiter hours, days, weeks, months, or years", true)
                                .addChoice("Hours", 1)
                                .addChoice("Days", 24)
                                .addChoice("Weeks", 168)
                                .addChoice("Months", 730)
                                .addChoice("Year", 8760),
                        new OptionData(OptionType.INTEGER, "duration", "The number of Hours/Days/etc until the VIP expires", true)
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
        );
        commandData.add(
                Commands.slash("add-group-vip", "Adds (temp) VIP to players on the HLL server for a given amount of time. If already vip, doesn't add")
                .addOptions(
                        new OptionData(OptionType.INTEGER, "time-unit", "Eiter hours, days, weeks, months, or years, or indefinite", true)
                                .addChoice("Hours", 1)
                                .addChoice("Days", 24)
                                .addChoice("Weeks", 168)
                                .addChoice("Months", 730)
                                .addChoice("Year", 8760)
                                .addChoice("Indefinite (forever, and ever...)", -1),
                        new OptionData(OptionType.INTEGER, "duration", "The number of Hours/Days/etc until the VIP expires (is ignored if indefinite)", true),
                        new OptionData(OptionType.INTEGER, "number-players", "Will give the first (num) players who joined the server, vip. if larger than player count, adds all", true)
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
        );

        event.getJDA().updateCommands().addCommands(commandData).queue();
    }
}
