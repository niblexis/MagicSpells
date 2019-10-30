package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.mana.ManaHandler;
import com.nisovin.magicspells.castmodifiers.Condition;

public class MaxManaCondition extends Condition {

    private ManaHandler mana;

    private int amount = 0;

    private boolean equals;
    private boolean moreThan;
    private boolean lessThan;

    @Override
    public boolean setVar(String var) {
        if (var.length() < 2) {
            return false;
        }

        switch (var.charAt(0)) {
            case '=':
            case ':':
                equals = true;
                break;
            case '>':
                moreThan = true;
                break;
            case '<':
                lessThan = true;
                break;
        }

        mana = MagicSpells.getManaHandler();
        if (mana == null) return false;

        try {
            amount = Integer.parseInt(var.substring(1));
            return true;
        } catch (NumberFormatException e) {
            DebugHandler.debugNumberFormat(e);
            return false;
        }
    }

    @Override
    public boolean check(LivingEntity livingEntity) {
        return maxMana(livingEntity);
    }

    @Override
    public boolean check(LivingEntity livingEntity, LivingEntity target) {
        return maxMana(target);
    }

    @Override
    public boolean check(LivingEntity livingEntity, Location location) {
        return false;
    }

    private boolean maxMana(LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player)) return false;
        if (equals) return mana.getMaxMana((Player) livingEntity) == amount;
        else if (moreThan) return mana.getMaxMana((Player) livingEntity) > amount;
        else if (lessThan) return mana.getMaxMana((Player) livingEntity) < amount;
        return false;
    }

}