package me.hasenzahn1.discordmusicbot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.hasenzahn1.discordmusicbot.DiscordMusicBot;
import me.hasenzahn1.discordmusicbot.commandsystem.Command;
import me.hasenzahn1.discordmusicbot.music.MusicController;
import me.hasenzahn1.discordmusicbot.music.Queue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import javax.tools.Diagnostic;
import java.awt.*;
import java.util.List;

public class QueueCommand extends Command {

    public QueueCommand() {
        super("queue", "Displaay the current queue", false);
    }

    @Override
    public void onExecute(Member author, MessageChannelUnion channel, Message message, String[] args) {
        channel.sendMessageEmbeds(getQueueList(message.getGuild().getIdLong())).queue();
    }

    public MessageEmbed getQueueList(long guildId){
        MusicController controller = DiscordMusicBot.INSTANCE.getPlayerManager().getController(guildId);
        Queue queue = controller.getQueue();
        if(queue.getQueuelist().size() == 0){
            return new EmbedBuilder().setColor(Color.RED).setDescription("Empty Queue").build();
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.DARK_GRAY);
        builder.setTitle(queue.getQueuelist().size() + " tracks:");

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < Math.min(10, queue.getQueuelist().size()); i++){
            AudioTrackInfo track = queue.getQueuelist().get(i).getInfo();
            sb.append("`").append((i + 1)).append(".` ").append("[").append(track.title).append("](").append(track.uri).append(")\n");
        }
        builder.setDescription(sb.toString());
        builder.setFooter("Viewing page 1/" + (int)Math.ceil(queue.getQueuelist().size() / 10f));

        return builder.build();
    }

    @Override
    public void onExecuteSlash(SlashCommandInteractionEvent event, Member author, MessageChannelUnion channel, long messageIdLong, List<OptionMapping> options) {
        event.replyEmbeds(getQueueList(event.getGuild().getIdLong())).queue();
    }

    @Override
    public CommandData getSlashCommand() {
        return Commands.slash(name, description);
    }
}
