//
// Decompiled by Procyon v0.5.36
//

package com.nisovin.magicspells.spells.buff;

import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.bukkit.entity.Vehicle;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import java.util.HashSet;
import com.nisovin.magicspells.util.MagicConfig;
import java.util.UUID;
import java.util.Set;
import com.nisovin.magicspells.spells.BuffSpell;

public class VehicleSpeedSpell extends BuffSpell
{
    private Set<UUID> boosted;
    private float speed;
    private boolean addVelocityInstead;
    private boolean boostOutsideWater;
    private boolean boostInAir;

    public VehicleSpeedSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        boosted = new HashSet<>();
        speed = getConfigFloat("speed", 40.0f) / 10.0f;
        addVelocityInstead = getConfigBoolean("add-velocity-instead", false);
        boostOutsideWater = getConfigBoolean("boost-outside-water", false);
        boostInAir = getConfigBoolean("boost-in-air", false);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public boolean castBuff(LivingEntity entity, float power, String[] args) {
        if (!(entity instanceof Player)) {
            return true;
        }
        boosted.add(entity.getUniqueId());
        return true;
    }

    @Override
    public boolean isActive(LivingEntity entity) {
        return boosted.contains(entity.getUniqueId());
    }

    @Override
    protected void turnOffBuff(LivingEntity entity) {
        boosted.remove(entity.getUniqueId());
    }

    @Override
    protected void turnOff() {
        boosted.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVehicleMove(VehicleMoveEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (vehicle.getPassengers().isEmpty()) return;
        Entity entity = vehicle.getPassengers().get(0);
        if (entity.getType() != EntityType.PLAYER) return;
        Player player = (Player) entity;
        if (isExpired(player)) {
            turnOff(player);
            return;
        }
        if (!isActive(player)) return;
        if (!boostOutsideWater && vehicle.getLocation().getBlock().getType() != Material.WATER) return;
        if (!boostInAir && vehicle.getLocation().getBlock().getType() == Material.AIR) return;
        Vector v = vehicle.getLocation().getDirection().normalize().multiply(speed);
        v.setY(0);
        if (!addVelocityInstead) vehicle.setVelocity(v);
        else vehicle.setVelocity(vehicle.getVelocity().add(v));
    }
}
