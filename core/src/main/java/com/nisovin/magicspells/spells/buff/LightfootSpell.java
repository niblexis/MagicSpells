package com.nisovin.magicspells.spells.buff;

import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.LivingEntity;
import java.util.HashSet;
import com.nisovin.magicspells.util.MagicConfig;
import java.util.UUID;
import java.util.Set;
import com.nisovin.magicspells.spells.BuffSpell;

public class LightfootSpell extends BuffSpell {
    private Set<UUID> lightfoots;

    public LightfootSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        lightfoots = new HashSet<UUID>();
    }

    @Override
    public boolean castBuff(LivingEntity entity, float power, String[] args) {
        lightfoots.add(entity.getUniqueId());
        return true;
    }

    @Override
    public boolean isActive(LivingEntity entity) {
        return lightfoots.contains(entity.getUniqueId());
    }

    @Override
    protected void turnOffBuff(LivingEntity entity) {
        lightfoots.remove(entity.getUniqueId());
    }

    @Override
    protected void turnOff() {
        lightfoots.clear();
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!(isActive(player))) {
            return;
        }
        if (isExpired(player)) {
            turnOff(player);
            return;
        }
        Material material = event.getClickedBlock().getType();
        if (event.getAction() != Action.PHYSICAL) return;
        if (material.toString().contains("PRESSURE_PLATE") || material == Material.TRIPWIRE) event.setCancelled(true);
    }
}
