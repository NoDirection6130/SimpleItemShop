package pl.nodirection6130.itemshop;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShopManager {

    private final ShopPlugin plugin;
    private final Map<String, Shop> shops = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public ShopManager(ShopPlugin plugin) {
        this.plugin = plugin;
        loadShops();
    }

    public static String locationKey(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public void registerShop(Shop shop) {
        shops.put(locationKey(shop.getChestLocation()), shop);
        saveShops();
    }

    public void removeShop(Location chestLocation) {
        shops.remove(locationKey(chestLocation));
        saveShops();
    }

    public Shop getShopByChest(Location loc) {
        return shops.get(locationKey(loc));
    }

    public boolean isShopChest(Location loc) {
        return shops.containsKey(locationKey(loc));
    }

    public Collection<Shop> getAllShops() {
        return shops.values();
    }

    private void loadShops() {
        dataFile = new File(plugin.getDataFolder(), "shops.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (!dataConfig.contains("shops")) return;

        for (String key : dataConfig.getConfigurationSection("shops").getKeys(false)) {
            String path = "shops." + key + ".";
            try {
                UUID ownerUUID = UUID.fromString(dataConfig.getString(path + "ownerUUID"));
                String ownerName = dataConfig.getString(path + "ownerName");
                String world = dataConfig.getString(path + "world");
                int x = dataConfig.getInt(path + "x");
                int y = dataConfig.getInt(path + "y");
                int z = dataConfig.getInt(path + "z");
                Material sellItem = Material.valueOf(dataConfig.getString(path + "sellItem"));
                int sellAmount = dataConfig.getInt(path + "sellAmount");
                Material priceItem = Material.valueOf(dataConfig.getString(path + "priceItem"));
                int priceAmount = dataConfig.getInt(path + "priceAmount");

                org.bukkit.World w = plugin.getServer().getWorld(world);
                if (w == null) continue;

                Location chestLoc = new Location(w, x, y, z);
                Shop shop = new Shop(ownerUUID, ownerName, chestLoc, sellItem, sellAmount, priceItem, priceAmount);
                shops.put(locationKey(chestLoc), shop);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load shop entry: " + key + " - " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + shops.size() + " shops.");
    }

    public void saveShops() {
        dataConfig.set("shops", null);
        int i = 0;
        for (Shop shop : shops.values()) {
            String path = "shops.shop" + i + ".";
            dataConfig.set(path + "ownerUUID", shop.getOwnerUUID().toString());
            dataConfig.set(path + "ownerName", shop.getOwnerName());
            dataConfig.set(path + "world", shop.getChestLocation().getWorld().getName());
            dataConfig.set(path + "x", shop.getChestLocation().getBlockX());
            dataConfig.set(path + "y", shop.getChestLocation().getBlockY());
            dataConfig.set(path + "z", shop.getChestLocation().getBlockZ());
            dataConfig.set(path + "sellItem", shop.getSellItem().name());
            dataConfig.set(path + "sellAmount", shop.getSellAmount());
            dataConfig.set(path + "priceItem", shop.getPriceItem().name());
            dataConfig.set(path + "priceAmount", shop.getPriceAmount());
            i++;
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
