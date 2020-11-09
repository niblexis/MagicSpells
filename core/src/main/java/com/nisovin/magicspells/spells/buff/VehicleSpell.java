//
// Decompiled by Procyon v0.5.36
//

package com.nisovin.magicspells.spells.buff;

import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import java.util.HashMap;
import com.nisovin.magicspells.util.MagicConfig;
import org.bukkit.entity.Vehicle;
import java.util.UUID;
import java.util.Map;
import com.nisovin.magicspells.spells.BuffSpell;

public class VehicleSpell extends BuffSpell
{
    private Map<UUID, Vehicle> vehicles;
    private Map<Vehicle, UUID> players;
    private String vehicleTypeStr;
    private boolean despawnOnDismount;

    public VehicleSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        players = new HashMap<Vehicle, UUID>();
        vehicles = new HashMap<UUID, Vehicle>();
        vehicleTypeStr = getConfigString("vehicle-type", "boat").toUpperCase();
        despawnOnDismount = getConfigBoolean("despawn-on-dismount", true);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public boolean castBuff(LivingEntity entity, float power, String[] args) {
        if (!(entity instanceof Player)) return true;
        Player player = (Player)entity;
        EntityType vehicleType = Util.getEntityType(vehicleTypeStr);
        if (vehicleType == null) return false;
        Vehicle vehicle = (Vehicle)player.getWorld().spawnEntity(player.getLocation(), vehicleType);
        if (!(vehicle instanceof Vehicle)) {
            vehicle.remove();
            MagicSpells.error("vehicleSpell '" + internalName + "' can only summon vehicles!");
            return false;
        }
        vehicles.put(player.getUniqueId(), vehicle);
        players.put(vehicle, player.getUniqueId());
        vehicle.addPassenger(player);
        return true;
    }

    @Override
    public boolean isActive(LivingEntity entity) {
        return vehicles.containsKey(entity.getUniqueId());
    }

    public boolean isVehicle(Entity entity) {
        return vehicles.containsValue(entity);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDestroyVehicle(VehicleExitEvent event) {
        if (!despawnOnDismount) return;
        if (!isVehicle(event.getVehicle())) return;
        Player pl = Bukkit.getPlayer(players.get(event.getVehicle()));
        if (pl == null || !pl.isValid() || !pl.isOnline()) return;
        turnOffBuff(pl);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isActive(player)) return;
        if (isExpired(player)) turnOff(player);
    }

    public void turnOffBuff(LivingEntity entity) {
        Vehicle vehicle = vehicles.remove(entity.getUniqueId());
        if (vehicle != null && !vehicle.isDead()) vehicle.remove();
        players.remove(vehicle);
    }

    @Override
    protected void turnOff() {
        Util.forEachValueOrdered(vehicles, Entity::remove);
        vehicles.clear();
        players.clear();
    }
}
