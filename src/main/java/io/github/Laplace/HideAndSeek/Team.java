package io.github.Laplace.HideAndSeek;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.Player;

public class Team {
    private String name;
    private String prefix;
    /*private int maxPlayers;*/
    private HashSet<String> players = new HashSet<String>();
    private HashSet<UUID> deads = new HashSet<UUID>();

    public Team(String name) {
        this.name = name;
        this.Reset();
    }

    public String GetPrefix() {
        return this.prefix;
    }

    public String GetName() {
    	return this.name;
    }

    public void Reset() {
        this.players.clear();
        this.deads.clear();
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

    public HashSet<String> GetPlayerNames() {
        return this.players;
    }

    public boolean HasPlayer(String name) {
        return this.players.contains(name);
    }
    public boolean HasPlayer(Player p) {
        return HasPlayer(p.getName());
    }

    public void MarkDeadIfHas(Player p) {
        if (this.HasPlayer(p)) {
            this.deads.add(p.getUniqueId());
        }
    }

    public boolean IsAllDead() {
        for (String pName : this.players) {
            Player p = Bukkit.getPlayer(pName);
            if (p == null) {
                continue;
            }
            if (!this.deads.contains(p.getUniqueId())) {
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
            if (this.deads.contains(pName)) {
                continue;
            }
            double currentMaxHp = p.getMaxHealth();
            double newMaxHp = currentMaxHp + offset;
            if (newMaxHp < 10) newMaxHp = 10;
            if (newMaxHp > 30) newMaxHp = 30;
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
            try {
                if (newHp < currHp) {
                    p.damage(currHp - newHp);
                    p.setMaxHealth(newMaxHp);
                } else {
                    p.setMaxHealth(newMaxHp);
                    if (newHp <= 0) {
                        p.damage(p.getHealth());
                    } else {
                        p.setHealth(newHp);
                    }
                    if (offset > 0) {
                        p.playSound​(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 0f);
                    }
                }
            } catch (Exception e) {
                // tood
            }
        }
    }

    public String PrintPlayerList() {
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.BOLD);
        sb.append(ChatColor.GOLD);
        sb.append(this.GetName());
        sb.append(ChatColor.RESET);
        if (this.IsAllDead()) {
            sb.append(" (all dead)");
        }
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
