package pl.nodirection6130.itemshop;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

public class Shop {

    private final UUID ownerUUID;
    private final String ownerName;
    private final Location chestLocation;
    private final Material sellItem;
    private final int sellAmount;
    private final Material priceItem;
    private final int priceAmount;

    public Shop(UUID ownerUUID, String ownerName, Location chestLocation,
                Material sellItem, int sellAmount, Material priceItem, int priceAmount) {
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.chestLocation = chestLocation;
        this.sellItem = sellItem;
        this.sellAmount = sellAmount;
        this.priceItem = priceItem;
        this.priceAmount = priceAmount;
    }

    public UUID getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public Location getChestLocation() { return chestLocation; }
    public Material getSellItem() { return sellItem; }
    public int getSellAmount() { return sellAmount; }
    public Material getPriceItem() { return priceItem; }
    public int getPriceAmount() { return priceAmount; }
}
