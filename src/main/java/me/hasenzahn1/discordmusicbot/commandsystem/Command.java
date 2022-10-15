package me.hasenzahn1.discordmusicbot.commandsystem;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;
import java.util.List;

public abstract class Command {

    protected String name, description;
    private boolean canExecutePrivate;

    public Command(String name, String description, boolean canExecutePrivate) {
        this.name = name;
        this.description = description;
        this.canExecutePrivate = canExecutePrivate;
    }

    public abstract void onExecute(Member author, MessageChannelUnion channel, Message message, String[] args);
    public abstract void onExecuteSlash(SlashCommandInteractionEvent event, Member author, MessageChannelUnion channel, long messageIdLong, List<OptionMapping> options);
    public abstract CommandData getSlashCommand();


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean canExecutePrivate() {
        return canExecutePrivate;
    }

    public MessageEmbed incorrectArguments(String correct){
        return new EmbedBuilder().setColor(Color.RED).setTitle("Incorrect Argument").setFooter("Please use: " + correct).build();
    }

    public MessageEmbed errorEmbed(String description){
        return new EmbedBuilder().setColor(Color.RED).setDescription(description).build();
    }
}
