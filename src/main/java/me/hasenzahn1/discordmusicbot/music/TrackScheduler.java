package me.hasenzahn1.discordmusicbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.hasenzahn1.discordmusicbot.DiscordMusicBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class TrackScheduler extends AudioEventAdapter {

    @Override
    public void onPlayerPause(AudioPlayer player) {
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        long guildId = DiscordMusicBot.INSTANCE.getPlayerManager().getGuildByPlayerHash(player.hashCode());
        MusicController controller = DiscordMusicBot.INSTANCE.getPlayerManager().getController(guildId);
        controller.stopDisconnectCheck();

        AudioTrackInfo info = track.getInfo();
        long sec = info.length / 1000;
        long min = sec / 60;
        long hour = min / 60;
        min %= 60;
        hour %= 60;
        sec %= 60;

        String durationString = "";
        if(hour != 0) durationString += hour + " hours, ";
        if(min != 0) durationString += min + " minutes, ";
        if(sec != 0) durationString += sec + " seconds, ";
        durationString = durationString.substring(0, durationString.length() - 2);

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Now playing" + (controller.isLooping() ? " (Looping)" : ""))
                .setColor(new Color(80, 135, 208))
                .setDescription("```" + track.getInfo().title + "```")
                .addField("Duration", durationString, true)
                .addField("Uploader", "[" + info.author + "](https://www.youtube.com/c/" + info.author + ")", true)
                .addField("URL", "[Click](" + info.uri + ")", false)
                ;

        String url = info.uri;
        if(url.startsWith("https://www.youtube.com/watch?v=")){
            InputStream file;
            String videoId = url.replace("https://www.youtube.com/watch?v=", "");

            try{
                file = new URL("https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg").openStream();
                embedBuilder.setThumbnail("attachment://thumbnail.png");

                DiscordMusicBot.INSTANCE.getPlayerManager().getController(guildId).getChannel().sendFiles(FileUpload.fromData(file, "thumbnail.png")).addEmbeds(embedBuilder.build()).queue();
            } catch (IOException e) {
                e.printStackTrace();
                DiscordMusicBot.INSTANCE.getPlayerManager().getController(guildId).getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
            }

        }else{
            DiscordMusicBot.INSTANCE.getPlayerManager().getController(guildId).getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
        }

    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {

        System.out.println("Track end " + endReason);
        long guildId = DiscordMusicBot.INSTANCE.getPlayerManager().getGuildByPlayerHash(player.hashCode());
        MusicController controller = DiscordMusicBot.INSTANCE.getPlayerManager().getController(guildId);

        if(endReason == AudioTrackEndReason.LOAD_FAILED){
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("An Error Occurred")
                    .setDescription("Could not load song " + track.getInfo().title + "\nPlease try again");

            controller.getChannel().sendMessageEmbeds(builder.build()).queue();
        }

        if(endReason.mayStartNext && !controller.isLooping()){
            Queue queue = controller.getQueue();

            if(queue.next()){
                return;
            }

            player.stopTrack();
            controller.startDisconnectCheck();
        }else if(endReason.mayStartNext){
            track.setPosition(0);
            player.playTrack(track.makeClone());
        }
    }
}
