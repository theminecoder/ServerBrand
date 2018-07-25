package me.theminecoder.minecraft.serverbrand;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

public final class ServerBrandSpigot extends JavaPlugin implements Listener, ServerBrandPlugin {

    private static Field playerChannelsField;
    private String channel;

    @Override
    public void onEnable() {
        try {
            Class.forName("org.bukkit.entity.Dolphin");
            channel = "minecraft:brand";
        } catch (ClassNotFoundException ignored) {
            channel = "MC|Brand";
        }

        this.saveDefaultConfig();
        getLogger().info("Using channel: " + channel);

        // We have to do this to bypass the reserved channel check
        try {
            Method registerMethod = this.getServer().getMessenger().getClass().getDeclaredMethod("addToOutgoing", Plugin.class, String.class);
            registerMethod.setAccessible(true);
            registerMethod.invoke(this.getServer().getMessenger(), this, channel);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Error while attempting to register plugin message channel", e);
        }

        ServerBrandAPI.getInstance().setPlugin(this);
        ServerBrandAPI.getInstance().setBrand(this.getConfig().getString("brand", "My Server"));


        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (playerChannelsField == null) {
            try {
                playerChannelsField = event.getPlayer().getClass().getDeclaredField("channels");
                playerChannelsField.setAccessible(true);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                this.getServer().getPluginManager().disablePlugin(this);
            }
        }

        try {
            Set<String> channels = (Set<String>) playerChannelsField.get(event.getPlayer());
            channels.add(channel);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
        }

        updateBrand(event.getPlayer());
    }

    private void updateBrand(Player player) {
        ByteBuf byteBuf = Unpooled.buffer();
        ByteBufData.writeString(ChatColor.translateAlternateColorCodes('&', ServerBrandAPI.getInstance().getBrand()) + ChatColor.RESET, byteBuf);
        player.sendPluginMessage(this, channel, ByteBufData.toArray(byteBuf));
        byteBuf.release();
    }

    @Override
    public void updateEveryonesBrand() {
        Bukkit.getOnlinePlayers().forEach(this::updateBrand);
    }
}
