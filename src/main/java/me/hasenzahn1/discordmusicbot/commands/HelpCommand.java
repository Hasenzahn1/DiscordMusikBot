package me.hasenzahn1.discordmusicbot.commands;

import me.hasenzahn1.discordmusicbot.DiscordMusicBot;
import me.hasenzahn1.discordmusicbot.commandsystem.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "Display a list of all available commands", false);
    }

    @Override
    public void onExecute(Member author, MessageChannelUnion channel, Message message, String[] args) {
        channel.sendMessage(getCommandHelpList()).queue();
    }

    @Override
    public void onExecuteSlash(SlashCommandInteractionEvent event, Member author, MessageChannelUnion channel, long messageIdLong, List<OptionMapping> options) {
        event.reply(getCommandHelpList()).queue();
    }

    private String getCommandHelpList(){
        StringBuilder sb = new StringBuilder("```");
        for(Command c : DiscordMusicBot.INSTANCE.getCommandManager().getCommands().values().stream().sorted(Comparator.comparing(Command::getName)).collect(Collectors.toList())){
            sb.append(c.getName()).append(" ".repeat(10 - c.getName().length())).append(c.getDescription()).append("\n");
        }
        sb.append("```");

        return sb.toString();
    }

    @Override
    public CommandData getSlashCommand() {
        return Commands.slash(name, description);
    }
}
