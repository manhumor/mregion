package me.manhumor.mregion;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MRegion extends JavaPlugin implements Listener {
    private FileConfiguration config;

    private Set<String> disabledWorlds;
    private Set<String> disabledRegions;
    private String ownersJoining;
    private String actionbarMessage;

    private final StringBuilder regionOwner = new StringBuilder();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        disabledWorlds = new HashSet<>(config.getStringList("disable-worlds"));
        disabledRegions = new HashSet<>(config.getStringList("disable-regions"));

        ownersJoining = ColorParser.parseString(config.getString("owners-joining"));
        actionbarMessage = ColorParser.parseString(config.getString("actionbar-message"));

        getLogger().info("§c. . . . . . . . . . . .");
        getLogger().info("§c| §fPlugin §cM§fRegion");
        getLogger().info("§c| §f- §cSuccessful §floaded");
        getLogger().info("§c| §f- §cI wish you §fluck!!!");
        getLogger().info("§c˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙");

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::run, 5L, (long)config.getInt("update-time"));
    }

    @Override
    public void onDisable() {
        getLogger().info("§c. . . . . . . . . . . .");
        getLogger().info("§c| §fPlugin §cM§fRegion");
        getLogger().info("§c| §f- §cSuccessful §funloaded");
        getLogger().info("§c| §f- §cI wish you §fluck!!!");
        getLogger().info("§c˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙");
    }

    private void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location location = player.getLocation();
            if (player.hasPermission("mregion.see")) {
                if (disabledWorlds.contains(location.getWorld().getName())) return;

                ApplicableRegionSet regionSet = WorldGuard.getInstance().getPlatform().getRegionContainer()
                        .get(BukkitAdapter.adapt(location.getWorld())).getApplicableRegions(BukkitAdapter.asBlockVector(location));

                for (ProtectedRegion region : regionSet) {
                    if (disabledRegions.contains(region.getId())) return;

                    Set<UUID> ownerUUIDs = region.getOwners().getUniqueIds();

                    if (ownerUUIDs.isEmpty()) return;

                    regionOwner.setLength(0);
                    for (UUID uuid : ownerUUIDs) {
                        if (regionOwner.length() > 0) {
                            regionOwner.append(ColorParser.parseString(ownersJoining));
                        }
                        regionOwner.append(Bukkit.getServer().getOfflinePlayer(uuid).getName());
                    }

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent
                            .fromLegacyText(ColorParser.parseString(actionbarMessage
                                    .replaceAll("%region-owner", ColorParser.parseString(regionOwner.toString()))
                                    .replaceAll("%region-name", ColorParser.parseString(region.getId()))
                            )));
                }
            }
        }
    }
}
