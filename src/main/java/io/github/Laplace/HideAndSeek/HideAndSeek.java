package io.github.Laplace.HideAndSeek;

import java.util.Arrays;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class HideAndSeek extends JavaPlugin{
	@Override
    public void onEnable() {
    }
    
    @Override
    public void onDisable() {
    }
    public void onPlayerDeath(PlayerDeathEvent event)
    {
    	event.setDeathMessage("What...!");//So no one knows who killed whom
        Player player = event.getEntity();
        player.getLocation().getWorld().playEffect(player.getLocation(), Effect.SMOKE, 1);
        //TODO:
        //If a player died
        //Respawn the player outside the game, or just gamemode 2
        //Change the hp limit according to team
        //Hunters + 1 hearts
        //Moles unchanged
        //Preys all -2 hearts
        //Minimum 5 hearts and maximum 15 hearts
        //Alternative: if the dead Prey was killed by another Prey, do the above actions
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if (cmd.getName().equalsIgnoreCase("addPlayers")) {
    		//TODO: should we add players who want to play before starting game?
    		return true;
    	}
    	if (cmd.getName().equalsIgnoreCase("gameStart")) {
    		TeamPlayers();
    		return true;
    	}
    	if (cmd.getName().equalsIgnoreCase("setTeam")) {
    		
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
    public void TeamPlayers() {
    	Player[] players = (Player[])Bukkit.getOnlinePlayers().toArray();
    	Collections.shuffle(Arrays.asList(players));
    	for(Player p: players){
    		if(Team.Hunter.TeamAvailable()) {
    			Team.Hunter.AddPlayer(p);
    			break;
    		}
    		if(Team.Mole.TeamAvailable()) {
    			Team.Mole.AddPlayer(p);
    			break;
    		}
    		Team.Prey.AddPlayer(p);
    	}
    }
    public void EquipPlayers() {
    	//TODO:
		//Give players some items according to their roles
    	//Hunters: Almost-Broken Iron-Axe
    	//Moles: Lingering Potion of Poison
    	//Possibly tag players only where they can see it
    	Player[] players = (Player[])Bukkit.getOnlinePlayers().toArray();
    	for(Player p: players){
    		if(Team.GetPlayerTeam(p) == Team.Hunter) {
    			//Give Iron-Axe
    			break;
    		}
    		if(Team.GetPlayerTeam(p) == Team.Mole) {
    			//Give Lingering Potion of Poison
    			break;
    		}
    		//Give Preys a stick
    	}
    }
}
