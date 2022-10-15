package me.hasenzahn1.discordmusicbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.awt.*;

public class AudioLoadResult implements AudioLoadResultHandler {

    private final MusicController controller;
    private final String uri;
    private final MessageChannelUnion channel;

    public AudioLoadResult(MusicController controller, String uri, MessageChannelUnion channel) {
        this.controller = controller;
        this.uri = uri;
        this.channel = channel;
    }

    @Override
    public void trackLoaded(AudioTrack audioTrack) {
        if(controller.getPlayer().getPlayingTrack() != null) channel.sendMessage("Enqueued **" + audioTrack.getInfo().title + "** by **" + audioTrack.getInfo().author + "**").queue();
        controller.getQueue().addTrackToQueue(audioTrack);
        System.out.println("trackLoaded");
    }

    @Override
    public void playlistLoaded(AudioPlaylist audioPlaylist) {
        Queue queue = controller.getQueue();
        System.out.println("Playlist loaded");

        if(uri.startsWith("ytsearch: ")){
            AudioTrack track = audioPlaylist.getTracks().get(0);
            if(controller.getPlayer().getPlayingTrack() != null) channel.sendMessage("Enqueued **" + track.getInfo().title + "** by **" + track.getInfo().author + "**").queue();
            queue.addTrackToQueue(track);
            return;
        }

        int added = 0;
        for(AudioTrack track : audioPlaylist.getTracks()){
            queue.addTrackToQueue(track);
            added++;
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setColor(new Color(80, 135, 208))
                .setTitle("Queue")
                .setDescription("Added " + added + " track to the queue");

        channel.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public void noMatches() {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("An Error Occurred")
                .setDescription("Found no matches for Song with url: `" + uri + "`");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public void loadFailed(FriendlyException e) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("An Error Occurred")
                .setDescription("Could not load song: ```" + e.getLocalizedMessage() + "```");
        channel.sendMessageEmbeds(builder.build()).queue();
    }
}
