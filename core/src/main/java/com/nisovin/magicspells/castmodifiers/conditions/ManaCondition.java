package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.mana.ManaHandler;
import com.nisovin.magicspells.castmodifiers.Condition;

public class ManaCondition extends Condition {

	private ManaHandler mana;

	private int amount;
	private boolean percent = false;

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

		String number = var.substring(1);

		mana = MagicSpells.getManaHandler();
		if (mana == null) return false;

		try {
			if (number.endsWith("%")) {
				percent = true;
				number = number.replace("%", "");
			}
			amount = Integer.parseInt(number);
			return true;
		} catch (NumberFormatException e) {
			DebugHandler.debugNumberFormat(e);
			return false;
		}
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		return mana(livingEntity);
	}

	@Override
	public boolean check(LivingEntity livingEntity, LivingEntity target) {
		return mana(target);
	}

	@Override
	public boolean check(LivingEntity livingEntity, Location location) {
		return false;
	}

	private boolean mana(LivingEntity livingEntity) {
		if (!(livingEntity instanceof Player)) return false;
		double currentMana = mana.getMana((Player) livingEntity);
		double percentMana = currentMana / mana.getMaxMana((Player) livingEntity) * 100;
		if (equals) {
			if (percent) return percentMana == amount;
			return currentMana == amount;
		} else if (moreThan) {
			if (percent) return percentMana > amount;
			return currentMana > amount;
		} else if (lessThan) {
			if (percent) return percentMana < amount;
			return currentMana < amount;
		}
		return false;
	}

}
