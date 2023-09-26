package de.andre.villagerperformancefix;

import de.andre.villagerperformancefix.Commands.TradeMove;
import de.andre.villagerperformancefix.Commands.VillagerMerge;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        registerCommands();
        Util.main = this;
    }

    private void registerCommands(){
        registerCommand("villagermerge", new VillagerMerge());
        registerCommand("trademove", new TradeMove());
    }

    private void registerCommand(String s, Object e){
        PluginCommand command = getCommand(s);

        if(e instanceof CommandExecutor)command.setExecutor((CommandExecutor) e);

        if (e instanceof TabCompleter)command.setTabCompleter((TabCompleter) e);
    }

    @Override
    public void onDisable() {
    }
}
