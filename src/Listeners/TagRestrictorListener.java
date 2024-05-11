package Listeners;

import Classes.Bot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TagRestrictorListener extends ListenerAdapter {

    private String DDroleID = "1001491099749204048";
    private String DDRroleID = "1001491310957576223";
    private String DDGeneral = "1001495671330836591";

    // variables needed to maintain the 'promotion from DDR to DD' message in general
    private ScheduledFuture<?> DD_handle;
    private ScheduledFuture<?> DDR_handle;
    private final ScheduledExecutorService DD_executor = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService DDR_executor = Executors.newScheduledThreadPool(1);
    private final List<String> new_DD_members = new ArrayList<>();
    private final List<String> new_DDR_members = new ArrayList<>();
    private StringBuilder promotion_message;


    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event){

        DDroleID = "1001491099749204048";
        DDRroleID = "1001491310957576223";
        if (event.getGuild().getId().equals("821405370014629930")) { // sets to different if the guild is in test server
            DDroleID = "1127629409772376144";
            DDRroleID = "1127629496024051842";
        }

        String new_nick = event.getNewNickname();
        if ((new_nick == null) || (!new_nick.toLowerCase().contains("[dd]") && !new_nick.toLowerCase().contains("[ddr]"))) return;


        boolean is_DD_member = false;
        boolean is_DD_recruit = false;
        for (Role role:event.getMember().getRoles()) {
            if (role.getId().equals(DDroleID)){
                is_DD_member = true;
                break;
            }
            // only says they are a recruit if they are not a full DD member first, e.g. if they have both roles, it will only say they are a dd member
            else if (role.getId().equals(DDRroleID)){
                is_DD_recruit = true;
                break;
            }
        }

        if (is_DD_member || is_DD_recruit){
            return;
        }

        try{
            // remove tags, even if lower case, to prevent future confusion. (?i) means the regex is not case-sensitive, and \\[ is just checking for a '['. ']' is fine without the \\, but I left it in for consistency
            if (new_nick.toLowerCase().contains("[dd]")){
                changeNickNameRegex(event.getMember(), "(?i)\\[dd\\]", "");
                Consumer<PrivateChannel> messageSender = channel -> channel.sendMessage("Hey! I noticed that you set [DD] tags in your nickname over on Devil Dogs, and while we do appreciate the enthusiasm, we have a process for becoming a member of DD. Once you apply over in the team-application Channel, you can become a Devil Dogs Recruit. After that, you can hang around for a while and be active, to be bumped to a full member of our clan! If you don't want to be member just yet, you can still hang out with us in the community areas \\:) \n-Sgt Stubby").queue();
                event.getUser().openPrivateChannel().queue(messageSender);
            }
            else if (new_nick.toLowerCase().contains("[ddr]")){
                changeNickNameRegex(event.getMember(), "(?i)\\[ddr\\]", "");
                Consumer<PrivateChannel> messageSender = channel -> channel.sendMessage("Hey! I noticed that you set [DDR] tags in your nickname over on Devil Dogs, and while we do appreciate the enthusiasm, we have a process for becoming a recruit of DD. Once you apply over in the team-application Channel, you can become a Devil Dogs Recruit, and tags can be added to your name. After that, you can hang around for a while and be active, to be bumped to a full member of our clan! If you don't want to be member just yet, you can still hang out with us in the community areas \\:) \n-Sgt Stubby").queue();
                event.getUser().openPrivateChannel().queue(messageSender);
            }
        } catch (Exception e){
            System.out.println("Could not send message to " + event.getUser().getName());
        }


    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event){
        // get the roles added, and the member the roles were added to, add tags where relevant

        List<Role> roles = event.getRoles();
        Member member = event.getMember();

        DDroleID = "1001491099749204048";
        DDRroleID = "1001491310957576223";
        DDGeneral = "1001500240907620453";
        if (event.getGuild().getId().equals("821405370014629930")) { // sets to different if the guild is in test server
            DDroleID = "1127629409772376144";
            DDRroleID = "1127629496024051842";
            DDGeneral = "1121022365921460355";
        }

        for (Role role : roles){
            // new DD role:
            if (role.getId().equals(DDroleID)) {

                // member -> DD (no message)
                if (!member.getRoles().contains(event.getGuild().getRoleById(DDRroleID))) {
                    changeNickName(member, " [DD]");
                    continue;
                }

                // DDR -> DD (send message):
                event.getGuild().removeRoleFromMember(member.getUser(), Objects.requireNonNull(event.getGuild().getRoleById(DDRroleID))).queue();
                changeNickName(member, " [DD]");

                // promotion message
                if (!Bot.getPromotionMessageStatus()) return;
                if (!new_DD_members.contains(member.getId())) new_DD_members.add(member.getId());

                updateDDHandle(event.getGuild());
                return;
            }
            // new DDR role:
            else if (role.getId().equals(DDRroleID)) {
                changeNickName(member, " [DDR]");

                // but already a DD Member (no message)
                if (member.getRoles().contains(event.getGuild().getRoleById(DDroleID)))
                    continue;

                // new DDR member (send message):

                // recruitment message
                if (!Bot.getPromotionMessageStatus()) return;
                if (!new_DDR_members.contains(member.getId())) new_DDR_members.add(member.getId());

                updateDDRHandle(event.getGuild());
                return;
            }
        }
    }


    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event){
        List<Role> roles = event.getRoles();
        Member member = event.getMember();

        DDroleID = "1001491099749204048";
        DDRroleID = "1001491310957576223";
        if (event.getGuild().getId().equals("821405370014629930")) { // sets to different if the guild is in test server
            DDroleID = "1127629409772376144";
            DDRroleID = "1127629496024051842";
        }


        for (Role role : roles){
            if (role.getId().equals(DDroleID)){
                new_DD_members.remove(member.getId());
                changeNickName(member, "[DD]", "");
                break;
            } else if (role.getId().equals(DDRroleID)){
                changeNickName(member, "[DDR]", "");
                break;
            }
        }
    }

    public void changeNickName(Member member, String addition){
        try {
            member.modifyNickname(member.getEffectiveName() + addition).queue();
        } catch (HierarchyException | NullPointerException ignored){}
    }

    public void changeNickName(Member member, String target, String replacement){
        try {
            member.modifyNickname(member.getEffectiveName().replace(target, replacement).strip()).queue();
        } catch (HierarchyException | NullPointerException ignored){}
    }

    public void changeNickNameRegex(Member member, String regex, String replacement){
        try {
            member.modifyNickname(member.getEffectiveName().replaceAll(regex, replacement).strip()).queue();
        } catch (HierarchyException | NullPointerException ignored){}
    }


    public void updateDDHandle(@NotNull Guild guild){
        try {
            DD_handle.cancel(false);
        } catch (NullPointerException ignored){}


        DD_handle = DD_executor.schedule(() ->{
                    if (new_DD_members.isEmpty()) return;

                    String[] emojis = new String[]{"<:Dog:1010471609305419796>", "<:HLLSalute:1012735244597743636>", "<:DDMaj:1022809742638338058>"};
                    // Bot Testing Server emojis
                    if (guild.getId().equals("821405370014629930")) emojis = new String[]{"<:Dog:1121032301522997299>", "<:HLLSalute:1121032288436768930>", "<:DDMaj:1121032268291510303>"};

                    if (new_DD_members.size() == 1){
                        promotion_message = new StringBuilder(String.format("Welcome to the new member: <@%s> %s \nCongratulations on becoming a full member of DD! %s", new_DD_members.get(0), emojis[1], emojis[0]));
                    } else if (new_DD_members.size() == 2){
                        promotion_message = new StringBuilder(String.format("Welcome to the new members: <@%s> and <@%s> %s \nCongratulations to both of you, on becoming full members of DD! %s", new_DD_members.get(0), new_DD_members.get(1), emojis[1], emojis[0]));
                    } else {
                        int i = 0;
                        promotion_message = new StringBuilder("Welcome to the new members: <@").append(new_DD_members.get(i++)).append(">");
                        for (; i < new_DD_members.size() - 1; i++){
                            promotion_message.append(", <@").append(new_DD_members.get(i)).append(">");
                        }
                        promotion_message.append(String.format(", and <@%s> %s \nCongratulations, all of you, on becoming full members of DD! %s", new_DD_members.get(i), emojis[1], emojis[0]));
                    }
                    new_DD_members.clear();
                    Objects.requireNonNull(guild.getTextChannelById(DDGeneral)).sendMessage(promotion_message.toString()).queue();

                    System.out.println("dd handle has been handled");
                }
                , 30, TimeUnit.SECONDS);
    }

    public void updateDDRHandle(@NotNull Guild guild){
        try {
            DDR_handle.cancel(false);
        } catch (NullPointerException ignored){}

        System.out.println("ddr handle being handled");

        DDR_handle = DDR_executor.schedule(() ->{
                    if (new_DDR_members.isEmpty()) return;

                    String[] emojis = new String[]{"<:Dog:1010471609305419796>", "<:HLLSalute:1012735244597743636>", "<:DDMaj:1022809742638338058>"};
                    // Bot Testing Server
                    if (guild.getId().equals("821405370014629930")) emojis = new String[]{"<:Dog:1121032301522997299>", "<:HLLSalute:1121032288436768930>", "<:DDMaj:1121032268291510303>"};

                    if (new_DDR_members.size() == 1){
                        promotion_message = new StringBuilder(String.format("Welcome to our newest recruit: <@%s>! %s", new_DDR_members.get(0), emojis[0]));
                    } else if (new_DDR_members.size() == 2){
                        promotion_message = new StringBuilder(String.format("Welcome to the new recruits: <@%s> and <@%s>! %s", new_DDR_members.get(0), new_DDR_members.get(1), emojis[0]));
                    } else {
                        int i = 0;
                        promotion_message = new StringBuilder("Welcome to the new recruits: <@").append(new_DDR_members.get(i++)).append(">");
                        for (; i < new_DDR_members.size() - 1; i++){
                            promotion_message.append(", <@").append(new_DDR_members.get(i)).append(">");
                        }
                        promotion_message.append(String.format(", and <@%s>! %s", new_DDR_members.get(i), emojis[0]));
                    }
                    new_DDR_members.clear();
                    Objects.requireNonNull(guild.getTextChannelById(DDGeneral)).sendMessage(promotion_message.toString()).queue();

                    System.out.println("ddr handle has been handled");
                }
                , 30, TimeUnit.SECONDS);
    }

}
