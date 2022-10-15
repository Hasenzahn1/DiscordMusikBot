package me.hasenzahn1.discordmusicbot.commands;

import me.hasenzahn1.discordmusicbot.commandsystem.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.List;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", "Get the ping of the bot", false);
    }

    @Override
    public void onExecute(Member author, MessageChannelUnion channel, Message message, String[] args) {
        long time = System.currentTimeMillis();
        channel.sendMessage("Pong").queue(c ->
                c.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue());
    }

    @Override
    public void onExecuteSlash(SlashCommandInteractionEvent event, Member author, MessageChannelUnion channel, long messageIdLong, List<OptionMapping> options) {
        long time = System.currentTimeMillis();
        event.deferReply(true)
                .flatMap(v -> event.getHook().editOriginalFormat("Pong %d ms", System.currentTimeMillis() - time)).queue();
    }

    @Override
    public CommandData getSlashCommand() {
        return Commands.slash(name, description);
    }
}
