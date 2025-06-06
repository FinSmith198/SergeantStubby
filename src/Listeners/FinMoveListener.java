package Listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class FinMoveListener extends ListenerAdapter {
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        AudioChannel oldChannel = event.getOldValue(); // Old voice channel (can be null)
        AudioChannel newChannel = event.getNewValue(); // New voice channel (can be null)

        Member member = event.getMember();

        if (member.getIdLong() != 417228810715660289L)
            return;

        if (oldChannel == null || newChannel == null || oldChannel.equals(newChannel))
            return;

        event.getGuild().moveVoiceMember(member, oldChannel).queue();
    }
}
