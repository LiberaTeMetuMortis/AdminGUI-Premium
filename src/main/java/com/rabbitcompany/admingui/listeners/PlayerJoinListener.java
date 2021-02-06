package com.rabbitcompany.admingui.listeners;

import com.rabbitcompany.admingui.AdminGUI;
import com.rabbitcompany.admingui.ui.AdminUI;
import com.rabbitcompany.admingui.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class PlayerJoinListener implements Listener {

    private AdminGUI adminGUI;

    public PlayerJoinListener(AdminGUI plugin){
        adminGUI = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){

        Player player = event.getPlayer();

        if(adminGUI.getConf().getBoolean("cjlm_enabled", true)){
            if(adminGUI.getConf().getString("join_message", "&7[&a+&7] &6{display_name}") != null){
                event.setJoinMessage(Message.chat(adminGUI.getConf().getString("join_message", "&7[&a+&7] &6{display_name}").replace("{name}", player.getName()).replace("{display_name}", player.getDisplayName())));
            }else{
                event.setJoinMessage(null);
            }
        }

        if(adminGUI.getPlayers().getString(player.getUniqueId().toString(), null) == null){
            adminGUI.getPlayers().set(player.getUniqueId() + ".name", player.getName());
            if(player.getAddress() != null && player.getAddress().getAddress() != null)
                adminGUI.getPlayers().set(player.getUniqueId() + ".ips", new String[]{player.getAddress().getAddress().toString().replace("/", "")});
            adminGUI.getPlayers().set(player.getUniqueId() + ".firstJoin", System.currentTimeMillis());
        }else{
            List<String> ips = adminGUI.getPlayers().getStringList(player.getUniqueId() + ".ips");
            if(player.getAddress() != null && player.getAddress().getAddress() != null)
                if(!ips.contains(player.getAddress().getAddress().toString().replace("/", ""))) ips.add(player.getAddress().getAddress().toString().replace("/", ""));
            adminGUI.getPlayers().set(player.getUniqueId() + ".ips", ips);
        }
        adminGUI.getPlayers().set(player.getUniqueId() + ".lastJoin", System.currentTimeMillis());
        adminGUI.savePlayers();

        //TODO: Permissions
        if(adminGUI.getConf().getBoolean("mysql", false) && adminGUI.getConf().getBoolean("ap_enabled", false) && adminGUI.getConf().getInt("ap_storage_type", 0) == 2){
            if(Database.rankNeedFix(player.getName())) Database.fixRank(player.getUniqueId(), player.getName());
            Database.cacheRank(player.getUniqueId());
        }

        if(adminGUI.getConf().getBoolean("ap_enabled", false)){
            String rank = adminGUI.getPlayers().getString(player.getName() + ".rank", null);
            if(rank != null){
                adminGUI.getPlayers().set(player.getName(), null);
                adminGUI.getPlayers().set(player.getUniqueId() + ".rank", rank);
                adminGUI.savePlayers();
            }
            TargetPlayer.refreshPermissions(player);
        }

        if(adminGUI.getConf().getBoolean("bungeecord_enabled", false)){
            Channel.send(player.getName(),"send", "online_players");
        }else{
            AdminUI.online_players.add(player.getName());
        }

        AdminUI.skulls_players.put(player.getName(), Item.pre_createPlayerHead(player.getName()));

        if(adminGUI.getConf().getBoolean("atl_enabled", false)) TargetPlayer.refreshPlayerTabList(player);

        //Update Checker
        if(adminGUI.getConf().getBoolean("uc_enabled", true) && adminGUI.getConf().getInt("uc_send_type", 1) == 1 && AdminGUI.new_version != null && (player.hasPermission("admingui.admin") || player.isOp())){
            player.sendMessage(Message.getMessage(player.getUniqueId(), "prefix") + Message.chat(adminGUI.getConf().getString("uc_notify", "&aNew update is available. Please update me to &b{version}&a.").replace("{version}", AdminGUI.new_version)));
        }

        if(adminGUI.getConf().getInt("initialize_gui",0) == 1) {
            if(!AdminUI.task_gui.containsKey(player.getUniqueId())) Initialize.GUI(player, player.getInventory().getHelmet());
        }
    }
}
