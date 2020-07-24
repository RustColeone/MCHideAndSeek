# MCHideAndSeek
A Minecraft server plugin for a customized rule of hide and seek

The rules are subject to change if needed.
## Roles

There are three roles in this game
1. Hunter
   - Kill every mole and prey to win
2. Mole
   - Survived untill the end, or is one on one with the Hunter
3. Prey
   - Kill the Mole and the Hunter to win

## Rules

In a small map like a mansion for example, for a total number of N players:

1. Randomly choose $\lceil\frac{N}{8}\rceil$ player(s) as **Hunter** and the same number of player(s) as **Mole**. Rest of the players are all **preys**. (if there are 4 players playing this game, there would be 1 hunter, 1 mole, and 2 prey; if there are 10 players, there will be 2 hunter, 2 mole, and 6 prey)

2. The Hunter has a good weapon, typically given an **almost-broken-iron-axe** at the start, and the mole has a **lingering poison potion**, while the preys starts with nothing.

3. No one knows the role of each other.

4. Inside the map there are traps and chests with armors or weapons.

5. For each prey died, the HP limit of the hunter increase by one, while the HP limit of all the preys would decrease by 2, with a minimum of 5 hearts and maximum of 15 hearts.

6. Zombies spawns gradually as time pass.

7. Ideally, people can only hear the voice of people near them. You can start seperate voice channel if a subset of players would like to team up. (so if a number of x, y and z players formed three teams, they could technicall start three voice channel) 
