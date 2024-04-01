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
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

        if(url.contains("https://open.spotify.com/track")){
            queueSpotifySearch(audioPlayerManager, controller, channel, url);
            return true;
        }

        if(url.contains("https://open.spotify.com/playlist")){
            queueSpotifyPlaylist(audioPlayerManager, controller, channel, url, new int[]{0});
            return true;
        }

        if(url.contains("https://tidal.com/browse/track")){
            queueTidalTrack(audioPlayerManager, controller, channel, url);
        }

        if(!url.startsWith("http")){
            url = "ytsearch: " + url;
        }

        System.out.println(url);

        loadItem(audioPlayerManager, controller, channel, url);
        return true;
    }

    private void queueTidalTrack(AudioPlayerManager audioPlayerManager, MusicController controller, MessageChannelUnion channel, String url) {
    }

    private void queueSpotifyPlaylist(AudioPlayerManager audioPlayerManager, MusicController controller, MessageChannelUnion channel, String url, int[] offset) {
        SpotifyApi api = DiscordMusicBot.INSTANCE.getSpotifyApi();
        String id = url.replace("https://open.spotify.com/playlist/", "").split("\\?si=")[0];

        System.out.println("Queue Playlist");
        GetPlaylistsItemsRequest request = DiscordMusicBot.INSTANCE.getSpotifyApi().getPlaylistsItems(id).offset(offset[0]).build();
        CompletableFuture<Paging<PlaylistTrack>> future = request.executeAsync();
        future.thenAccept(playlist -> {
            for(PlaylistTrack track : playlist.getItems()){
                //System.out.println(((Track) track.getTrack()).getArtists()[0].getName());
                String u = "ytsearch: " + track.getTrack().getName() + " " + ((Track) track.getTrack()).getArtists()[0].getName();
                System.out.println(u);
                audioPlayerManager.loadItem(u, new AudioLoadResult(controller, u, channel, false));
                offset[0] ++;
            }
            if(offset[0] < playlist.getTotal()){
                queueSpotifyPlaylist(audioPlayerManager, controller, channel, url, offset);
            }
        });

        try {
            future.get();
            System.out.println("Request Playlist");
        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (ExecutionException e) {
            DiscordMusicBot.INSTANCE.refreshCredentials();
            queueSpotifyPlaylist(audioPlayerManager, controller, channel, url, new int[]{0});
            System.out.println("Have to refresh credentials");
        }
    }

    private void loadItem(AudioPlayerManager manager, MusicController controller, MessageChannelUnion channel, String url){
        manager.loadItem(url, new AudioLoadResult(controller, url, channel, true));
    }

    private void queueSpotifySearch(AudioPlayerManager manager, MusicController controller, MessageChannelUnion channel, String url){
        SpotifyApi api = DiscordMusicBot.INSTANCE.getSpotifyApi();
        String id = url.replace("https://open.spotify.com/track/", "").split("\\?si=")[0];

        GetTrackRequest request = api.getTrack(id).build();
        CompletableFuture<Track> future = request.executeAsync();
        future.thenAccept(track -> {
            loadItem(manager, controller, channel, "ytsearch: " + track.getName() + " " + track.getArtists()[0].getName());
            System.out.println("ytsearch: " + track.getName() + " " + track.getArtists()[0].getName());
        });

        try {
            future.get();
        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (ExecutionException e) {
            DiscordMusicBot.INSTANCE.refreshCredentials();
            queueSpotifySearch(manager, controller, channel, url);
            System.out.println("Have to refresh credentials");
        }

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
