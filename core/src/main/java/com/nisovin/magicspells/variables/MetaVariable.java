package com.nisovin.magicspells.variables;

public abstract class MetaVariable extends Variable {

	public MetaVariable() {
		permanent = false;
		expBar = false;
		bossbarTitle = null;
		objective = null;
		minValue = Double.MIN_VALUE;
	}
	
	@Override
	protected void init() {
		permanent = false;
	}

	@Override
	public void set(String player, double amount) {
		// No op
	}
	
	@Override
	public void reset(String player) {
		// No op
	}

}
