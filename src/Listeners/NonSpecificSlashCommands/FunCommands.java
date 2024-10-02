package Listeners.NonSpecificSlashCommands;

import Classes.Bot;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class FunCommands extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();

        // simple ping reply
        if (command.equals("ping")) {
            event.deferReply(true).queue();
            event.getHook().sendMessage("Pong").queue();
        }

        // set stubby's discord activity
        else if (command.equals("set-activity")) {
            event.deferReply(true).queue();
            try {
                String choice = Objects.requireNonNull(event.getOption("type")).getAsString();
                String text = Objects.requireNonNull(event.getOption("text")).getAsString();
                switch (choice){
                    case "listening to":
                        Bot.jda.getPresence().setActivity(Activity.listening(text));
                        event.getHook().sendMessage("Set activity to: Listening to "+text).queue();
                        break;
                    case "playing":
                        Bot.jda.getPresence().setActivity(Activity.playing(text));
                        event.getHook().sendMessage("Set activity to: Playing "+text).queue();
                        break;
                    case "watching":
                        Bot.jda.getPresence().setActivity(Activity.watching(text));
                        event.getHook().sendMessage("Set activity to: Watching "+text).queue();
                        break;
                    default:
                        event.getHook().sendMessage("Error Occurred, Activity was not changed.").queue();
                }
            } catch (NullPointerException e){
                event.getHook().sendMessage("Command Not Processed. Did you format it correctly? (/set-activity {type} {text})").queue();
            }
        }

        // gives a random member from the vc of the member who called the function
        else if (command.equals("sl-roulette")) {
            event.deferReply(true).queue();
            GuildVoiceState vcState = Objects.requireNonNull(event.getMember()).getVoiceState();

            assert vcState != null;
            if (!vcState.inAudioChannel()) {
                event.getHook().sendMessage("```diff\nYou are not currently in a VC, you must join one to use this command\n```").queue();
                return;
            }

            List<Member> vcMembers = Objects.requireNonNull(vcState.getChannel()).getMembers();

            Random random = new Random();
            int rand = random.nextInt(vcMembers.size());

            String s = "```diff\nAnd selected from the "+vcMembers.size()+" Member(s) of the '"+vcState.getChannel().getName()+"' VC, The winner of the Squad Leader Lottery is...\n\n+"+vcMembers.get(rand).getEffectiveName()+"!```";
            event.getHook().sendMessage(s).queue();
        }

    }

}
