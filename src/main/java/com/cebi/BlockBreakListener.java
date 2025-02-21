package com.cebi;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BlockBreakListener implements Listener {

    // Desteklenen pickaxe türleri
    private static final Set<Material> VALID_PICKAXES = Set.of(
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.DIAMOND_PICKAXE
            // Netherite varsa ekleyebilirsiniz: Material.NETHERITE_PICKAXE
    );

    // Hangi blokların "ore" sayıldığını tanımlıyoruz
    private static final Set<Material> ORES = Set.of(
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE
    );

    // Maksimum arama yarıçapı (blok cinsinden)
    private static final int RADIUS = 5;

    // DFS/BFS araması için yön vektörleri (6 yön: x±1, y±1, z±1)
    private static final int[][] DIRECTIONS = {
            {1, 0, 0}, {-1, 0, 0},
            {0, 1, 0}, {0, -1, 0},
            {0, 0, 1}, {0, 0, -1}
    };

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        // 1) Oyuncu geçerli pickaxe türlerinden birini tutuyor mu?
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (!VALID_PICKAXES.contains(itemInHand.getType())) {
            // Eğer geçerli pickaxe değilse, normal şekilde kırılma devam etsin ama toplu kırma yapma
            return;
        }

        // 2) Kırılan blok "ore" mu?
        if (!ORES.contains(blockType)) {
            // Ore değilse, yine normal kırılma sürsün
            return;
        }

        // 3) Survival veya Adventure modda mıyız? (Creative'de anlık kırma farklı)
        if (player.getGameMode() == GameMode.CREATIVE) {
            // Creative moddaysa, pluginin toplu kırma özelliği devre dışı kalabilir
            // İsterseniz yine de çalıştırabilirsiniz ama genelde yaratıcı modda gereksiz.
            return;
        }

        // 4) Normal kırma kuralları
        //    Minecraft'ta, mesela Wooden Pickaxe ile Diamond Ore kırarsanız eşya düşmez (drop = null).
        //    Bu kuralı yeniden uygulamak için hasSilkTouch vb. kontroller yapabiliriz.
        //    Fakat en basit yol: block.breakNaturally(itemInHand) gibi bir işlem yapmak.
        //    Toplu kırmada bu mantığı simüle edeceğiz.

        // 5) Toplu kırma (DFS veya BFS)
        //    Aynı türdeki ore bloklarını bulmak ve kırmak için
        //    Bloğun merkezinden RADIUS kadar uzakta arayacağız.
        Set<Block> veinBlocks = findConnectedOres(block, blockType, RADIUS);

        // Bu bloklar tek seferde kırılacağı için, orijinal event iptal edilmesin,
        // ama drops'ları toplayıp, hepsini ilk bloğun konumuna düşürelim.

        // 6) Drop eşyalarını hesapla
        // Kırılan tüm blokların "doğru" drop’unu elde etmek için her bloğa breakNaturally() uygulayabiliriz,
        // ancak bu, eşyaları farklı konumlara düşürür. Onları ilk blokta toplamak için, ya envantere direkt ekleriz
        // ya da dropItem() metoduyla ilk blokta spawnlarız.
        //
        // Performans açısından block.breakNaturally() her seferinde event tetikleyebilir,
        // bu yüzden geçici olarak eventler devre dışı bırakılabilir veya "silently" drops hesaplayabiliriz.
        // En basit yaklaşım: block.getDrops(itemInHand) -> topla -> spawn et.

        // Kırılan tüm bloklarda toplanacak eşyalar
        List<ItemStack> allDrops = new ArrayList<>();

        for (Block oreBlock : veinBlocks) {
            Collection<ItemStack> drops = oreBlock.getDrops(itemInHand);
            allDrops.addAll(drops);
        }

        // 7) Tüm blokları gerçek anlamda kır
        //    (Eğer Silk Touch var ise, getDrops durumu değişebilir, normal drop gibi.)
        //    Ama "block.getDrops(itemInHand)" silk touch, fortune vb. enchant’leri zaten hesaba katar.
        // Bu nedenle, gerçekte bloğu setType(AIR) yapmadan önce drop'ları hesapladık.
        for (Block oreBlock : veinBlocks) {
            oreBlock.setType(Material.AIR); 
        }

        // 8) Tüm drop'ları ilk kırılan bloğun konumuna düşür
        //    (İsteğe göre, birer birer spawn edebilir veya envantere ekleyebilirsiniz.)
        for (ItemStack drop : allDrops) {
            block.getWorld().dropItem(block.getLocation(), drop);
        }

        // Oyuncunun kazmasını da yıpranma payına (durability) uygun davranalım:
        damagePickaxe(itemInHand, player);

        // Artık normal BlockBreakEvent akışı esnasında
        // orijinal kırılan blok tekrar işlenmek istenirse engellemiş oluyor muyuz?
        // Yukarıda "tek seferde" hallettik.
        // Uyarı: "event.setDropItems(false)" diyerek orijinal dropları iptal edebiliriz, 
        // çünkü biz zaten dropları kendimiz spawn ettik.
        event.setDropItems(false);
    }

    /**
     * Belirtilen bloktan başlayarak, RADIUS dahilinde
     * bitişik (6 yönde) aynı türdeki tüm ore bloklarını bulur.
     */
    private Set<Block> findConnectedOres(Block start, Material oreType, int radius) {
        Set<Block> result = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(start);
        result.add(start);

        // Orijinal bloğun konumunu referans alarak, mesafe kontrolü yapabiliriz
        int startX = start.getX();
        int startY = start.getY();
        int startZ = start.getZ();

        while (!queue.isEmpty()) {
            Block current = queue.poll();
            for (int[] dir : DIRECTIONS) {
                Block neighbor = current.getRelative(dir[0], dir[1], dir[2]);

                // Mesafe kontrolü
                if (Math.abs(neighbor.getX() - startX) > radius ||
                    Math.abs(neighbor.getY() - startY) > radius ||
                    Math.abs(neighbor.getZ() - startZ) > radius) {
                    continue;
                }

                if (!result.contains(neighbor) && neighbor.getType() == oreType) {
                    result.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return result;
    }

    /**
     * Oyuncunun elindeki kazmayı 1 durability yıpratır (veya enchant'lerine bakar).
     * Not: Minecraft'taki normal yıpranma mantığını yansıtmak isterseniz,
     * event'ten "block damage" tetiklenmesini taklit edebilirsiniz.
     */
    private void damagePickaxe(ItemStack pickaxe, Player player) {
        // Eğer kazmada Unbreaking vb. varsa, yıpranma şansı düşer
        // Basit yaklaşım: her toplu kırmada 1 durability azalt
        if (pickaxe.getType().getMaxDurability() <= 0) {
            return; // Örneğin creative, vs.
        }

        // Unbreaking kontrolü
        int unbreakingLevel = pickaxe.getEnchantmentLevel(Enchantment.UNBREAKING);
        // % şansı: 100 / (unbreakingLevel+1)
        double chance = 1.0 / (unbreakingLevel + 1);
        if (Math.random() <= chance) {
            short durability = pickaxe.getDurability();
            durability++;
            pickaxe.setDurability(durability);

            // Kazma kırıldı mı?
            if (durability >= pickaxe.getType().getMaxDurability()) {
                // Elde bulanan kazma kırılır
                player.getInventory().remove(pickaxe);
                player.updateInventory();
            }
        }
    }
}
