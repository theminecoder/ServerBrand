package me.theminecoder.minecraft.serverbrand;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.io.*;
import java.util.Map;
import java.util.UUID;

public final class ServerBrandBungeeCord extends Plugin implements Listener, ServerBrandPlugin {

    private Map<UUID, String> serverBrands = Maps.newHashMap();

    @Override
    public void onEnable() {

        //region Save Default Config
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }
        //endregion

        ServerBrandAPI.getInstance().setPlugin(this);
        try {
            ServerBrandAPI.getInstance().setBrand(ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile).getString("brand", "My Server"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        this.getProxy().getPluginManager().registerListener(this, this);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getReceiver() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
            if (event.getTag().equalsIgnoreCase(getBrandChannel(player))) {
                event.setCancelled(true);

                ByteBuf oldByteBuf = ByteBufAllocator.DEFAULT.heapBuffer();
                oldByteBuf.writeBytes(event.getData());
                String serverBrand = ByteBufData.readString(oldByteBuf);
                serverBrands.put(player.getUniqueId(), serverBrand);
                oldByteBuf.release();

                updateBrand(player);
            }
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        serverBrands.remove(event.getPlayer().getUniqueId());
    }

    private String getBrandChannel(ProxiedPlayer player) {
        return player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_13 ? "minecraft:brand" : "MC|Brand";
    }

    private void updateBrand(ProxiedPlayer player) {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.heapBuffer();
        ByteBufData.writeString(ChatColor.translateAlternateColorCodes('&',
                ServerBrandAPI.getInstance().getBrand()).replaceAll("%%[sS][eE][rR][vV][eE][rR]%%",
                serverBrands.getOrDefault(player.getUniqueId(), "")) + ChatColor.RESET, byteBuf);
        player.sendData(getBrandChannel(player), ByteBufData.toArray(byteBuf));
        byteBuf.release();
    }

    @Override
    public void updateEveryonesBrand() {
        this.getProxy().getPlayers().forEach(this::updateBrand);
    }
}
