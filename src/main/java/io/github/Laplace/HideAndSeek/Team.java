package io.github.Laplace.HideAndSeek;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Team {
    private static List<Team> teams  = new ArrayList<Team>();
    
    public static final Team Hunter = new Team("Hunter");
    public static final Team Mole   = new Team("Mole");
    public static final Team Prey   = new Team("Prey");
    
    private String name;
    private String prefix;
    /*private int maxPlayers;*/
    private List<String> players = new ArrayList<String>();
    
    public Team(String name/*, int maxPlayers*/) {
        this.name = name;
        /*this.maxPlayers = maxPlayers;*/
        teams.add(this);
    }
    
    public String GetPrefix() {
        return this.prefix;
    }
    
    public String GetName() {
    	return this.name;
    }
    
    public void AddPlayer(Player player) {
        players.add(player.getName());
    }
    
    public void RemovePlayer(Player player) {
        players.remove(player.getName());
    }
    
    public List<String> GetPlayers() {
        return this.players;
    }
    
    public static Team GetSmallestTeam() {
        //smallest team, starting with a default team
        Team teamMin = Team.Hunter;
        
        //loop through all teams and update the smallest team if needed
        for (Team team : teams) {
            if (team.GetPlayers().size() < teamMin.GetPlayers().size()) {
                teamMin = team;
            }
        }
        
        //return the smallest team
        return teamMin;
    }
    
    public Boolean TeamAvailable() {
    	int totalPlayers = Bukkit.getOnlinePlayers().size();
    	//int totalPlayersInTeam = 0;
    	
    	int maxHunters = (int)Math.ceil(totalPlayers / 8f);
    	int maxMoles = (int)Math.floor(totalPlayers / 8f);
    	int maxPreys = totalPlayers - maxHunters - maxMoles;
    	/*
    	for (Team team : teams) {
    		totalPlayersInTeam += team.GetPlayers().size();
        }*/
        if(this.name == "Hunter") {
        	if(this.players.size() <= maxHunters) {
        		return true;
        	}
        }
        if(this.name == "Mole") {
        	if(this.players.size() <= maxMoles) {
        		return true;
        	}
        }
        if(this.name == "Prey") {
        	if(this.players.size() <= maxPreys) {
        		return true;
        	}
        }
        return false;
    }
    
    public static Team GetPlayerTeam(Player player) {
        for (Team team : teams) {
            if (team.GetPlayers().contains(player.getName())) {
                return team;
            }
        }
        return null;
    }
}