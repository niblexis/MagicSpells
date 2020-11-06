package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.nisovin.magicspells.castmodifiers.Condition;

public class ShearedCondition extends Condition {

    @Override
    public boolean initialize(String var) {
        return false;
    }

    @Override
    public boolean check(LivingEntity livingEntity, LivingEntity target) {
        return check(target);
    }

    @Override
    public boolean check(LivingEntity livingEntity) {
        if (livingEntity.getType() != EntityType.SHEEP) return false;
        final Sheep sheep = (Sheep) livingEntity;
        return sheep.isSheared();
    }

    @Override
    public boolean check(LivingEntity livingEntity, Location location) {
        return false;
    }

}
