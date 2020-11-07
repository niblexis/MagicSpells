package com.nisovin.magicspells.spells.passive;

import java.util.Set;
import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.entity.EntityPickupItemEvent;

import com.nisovin.magicspells.util.OverridePriority;
import com.nisovin.magicspells.util.magicitems.MagicItem;
import com.nisovin.magicspells.util.magicitems.MagicItems;
import com.nisovin.magicspells.util.magicitems.MagicItemData;
import com.nisovin.magicspells.spells.passive.util.PassiveListener;
import org.bukkit.inventory.meta.Damageable;

// Optional trigger variable that is a comma separated list of items to accept
public class ItemBreakListener extends PassiveListener {

    private final Set<MagicItemData> items = new HashSet<>();

    @Override
    public void initialize(String var) {
        if (var == null || var.isEmpty()) return;

        String[] split = var.split("\\|");
        for (String s : split) {
            s = s.trim();
            MagicItem magicItem = MagicItems.getMagicItemFromString(s);
            MagicItemData itemData = null;
            if (magicItem != null) itemData = magicItem.getMagicItemData();
            if (itemData == null) continue;

            items.add(itemData);
        }
    }

    @OverridePriority
    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        if (!hasSpell(player)) return;
        if (!canTrigger(player)) return;

        ItemStack item = event.getItem();
        Damageable damageable = (Damageable)item.getItemMeta();
        int durability = item.getType().getMaxDurability() - damageable.getDamage();
        if (durability - event.getDamage() >= 1) return;

        if (items.isEmpty()) {
            if (!isCancelStateOk(event.isCancelled())) return;
            boolean casted = passiveSpell.activate(player);
            if (!cancelDefaultAction(casted)) return;
            event.setCancelled(true);
            return;
        }
        MagicItemData itemData = MagicItems.getMagicItemDataFromItemStack(item);
        if (!items.contains(itemData)) return;

        if (!isCancelStateOk(event.isCancelled())) return;
        boolean casted = passiveSpell.activate(player);
        if (!cancelDefaultAction(casted)) return;
        event.setCancelled(true);
    }

}
