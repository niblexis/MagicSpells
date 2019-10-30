package com.nisovin.magicspells.volatilecode.v1_13_R2

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.nisovin.magicspells.MagicSpells
import com.nisovin.magicspells.util.*
import com.nisovin.magicspells.util.compat.CompatBasics
import com.nisovin.magicspells.util.compat.EventUtil
import com.nisovin.magicspells.volatilecode.DisguiseManagerEmpty
import com.nisovin.magicspells.volatilecode.DisguiseManagerLibsDisguises
import com.nisovin.magicspells.volatilecode.VolatileCodeDisabled
import com.nisovin.magicspells.volatilecode.VolatileCodeHandle
import net.minecraft.server.v1_13_R2.*
import net.minecraft.server.v1_13_R2.Item
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.block.Block
import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.block.data.Powerable
import org.bukkit.craftbukkit.v1_13_R2.CraftServer
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld
import org.bukkit.craftbukkit.v1_13_R2.entity.*
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.util.Vector
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Field
import java.util.*

private typealias nmsItemStack = net.minecraft.server.v1_13_R2.ItemStack

class VolatileCode1_13_R2: VolatileCodeHandle {

    private var fallback = VolatileCodeDisabled()

    private val fireworks = ArrayList<EntityFireworks>()

    private var craftItemStackHandleField: Field? = null
    private var entityFallingBlockHurtEntitiesField: Field? = null
    private var entityFallingBlockFallHurtAmountField: Field? = null
    private var entityFallingBlockFallHurtMaxField: Field? = null
    private var craftMetaSkullClass: Class<*>? = null
    private var craftMetaSkullProfileField: Field? = null

    init {
        try {
            this.craftItemStackHandleField = CraftItemStack::class.java.getDeclaredField("handle")
            this.craftItemStackHandleField!!.isAccessible = true

            this.entityFallingBlockHurtEntitiesField = EntityFallingBlock::class.java.getDeclaredField("hurtEntities")
            this.entityFallingBlockHurtEntitiesField!!.isAccessible = true

            this.entityFallingBlockFallHurtAmountField = EntityFallingBlock::class.java.getDeclaredField("fallHurtAmount")
            this.entityFallingBlockFallHurtAmountField!!.isAccessible = true

            this.entityFallingBlockFallHurtMaxField = EntityFallingBlock::class.java.getDeclaredField("fallHurtMax")
            this.entityFallingBlockFallHurtMaxField!!.isAccessible = true

            this.craftMetaSkullClass = Class.forName("org.bukkit.craftbukkit.v1_13_R2.inventory.CraftMetaSkull")
            this.craftMetaSkullProfileField = this.craftMetaSkullClass!!.getDeclaredField("profile")
            this.craftMetaSkullProfileField!!.isAccessible = true
        } catch (e: Exception) {
            MagicSpells.error("THIS OCCURRED WHEN CREATING THE VOLATILE CODE HANDLE FOR 1.13, THE FOLLOWING ERROR IS MOST LIKELY USEFUL IF YOU'RE RUNNING THE LATEST VERSION OF MAGICSPELLS.")
            e.printStackTrace()
        }
    }

    private fun org.bukkit.inventory.ItemStack.asCraftCopy(): CraftItemStack {
        return CraftItemStack.asCraftCopy(this)
    }

    private fun getTag(item: ItemStack): NBTTagCompound? {
        // Don't spam the user with errors, just stop
        if (SafetyCheckUtils.areAnyNull(this.craftItemStackHandleField)) return null

        if (item is CraftItemStack) {
            try {
                return (this.craftItemStackHandleField!!.get(item) as nmsItemStack).tag
            } catch (e: Exception) {
                // No op currently
            }

        }
        return null
    }

    private fun setTag(item: ItemStack, tag: NBTTagCompound): ItemStack {
        val craftItem: CraftItemStack = if (item is CraftItemStack) {
            item
        } else {
            item.asCraftCopy()
        }

        var nmsItem: nmsItemStack? = null
        try {
            nmsItem = this.craftItemStackHandleField!!.get(item) as nmsItemStack
        } catch (e: Exception) {
            // No op currently
        }

        if (nmsItem == null) nmsItem = CraftItemStack.asNMSCopy(craftItem)

        if (nmsItem != null) {
            nmsItem.tag = tag
            try {
                this.craftItemStackHandleField!!.set(craftItem, nmsItem)
            } catch (e: Exception) {
                // No op currently
            }

        }

        return craftItem
    }

    override fun addPotionGraphicalEffect(entity: LivingEntity, color: Int, duration: Int) {
        /*final EntityLiving el = ((CraftLivingEntity)entity).getHandle();
        final DataWatcher dw = el.getDataWatcher();
        dw.watch(7, Integer.valueOf(color));

        if (duration > 0) {
            MagicSpells.scheduleDelayedTask(new Runnable() {
                public void run() {
                    int c = 0;
                    if (!el.effects.isEmpty()) {
                        c = net.minecraft.server.v1_12_R1.PotionBrewer.a(el.effects.values());
                    }
                    dw.watch(7, Integer.valueOf(c));
                }
            }, duration);
        }*/
    }

    override fun entityPathTo(creature: LivingEntity, target: LivingEntity) {
        //EntityCreature entity = ((CraftCreature)creature).getHandle();
        //entity.pathEntity = entity.world.findPath(entity, ((CraftLivingEntity)target).getHandle(), 16.0F, true, false, false, false);
    }

    override fun creaturePathToLoc(creature: Creature, loc: Location, speed: Float) {
        val entity = (creature as CraftCreature).handle
        val pathEntity = entity.navigation.a(loc.x, loc.y, loc.z)
        entity.navigation.a(pathEntity, speed.toDouble())
    }

    override fun sendFakeSlotUpdate(player: Player, slot: Int, item: ItemStack?) {
        val nmsItem: nmsItemStack?
        if (item != null) {
            nmsItem = CraftItemStack.asNMSCopy(item)
        } else {
            nmsItem = null
        }
        val packet = PacketPlayOutSetSlot(0, slot.toShort() + 36, nmsItem!!)
        (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
    }

    override fun toggleLeverOrButton(block: Block) {
        val powerable = block.blockData as Powerable
        powerable.isPowered = true
        block.setBlockData(powerable, true)
    }

    override fun pressPressurePlate(block: Block) {
        if (block.type == Material.HEAVY_WEIGHTED_PRESSURE_PLATE || block.type == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            val powerable = block.blockData as AnaloguePowerable
            powerable.power = powerable.maximumPower
            block.setBlockData(powerable, true)
            return
        }
        val powerable = block.blockData as Powerable
        powerable.isPowered = true
        block.setBlockData(powerable, true)
    }

    override fun simulateTnt(target: Location, source: LivingEntity, explosionSize: Float, fire: Boolean): Boolean {
        val e = EntityTNTPrimed((target.world as CraftWorld).handle, target.x, target.y, target.z, (source as CraftLivingEntity).handle)
        val c = CraftTNTPrimed(Bukkit.getServer() as CraftServer, e)
        val event = ExplosionPrimeEvent(c, explosionSize, fire)
        EventUtil.call(event)
        return event.isCancelled
    }

    override fun createExplosionByPlayer(player: Player, location: Location, size: Float, fire: Boolean, breakBlocks: Boolean): Boolean {
        return !(location.world as CraftWorld).handle.createExplosion((player as CraftPlayer).handle, location.x, location.y, location.z, size, fire, breakBlocks).wasCanceled
    }

    override fun playExplosionEffect(location: Location, size: Float) {
        val packet = PacketPlayOutExplosion(location.x, location.y, location.z, size, ArrayList(), null)
        for (player in location.world!!.players) {
            if (LocationUtil.distanceGreaterThan(player, location, 50.0)) {
                (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
            }
        }
    }

    override fun setExperienceBar(player: Player, level: Int, percent: Float) {
        val packet = PacketPlayOutExperience(percent, player.totalExperience, level)
        (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
    }

    override fun shootSmallFireball(player: Player): Fireball {
        val w = (player.world as CraftWorld).handle
        val playerLoc = player.location
        val loc = player.eyeLocation.toVector().add(player.location.direction.multiply(10))

        val d0 = loc.x - playerLoc.x
        val d1 = loc.y - (playerLoc.y + 1.5)
        val d2 = loc.z - playerLoc.z
        val entitysmallfireball = EntitySmallFireball(w, (player as CraftPlayer).handle, d0, d1, d2)

        entitysmallfireball.locY = playerLoc.y + 1.5
        w.addEntity(entitysmallfireball)

        return entitysmallfireball.bukkitEntity as Fireball
    }

    override fun setTarget(entity: LivingEntity?, target: LivingEntity?) {
        if (entity is Creature) {
            entity.target = target
        } else {
            ((entity as CraftLivingEntity).handle as EntityInsentient).setGoalTarget((target as CraftLivingEntity).handle, EntityTargetEvent.TargetReason.CUSTOM, true)
        }
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        for (player in location.world!!.players) {
            playSound(player, location, sound, volume, pitch)
        }
    }

    override fun playSound(player: Player, sound: String, volume: Float, pitch: Float) {
        playSound(player, player.location, sound, volume, pitch)
    }

    private fun playSound(player: Player, loc: Location, sound: String, volume: Float, pitch: Float) {
        player.playSound(loc, sound, volume, pitch)
        //PacketPlayOutCustomSoundEffect packet = new PacketPlayOutCustomSoundEffect(sound, SoundCategory.MASTER, loc.getX(), loc.getY(), loc.getZ(), volume, pitch);
        //((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }

    override fun addFakeEnchantment(item: ItemStack): ItemStack {
        var item = item
        if (item !is CraftItemStack) item = item.asCraftCopy()
        var tag = getTag(item)
        if (tag == null) tag = NBTTagCompound()
        if (!tag.hasKey("ench")) tag.set("ench", NBTTagList())
        return setTag(item, tag)
    }

    override fun setFallingBlockHurtEntities(block: FallingBlock, damage: Float, max: Int) {
        val efb = (block as CraftFallingBlock).handle
        try {
            this.entityFallingBlockHurtEntitiesField!!.setBoolean(efb, true)
            this.entityFallingBlockFallHurtAmountField!!.setFloat(efb, damage)
            this.entityFallingBlockFallHurtMaxField!!.setInt(efb, max)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun createFireworksExplosion(location: Location, flicker: Boolean, trail: Boolean, type: Int, colors: IntArray, fadeColors: IntArray, flightDuration: Int) {
        // Create item
        val item = CraftItemStack.asNMSCopy(ItemStack(Material.FIREWORK_ROCKET))

        // Get tag
        var tag = item.tag
        if (tag == null) tag = NBTTagCompound()

        // Create explosion tag
        val explTag = NBTTagCompound()
        explTag.setByte("Flicker", if (flicker) 1.toByte() else 0.toByte())
        explTag.setByte("Trail", if (trail) 1.toByte() else 0.toByte())
        explTag.setByte("Type", type.toByte())
        explTag.setIntArray("Colors", colors)
        explTag.setIntArray("FadeColors", fadeColors)

        // Create fireworks tag
        val fwTag = NBTTagCompound()
        fwTag.setByte("Flight", flightDuration.toByte())
        val explList = NBTTagList()
        explList.add(explTag)
        fwTag.set("Explosions", explList)
        tag.set("Fireworks", fwTag)

        // Set tag
        item.tag = tag

        val world = (location.world as CraftWorld).handle

        // Create fireworks entity
        val firework = EntityFireworks((location.world as CraftWorld).handle, location.x, location.y, location.z, item)
        (location.world as CraftWorld).handle.addEntity(firework)
        fireworks.add(firework)

        // Cause explosion
        if (flightDuration == 0) {
            (location.world as CraftWorld).handle.broadcastEntityEffect(firework, 17.toByte())
            firework.die()
        } else {
            MagicSpells.scheduleDelayedTask({
                world.broadcastEntityEffect(firework, 17.toByte())
                fireworks.remove(firework)
                firework.die()
            }, flightDuration)
        }
    }

    override fun playParticleEffect(location: Location, name: String, spreadHoriz: Float, spreadVert: Float, speed: Float, count: Int, radius: Int, yOffset: Float) {
        playParticleEffect(location, name, spreadHoriz, spreadVert, spreadHoriz, speed, count, radius, yOffset)
    }

    override fun playParticleEffect(location: Location, name: String, spreadX: Float, spreadY: Float, spreadZ: Float, speed: Float, count: Int, radius: Int, yOffset: Float) {
        location.world!!.spawnParticle(ParticleUtil.ParticleEffect.getParticle(name), location.add(0.0, yOffset as Double, 0.0), count, spreadX as Double, spreadY as Double, spreadZ as Double, speed)
    }

    override fun playDragonDeathEffect(location: Location) {
        val dragon = EntityEnderDragon((location.world as CraftWorld).handle)
        dragon.setPositionRotation(location.x, location.y, location.z, location.yaw, 0f)

        val packet24 = PacketPlayOutSpawnEntityLiving(dragon)
        val packet38 = PacketPlayOutEntityStatus(dragon, 3.toByte())
        val packet29 = PacketPlayOutEntityDestroy(dragon.bukkitEntity.entityId)

        val box = BoundingBox(location, 64.0)
        val players = ArrayList<Player>()
        for (player in location.world!!.players) {
            if (!box.contains(player)) continue
            players.add(player)
            (player as CraftPlayer).handle.playerConnection.sendPacket(packet24)
            player.handle.playerConnection.sendPacket(packet38)
        }

        MagicSpells.scheduleDelayedTask({
            for (player in players) {
                if (player.isValid) {
                    (player as CraftPlayer).handle.playerConnection.sendPacket(packet29)
                }
            }
        }, 250)
    }

    override fun setKiller(entity: LivingEntity, killer: Player) {
        (entity as CraftLivingEntity).handle.killer = (killer as CraftPlayer).handle
    }

    override fun getDisguiseManager(config: MagicConfig): IDisguiseManager {
        if (CompatBasics.pluginEnabled("LibsDisguises")) {
            return try {
                DisguiseManagerLibsDisguises(config)
            } catch (e: Exception) {
                DisguiseManagerEmpty(config)
            }

        }
        return DisguiseManagerEmpty(config)
    }

    override fun addAttributes(item: ItemStack, names: Array<String>, types: Array<String>, amounts: DoubleArray, operations: IntArray, slots: Array<String>): ItemStack {
        var item = item
        if (item !is CraftItemStack) item = item.asCraftCopy()
        val tag = getTag(item)

        val list = NBTTagList()
        for (i in names.indices) {
            if (names[i] == null) continue
            val uuid = UUID.randomUUID()
            val attr = buildAttributeTag(names[i], types[i], amounts[i], operations[i], uuid, slots[i])
            list.add(attr)
        }

        tag!!.set("AttributeModifiers", list)

        setTag(item, tag)
        return item
    }

    private fun buildAttributeTag(name: String, attributeName: String, amount: Double, operation: Int, uuid: UUID, slot: String?): NBTTagCompound {
        val tag = NBTTagCompound()

        tag.setString("Name", name)
        tag.setString("AttributeName", attributeName)
        tag.setDouble("Amount", amount)
        tag.setInt("Operation", operation)
        tag.setLong("UUIDLeast", uuid.leastSignificantBits)
        tag.setLong("UUIDMost", uuid.mostSignificantBits)
        if (slot != null) tag.setString("Slot", slot)

        return tag
    }

    override fun hideTooltipCrap(item: ItemStack): ItemStack {
        val meta = item.itemMeta!!
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS)
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON)
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
        item.itemMeta = meta
        return item
    }

    override fun addEntityAttribute(entity: LivingEntity, attribute: String, amount: Double, operation: Int) {
        var attr: Attribute? = null
        when (attribute) {
            "generic.maxHealth" -> attr = Attribute.GENERIC_MAX_HEALTH
            "generic.followRange" -> attr = Attribute.GENERIC_FOLLOW_RANGE
            "generic.knockbackResistance" -> attr = Attribute.GENERIC_KNOCKBACK_RESISTANCE
            "generic.movementSpeed" -> attr = Attribute.GENERIC_MOVEMENT_SPEED
            "generic.attackDamage" -> attr = Attribute.GENERIC_ATTACK_DAMAGE
            "generic.attackSpeed" -> attr = Attribute.GENERIC_ATTACK_SPEED
            "generic.armor" -> attr = Attribute.GENERIC_ARMOR
            "generic.luck" -> attr = Attribute.GENERIC_LUCK
        }

        var oper: AttributeModifier.Operation? = null
        when (operation) {
            0 -> oper = AttributeModifier.Operation.ADD_NUMBER
            1 -> oper = AttributeModifier.Operation.MULTIPLY_SCALAR_1
            2 -> oper = AttributeModifier.Operation.ADD_SCALAR
        }
        if (attr != null && oper != null) {
            entity.getAttribute(attr)!!.addModifier(AttributeModifier("MagicSpells $attribute", amount, oper))
        }
    }

    override fun removeAI(entity: LivingEntity) {
        try {
            val ev = (entity as CraftLivingEntity).handle as EntityInsentient

            // TODO this field should be calculated only once
            val goalsField = EntityInsentient::class.java.getDeclaredField("goalSelector")
            goalsField.isAccessible = true
            val goals = goalsField.get(ev) as PathfinderGoalSelector

            // TODO this field should be calculated only once
            var listField = PathfinderGoalSelector::class.java.getDeclaredField("b")
            listField.isAccessible = true
            var list: MutableSet<*> = listField.get(goals) as MutableSet<*>
            list.clear()
            listField = PathfinderGoalSelector::class.java.getDeclaredField("c")
            listField.isAccessible = true
            list = listField.get(goals) as MutableSet<*>
            list.clear()

            goals.a(0, PathfinderGoalFloat(ev))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun addAILookAtPlayer(entity: LivingEntity, range: Int) {
        try {
            val ev = (entity as CraftLivingEntity).handle as EntityInsentient

            val goalsField = EntityInsentient::class.java.getDeclaredField("goalSelector")
            goalsField.isAccessible = true
            val goals = goalsField.get(ev) as PathfinderGoalSelector

            goals.a(1, PathfinderGoalLookAtPlayer(ev, EntityHuman::class.java, range.toFloat(), 1.0f))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /*private void updateBossBarEntity(Player player, String title, double percent) {
        if (title != null) {
            if (percent <= 0.01) percent = 0.01D;
            bossBarEntity.setCustomName(ChatColor.translateAlternateColorCodes('&', title));
            bossBarEntity.getDataWatcher().watch(6, (float)(percent * 300f));
        }

        Location l = player.getLocation();
        l.setPitch(l.getPitch() + 10);
        Vector v = l.getDirection().multiply(20);
        Util.rotateVector(v, 15);
        l.add(v);
        bossBarEntity.setLocation(l.getX(), l.getY(), l.getZ(), 0, 0);
    }*/


    override fun saveSkinData(player: Player, name: String) {
        val profile = (player as CraftPlayer).handle.profile
        val props = profile.properties.get("textures")
        for (prop in props) {
            val skin = prop.value
            val sig = prop.signature

            val folder = File(MagicSpells.getInstance().dataFolder, "disguiseskins")
            if (!folder.exists()) folder.mkdir()
            val skinFile = File(folder, "$name.skin.txt")
            val sigFile = File(folder, "$name.sig.txt")
            try {
                var writer = FileWriter(skinFile)
                writer.write(skin)
                writer.flush()
                writer.close()
                writer = FileWriter(sigFile)
                writer.write(sig)
                writer.flush()
                writer.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            break
        }
    }

    override fun setUnbreakable(item: ItemStack): ItemStack {
        val meta = item.itemMeta!!
        meta.isUnbreakable = true
        //meta.spigot().setUnbreakable(true);
        item.itemMeta = meta
        return item
    }

    override fun sendTitleToPlayer(player: Player, title: String?, subtitle: String?, fadeIn: Int, stay: Int, fadeOut: Int) {
        val conn = (player as CraftPlayer).handle.playerConnection
        var packet = PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, fadeIn, stay, fadeOut)
        conn.sendPacket(packet)
        if (title != null) {
            packet = PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, ChatComponentText(title))
            conn.sendPacket(packet)
        }
        if (subtitle != null) {
            packet = PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, ChatComponentText(subtitle))
            conn.sendPacket(packet)
        }
    }

    override fun sendActionBarMessage(player: Player, message: String) {
        val conn = (player as CraftPlayer).handle.playerConnection
        val packet = PacketPlayOutChat(ChatComponentText(message), ChatMessageType.GAME_INFO)
        conn.sendPacket(packet)
    }

    override fun setClientVelocity(player: Player, velocity: Vector) {
        val packet = PacketPlayOutEntityVelocity(player.entityId, velocity.x, velocity.y, velocity.z)
        (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
    }

    override fun getAbsorptionHearts(entity: LivingEntity): Double {
        return (entity as CraftLivingEntity).handle.absorptionHearts.toDouble()
    }

    override fun showItemCooldown(player: Player, item: ItemStack, duration: Int) {
        val packet = PacketPlayOutSetCooldown(Item.getById(item.type.id), duration)
        (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
    }

    override fun setTexture(meta: SkullMeta, texture: String, signature: String) {
        // Don't spam the user with errors, just stop
        if (SafetyCheckUtils.areAnyNull(this.craftMetaSkullProfileField)) return

        try {
            val profile = this.craftMetaSkullProfileField!!.get(meta) as GameProfile
            setTexture(profile, texture, signature)
            this.craftMetaSkullProfileField!!.set(meta, profile)
        } catch (e: SecurityException) {
            MagicSpells.handleException(e)
        } catch (e: IllegalArgumentException) {
            MagicSpells.handleException(e)
        } catch (e: IllegalAccessException) {
            MagicSpells.handleException(e)
        }

    }

    override fun setSkin(player: Player, skin: String, signature: String) {
        val craftPlayer = player as CraftPlayer
        setTexture(craftPlayer.profile, skin, signature)
    }

    private fun setTexture(profile: GameProfile, texture: String, signature: String?): GameProfile {
        if (signature == null || signature.isEmpty()) {
            profile.properties.put("textures", Property("textures", texture))
        } else {
            profile.properties.put("textures", Property("textures", texture, signature))
        }
        return profile
    }

    override fun setTexture(meta: SkullMeta, texture: String, signature: String, uuid: String?, name: String) {
        // Don't spam the user with errors, just stop
        if (SafetyCheckUtils.areAnyNull(this.craftMetaSkullProfileField)) return

        try {
            val profile = GameProfile(if (uuid != null) UUID.fromString(uuid) else null, name)
            setTexture(profile, texture, signature)
            this.craftMetaSkullProfileField!!.set(meta, profile)
        } catch (e: SecurityException) {
            MagicSpells.handleException(e)
        } catch (e: IllegalArgumentException) {
            MagicSpells.handleException(e)
        } catch (e: IllegalAccessException) {
            MagicSpells.handleException(e)
        }

    }

    override fun turnOff() {
        for (entity in fireworks) {
            entity.die()
        }

        fireworks.clear()
    }
}