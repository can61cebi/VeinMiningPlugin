# VeinMiningPlugin

A simple Minecraft Paper plugin that allows you to mine an entire vein of ores with a single block break, if you're holding a valid pickaxe. When you break an ore, any connected ores of the same type within a 5-block radius will also break, and all the drops will appear at the position of the originally mined block.

---

## Features

- **Vein Mining:**  
  - Break one ore block, and all the directly connected blocks of the same ore type (up to a 5-block radius) will break simultaneously.
  - Drops from all the connected ores will be collected at the first block's location.
- **Pickaxe Requirement:**  
  - Only works if you hold a valid pickaxe (wooden, stone, iron, golden, diamond by default).
  - Honors Minecraft's standard pickaxe/ore-breaking rules (e.g., no drops for diamond ore when using a wooden pickaxe).
- **Durability & Enchantments:**  
  - Basic durability logic applies each time you perform a vein break. 
  - Takes Unbreaking enchantment into account to reduce the chance of durability loss.
- **Survival Mode Oriented:**  
  - The plugin's vein mining behavior is disabled in Creative mode by default.

---

## Installation

1. **Download** the latest `.jar` file from the [Releases](https://github.com/can61cebi/VeinMiningPlugin/releases/tag/Minecraft) page.
2. **Place** the `.jar` file into your Paper server's `plugins` folder.
3. **Restart** or **start** your Paper server.
4. You should see a message in the console indicating that **VeinMiningPlugin** is enabled.

---

## Usage

1. **Hold a valid pickaxe** (Wooden, Stone, Iron, Golden, or Diamond).  
2. **Break an ore block** (coal, iron, gold, diamond, emerald, lapis, or redstone).  
3. Any connected ore blocks of the **same type** within a 5-block radius will also break.  
4. All drops from the vein will spawn at the position of the originally broken block.  
5. Your pickaxe will lose a small amount of durability (influenced by Unbreaking).  

---

## How It Works

- The plugin uses a simple BFS/DFS search to find all adjacent ore blocks of the same type around the first broken block, up to a 5-block radius.
- Once collected, it calculates the drops using Bukkit's `getDrops(itemInHand)` method to respect Silk Touch, Fortune, and normal rules for pickaxe-tier vs. ore block.
- It then sets all these blocks to air and spawns the collected drops at the original block's location.
- Finally, it applies minor durability loss logic to your pickaxe. If the pickaxe breaks, it's removed from your inventory.

---

## Compatibility

- **Paper version:** Tested on Paper 1.21.x; should generally work on corresponding Bukkit/Spigot versions (but recommended to use Paper).  
- **Java version:** Requires Java 17 or later (matching the Minecraft 1.21.4 requirement).
- **No external libraries** are required.

---

## Contributing

- Feel free to open issues or pull requests.
- For significant changes, open an issue to discuss proposed modifications.
