package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.util.compat.CompatBasics;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class InRegionCondition extends Condition {

	private WorldGuardPlugin worldGuard;
	private String worldName;
	private String regionName;
	private ProtectedRegion region;

	@Override
	public boolean initialize(String var) {
		if (var == null) return false;

		worldGuard = (WorldGuardPlugin) CompatBasics.getPlugin("WorldGuard");
		if (worldGuard == null || !worldGuard.isEnabled()) return false;

		String[] split = var.split(":");
		if (split.length == 2) {
			worldName = split[0];
			regionName = split[1];
			return true;
		}
		return false;
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		return check(livingEntity, livingEntity.getLocation());
	}

	@Override
	public boolean check(LivingEntity livingEntity, LivingEntity target) {
		return check(target, target.getLocation());
	}

	@Override
	public boolean check(LivingEntity livingEntity, Location location) {
		World world = Bukkit.getWorld(worldName);
		if (world == null) return false;
		if (world != location.getWorld()) return false;
		com.sk89q.worldedit.world.World aWorld = BukkitAdapter.adapt(world);
		RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regionManager = regionContainer.get(aWorld);
		if (regionManager == null) return false;
		ProtectedRegion region = regionManager.getRegion(this.regionName);
		return region != null && region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

}
