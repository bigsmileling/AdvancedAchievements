package com.hm.achievement.runnable;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.YamlManager;

/**
 * Runnable task to check for Connection achievements after a player has connected.
 * 
 * @author Pyves
 *
 */
public class AchieveConnectionRunnable implements Runnable {

	private final Player player;
	private final AdvancedAchievements plugin;

	public AchieveConnectionRunnable(Player player, AdvancedAchievements plugin) {

		this.player = player;
		this.plugin = plugin;
	}

	@Override
	public void run() {

		// Check if player is still online.
		if (!player.isOnline()) {
			return;
		}

		// Check again in case player has changed world or game mode by the time this runnable was scheduled.
		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player)) {
			return;
		}

		// Check whether another runnable has already done the work (even though this method is intended to run once
		// per player per connection instance, this might happen in some server configurations).
		if (plugin.getConnectionListener().getPlayersAchieveConnectionRan().contains(player.getUniqueId().toString())) {
			return;
		}

		NormalAchievements category = NormalAchievements.CONNECTIONS;

		if (!player.hasPermission(category.toPermName())) {
			return;
		}

		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		if (!format.format(now).equals(plugin.getDb().getPlayerConnectionDate(player))) {

			int connections = plugin.getDb().updateAndGetConnection(player, format.format(now));
			String configAchievement = category + "." + connections;
			YamlManager pluginConfig = plugin.getPluginConfig();
			if (pluginConfig.getString(configAchievement + ".Message", null) != null) {
				plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
				plugin.getDb().registerAchievement(player,
						pluginConfig.getString(configAchievement + ".Name"),
						pluginConfig.getString(configAchievement + ".Message"));
				plugin.getReward().checkConfig(player, configAchievement);
			}
		}

		// Ran successfully to completion: no need to re-run while player is connected.
		plugin.getConnectionListener().getPlayersAchieveConnectionRan().add(player.getUniqueId().toString());
	}
}
