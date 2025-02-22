# VeinMiningPlugin

VeinMiningPlugin is a Minecraft Paper plugin that allows you to mine an entire vein of ores with a single block break if you're holding a valid pickaxe. When you break an ore, any connected ores of the same type within a 5-block radius will also break, and all drops will appear at the position of the originally mined block. Additionally, this version now properly calculates and spawns experience orbs based on each ore’s XP value using native BlockExpEvent handling.

---

## Features

- **Vein Mining:**  
  - Break one ore block, and all directly connected blocks of the same ore type (up to a 5-block radius) will break simultaneously.
  - Drops from all connected ores are collected and spawned at the first block's location.
- **Experience Handling:**  
  - The plugin now correctly calculates the XP from each ore block using BlockExpEvent and spawns Experience Orbs with a natural spawn reason.
  - Experience is summed from all connected ore blocks, ensuring you receive the proper amount.
- **Pickaxe Requirement:**  
  - Only works if you hold a valid pickaxe (wooden, stone, iron, golden, diamond by default).
  - Honors Minecraft's standard pickaxe/ore-breaking rules (e.g., no drops for diamond ore when using a wooden pickaxe).
- **Durability & Enchantments:**  
  - Basic durability logic applies each time you perform a vein break.
  - Takes the Unbreaking enchantment into account to reduce the chance of durability loss.
- **Survival Mode Oriented:**  
  - The vein mining behavior is disabled in Creative mode by default.

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
5. Experience orbs will be spawned based on the XP values of all broken ore blocks.  
6. Your pickaxe will lose a small amount of durability (influenced by Unbreaking).

---

## How It Works

- The plugin uses a simple BFS search to find all adjacent ore blocks of the same type around the first broken block, up to a 5-block radius.
- For each ore block found, the plugin calculates the XP it would normally drop—either by using a built-in estimation or via a BlockExpEvent—to let other plugins modify the XP value.
- All these ore blocks are then set to air, and their drops are collected and spawned at the location of the originally broken block.
- Experience orbs are spawned at the ore block locations using the XP determined by the BlockExpEvent, with a natural spawn reason (BLOCK_BREAK) so that players receive the proper XP.
- Finally, a minor durability loss is applied to your pickaxe; if it breaks, it is removed from your inventory.

<img src="https://github.com/can61cebi/VeinMiningPlugin/blob/main/images/demo.gif" width="400">

---

## Compatibility

- **Paper version:** Tested on Paper 1.21.x; should generally work on corresponding Bukkit/Spigot versions (but it is recommended to use Paper).  
- **Java version:** Requires Java 17 or later (matching the Minecraft 1.21.4 requirement).  
- **No external libraries** are required.

---

## Contributing

- Feel free to open issues or pull requests.
- For significant changes, please open an issue first to discuss proposed modifications.

---

## License

This project is available under the [MIT License](./LICENSE).
