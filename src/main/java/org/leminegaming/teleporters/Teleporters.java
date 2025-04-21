package org.leminegaming.teleporters;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.Particle;
import org.bukkit.Sound;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Teleporters extends JavaPlugin implements Listener {

    private Location teleport1;
    private Location teleport2;
    private final HashMap<Location, TeleportInfo> teleportPoints = new HashMap<>();
    private File teleportsFile;
    private FileConfiguration teleportsConfig;
    private final HashMap<UUID, Long> teleportCooldowns = new HashMap<>();  // Cooldown storage
    private static final long TELEPORT_COOLDOWN = 5000;
    private final Set<UUID> recentlyTeleportedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadTeleportsFile();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("teleport1").setExecutor(new TeleportCommand());
        getCommand("teleport2").setExecutor(new TeleportCommand());
        getCommand("setupteleporter").setExecutor(new TeleportCommand());

        // Schedule repeating task for particle effects
        new BukkitRunnable() {
            @Override
            public void run() {
                showParticles();
            }
        }.runTaskTimer(this, 0L, 20L); // Run every second (20 ticks)

        getLogger().info("TeleportPlugin enabled.");
    }

    @Override
    public void onDisable() {
        saveTeleportsToFile();
        getLogger().info("TeleportPlugin disabled.");
    }

    private void loadTeleportsFile() {
        teleportPoints.clear();  // Clear existing teleports in case of reload
        teleportsFile = new File(getDataFolder(), "teleports.yml");
        if (!teleportsFile.exists()) {
            teleportsFile.getParentFile().mkdirs();
            saveResource("teleports.yml", false);
        }

        teleportsConfig = YamlConfiguration.loadConfiguration(teleportsFile);
        for (String key : teleportsConfig.getKeys(false)) {
            Location emeraldLocation1 = new Location(
                    Bukkit.getWorld(teleportsConfig.getString(key + ".world")),
                    teleportsConfig.getDouble(key + ".loc1X"),
                    teleportsConfig.getDouble(key + ".loc1Y"),
                    teleportsConfig.getDouble(key + ".loc1Z")
            );
            Location emeraldLocation2 = new Location(
                    Bukkit.getWorld(teleportsConfig.getString(key + ".world")),
                    teleportsConfig.getDouble(key + ".loc2X"),
                    teleportsConfig.getDouble(key + ".loc2Y"),
                    teleportsConfig.getDouble(key + ".loc2Z")
            );
            UUID owner = UUID.fromString(teleportsConfig.getString(key + ".owner"));
            boolean isPrivate = teleportsConfig.getBoolean(key + ".isPrivate");

            // Save both directions in teleportPoints for two-way teleport
            teleportPoints.put(emeraldLocation1, new TeleportInfo(emeraldLocation2, owner, isPrivate));
            teleportPoints.put(emeraldLocation2, new TeleportInfo(emeraldLocation1, owner, isPrivate));
        }
    }

    private void showParticles() {
        for (Location loc : teleportPoints.keySet()) {
            if (isPlayerNearby(loc, 10)) { // Check if any player is within 10 blocks
                Location destination = teleportPoints.get(loc).destination;
                Block blockAbove1 = destination.clone().add(0, 1, 0).getBlock();
                Block blockAbove2 = destination.clone().add(0, 2, 0).getBlock();

                boolean isBlocked = (blockAbove1.getType() != Material.AIR && !isSign(blockAbove1)) ||
                        (blockAbove2.getType() != Material.AIR && !isSign(blockAbove2));

                if (isBlocked) {
                    loc.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0.5, 1.5, 0.5), 5, new Particle.DustOptions(org.bukkit.Color.RED, 1));
                } else {
                    loc.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0.5, 1.5, 0.5), 5, 0.2, 0.2, 0.2, 0.01);
                }
            }
        }
    }



    private boolean isSign(Block block) {
        Material type = block.getType();
        return type == Material.OAK_SIGN || type == Material.OAK_WALL_SIGN ||
                type == Material.SPRUCE_SIGN || type == Material.SPRUCE_WALL_SIGN ||
                type == Material.BIRCH_SIGN || type == Material.BIRCH_WALL_SIGN ||
                type == Material.JUNGLE_SIGN || type == Material.JUNGLE_WALL_SIGN ||
                type == Material.ACACIA_SIGN || type == Material.ACACIA_WALL_SIGN ||
                type == Material.DARK_OAK_SIGN || type == Material.DARK_OAK_WALL_SIGN ||
                type == Material.CRIMSON_SIGN || type == Material.CRIMSON_WALL_SIGN ||
                type == Material.CHERRY_SIGN || type == Material.CHERRY_WALL_SIGN ||
                type == Material.LEVER || type == Material.STONE_BUTTON ||
                type == Material.WARPED_SIGN || type == Material.WARPED_WALL_SIGN;
    }

    private boolean isPlayerNearby(Location location, double radius) {
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= radius) {
                return true;
            }
        }
        return false;
    }

    private void saveTeleportsToFile() {
        teleportsConfig = new YamlConfiguration();

        // Save each teleporter pair uniquely by their linked locations
        int index = 0;
        for (Location loc1 : teleportPoints.keySet()) {
            TeleportInfo info = teleportPoints.get(loc1);

            if (teleportPoints.containsKey(info.destination)) {  // Only save one direction of each pair
                String key = "teleporter" + index++;
                teleportsConfig.set(key + ".world", loc1.getWorld().getName());
                teleportsConfig.set(key + ".loc1X", loc1.getX());
                teleportsConfig.set(key + ".loc1Y", loc1.getY());
                teleportsConfig.set(key + ".loc1Z", loc1.getZ());
                teleportsConfig.set(key + ".loc2X", info.destination.getX());
                teleportsConfig.set(key + ".loc2Y", info.destination.getY());
                teleportsConfig.set(key + ".loc2Z", info.destination.getZ());
                teleportsConfig.set(key + ".owner", info.owner.toString());
                teleportsConfig.set(key + ".isPrivate", info.isPrivate);
            }
        }

        try {
            teleportsConfig.save(teleportsFile);
        } catch (IOException e) {
            getLogger().severe("Could not save teleports.yml!");
            e.printStackTrace();
        }
    }

    public class TeleportInfo {
        public Location destination;
        public UUID owner;
        public boolean isPrivate;

        public TeleportInfo(Location destination, UUID owner, boolean isPrivate) {
            this.destination = destination;
            this.owner = owner;
            this.isPrivate = isPrivate;
        }
    }

    public class TeleportCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED +"[Teleporter] This command can only be used by players.");
                return true;
            }

            Player player = (Player) sender;

            if (label.equalsIgnoreCase("teleport1")) {
                Block block = player.getLocation().getBlock().getRelative(0, -1, 0);
                if (block.getType() == Material.EMERALD_BLOCK) {
                    teleport1 = block.getLocation();
                    player.sendMessage(ChatColor.GREEN +"[Teleporter] First teleport location set.");
                    getLogger().info(player.getName() + " set the first teleporter location.");
                } else {
                    player.sendMessage(ChatColor.RED +"[Teleporter] You must be standing on an emerald block to set the first teleport location.");
                }
                return true;
            }

            if (label.equalsIgnoreCase("teleport2")) {
                Block block = player.getLocation().getBlock().getRelative(0, -1, 0);
                if (block.getType() == Material.EMERALD_BLOCK) {
                    teleport2 = block.getLocation();
                    player.sendMessage(ChatColor.GREEN +"[Teleporter] Second teleport location set.");
                    getLogger().info(player.getName() + " set the second teleporter location.");
                } else {
                    player.sendMessage(ChatColor.RED +"[Teleporter] You must be standing on an emerald block to set the second teleport location.");
                }
                return true;
            }

            if (label.equalsIgnoreCase("setupteleporter")) {
                if (teleport1 == null || teleport2 == null) {
                    player.sendMessage(ChatColor.RED +"[Teleporter] You need to set both teleport locations using /teleport1 and /teleport2.");
                    return true;
                }

                boolean isPrivate = args.length > 0 && Boolean.parseBoolean(args[0]);

                teleportPoints.put(teleport1, new TeleportInfo(teleport2, player.getUniqueId(), isPrivate));
                teleportPoints.put(teleport2, new TeleportInfo(teleport1, player.getUniqueId(), isPrivate));
                saveTeleportsToFile();

                player.sendMessage(ChatColor.GREEN +"[Teleporter] Two-way teleporter set up between the two emerald blocks.");
                getLogger().info(player.getName() + " created a two-way teleporter.");
                return true;
            }

            return false;
        }
    }

    private final Set<UUID> invinciblePlayers = new HashSet<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Block blockBelow = player.getLocation().getBlock().getRelative(0, -1, 0);

        // Check if player has moved away from the destination block
        if (recentlyTeleportedPlayers.contains(player.getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
                recentlyTeleportedPlayers.remove(player.getUniqueId());
            }
            return;
        }

        TeleportInfo teleportInfo = teleportPoints.get(blockBelow.getLocation());
        if (teleportInfo != null) {

            // If cooldown is clear, proceed with teleport
            if (teleportInfo.isPrivate && !teleportInfo.owner.equals(player.getUniqueId())
                    && !player.hasPermission("teleporters.bypass")) {
                player.sendMessage(ChatColor.RED + "[Teleporter] This teleporter is private.");
                return;
            }

            // Perform teleport
            Location destination = teleportInfo.destination.clone().add(0.5, 1, 0.5);
            player.teleport(destination, PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.sendMessage(ChatColor.GREEN + "Teleported to " + teleportInfo.destination.toVector());

            // Play enderpearl teleport sound
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

            // Spawn particle effect at destination
            player.getWorld().spawnParticle(Particle.PORTAL, destination, 100);

            // Make player invincible for 2 seconds
            UUID playerId = player.getUniqueId();
            invinciblePlayers.add(playerId);
            new BukkitRunnable() {
                @Override
                public void run() {
                    invinciblePlayers.remove(playerId);
                }
            }.runTaskLater(this, 40L); // 40 ticks = 2 seconds

            // Add player to recently teleported set
            recentlyTeleportedPlayers.add(playerId);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (invinciblePlayers.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location blockLocation = event.getBlock().getLocation();
        if (teleportPoints.containsKey(blockLocation)) {
            TeleportInfo linkedTeleport = teleportPoints.get(blockLocation);
            teleportPoints.remove(blockLocation);
            teleportPoints.remove(linkedTeleport.destination);
            saveTeleportsToFile();

            event.getPlayer().sendMessage(ChatColor.YELLOW +"[Teleporter] Teleporter removed.");
            getLogger().info("Teleporter removed due to block break at " + blockLocation.toVector());
        }
    }
}
