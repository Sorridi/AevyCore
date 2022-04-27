package fun.aevy.aevycore.utils.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * The representation of a {@link FileConfiguration} item.
 * @since 1.0
 * @author Sorridi
 */
@Getter
@AllArgsConstructor
public class ConfigEntry
{
    private final String path;
    private final Object value;
}
