package org.nanfans;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener{
	static List<String> officialList = new ArrayList<String>();
	static KeyedBossBar kb = Bukkit.getBossBar(NamespacedKey.minecraft("whitelist"));
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChat(AsyncPlayerChatEvent event) {
		if(officialList.contains(event.getPlayer().getName().toLowerCase())) {
			return;
		}
		event.getPlayer().sendMessage(OfficialList.prefix + OfficialList.OnChat);
		event.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void useCommand(PlayerCommandPreprocessEvent event) {
		if(officialList.contains(event.getPlayer().getName().toLowerCase())) {
			return;
		}
		String command = event.getMessage();
		int spaceIndex = command.indexOf(" ");
		if(spaceIndex<0) {
			event.getPlayer().sendMessage(OfficialList.prefix + OfficialList.OnCommand);
    		event.setCancelled(true);
    		return;
    	}
		String commandHead = command.substring(1,spaceIndex);
    	if(commandHead.equals("login")||commandHead.equals("l")||commandHead.equals("reg")||commandHead.equals("register")) {
    		return;
    	}
		
		event.getPlayer().sendMessage(OfficialList.prefix + OfficialList.OnCommand);
		event.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onDrop(PlayerDropItemEvent event) {
		if(officialList.contains(event.getPlayer().getName().toLowerCase())) {
			return;
		}
		event.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onDamage(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Player&&!(officialList.contains(event.getDamager().getName().toLowerCase()))){
			event.setCancelled(true);
			return;
		}
		if(event.getEntity() instanceof Player&&!(officialList.contains(event.getEntity().getName().toLowerCase()))){
			event.setCancelled(true);
			return;
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onUse(PlayerInteractEvent event) {
		if(officialList.contains(event.getPlayer().getName().toLowerCase())) {
			return;
		}
		event.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onClick(PlayerInteractEntityEvent event) {
		if(officialList.contains(event.getPlayer().getName().toLowerCase())) {
			return;
		}
		event.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPickup(EntityPickupItemEvent event) {
		if(!(event.getEntity() instanceof Player)) {
			return;
		}
		if(officialList.contains(event.getEntity().getName().toLowerCase())) {
			return;
		}
		event.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onTP(PlayerTeleportEvent event) {
		if(officialList.contains(event.getPlayer().getName().toLowerCase())) {
			return;
		}
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		String sqlLine = "SELECT * from officiallist where player=?";
		try {
			PreparedStatement statement = OfficialList.sql.prepareStatement(sqlLine);
			statement.setString(1, event.getPlayer().getName());
			ResultSet data = statement.executeQuery();
			if(data.next()) {
				if(!(officialList.contains(event.getPlayer().getName().toLowerCase()))) {
					officialList.add(event.getPlayer().getName().toLowerCase());
		    	}
				kb.removePlayer(event.getPlayer());
				return;
			}
			kb.addPlayer(event.getPlayer());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
