package net.hypixel.nerdbot.listener;

import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.hypixel.nerdbot.NerdBotApp;
import net.hypixel.nerdbot.channel.ChannelGroup;
import net.hypixel.nerdbot.channel.Reactions;
import net.hypixel.nerdbot.database.Database;

import java.util.List;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() && !event.getAuthor().getId().equals(NerdBotApp.getBot().getJDA().getSelfUser().getId())) {
            return;
        }

        Guild guild = event.getGuild();
        Emote yes = guild.getEmoteById(Reactions.AGREE.getId());
        Emote no = guild.getEmoteById(Reactions.DISAGREE.getId());
        if (yes == null || no == null) return;

        List<ChannelGroup> groups = Database.getInstance().getChannelGroups();

        // TODO make better
        if (groups == null) return;
        if (groups.isEmpty()) return;

        Channel channel = event.getChannel();
        Message message = event.getMessage();

        for (ChannelGroup group : groups) {
            // TODO check if it's a suggestion based channel
            if (!group.getFrom().equals(channel.getId())) continue;

            message.addReaction(yes).queue();
            message.addReaction(no).queue();

            String firstLine = message.getContentRaw().split("\n")[0];

            if (firstLine == null || firstLine.equals("")) {
                if (message.getEmbeds().get(0) != null) {
                    firstLine = message.getEmbeds().get(0).getTitle();
                } else {
                    firstLine = "No Title";
                }
            } else if (firstLine.length() > 30) {
                firstLine = firstLine.substring(0, 30) + "...";
            }

            message.createThreadChannel("Discussion - " + firstLine).queue(threadChannel -> threadChannel.addThreadMember(message.getAuthor()).queue());
        }
    }

}
