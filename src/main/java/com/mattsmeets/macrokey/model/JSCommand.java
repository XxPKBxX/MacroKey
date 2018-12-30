package com.mattsmeets.macrokey.model;

import com.mattsmeets.macrokey.api.ChatAPI;
import com.mattsmeets.macrokey.api.PlayerAPI;
import com.sun.javafx.application.PlatformImpl;
import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.Arrays;

public class JSCommand extends AbstractCommand implements CommandInterface {

    /**
     * File to parse
     */
    private final String command;

    private Invocable inv;

    public JSCommand(String command) {
        super("javascript");

        this.command = command;

        setup();
    }

    @Override
    public void execute(EntityPlayerSP player) {
        try {
            inv.invokeFunction("main");
        } catch (ScriptException | NoSuchMethodException | NullPointerException e) {
            e.printStackTrace();

            ICommandSender sender = player.getCommandSenderEntity();
            if(sender != null) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "[MacroKey] Something went wrong while parsing command"));
                sender.sendMessage(
                        new TextComponentString(
                                e.getMessage() + ""
                        )
                );
            }
        }
    }

    @Override
    public void setup() {
        String[] allowedFiles = {"javafx.application.Platform","java.util.Timer"};
        ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine((ClassFilter) s -> {
            return Arrays.stream(allowedFiles).filter(i -> i.equalsIgnoreCase(s)).count() >= 1;
        });

        engine.put("Player", new PlayerAPI());
        engine.put("Chat", new ChatAPI());

        try {
            PlatformImpl.startup(() -> {});
            engine.eval(new FileReader(new File(command)));


            inv = (Invocable) engine;
        } catch (ScriptException | FileNotFoundException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return command;
    }
}
