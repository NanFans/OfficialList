package org.nanfans;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class OfficialList extends JavaPlugin {
	public static String prefix;
	public static String NotOnList;
	public static String AddSuccess;
	public static String OnChat;
	public static String OnCommand;
	public static Connection sql;

	public void onEnable() {
		saveDefaultConfig();
		loadCfg();
		sql = getMysql();
		sqlReady();
		Bukkit.removeBossBar(NamespacedKey.minecraft("whitelist"));
		Bukkit.createBossBar(NamespacedKey.minecraft("whitelist"), NotOnList, BarColor.GREEN, BarStyle.SOLID,
				BarFlag.values());
		Bukkit.getBossBar(NamespacedKey.minecraft("whitelist")).setVisible(true);
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) {
			return true;
		}
		if (args[0].equalsIgnoreCase("reload")) {
			loadCfg();
			sender.sendMessage(prefix + "配置文件已重载");
		}
		if (args.length < 2) {
			return true;
		}
		String senderName = "console";
		if (sender instanceof Player) {
			senderName = sender.getName();
		}
		if (args[0].equalsIgnoreCase("add")) {
			String sqlLine = null;
			sqlLine = "SELECT * from officiallist where player=?";
			try {
				PreparedStatement statement = sql.prepareStatement(sqlLine);
				statement.setString(1, args[1]);
				ResultSet data = statement.executeQuery();
				if (data.next()) {
					sender.sendMessage(prefix + "添加失败，玩家 §a" + args[1] + " §f已经在名单中了");
					return true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				sender.sendMessage(prefix + "数据库连接已丢失");
				return true;
			}
			sqlLine = "INSERT INTO officiallist(player,operator) values(?,?)";
			try {
				PreparedStatement statement = sql.prepareStatement(sqlLine);
				statement.setString(1, args[1]);
				statement.setString(2, senderName);
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
				sender.sendMessage(prefix + "数据库连接已丢失");
				return true;
			}
			if (!(PlayerListener.officialList.contains(args[1].toLowerCase()))) {
				PlayerListener.officialList.add(args[1].toLowerCase());
			}
			try {
				PlayerListener.kb.removePlayer(Bukkit.getPlayer(args[1]));
				Bukkit.getPlayer(args[1]).sendMessage(prefix + AddSuccess);
			}catch (Exception e) {
				sender.sendMessage(prefix + "此玩家不在线，他不会收到欢迎消息");
				
			}
			sender.sendMessage(prefix + "玩家 §a" + args[1] + " §f已添加");
			return true;
		}
		if (args[0].equalsIgnoreCase("remove")) {
			String sqlLine = null;
			sqlLine = "SELECT * from officiallist where player=?";
			try {
				PreparedStatement statement = sql.prepareStatement(sqlLine);
				statement.setString(1, args[1]);
				ResultSet data = statement.executeQuery();
				if (!(data.next())) {
					sender.sendMessage(prefix + "移除失败，玩家 §a" + args[1] + " §f不在名单中");
					return true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				sender.sendMessage(prefix + "数据库连接已丢失");
				return true;
			}
			sqlLine = "DELETE from officiallist where player=?";
			try {
				PreparedStatement statement = sql.prepareStatement(sqlLine);
				statement.setString(1, args[1]);
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
				sender.sendMessage(prefix + "数据库连接已丢失");
				return true;
			}
			if (PlayerListener.officialList.contains(args[1].toLowerCase())) {
				PlayerListener.officialList.remove(args[1].toLowerCase());
			}
			try {
				PlayerListener.kb.addPlayer(Bukkit.getPlayer(args[1]));
			}catch (Exception e) {
				sender.sendMessage(prefix + "此玩家不在线，他不会收到名单移除消息");
			}
			sender.sendMessage(prefix + "玩家 §a" + args[1] + " §f已从名单中移除");
			return true;
		}
		if (args[0].equalsIgnoreCase("debug")) {
			String list = "缓存：";
			for(int i = 0;i<PlayerListener.officialList.size();i++) {
				list += "--" + PlayerListener.officialList.get(i);
			}
			sender.sendMessage(prefix + list);
			return true;
		}
		return false;
	}

	public boolean sqlReady() {
		(new BukkitRunnable() {
			public void run() {
				try {
					if (sql != null && !sql.isClosed()) {
						sql.createStatement().execute("SELECT 1");
					}
				} catch (SQLException e) {
					sql = getMysql();
				}
			}
		}).runTaskTimerAsynchronously(this, 60 * 20, 60 * 20);
		String sqlLine = "CREATE TABLE IF NOT EXISTS officiallist(player varchar(50) NOT NULL,operator varchar(50) NOT NULL)";
		try {
			PreparedStatement statement = sql.prepareStatement(sqlLine);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public Connection getMysql() {
		Connection connection = null;
		String host = getConfig().getString("mysql.host");
		String port = getConfig().getString("mysql.port");
		String database = getConfig().getString("mysql.database");
		String user = getConfig().getString("mysql.user");
		String password = getConfig().getString("mysql.password");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
			connection = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			getLogger().info("§c>>>>>>>>严重错误<<<<<<<<");
			getLogger().info("§cMySql驱动异常，OfficialList无法正常工作");
			getLogger().info("§c白名单拦截已失效，若需启用请检查配置文件并重启");
		} catch (SQLException e) {
			getLogger().info("§c>>>>>>>>严重错误<<<<<<<<");
			getLogger().info("§cMySql连接异常，OfficialList无法正常工作");
			getLogger().info("§c白名单拦截已失效，若需启用请检查配置文件并重启");
		}
		return connection;
	}

	public void loadCfg() {
		reloadConfig();
		prefix = getConfig().getString("prefix").replace("&", "§");
		NotOnList = getConfig().getString("NotOnList").replace("&", "§");
		AddSuccess = getConfig().getString("AddSuccess").replace("&", "§");
		OnChat = getConfig().getString("OnChat").replace("&", "§");
		OnCommand = getConfig().getString("OnCommand").replace("&", "§");
		try {
			Bukkit.getBossBar(NamespacedKey.minecraft("whitelist")).setTitle(NotOnList);
		}catch (Exception e) {
			
		}
		getLogger().info("§aOfficialList读取完毕");
	}
}