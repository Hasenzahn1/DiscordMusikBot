package me.hasenzahn1.discordmusicbot.commandsystem;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandManager extends ListenerAdapter {

    private HashMap<String, Command> commands;
    private String prefix;

    public CommandManager(String prefix){
        this.prefix = prefix;
        commands = new HashMap<>();
    }

    public void addCommand(Command command){
        if(commands.containsKey(command.getName())){
            System.out.println("Could not register command " + command.getName() + " as it is already registered");
            return;
        }
        commands.put(command.getName(), command);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        for(Map.Entry<String, Command> c : commands.entrySet()){
            if(event.getName().equalsIgnoreCase(c.getKey())){

                c.getValue().onExecuteSlash(event, event.getMember(), event.getChannel(), event.getIdLong(), event.getOptions());
            }
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        event.getJDA().updateCommands().addCommands(commands.values().stream().map(Command::getSlashCommand).collect(Collectors.toList())).queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        //We don't want bot spam
        if(event.getMessage().getAuthor().isBot()){
            return;
        }

        //Check if message startswith prefix
        String message = event.getMessage().getContentDisplay();
        if(!message.startsWith(prefix)){
            return;
        }

        //Check if message is command
        message = message.substring(prefix.length());
        for(Map.Entry<String, Command> c : commands.entrySet()){
            if(message.startsWith(c.getKey())){
                //Command can't be executed in private channel
                if(event.isFromType(ChannelType.PRIVATE) && !c.getValue().canExecutePrivate()){
                    event.getChannel().sendMessage("Command **" + c.getKey() + "** can't be executed in a private channel").queue();
                    return;
                }

                //Execute command
                String[] args;
                if(message.substring(c.getKey().length()).strip().length() == 0){
                    args = new String[0];
                }else {
                    args = message.substring(c.getKey().length()).strip().split(" ");
                }
                c.getValue().onExecute(event.getMember(), event.getChannel(), event.getMessage(), args);
                return;
            }
        }
    }

    public HashMap<String, Command> getCommands() {
        return commands;
    }
}
