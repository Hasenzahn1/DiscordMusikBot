package me.hasenzahn1.discordmusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import me.hasenzahn1.discordmusicbot.commands.HelpCommand;
import me.hasenzahn1.discordmusicbot.commands.PingCommand;
import me.hasenzahn1.discordmusicbot.commands.music.*;
import me.hasenzahn1.discordmusicbot.commandsystem.CommandManager;
import me.hasenzahn1.discordmusicbot.music.PlayerManager;
import me.hasenzahn1.discordmusicbot.music.Queue;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DiscordMusicBot {

    public static DiscordMusicBot INSTANCE;

    private final JDA jda;
    private final String prefix;
    private final CommandManager commandManager;

    //Music
    private final AudioPlayerManager audioPlayerManager;
    private final PlayerManager playerManager;

    public DiscordMusicBot(){
        INSTANCE = this;
        prefix = "!";

        //Commands
        commandManager = new CommandManager(prefix);
        commandManager.addCommand(new PingCommand());
        commandManager.addCommand(new PlayCommand());
        commandManager.addCommand(new StopCommand());
        commandManager.addCommand(new HelpCommand());
        commandManager.addCommand(new ForceSkipCommand());
        commandManager.addCommand(new ShuffleCommand());
        commandManager.addCommand(new QueueCommand());
        commandManager.addCommand(new DisconnectCommand());
        commandManager.addCommand(new PauseCommand());
        commandManager.addCommand(new ResumeCommand());
        commandManager.addCommand(new LoopCommand());

        //Bot setup
        JDABuilder jdaBuilder = JDABuilder.createDefault("ODkxNjIxOTczNDg5MjI5ODk0.YVBBrA.c3UDjrXFpRogz2a96xcwvyA3Awg");
        jdaBuilder.setActivity(Activity.listening("/help"));
        jdaBuilder.addEventListeners(commandManager);
        jdaBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES);
        jda = jdaBuilder.build();


        //AudioManager | PlayerManager
        audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
        playerManager = new PlayerManager();

        checkForShutDown();
    }

    public void shutDown(){

        jda.getPresence().setStatus(OnlineStatus.OFFLINE);
        jda.shutdown();

    }

    private void checkForShutDown(){
        new Thread(() -> {

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            try{
                while((line = reader.readLine()) != null){
                    if(line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")){
                        shutDown();
                        return;
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }).start();
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    public JDA getJda() {
        return jda;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
