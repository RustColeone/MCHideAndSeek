package io.github.Laplace.HideAndSeek;

import java.util.*;
import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
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
		Player player = event.getEntity();
		player.setGameMode​(GameMode.SPECTATOR);
		Player killer = player.getKiller();
		// So no one knows who killed whom
		event.setDeathMessage("You heard a scream in the distance, someone must have fade from existence.");
		player.getLocation().getWorld().playEffect(player.getLocation(), Effect.SMOKE, 2);

		player.sendMessage("Desperately you seek for help, but all you can do is moan and yelp.");
		killer.sendMessage("Its the right thing to do you told yourself, but tell me, was it for the pelf?");

		if(hunters.IsAllDead()){
			EndGame(prey);
		}
		else if(preys.IsAllDead()){
			if(!moles.IsAllDead()){
				EndGame(mole);
			}
			else{
				EndGame(hunters);
			}
			return;
		}

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
			EquipPlayers();
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
			if (args.length != 1) {
				sender.sendMessage("Usage: /listTeams <hunters|preys|moles|all>");
				return true;
			}
			String allHunters = "<Hunters>";
			String allMoles = "<Moles>";
			String allPreys = "<Preys>";
			for (Player p: getServer.getOnlinePlayers()) {
				if(preys.HasPlayer(p)){
					allPreys += "\n" + p.getName();
				}
				else if(hunters.HasPlayer(p)){
					allHunters += "\n" + p.getName();
				}
				else if(moles.HasPlayer(p)){
					allMoles += "\n" + p.getName();
				}
			}
			switch (args[0]) {
				case "hunters":
					sender.sendMessage(allHunters);
					break;
				case "preys":
					sender.sendMessage(allPreys);
					break;
				case "moles":
					sender.sendMessage(allMoles);
					break;
				case "all":
					sender.sendMessage(allHunters);
					sender.sendMessage(allPreys);
					sender.sendMessage(allMoles);
					break;
				default:
					sender.sendMessage("Invalid team.");
					return true;
			}
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
		for (Player p: getServer.getOnlinePlayers()) {
			if(preys.HasPlayer(p)){
				p.getInventory.add(new ItemStack(Material.BREAD, 1));
				//Giving one bread;
			}

			else if(hunters.HasPlayer(p)){
				ItemStack item = new ItemStack(Material.IRON_AXE);
				item.setDurability((short)10);
				p.getInventory.add(new ItemStack(item));
				//Giving one Iron_Axe that can only attack twice
			}

			else if(moles.HasPlayer(p)){
				ItemStack item = new ItemStack(Material.LINGERING_POTION);
                PotionMeta meta = ((PotionMeta) item.getItemMeta());
                meta.setColor(Color.BLUE);
                meta.addCustomEffect(new PotionEffect(PotionEffectType.Speed, 5, 2), true);
                item.setItemMeta(meta);
				player.getInventory().addItem(item);
				//Giving one Iron_Axe that can only attack twice
			}
		}
	}

	public void EndGame(Team winningTeam){
		if(winningTeam != null){
			for (Player p: getServer.getOnlinePlayers()) {

				p.setMaxHealth(20);
				p.setHealth​(20);

				String Message;
				if(winningTeam.HasPlayer(p)){
					Message = "Congradulations " + p.GetName() + ", the " + winningTeam.GetName() + " wins!";
				}
				else{
					Message = "Sorry  " + p.GetName() + ", the " + winningTeam.GetName() + " lost...";
				}
				p.sendMessage(Message);
			}
		}
		hunters.Reset();
		moles.Reset();
		preys.Reset();
		return;
	}

}
