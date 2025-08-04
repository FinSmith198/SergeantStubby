package Listeners;

import Classes.Bot;
import Classes.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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

        // early return for test guild
        if (event.getGuild().getId().equals(Config.TEST_GUILD_ID))
            return;

        // get the member who joined, and the current set welcome channel
        Member member = event.getMember();
        TextChannel welcomeChannel = event.getJDA().getTextChannelById(Config.WELCOME_CHANNEL);

        assert welcomeChannel != null;

        // send the message
        welcomeChannel.sendMessage("Welcome to the Devil Dogs discord server <@"+member.getId()+">! If you are interested in joining our clan, please find out recruitment channel at https://discord.com/channels/1001456689851146331/1001520952489807983 and it will walk you through our process. If there is anything we can help you with, feel free to shoot any of our Staff Sergeants a message.").queue();

        // try to add them to DB
        try {
            Bot.addMemberToDatabase(event.getMember());
        } catch (SQLException | ClassNotFoundException e) {
            Bot.sendErrorMessage(e);
        }
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        if (message.equalsIgnoreCase("hello stubby")){
            event.getChannel().sendMessage("Hi!").queue();
        }
    }
}
