package me.hasenzahn1.discordmusicbot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import me.hasenzahn1.discordmusicbot.DiscordMusicBot;
import me.hasenzahn1.discordmusicbot.commandsystem.Command;
import me.hasenzahn1.discordmusicbot.music.AudioLoadResult;
import me.hasenzahn1.discordmusicbot.music.MusicController;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.List;

public class PlayCommand extends Command {

    public PlayCommand() {
        super("play", "Play a song", false);
    }

    @Override
    public void onExecute(Member author, MessageChannelUnion channel, Message message, String[] args) {
        if(args.length == 0){
            channel.sendMessageEmbeds(incorrectArguments("!play <song>")).queue();
            return;
        }

        boolean success = playSong(author, channel, args);
        if(!success){
            channel.sendMessageEmbeds(errorEmbed("You are not connected to a voice channel")).queue();
        }
    }

    private boolean playSong(Member author, MessageChannelUnion channel, String[] args){
        GuildVoiceState state = author.getVoiceState();
        if(state == null){
            return false;
        }
        VoiceChannel vc = (VoiceChannel) state.getChannel();
        if(vc == null){
            return false;
        }
        MusicController controller = DiscordMusicBot.INSTANCE.getPlayerManager().getController(vc.getGuild().getIdLong());
        controller.setChannel(channel);
        AudioPlayerManager audioPlayerManager = DiscordMusicBot.INSTANCE.getAudioPlayerManager();
        AudioManager audioManager = vc.getGuild().getAudioManager();
        audioManager.openAudioConnection(vc);

        StringBuilder sb = new StringBuilder();
        for(String s : args) {
            sb.append(s).append(" ");
        }
        String url = sb.toString().trim();
        if(!url.startsWith("http")){
            url = "ytsearch: " + url;
        }
        System.out.println(url);

        audioPlayerManager.loadItem(url, new AudioLoadResult(controller, url, channel));
        return true;
    }

    @Override
    public void onExecuteSlash(SlashCommandInteractionEvent event, Member author, MessageChannelUnion channel, long messageIdLong, List<OptionMapping> options) {
        String songArg = options.get(0).getAsString();
        String[] args = songArg.strip().split(" ");
        if(args.length == 0 || songArg.strip().length() == 0){
            channel.sendMessageEmbeds(incorrectArguments("/play <song>")).queue();
            return;
        }

        boolean success = playSong(author, channel, args);
        if(!success){
            event.replyEmbeds(errorEmbed("You are not connected to a voice channel")).queue();
        }else{
            event.reply("Enqueued song with args **" + songArg.strip() + "**").queue();
        }
    }

    @Override
    public CommandData getSlashCommand() {
        return Commands.slash(name, description).addOption(OptionType.STRING, "song", "Song to play", true);
    }
}
