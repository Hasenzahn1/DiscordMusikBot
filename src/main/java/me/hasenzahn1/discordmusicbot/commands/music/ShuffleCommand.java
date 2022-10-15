package me.hasenzahn1.discordmusicbot.commands.music;

import me.hasenzahn1.discordmusicbot.DiscordMusicBot;
import me.hasenzahn1.discordmusicbot.commandsystem.Command;
import me.hasenzahn1.discordmusicbot.music.MusicController;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.List;

public class ShuffleCommand extends Command {

    public ShuffleCommand() {
        super("shuffle", "Shuffle the queue", false);
    }

    @Override
    public void onExecute(Member author, MessageChannelUnion channel, Message message, String[] args) {
        boolean success = shuffle(author);

        if(!success){
            channel.sendMessageEmbeds(errorEmbed("You are not connected to a voice channel")).queue();
        }else{
            message.addReaction(Emoji.fromUnicode("U+1F44D")).queue();
        }
    }

    public boolean shuffle(Member author){
        GuildVoiceState state = author.getVoiceState();
        if(state == null){
            return false;
        }
        VoiceChannel vc = (VoiceChannel) state.getChannel();
        if(vc == null){
            return false;
        }

        MusicController controller = DiscordMusicBot.INSTANCE.getPlayerManager().getController(vc.getGuild().getIdLong());
        controller.getQueue().shuffle();
        return true;
    }

    @Override
    public void onExecuteSlash(SlashCommandInteractionEvent event, Member author, MessageChannelUnion channel, long messageIdLong, List<OptionMapping> options) {
        boolean success = shuffle(author);

        if(!success){
            event.replyEmbeds(errorEmbed("You are not connected to a voice channel")).queue();
        }else{
            event.reply(Emoji.fromUnicode("U+1F44D").getFormatted()).queue();
        }
    }

    @Override
    public CommandData getSlashCommand() {
        return Commands.slash(name, description);
    }
}
