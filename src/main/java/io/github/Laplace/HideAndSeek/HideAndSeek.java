package io.github.Laplace.HideAndSeek;

import java.util.*;
import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public final class HideAndSeek extends JavaPlugin implements Listener {
	Team hunters = new Team("Hunter");
	Team moles   = new Team("Mole");
	Team preys   = new Team("Prey");

	@Override
	public void onEnable() {
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, this);
	}

	@Override
	public void onDisable() {
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		// So no one knows who killed whom
		event.setDeathMessage("Someone died.");
		Player player = event.getEntity();
		player.setGameMode​(GameMode.SPECTATOR);
		Player killer = player.getKiller();

		if (preys.HasPlayer(player)) {
			preys.AdjustMaxHealth(-1);
			if (killer != null && preys.HasPlayer(killer)) {
				hunters.AdjustMaxHealth(1);
			}
		}
		// TODO: if prey kills prey, max health of hunter += x, etc.


		// TODO:
		// Change the hp limit according to team
		// Hunters + 1 hearts
		// Moles unchanged
		// Preys all -2 hearts
		// Minimum 5 hearts and maximum 15 hearts
		// Alternative: if the dead Prey was killed by another Prey, do the above actions
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("addPlayers")) {
			//TODO: should we add players who want to play before starting game?
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("gamestart")) {
			GameStart();
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("setteam")) {
			if (args.length != 2) {
				sender.sendMessage("Usage: /setteam <player> <hunters|preys|moles>");
				return true;
			}
			String playerName = args[0];
			String tstr = args[1];
			Team t;
			switch (tstr) {
				case "hunters":
					t = hunters;
					break;
				case "preys":
					t = preys;
					break;
				case "moles":
					t = moles;
					break;
				default:
					sender.sendMessage("Invalid team.");
					return true;
			}
			if (t.HasPlayer(playerName)) {
				sender.sendMessage(String.format("%s is already part of %s", playerName, tstr));
			} else {
				Player player = Bukkit.getPlayer(playerName);
				if (player == null) {
					sender.sendMessage("Player off-line.");
					return true;
				}
				hunters.RemovePlayer(player);
				moles.RemovePlayer(player);
				preys.RemovePlayer(player);
				t.AddPlayer(player);
			}
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("listTeams")) {
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("endGame")) {
			return true;
		}
		return false;
	}

	public void GameStart() {
		List<? extends Player> players = new ArrayList(Bukkit.getOnlinePlayers());
		Collections.shuffle(players);
		int totalPlayers = players.size();

		int maxHunters = (int) Math.ceil(totalPlayers / 8f);
		int maxMoles = (int) Math.floor(totalPlayers / 8f);
		int maxPreys = totalPlayers - maxHunters - maxMoles;
		if (maxMoles == 0 && maxPreys > 1) {
			maxPreys -= 1;
			maxMoles += 1;
		}

		hunters.Reset();
		moles.Reset();
		preys.Reset();

		for (int i = 0; i < maxHunters; i ++) {
			hunters.AddPlayer(players.get(i));
		}
		for (int i = maxHunters; i < maxHunters + maxMoles; i ++) {
			moles.AddPlayer(players.get(i));
		}
		for (int i = maxHunters + maxMoles; i < totalPlayers; i ++) {
			preys.AddPlayer(players.get(i));
		}

		for (Player p : players) {
			p.setMaxHealth(20);
			p.setHealth​(20);
		}
	}

	public void EquipPlayers() {
		//TODO:
		//Give players some items according to their roles
		//Hunters: Almost-Broken Iron-Axe
		//Moles: Lingering Potion of Poison
		//Possibly tag players only where they can see it
	}
}
