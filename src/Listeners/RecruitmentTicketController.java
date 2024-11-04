package Listeners;

import Classes.Config;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageEmbedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.List;

public class RecruitmentTicketController extends ListenerAdapter {

    @Override
    public void onMessageEmbed(MessageEmbedEvent event) {

        // if message is not in the certain logging channel, return
        if (!event.getChannel().getId().equals(Config.APPLICATION_TICKET_LOGGING_CHANNEL))
            return;

        String ticketMaker = "unknown";
        String ticketName = "unknown-ticket";

        try {
            String[] ret = this.parseLoggingEmbed(event.getMessageEmbeds().get(0));
            ticketMaker = ret[0];
            ticketName = ret[1];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



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


        String userLink = message.getAuthor().getUrl();
        String[] ticketInfo = infoField.getValue().split("\n");

        String ticketName = ticketInfo[0].split(": ")[1];
        if (!ticketInfo[1].split(": ")[1].equals("Created"))
            throw new IOException("Ticket Tool Format not parsed correctly - Ticket Action was not of type Created");

        return new String[]{userLink, ticketName};
    }
}
