package Listeners;

import Classes.Bot;
import Classes.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// controls member XP and levels. levels are directly determined by XP, and XP needed to level up increases with each level. customisable
public class CommunityXPController extends ListenerAdapter{

    private final List<String> COMMUNITY_CATEGORIES = Arrays.asList("1001456690304135301", "1128007051600212019");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        // ignore messages from bots, we don't want bots to start getting XP...
        if (event.getAuthor().isBot()) return;

        // ignore messages which not in any category, or are in a category which is not the community area
        if (!event.getMessage().isFromGuild()) return;
        if (event.getMessage().getCategory() == null) return;
        if (!COMMUNITY_CATEGORIES.contains(event.getMessage().getCategory().getId())) return;

        String member_ID = Objects.requireNonNull(event.getMember()).getId();
        // if member's accumulator is at max capacity, do not give any xp for the message
        if (Bot.getMemberAccumulatedXP(member_ID) >= (long) Config.getInstance().config.get("MAX_XP_PER_DAY")) return;

        // give xp for messages
        try {
            // gets the xp, if function returns -1, that means levelling is disabled, so return from function
            int current_xp = Bot.getMemberXP(member_ID);
            if (current_xp == -1) return;

            long[] old_level = Bot.getMemberLevel(current_xp);
            long[] new_level;

            Bot.addXPToMember(member_ID, (long) Config.getInstance().config.get("XP_PER_MESSAGE"));
            current_xp += (long) Config.getInstance().config.get("XP_PER_MESSAGE");
            new_level = Bot.getMemberLevel(current_xp);

            if (new_level[0] == old_level[0]) return;

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Level UP!");
            eb.setDescription(String.format("**Hey, <@%s>**, you are now **Level %s**, and only _%s xp_ away from Level %s!", member_ID, new_level[0], new_level[1], new_level[0]+1));
            eb.setColor(Color.orange);
            eb.setFooter("-Sgt. Stubby");

            event.getChannel().sendMessageEmbeds(eb.build()).queue();


        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("level")){
            event.deferReply(true).queue();
            try {
                EmbedBuilder eb = new EmbedBuilder();
                int xp = Bot.getMemberXP(Objects.requireNonNull(event.getMember()).getId());
                if (xp == -1){
                    eb.setTitle("You are Currently **Level 0**");
                    eb.setDescription(String.format("Hey %s, you are only Level 0, as your levelling has been **disabled by an admin**.", event.getMember().getEffectiveName()));
                    eb.addField("Why, and How do I Re-Enable Levelling?", "Your levelling has been disabled, as you were either abusing it, or an admin has decided you would be better without it. To re-enable levelling, please contact somebody from Community Command.", false);
                } else {
                    long[] level_info = Bot.getMemberLevel(Bot.getMemberXP(Objects.requireNonNull(event.getMember()).getId()));
                    eb.setTitle(String.format("You are Currently **Level %s**", level_info[0]));
                    eb.setDescription(String.format("Hey %s, you are only _%s xp_ away from Level %s!", event.getMember().getEffectiveName(), level_info[1], level_info[0]+1));
                    eb.addField("How Do I Get More XP?", "To get more xp, just send some messages, or even some images, over in the _Community_ category of our discord, such as Media! (You are Limited to "+ Config.getInstance().config.get("MAX_XP_PER_DAY") +" xp per day)", false);
                }
                eb.setColor(Color.orange);
                eb.setFooter("-Sgt. Stubby");

                event.getHook().sendMessageEmbeds(eb.build()).queue();

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }

        else if (event.getName().equals("set-member-levelling")){
            try {
                event.deferReply(true).queue();
                boolean choice = Objects.requireNonNull(event.getOption("set")).getAsBoolean();
                String member_id = Objects.requireNonNull(Objects.requireNonNull(event.getOption("member")).getAsMember()).getId();
                Bot.setMemberLevelling(member_id, choice);
                event.getHook().sendMessage("Member "+ Objects.requireNonNull(Objects.requireNonNull(event.getOption("member")).getAsMember()).getEffectiveName()+" now has levelling set to "+choice).queue();
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // point limiter to max of MAX_XP xp per day. uses accumulator to add temp points to. if those points exceed 150, do not add any more points,
    // at end of day, every 1AM uk time, it flushes the accumulator into the member's main xp bank, and resets the accumulator to 0.
    // it does this for all members who have a xp value larger than 0 in accumulator. This system prevents the exploiting of the system to gain xp
    @Override
    public void onReady(@NotNull ReadyEvent event){

        // sets the time to restart a flush to be 1am every day
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime nextAccumulatorFlush = now.withHour(1).withMinute(0).withSecond(0);

        // if current day's time is already past 1am, set time to the next day
        if (now.compareTo(nextAccumulatorFlush) > 0) nextAccumulatorFlush = nextAccumulatorFlush.plusDays(1);

        // duration till the flush, and in seconds
        Duration durationUntilNextFlush = Duration.between(now, nextAccumulatorFlush);
        long initialDelayFlush = durationUntilNextFlush.getSeconds();

        scheduler.scheduleAtFixedRate(() -> {
                    // attempt to flush the accumulators of all the members' day's xp to banks
                    try{
                        Class.forName("org.sqlite.JDBC");
                        Connection c = DriverManager.getConnection("jdbc:sqlite:DDServerData.db");
                        c.createStatement().execute("UPDATE Members SET XP = XP + XP_accumulator, XP_accumulator = 0 WHERE XP_accumulator != 0;");
                        c.close();
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                },
                initialDelayFlush,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS);
    }

}
