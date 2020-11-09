package com.nisovin.magicspells.spells.passive;

import com.nisovin.magicspells.spells.passive.util.PassiveListener;
import com.nisovin.magicspells.util.OverridePriority;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener extends PassiveListener {

    @Override
    public void initialize(String var) {
    }

    @OverridePriority
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(!hasSpell(player)) return;

        if (!isCancelStateOk(event.isCancelled())) return;
        boolean casted = passiveSpell.activate(player);
        if (!cancelDefaultAction(casted)) return;
        event.setCancelled(true);
    }
}
