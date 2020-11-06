package com.nisovin.magicspells.spelleffects.effecttypes;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.SpellEffect;
import com.nisovin.magicspells.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class PlayerListEffect extends SpellEffect {

    private String message;
    private String type;

    @Override
    protected void loadFromConfig(ConfigurationSection config) {
        message = config.getString("message", "");
        type = config.getString("type", "header");
    }

    @Override
    public Runnable playEffectEntity(Entity entity) {
        if (!(entity.getType() == EntityType.PLAYER)) return null;
        Player player = (Player) entity;
        String msg = Util.doVarReplacementAndColorize(player, message);
        if (type.equalsIgnoreCase("header")) player.setPlayerListHeader(msg);
        if (type.equalsIgnoreCase("footer")) player.setPlayerListFooter(msg);
        if (type.equalsIgnoreCase("both")) player.setPlayerListHeaderFooter(msg, msg);
        return null;
    }
}
