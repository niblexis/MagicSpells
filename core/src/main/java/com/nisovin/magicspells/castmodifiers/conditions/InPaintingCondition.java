//
// Decompiled by Procyon v0.5.36
//

package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import java.util.Iterator;
import java.util.List;
import org.bukkit.entity.Painting;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.HashSet;
import org.bukkit.Art;
import java.util.Set;
import com.nisovin.magicspells.castmodifiers.Condition;

public class InPaintingCondition extends Condition
{
    Set<Art> arts;
    Art art;

    @Override
    public boolean initialize(String var) {
        if (var.contains(",")) {
            this.arts = new HashSet<>();
            String[] split = var.split(",");
            for (String s : split) {
                Art a = Art.getByName(s.toUpperCase());
                if (a == null) return false;
                this.arts.add(a);
            }
            return true;
        }
        this.art = Art.getByName(var);
        return this.art != null;
    }

    @Override
    public boolean check(LivingEntity livingEntity) {
        List<Entity> entities = (List<Entity>) livingEntity.getNearbyEntities(0.0, 0.0, 0.0);
        for (Entity entity : entities) {
            if (entity.getType() != EntityType.PAINTING) continue;
            Painting painting = (Painting)entity;
            if (this.art != null) return painting.getArt() == this.art;
            if (!this.arts.contains(painting.getArt())) return false;
            for (Art art : this.arts) {
                if (art.equals(painting.getArt())) return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean check(LivingEntity livingEntity, LivingEntity target) {
        return check(livingEntity);
    }

    @Override
    public boolean check(LivingEntity livingEntity, Location location) {
        return false;
    }
}
