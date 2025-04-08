package uk.finlaysmith.stubby.Listeners;

import uk.finlaysmith.stubby.Classes.Bot;
import uk.finlaysmith.stubby.Classes.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class RecruitmentTicketController extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getChannel().getId().equals(Config.APPLICATION_TICKET_LOGGING_CHANNEL))
            return;
        if (event.getMessage().getEmbeds().size() != 1)
            throw new RuntimeException("Too many/few embeds in recruitment ticket handler logs! embeds: "+event.getMessage().getEmbeds().size());

        MessageEmbed embed = event.getMessage().getEmbeds().get(0);


        // parse TicketMaker Message to get {UserName, ChannelName}, for the maker of the ticket, and channel of the ticket
        String ticketMaker;
        String ticketName;
        try {
            String[] tmp = this.parseLoggingEmbed(embed);
            ticketMaker = tmp[0].split("#")[0];
            ticketName = tmp[1];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // get channels by name specified
        List<TextChannel> candidateTicketChannels = Bot.jda.getTextChannelsByName(ticketName, true);
        if (candidateTicketChannels.isEmpty())
            throw new RuntimeException("Cannot find Ticket Channel: " + ticketName + ". no channels by that name were found.");


        // filter candidate channels for most recent
        TextChannel ticketChannel = candidateTicketChannels.get(0);

        for (int i = 1; i < candidateTicketChannels.size(); i++)
            if (candidateTicketChannels.get(i).getTimeCreated().isAfter(ticketChannel.getTimeCreated()))
                ticketChannel = candidateTicketChannels.get(i);



        sendTicketMessage(ticketChannel, ticketMaker);

    }

    // returns {AuthorURL, TicketName}
    private String[] parseLoggingEmbed(MessageEmbed message) throws IOException {

        List<MessageEmbed.Field> fields = message.getFields();

        if (fields.size() != 2) {
            throw new IOException("Ticket Tool Format not parsed correctly - no# of Fields != 2");
        }

        MessageEmbed.Field infoField = fields.get(0);
        MessageEmbed.Field panelField = fields.get(1);
        if (fields.get(0).getName().equals("Panel")) {
            infoField = fields.get(1);
            panelField = fields.get(0);
        }
        if (!panelField.getValue().equals("Team Application"))
            throw new IOException("Ticket Tool Format not parsed correctly - Panel was not through Team Application");


        String userName = message.getAuthor().getName();
        String[] ticketInfo = infoField.getValue().split("\n");

        String ticketName = ticketInfo[0].split(": ")[1];
        if (!ticketInfo[1].split(": ")[1].equals("Created"))
            throw new IOException("Ticket Tool Format not parsed correctly - Ticket Action was not of type Created");

        return new String[]{userName, ticketName};
    }

    private void sendTicketMessage(TextChannel ticketChannel, String user) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Hello " + user + "!");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.setDescription("This is your recruitment form for Devil Dogs!\nTo get started, please answer the questions in [Our Recruitment Google Form]("+Config.APPLICATION_RECRUIT_GOOGLE_FORM_URL+")");

        embedBuilder.addField("What do I Do?", "All you need to do is fill out the google form linked above, which includes several questions about your eligibility to join, and any preferences you have in your gameplay.", false);
        embedBuilder.addField("After Filling in the form?", "After filling in the form, please write in this channel that you have done so, as it may speed up response times.\nOur admins aren't always watching, so if they are not responding, please be patient! \\:D", false);
        embedBuilder.setFooter("-Sgt. Stubby", "https://ddclan.org/DDsmall.png");

        ticketChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
