package fun.aevy.aevycore.utils.builders;

import fun.aevy.aevycore.AevyCore;
import fun.aevy.aevycore.struct.elements.Reloadable;
import fun.aevy.aevycore.utils.configuration.entries.GlobalEntries;
import fun.aevy.aevycore.utils.formatting.Send;
import lombok.Getter;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates and registers new {@link Command}s.
 * @since 1.0
 * @author Sorridi, Niketion
 */
@SuppressWarnings("unused")
@Getter
public abstract class CommandsBuilder extends Reloadable implements CommandExecutor, TabCompleter
{
    private final boolean       onlyPlayer, onlyConsole, tabComplete;
    private final List<String>  usage;
    private String              permission;
    private final JavaPlugin    plugin;

    protected final AevyCore    aevyCore;
    protected final Send        send;

    /**
     * Constructor for new Commands, which they get automatically registered.
     *
     * @param plugin        The plugin that is using AevyCore as library.
     * @param send          Instance of Send.
     * @param permission    Permission required to run the command.
     * @param command       The command itself.
     * @param onlyPlayer    Makes the command runnable only to players.
     * @param onlyConsole   Makes the command runnable only to players.
     * @param usage         How the command should be run.
     * @param tabComplete   Enables tab complete.
     */
    public CommandsBuilder(
            @NotNull    JavaPlugin      plugin,
            @NotNull    Send            send,
            @Nullable   String          permission,
            @NotNull    String          command,
                        boolean         onlyPlayer,
                        boolean         onlyConsole,
            @Nullable   List<String>    usage,
                        boolean         tabComplete
    ) {
        this.aevyCore       = AevyCore.getInstance();
        this.config         = aevyCore.getConfiguration();

        this.plugin         = plugin;
        this.send           = send;

        this.onlyPlayer     = onlyPlayer;
        this.onlyConsole    = onlyConsole;
        this.usage          = usage;
        this.tabComplete    = tabComplete;

        if (permission != null)
            this.permission = permission;

        plugin.getCommand(command).setExecutor(this);
        plugin.getCommand(command).setTabCompleter(this);
    }

    public abstract boolean command(CommandSender sender, String[] args);

    public abstract List<String> tabComplete(String[] args);

    @Override
    public boolean onCommand(
            @NotNull    CommandSender sender,
            @NotNull    Command command,
            @NotNull    String label,
                        String[] args
    ) {
        if (onlyConsole && sender instanceof Player)
        {
            send.message(sender, GlobalEntries.NO_PLAYER);
            return false;
        }

        if (onlyPlayer && sender instanceof ConsoleCommandSender)
        {
            send.message(sender, GlobalEntries.NO_CONSOLE);
            return false;
        }

        if (permission != null && !sender.hasPermission(permission))
        {
            send.message(sender, GlobalEntries.NO_PERMS);
            return false;
        }

        if (!command(sender, args))
        {
            if (usage == null)
                return false;

            // TODO aggiungere usage configurabile da file
            //Send.message(sender, usage);
            return false;
        }

        return true;
    }

    /**
     * Returns all the players names whose name starts with the argument.
     * @param args Command arguments.
     * @return List of players names.
     */
    public List<String> getDefaultTabList(String[] args)
    {
        List<String> names = new ArrayList<>();

        Collection<? extends Player> players = aevyCore.getServer().getOnlinePlayers();

        int length = args.length;
        String argString = args[--length];

        if (argString.isEmpty())
            players.forEach(player -> names.add(player.getName()));
        else
            players
                    .stream()
                    .filter(player -> player.getName().startsWith(argString))
                    .forEach(player -> names.add(player.getName()));

        return names;
    }

    /**
     * Returns all the command arguments available that starts with the argument.
     * @param args Command arguments.
     * @return List of arguments.
     */
    public List<String> getArgsTabList(String[] args)
    {
        List<String> list = new ArrayList<>();

        int length = args.length;
        String argString = args[--length];

        if (argString.isEmpty())
            return Arrays.asList(args);

        Arrays.stream(args)
                .filter(s -> s.startsWith(argString))
                .forEach(s -> list.add(argString));

        return list;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull    CommandSender sender,
            @NotNull    Command command,
            @NotNull    String alias,
                        String[] args
    ) {
        if (tabComplete && args.length == 1)
        {
            return  tabComplete(args)
                    .stream()
                    .filter(a -> a.startsWith(args[0]))
                    .collect(Collectors.toList());
        }

        return getDefaultTabList(args);
    }

}