package com.nisovin.magicspells.spells.instant;

import com.nisovin.magicspells.util.magicitems.MagicItems;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.InventoryView;
import org.bukkit.event.inventory.InventoryClickEvent;
import java.io.IOException;
import org.bukkit.inventory.Inventory;
import org.bukkit.Bukkit;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import org.bukkit.entity.Player;
import com.nisovin.magicspells.MagicSpells;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import com.nisovin.magicspells.util.MagicConfig;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import com.nisovin.magicspells.spells.InstantSpell;

public class StorageSpell extends InstantSpell {

    private String title;
    private int rows;
    private File storageFile;
    private YamlConfiguration storageConfig;
    private List<String> staticSlots;
    private List<String> prefilledItems;
    private List<String> allowedItemsStr;
    private List<Material> allowedItems;
    private int saveInterval;
    private boolean saveContents;
    private ItemStack[] storedItems;
    private String strCantStoreItem;

    public StorageSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        title = ChatColor.translateAlternateColorCodes('&', getConfigString("title", "Window Title " + spellName));
        rows = getConfigInt("rows", 1);
        if (rows > 6) {
            rows = 6;
        }
        staticSlots = getConfigStringList("static-slots", null);
        prefilledItems = getConfigStringList("prefilled-items", null);
        allowedItemsStr = getConfigStringList("allowed-items", null);
        saveInterval = getConfigInt("save-delay", 0);
        saveContents = getConfigBoolean("save-contents", true);
        strCantStoreItem = ChatColor.translateAlternateColorCodes('&', getConfigString("str-cant-store", "You are not able to store item!"));
        allowedItems = new ArrayList<>();
        if (allowedItemsStr != null) {
            for (String s : allowedItemsStr) {
                ItemStack item = MagicItems.getMagicItemFromString(s).getItemStack();
                if (item == null) MagicSpells.error("StorageSpell " + internalName + " has an invalid item defined " + item);
                allowedItems.add(item.getType());
            }
        }
    }

    @Override
    public PostCastAction castSpell(LivingEntity livingEntity, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL) {
            openStorageInventory((Player) livingEntity);
            playSpellEffects(EffectPosition.CASTER, livingEntity);
        }
        return PostCastAction.HANDLE_NORMALLY;
    }

    private void openStorageInventory(Player caster) {
        Inventory storageInv = Bukkit.createInventory(caster, rows * 9, title);
        storageFile = new File(MagicSpells.plugin.getDataFolder().getAbsolutePath() + "\\storages\\", caster.getUniqueId() + ".yml");
        if (storageFile.exists()) storageInv = loadInventory(storageInv, caster);
        if (prefilledItems != null) storageInv = processItemsAndSlots(prefilledItems, storageInv);
        if (staticSlots != null) storageInv = processItemsAndSlots(staticSlots, storageInv);
        caster.openInventory(storageInv);
    }

    private Inventory processItemsAndSlots(List<String> list, Inventory inventory) {
        for (String itemString : list) {
            if (!itemString.contains(":")) {
                continue;
            }
            String[] itemSplit = itemString.split(":");
            if (itemSplit[1] == null) continue;
            if (MagicItems.getMagicItemFromString(itemSplit[1]).getItemStack() == null) continue;
            int slot = -1;
            try {
                slot = Integer.parseInt(itemSplit[0]);
            }
            catch (NumberFormatException e) {
                MagicSpells.error("StorageSpell '" + internalName + "' has invalid slot given");
            }
            if (MagicItems.getMagicItemFromString(itemSplit[1]) == null) continue;
            ItemStack item = MagicItems.getMagicItemFromString(itemSplit[1]).getItemStack();
            inventory.setItem(slot, item);
        }
        return inventory;
    }

    private List<Integer> getSlots(List<String> list) {
        List<Integer> slots = new ArrayList<>();
        for (String string : list) {
            if (!string.contains(":")) continue;
            String[] stringArray = string.split(":");
            try {
                slots.add(Integer.parseInt(stringArray[0]));
            }
            catch (NumberFormatException e) {
                MagicSpells.error("StorageSpell '" + internalName + "' has invalid slot given");
            }
        }
        return slots;
    }

    private void saveInventory(Inventory inventory, Player player) {
        storageFile = new File(MagicSpells.plugin.getDataFolder().getAbsolutePath() + "\\storages\\", player.getUniqueId() + ".yml");
        if (!storageFile.exists()) {
            storageFile.getParentFile().mkdirs();
            try {
                storageFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        storageConfig = YamlConfiguration.loadConfiguration(storageFile);
        if (staticSlots != null) {
            List<Integer> staticSlotsSlots = getSlots(staticSlots);
            for (int slot : staticSlotsSlots) inventory.setItem(slot, new ItemStack(Material.AIR));
        }
        storedItems = inventory.getContents();
        storageConfig.set(internalName, storedItems);
        saveFile(storageFile, storageConfig);
    }

    private void saveFile(File file, YamlConfiguration config) {
        try {
            config.save(file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Inventory loadInventory(Inventory inventory, Player player) {
        storageFile = new File(MagicSpells.plugin.getDataFolder().getAbsolutePath() + "\\storages\\", player.getUniqueId() + ".yml");
        storageConfig = YamlConfiguration.loadConfiguration(storageFile);
        if (storageConfig.get(internalName) == null) return inventory;
        List<ItemStack> loadedItemsList = (List<ItemStack>) storageConfig.get(internalName);
        if (loadedItemsList == null) return inventory;
        ItemStack[] loadedItems = loadedItemsList.toArray(new ItemStack[0]);
        inventory.setContents(loadedItems);
        return inventory;
    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        InventoryView view = event.getView();
        Player player = (Player)event.getWhoClicked();
        if (!view.getTitle().equals(title)) return;
        if (!allowedItems.isEmpty() && !allowedItems.contains(event.getCurrentItem().getType())) {
            event.setCancelled(true);
            if (!strCantStoreItem.equals("")) player.sendMessage(strCantStoreItem);
        }
        if (staticSlots == null || staticSlots.isEmpty()) return;
        for (int slot : getSlots(staticSlots)) {
            if (event.getRawSlot() != event.getSlot()) continue;
            if (event.getSlot() != slot) continue;
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        InventoryView view = event.getView();
        Inventory inventory = event.getInventory();
        Player player = (Player)event.getPlayer();
        if (!view.getTitle().equals(title)) return;
        if (saveContents) saveInventory(inventory, player);
    }
}
