package me.hasenzahn1.discordmusicbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.hasenzahn1.discordmusicbot.DiscordMusicBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.awt.*;
import java.util.concurrent.*;

public class MusicController {

    private final Guild guild;
    private final AudioPlayer player;
    private MessageChannelUnion channel;
    private final Queue queue;
    private boolean looping;

    private ScheduledExecutorService sheduleService;
    private ScheduledFuture<?> future;

    public MusicController(Guild guild){
        this.guild = guild;
        player = DiscordMusicBot.INSTANCE.getAudioPlayerManager().createPlayer();

        this.guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
        player.addListener(new TrackScheduler());
        player.setVolume(100);

        queue = new Queue(this);
        looping = false;

        sheduleService = Executors.newScheduledThreadPool(1);
    }

    public void setChannel(MessageChannelUnion channel) {
        this.channel = channel;
    }

    public MessageChannelUnion getChannel() {
        return channel;
    }

    public Guild getGuild() {
        return guild;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public Queue getQueue() {
        return queue;
    }

    public void forceSkip() {
        player.stopTrack();
        if(!queue.next()){
            startDisconnectCheck();
        }
    }

    public void startDisconnectCheck(){
        stopDisconnectCheck();

        future = sheduleService.schedule(() -> {
            guild.getAudioManager().closeAudioConnection();

            channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.DARK_GRAY).setDescription("Disconnected due to inactivity").build()).queue();
            future = null;
            looping = false;
        }, 5, TimeUnit.MINUTES);
    }

    public void stopDisconnectCheck(){
        if(future != null){
            future.cancel(true);
            future = null;
        }
    }

    public void disconnect() {
        stopDisconnectCheck();
        queue.clear();
        player.stopTrack();
        guild.getAudioManager().closeAudioConnection();
        looping = false;
    }

    public void setPaused(boolean paused) {
        player.setPaused(paused);
    }

    public boolean isPaused(){
        return player.isPaused();
    }

    public void toggleLooping(){
        looping = !looping;
    }

    public boolean isLooping() {
        return looping;
    }
}
