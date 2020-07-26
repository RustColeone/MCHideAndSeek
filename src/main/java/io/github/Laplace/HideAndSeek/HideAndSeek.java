package io.github.Laplace.HideAndSeek;

import java.util.*;
import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.*;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.persistence.PersistentDataType;

public final class HideAndSeek extends JavaPlugin implements Listener {
	Team hunters = new Team("Hunter");
	Team moles   = new Team("Mole");
	Team preys   = new Team("Prey");
	boolean gameInProgress = false;

	HashMap<UUID, Location> playerDeathPoint = new HashMap();

	@Override
	public void onEnable() {
		this.Reset();
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, this);
	}

	@Override
	public void onDisable() {
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		if (!gameInProgress) return;
		Player deadPlayer = event.getEntity();
		deadPlayer.setGameMode​(GameMode.SPECTATOR);
		hunters.MarkDeadIfHas(deadPlayer);
		moles.MarkDeadIfHas(deadPlayer);
		preys.MarkDeadIfHas(deadPlayer);
		Player killer = deadPlayer.getKiller();
		// So no one knows who killed whom
		event.setDeathMessage("");
		Location loc = deadPlayer.getLocation();
		loc.getWorld().playEffect(deadPlayer.getLocation(), Effect.SMOKE, 2);
		for (Player p : this.getServer().getOnlinePlayers()) {
			if (p == deadPlayer || p == killer) continue;
			p.sendMessage("A scream echos in distance, someone must have fade from existence.");
		}
		playerDeathPoint.put(deadPlayer.getUniqueId(), loc);

		deadPlayer.sendMessage("Desperately you seek for help, but all you can do is moan and yelp.");
		if (killer != null) {
			killer.sendMessage("Its the right thing to do you told yourself, but tell me, was it for the pelf?");
		}

		if (hunters.IsAllDead()){
			EndGame(preys);
			return;
		} else if (preys.IsAllDead()) {
			if (!moles.IsAllDead()){
				EndGame(moles);
				return;
			} else {
				EndGame(hunters);
				return;
			}
		} else if (moles.IsAllDead()) {
			for (String pName : moles.GetPlayerNames()) {
				Player p = Bukkit.getPlayer(pName);
				if (p != null) {
					p.sendMessage("Team moles has lost. The game continues with hunter vs preys.");
				}
			}
		}

		if (preys.HasPlayer(deadPlayer)) {
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
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent evt) {
		if (!gameInProgress) return;
		Player p = evt.getPlayer();
		Location deathLoc = playerDeathPoint.get(p.getUniqueId());
		if (deathLoc != null) {
			evt.setRespawnLocation​(deathLoc);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("addPlayers")) {
			//TODO: should we add players who want to play before starting game?
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("startgame")) {
			StartGame();
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("setteam")) {
			if (args.length != 2) {
				sender.sendMessage("Usage: /setteam <player> <hunters|preys|moles>");
				return true;
			}
			if (!gameInProgress) {
				sender.sendMessage("Game not started.");
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
		if (cmd.getName().equalsIgnoreCase("listteams")) {
			if (args.length != 1) {
				sender.sendMessage("Usage: /listteams <hunters|preys|moles|all>");
				return true;
			}
			if (!gameInProgress) {
				sender.sendMessage("Game not started.");
			}
			ArrayList<Team> teams_to_send = new ArrayList<Team>();
			switch (args[0]) {
				case "hunters":
					teams_to_send.add(hunters);
					break;
				case "preys":
					teams_to_send.add(preys);
					break;
				case "moles":
					teams_to_send.add(moles);
					break;
				case "all":
					teams_to_send.add(hunters);
					teams_to_send.add(preys);
					teams_to_send.add(moles);
					break;
				default:
					sender.sendMessage("Invalid team.");
					return true;
			}
			for (Team t : teams_to_send) {
				String msg = t.PrintPlayerList();
				sender.sendMessage(msg);
			}
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("endgame")) {
			EndGame(null);
			return true;
		}
		return false;
	}

	public void Reset() {
		hunters.Reset();
		moles.Reset();
		preys.Reset();
		playerDeathPoint.clear();
		gameInProgress = false;
	}

	public void StartGame() {
		this.Reset();
		gameInProgress = true;
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

		EquipPlayers();
	}

	public void EquipPlayers() {
		for (Player p : this.getServer().getOnlinePlayers()) {
			PlayerInventory inv = p.getInventory();
			inv.clear();
			if (preys.HasPlayer(p)){
				inv.addItem(new ItemStack(Material.BREAD, 1));
			} else if (hunters.HasPlayer(p)){

				//ItemStack item = new ItemStack(Material.IRON_AXE, 1);
				//weapon.setDurability(item.getType().getMaxDurability() - 240);

				inv.addItem(item);
				// FIXME
				//I be doing it already if I know how --10:31
				//Wait, try this -- 01:31
				//If it doesn't work, I'd settle for a brand new iron axe
				ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
				String command = "/give " + p.name + " iron_axe 1 240";
				Bukkit.dispatchCommand(console, command);

			} else if (moles.HasPlayer(p)){
				ItemStack item = new ItemStack(Material.LINGERING_POTION, 1);
				PotionMeta meta = ((PotionMeta) item.getItemMeta());
				meta.setColor(Color.BLUE);
				meta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 10, 2), true);
				item.setItemMeta(meta);
				inv.addItem(item);
			} else {
				p.sendMessage("For some reason you were not assigned a group. Therefore we did not give you any items.");
			}
		}
	}

	public void EndGame(Team winningTeam){
		for (Player p : this.getServer().getOnlinePlayers()) {
			p.setMaxHealth(20);
			p.setHealth​(20);
			String Message;
			if (winningTeam != null) {
				if (winningTeam.HasPlayer(p)) {
					p.sendMessage(String.format("%s%s**** %s won, congratulation! ****", ChatColor.BLUE, ChatColor.BOLD, winningTeam.GetName()));
				} else {
					p.sendMessage(String.format("%s**** %s won (your team lost) ****", ChatColor.RED, winningTeam.GetName()));
				}
			}
			p.sendMessage("Player and their teams:");
			p.sendMessage(hunters.PrintPlayerList());
			p.sendMessage(preys.PrintPlayerList());
			p.sendMessage(moles.PrintPlayerList());
		}
		this.Reset();
	}
}
