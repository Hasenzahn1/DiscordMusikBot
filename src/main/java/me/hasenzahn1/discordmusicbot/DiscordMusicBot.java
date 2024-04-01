package me.hasenzahn1.discordmusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import me.hasenzahn1.discordmusicbot.commands.HelpCommand;
import me.hasenzahn1.discordmusicbot.commands.PingCommand;
import me.hasenzahn1.discordmusicbot.commands.music.*;
import me.hasenzahn1.discordmusicbot.commandsystem.CommandManager;
import me.hasenzahn1.discordmusicbot.music.PlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class DiscordMusicBot {

    public static DiscordMusicBot INSTANCE;

    //CREDS
    private String[] credentials;

    private final JDA jda;
    private final String prefix;
    private final CommandManager commandManager;

    //Music
    private final AudioPlayerManager audioPlayerManager;
    private final PlayerManager playerManager;

    private SpotifyApi spotifyApi;

    public DiscordMusicBot(){
        INSTANCE = this;
        prefix = "!";

        //Credentials
        getCredentials();

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
        JDABuilder jdaBuilder = JDABuilder.createDefault(credentials[0]);
        jdaBuilder.setActivity(Activity.listening("/help"));
        jdaBuilder.addEventListeners(commandManager);
        jdaBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES);
        jda = jdaBuilder.build();

        //AudioManager | PlayerManager
        audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
        playerManager = new PlayerManager();

        //Spotify Api
        initializeSpotifyAPI();
        //refreshCredentials();

        checkForShutDown();
    }



    private void getCredentials(){
        BufferedReader is = new BufferedReader(new InputStreamReader(DiscordMusicBot.class.getClassLoader().getResourceAsStream("token.txt")));
        Object[] creds = is.lines()
                .map(String::strip)
                .toArray();
        credentials = Arrays.asList(creds).toArray(new String[creds.length]);
    }

    public void refreshCredentials(){
        ClientCredentialsRequest.Builder builder = new ClientCredentialsRequest.Builder(spotifyApi.getClientId(), spotifyApi.getClientSecret());
        try {
            ClientCredentials creds = builder.grant_type("client_credentials").build().execute();
            spotifyApi.setAccessToken(creds.getAccessToken());
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            e.printStackTrace();
        }
    }

    private void initializeSpotifyAPI(){
        spotifyApi = new SpotifyApi.Builder().setClientId(credentials[1]).setClientSecret(credentials[2]).build();
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

    public SpotifyApi getSpotifyApi() {
        return spotifyApi;
    }
}
