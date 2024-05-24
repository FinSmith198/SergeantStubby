package Listeners;

import Classes.Bot;
import Classes.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.SQLException;
import java.util.function.Consumer;

public class WelcomeMessageListener extends ListenerAdapter {

    // member joins server + message
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event){
        Member member = event.getMember();

        // send welcome message
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Welcome to Devil Dogs! 🎉");
        eb.setColor(Color.green);
        eb.setDescription(String.format("Hello <@%s>, \nThanks for Joining the **---Devil Dogs Hell Let Loose Server!---** \nWe're glad you could join!", member.getId()));
        eb.addField("\nRead THIS First:", "Please, before you do anything, Make sure you have gone over to https://discord.com/channels/1001456689851146331/1001507759143002132 for information on The Rules. \nYou **must** follow these when on our discord.", false);
        eb.addField("\nNotices", "If you need any admin assistance please contact an admin via https://discord.com/channels/1001456689851146331/1001508754338103337. \nOr type \"!admin {message}\" for in-game HLL assistance in the in-game chat.", false);
        eb.setFooter("-Sgt. Stubby");

        // try sending message to user, else print error
        try{
            Consumer<PrivateChannel> messageSender = channel -> channel.sendMessageEmbeds(eb.build()).queue();
            member.getUser().openPrivateChannel().queue(messageSender);
        } catch (Exception e){
            Bot.sendErrorMessage(new Exception("Could not send private message to " + member.getUser().getName()));
        }


        // try to add them to DB
        try {
            Bot.addMemberToDatabase(event.getMember());
        } catch (SQLException | ClassNotFoundException e) {
            Bot.sendErrorMessage(e);
        }
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event){
        String com_role_id = "1001465266926014505";
        String rep_role_id = "1001465501563752593";
        if (event.getGuild().getId().equals(Config.TEST_GUILD_ID)){
            com_role_id = "1138451798336733204";
            rep_role_id = "1138451857652584448";
        }
        Member member = event.getMember();

        boolean community = false;
        boolean representative = false;
        for (Role role_added : event.getRoles()){
            if (role_added.getId().equals(com_role_id)) community = true;
            else if (role_added.getId().equals(rep_role_id)) representative = true;
        }
        // a nor gate, to decline sending people a message who got neither rep nor community
        if (!(community || representative)) return;

        // send welcome message DM
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.green);
        if (community) {
            eb.setTitle("You Now a Community Member! 📗");
            eb.setDescription(String.format("Hello again <@%s>, \nYou have just gained the role 'Community'! \nPlease note that all Community members Must be 16+ years of age.", member.getId()));
            eb.addField("\nWhat does this Mean?", "As a Community member, you can hang out and chat with anyone in the community areas of the clan, such as chatting with members in the Hell-Let-Loose VC, or posting pictures in the various text channels.", false);
            eb.addField("\nServer Scoreboard and Seeding:", "Two such channels are: https://discord.com/channels/1001456689851146331/1002318069642834040, a channel where you can see your stats whilst playing on our server\n And https://discord.com/channels/1001456689851146331/1134088097290797148, where you can gain points and prizes for seeding our HLL Server!", false);
            eb.addField("\nWant to Join the Clan?", "We are almost always open for recruitment, so if you are ready, head over to https://discord.com/channels/1001456689851146331/1001520952489807983, and make a ticket. Once you have made this, you can start your application.\nWe are a Clan who focus on having fun, and participating in Competitive games, such as the ECL! \nApplicants must be 18+ Only!", false);
            eb.addField("\nOther Notices", "We hope you have a Good time as a community member! \nYou can change to representative in the Channels and Roles section if ever needed.", false);
        } else {
            eb.setTitle("You Now a Representative! 🗒");
            eb.setDescription(String.format("Hello again <@%s>, \nYou have just gained the role 'Representative'!", member.getId()));
            eb.addField("\nWhat does this Mean?", "As a Representative, you can hang out and chat just like a normal Community Member, but you are simply seen as a member Representing another HLL Clan.", false);
            eb.addField("\nOther Notices", "We hope you have a Good time as a clan representative! \nYou can change to Community in the Channels and Roles section if ever needed.", false);
        }
        eb.setFooter("-Sgt. Stubby");

        try{
            Consumer<PrivateChannel> messageSender = channel -> channel.sendMessageEmbeds(eb.build()).queue();
            member.getUser().openPrivateChannel().queue(messageSender);
        } catch (Exception e){
            Bot.sendErrorMessage(new Exception("Could not send private message to " + member.getUser().getName()));
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        if (message.equalsIgnoreCase("hello stubby")){
            event.getChannel().sendMessage("Hi!").queue();
            Bot.sendErrorMessage(new Exception("test lol"));
        }
    }
}
