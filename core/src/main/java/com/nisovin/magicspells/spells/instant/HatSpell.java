//
// Decompiled by Procyon v0.5.36
//

package com.nisovin.magicspells.spells.instant;

import com.nisovin.magicspells.util.magicitems.MagicItem;
import com.nisovin.magicspells.util.magicitems.MagicItems;
import org.bukkit.entity.Entity;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.Spell;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.Util;
import org.bukkit.ChatColor;
import com.nisovin.magicspells.util.MagicConfig;
import org.bukkit.inventory.ItemStack;
import com.nisovin.magicspells.spells.InstantSpell;

public class HatSpell extends InstantSpell
{
    private boolean replace;
    private String itemName;
    private String strHelmetFull;
    private ItemStack item;

    public HatSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        replace = getConfigBoolean("replace", false);
        itemName = getConfigString("item", "glass");
        strHelmetFull = ChatColor.translateAlternateColorCodes('&', getConfigString("str-helmet-full", "You're already wearing something on your head!"));
        item = MagicItems.getMagicItemFromString(itemName).getItemStack();
        if (item == null) MagicSpells.error("HatSpell " + internalName + " has an invalid item defined " + itemName);
    }

    @Override
    public PostCastAction castSpell(LivingEntity livingEntity, SpellCastState state, float power, String[] args) {
        if (!(livingEntity instanceof Player)) return null;
        Player player = (Player) livingEntity;
        if (state == SpellCastState.NORMAL) {
            if (!replace && player.getInventory().getHelmet() != null) {
                player.sendMessage(strHelmetFull);
                return null;
            }
            player.getInventory().setHelmet(item);
            playSpellEffects(EffectPosition.CASTER, player);
        }
        return PostCastAction.HANDLE_NORMALLY;
    }
}
