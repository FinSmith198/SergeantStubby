package Listeners;

import Classes.Bot;
import Classes.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MemberCounter extends ListenerAdapter {

    //   [wardogs, devildogs, recruits, members]
    private VoiceChannel[] channels = new VoiceChannel[4];
    private int[] roleCounts = new int[4];
    private List<Role> countedRoles = new ArrayList<>();

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        if (!event.getGuild().getId().equals("1001456689851146331"))
            return;

        countedRoles.add(event.getJDA().getRoleById(1126070633382555658L));
        countedRoles.add(event.getJDA().getRoleById(1001491099749204048L));
        countedRoles.add(event.getJDA().getRoleById(1001491310957576223L));

        // get number of DD, DDR, and Total members.
        roleCounts[0] = event.getGuild().getMembersWithRoles(countedRoles.get(0)).size();
        roleCounts[1] = event.getGuild().getMembersWithRoles(countedRoles.get(1)).size();
        roleCounts[2] = event.getGuild().getMembersWithRoles(countedRoles.get(2)).size();
        roleCounts[3] = event.getGuild().getMembers().size();

        // get channels
        channels[0] = event.getGuild().getVoiceChannelById(1188455767502766190L);
        channels[1] = event.getGuild().getVoiceChannelById(1188455844879278121L);
        channels[2] = event.getGuild().getVoiceChannelById(1188455932468936785L);
        channels[3] = event.getGuild().getVoiceChannelById(1188456017076441118L);


        updateRoleStats();
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        for (Role role : event.getRoles()){
            if (!countedRoles.contains(role))
                continue;
            roleCounts[countedRoles.indexOf(role)]++;
        }
        updateRoleStats();
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        for (Role role : event.getRoles()){
            if (!countedRoles.contains(role))
                continue;
            roleCounts[countedRoles.indexOf(role)]--;
        }
        updateRoleStats();
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        roleCounts[roleCounts.length-1]++;
        updateTotalMemberCount();
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        roleCounts[roleCounts.length-1]--;
        updateTotalMemberCount();
    }

    public void updateTotalMemberCount(){
        channels[channels.length - 1].getManager().setName(
                channels[channels.length - 1].getName().split(": ")[0] + ": " + roleCounts[roleCounts.length-1]
        ).queue();
    }

    public void updateRoleStats(){
        for (int i = 0; i < roleCounts.length; i++) {
            String[] data = channels[i].getName().split(": ");
            if (Integer.parseInt(data[1]) == roleCounts[i])
                continue;
            String newName = data[0] + ": " + roleCounts[i];
            channels[i].getManager().setName(newName).queue();
        }
    }
}
