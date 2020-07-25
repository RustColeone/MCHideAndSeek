package io.github.Laplace.HideAndSeek;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.Player;

public class Team {
    private String name;
    private String prefix;
    /*private int maxPlayers;*/
    private HashSet<String> players = new HashSet<String>();

    public Team(String name) {
        this.name = name;
    }

    public String GetPrefix() {
        return this.prefix;
    }

    public String GetName() {
    	return this.name;
    }

    public void Reset() {
        this.players.clear();
    }

    public void AddPlayer(Player player) {
        players.add(player.getName());
        player.setGameMode​(GameMode.ADVENTURE);
        player.sendMessage(String.format("You are now a %s", this.GetName()));
    }

    public void RemovePlayer(String name) {
        players.remove(name);
    }
    public void RemovePlayer(Player p) {
        RemovePlayer(p.getName());
    }

    public HashSet<String> GetPlayers() {
        return this.players;
    }

    public boolean HasPlayer(String name) {
        return this.players.contains(name);
    }
    public boolean HasPlayer(Player p) {
        return HasPlayer(p.getName());
    }

    public boolean IsAllDead() {
        for (String pName : this.players) {
            Player p = Bukkit.getPlayer(pName);
            if (p == null) {
                continue;
            }
            if (p.getGameMode() != GameMode.SPECTATOR) {
                return false;
            }
        }
        return true;
    }

    public void AdjustMaxHealth(int offset) {
        for (String pName : this.players) {
            Player p = Bukkit.getPlayer(pName);
            if (p == null) {
                // TODO
                continue;
            }
            if (p.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            double currentMaxHp = p.getMaxHealth();
            double newMaxHp = currentMaxHp + offset;
            if (newMaxHp < 10) newMaxHp = 10;
            if (newMaxHp > 30) newMaxHp = 30;
            p.setMaxHealth(newMaxHp);
            double currHp = p.getHealth();
            double newHp = currHp;
            if (offset > 0) {
                newHp += offset;
            }
            if (newHp > newMaxHp) {
                newHp = newMaxHp;
            }
            if (newHp <= 0) {
                newHp = 0;
            }
            p.setHealth(newHp);
        }
    }

    public String PrintPlayerList() {
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.BOLD);
        sb.append(ChatColor.GOLD);
        sb.append(this.GetName());
        sb.append(ChatColor.RESET);
        sb.append(": ");
        boolean first = true;
        for (String pName : this.players) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(pName);
        }
        return sb.toString();
    }
}
