package me.hasenzahn1.discordmusicbot.music;

import me.hasenzahn1.discordmusicbot.DiscordMusicBot;

import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    public ConcurrentHashMap<Long, MusicController> controllers;

    public PlayerManager() {
        controllers = new ConcurrentHashMap<>();
    }

    public MusicController getController(long guildId){
        if(!controllers.containsKey(guildId)){
            controllers.put(guildId, new MusicController(DiscordMusicBot.INSTANCE.getJda().getGuildById(guildId)));
        }
        return controllers.get(guildId);
    }

    public long getGuildByPlayerHash(int hash){
        for(MusicController c : controllers.values()){
            if(c.getPlayer().hashCode() == hash){
                return c.getGuild().getIdLong();
            }
        }
        return -1;
    }


}
