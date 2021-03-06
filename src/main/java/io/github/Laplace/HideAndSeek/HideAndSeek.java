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
import org.bukkit.block.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;

public final class HideAndSeek extends JavaPlugin implements Listener {
	Team hunters = new Team("Hunter");
	Team moles   = new Team("Mole");
	Team preys   = new Team("Prey");
	boolean gameInProgress = false;

	HashMap<UUID, Location> playerDeathPoint = new HashMap();
	HashMap<UUID, Location[]> playerCornerSetting = new HashMap();

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
			killer.sendMessage("Its the right thing to do you tell yourself, but tell me, was it for the pelf?");
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
			if (!(sender instanceof Player)) {
				sender.sendMessage("Can only be called player because region selection is required.");
				return true;
			}
			Player player = (Player) sender;
			UUID player_uuid = player.getUniqueId();
			Location[] arr = playerCornerSetting.get(player_uuid);
			if (arr == null || arr[0] == null || arr[1] == null || !arr[0].getWorld().equals(arr[1].getWorld()) || !arr[0].getWorld().equals(player.getLocation().getWorld())) {
				sender.sendMessage("Invalid region selection. Select bounding box of map with /hspos1 and /hspos2.");
				return true;
			}
			BoundingBox bb = BoundingBox.of(arr[0], arr[1]);
			FillChest(player.getLocation().getWorld(), bb);
			StartGame();
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("setteam")) {
			if (args.length != 2) {
				sender.sendMessage("Usage: /setteam <player> <hunters | preys | moles>");
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
				sender.sendMessage("Usage: /listteams <hunters | preys | moles | all>");
				return true;
			}
			if (!gameInProgress) {
				sender.sendMessage("Game not started.");
				return true;
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
			if (args.length != 0) {
				sender.sendMessage("Usage: /endgame");
				return true;
			}
			EndGame(null);
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("adjustmaxhealth")) {
			if (args.length != 2) {
				return false;
			}
			if (!gameInProgress) {
				sender.sendMessage("Game not started.");
				return true;
			}
			String tstr = args[0];
			int adj;
			try {
				adj = Integer.parseInt(args[1]);
			} catch (Exception e) {
				sender.sendMessage("Failed to parse int.");
				return true;
			}
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
			t.AdjustMaxHealth(adj);
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("hspos1") || cmd.getName().equalsIgnoreCase("hspos2")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Expected this to be called by player.");
				return true;
			}
			int corner_index = cmd.getName().equalsIgnoreCase("hspos1") ? 0 : 1;
			UUID player_uuid = ((Player) sender).getUniqueId();
			Location[] arr = playerCornerSetting.get(player_uuid);
			if (arr == null) {
				arr = new Location[2];
				playerCornerSetting.put(player_uuid, arr);
			}
			Location l = ((Player) sender).getLocation();
			arr[corner_index] = new Location(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
			sender.sendMessage(String.format("Set position %d!", corner_index + 1));
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
				// FIXME: this is not the way.
				ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
				String command = "minecraft:give \"" + p.getName().replaceAll​("\\\\", "\\\\").replaceAll​("\"", "\\\"") + "\" minecraft:iron_axe{Damage: 245}";
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

	public void FillChest (World world, BoundingBox bb){
		ItemStack[] items = new ItemStack[17];
		items[0] = new ItemStack(Material.WOODEN_PICKAXE, 1);
		items[1] = new ItemStack(Material.STONE_HOE, 1);
		items[2] = new ItemStack(Material.LEATHER_HELMET, 1);
		items[3] = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		items[4] = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		items[5] = new ItemStack(Material.LEATHER_BOOTS, 1);
		items[6] = new ItemStack(Material.CHAINMAIL_HELMET, 1);
		items[7] = new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1);
		items[8] = new ItemStack(Material.CHAINMAIL_LEGGINGS, 1);
		items[9] = new ItemStack(Material.CHAINMAIL_BOOTS, 1);
		items[10] = new ItemStack(Material.WHEAT, 1);
		items[11] = new ItemStack(Material.WHEAT, 2);
		ItemStack poisonArrow = new ItemStack(Material.TIPPED_ARROW, 1);
		PotionMeta meta = (PotionMeta) poisonArrow.getItemMeta();
		meta.setBasePotionData(new PotionData(PotionType.POISON));
		//meta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 10, 2), true);
		poisonArrow.setItemMeta(meta);
		items[12] = poisonArrow;
		items[13] = new ItemStack(Material.ROTTEN_FLESH, 2);
		items[14] = new ItemStack(Material.BOW, 1);
		items[15] = new ItemStack(Material.BOW, 1);
		items[16] = new ItemStack(Material.WOODEN_SHOVEL, 1);

		Random rng = new Random();
		for (int x = (int) bb.getMinX(); x <= (int) bb.getMaxX(); x ++) {
			for (int y = (int) bb.getMinY(); y <= (int) bb.getMaxY(); y ++) {
				for (int z = (int) bb.getMinZ(); z <= (int) bb.getMaxZ(); z ++) {
					Block b = world.getBlockAt​(x, y, z);
					if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
						Chest chest = (Chest) b.getState();
						Inventory inv = chest.getSnapshotInventory();
						inv.clear();
						ItemStack[] new_contents = new ItemStack[inv.getSize()];
						int nb_items = rng.nextInt(3) + 1;
						for (int _i = 0; _i < nb_items; _i ++) {
							int index;
							while (new_contents[index = rng.nextInt(new_contents.length)] != null) {}
							new_contents[index] = items[rng.nextInt(items.length)].clone();
						}
						inv.setContents(new_contents);
						chest.update();
					}
				}
			}
		}
	}

	public void EndGame(Team winningTeam){
		for (Player p : this.getServer().getOnlinePlayers()) {
			p.setMaxHealth(20);
			p.setHealth​(20);
			p.setFoodLevel(20);
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
			p.sendMessage("=============================");
		}
		this.Reset();
	}
}
