package me.hasenzahn1.discordmusicbot.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Queue {

    private List<AudioTrack> queuelist;
    private MusicController controller;

    public Queue(MusicController controller) {
        this.controller = controller;
        queuelist = new ArrayList<>();
    }

    public boolean next(){
        if(queuelist.size() >= 1){
            AudioTrack track = queuelist.remove(0);

            if(track != null){
                controller.getPlayer().playTrack(track);
                return true;
            }
        }
        return false;
    }

    public void addTrackToQueue(AudioTrack track){
        queuelist.add(track);

        if(controller.getPlayer().getPlayingTrack() == null){
            next();
        }
    }

    public void shuffle(){
        Collections.shuffle(queuelist);
    }

    public List<AudioTrack> getQueuelist() {
        return queuelist;
    }

    public MusicController getController() {
        return controller;
    }

    public void clear() {
        queuelist.clear();
    }
}
