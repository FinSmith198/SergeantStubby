package Listeners;

import Classes.Bot;
import Classes.Config;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WelcomeMessageListener extends ListenerAdapter {

    // member joins server + message
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event){

        // early return for test guild
        if (event.getGuild().getId().equals(Config.TEST_GUILD_ID))
            return;

        // get the member who joined, and the current set welcome channel
        Member member = event.getMember();

        if (member.getUser().isBot())
            return;

        TextChannel welcomeChannel = event.getJDA().getTextChannelById(Config.WELCOME_CHANNEL);
        assert welcomeChannel != null;

        // use a scheduler as a kind of time-out, periodically checking if the user has accepted the onboarding rules
        AtomicInteger timer = new AtomicInteger(0);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (timer.addAndGet(10) >= 300)
                scheduler.shutdown();
            if (!member.isPending()) {
                sendWelcomeMessage(member, welcomeChannel);
                scheduler.shutdown();
            }
        }, 10, 10, TimeUnit.SECONDS); // check every 10 seconds
    }

    protected void sendWelcomeMessage(Member member, TextChannel welcomeChannel){
        String message;
        long representativeRole = 1001465501563752593L;
        // if user chooses rep, don't ask to join clan
        if (member.getRoles().stream().anyMatch(r -> r.getIdLong() == representativeRole))
            message = "Welcome to the Devil Dogs discord server <@"+member.getId()+">! If there is anything we can help you with, feel free to shoot any of our Staff Sergeants a message.";
        else
            message = "Welcome to the Devil Dogs discord server <@"+member.getId()+">! If you are interested in joining our clan, please find out recruitment channel at https://discord.com/channels/1001456689851146331/1001520952489807983 and it will walk you through our process. If there is anything we can help you with, feel free to shoot any of our Staff Sergeants a message.";

        // send the message
        welcomeChannel.sendMessage(message).queue();

        // try to add them to DB
        try {
            Bot.addMemberToDatabase(member);
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
