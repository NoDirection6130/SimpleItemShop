package pl.nodirection6130.itemshop;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class ShopListener implements Listener {

    private final ShopPlugin plugin;
    private final ShopManager shopManager;

    private static final int LINE_HEADER = 0;
    private static final int LINE_SELL   = 1;
    private static final int LINE_PRICE  = 2;
    private static final int LINE_STATUS = 3;

    private static final String HEADER_TAG = "[Sklep]";
    private static final String TAG_OPEN   = "[Open]";
    private static final String TAG_CLOSED = "[Closed]";

    public ShopListener(ShopPlugin plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
    }

    private Messages msg() {
        return plugin.getMessages();
    }

    // -----------------------------------------------------------------------
    // 1. Sign placement - create shop
    // -----------------------------------------------------------------------
    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignPlace(SignChangeEvent event) {
        Player player = event.getPlayer();

        String line0 = ChatColor.stripColor(event.getLine(0)).trim();
        if (!line0.equalsIgnoreCase(HEADER_TAG)) return;

        if (!player.hasPermission("itemshop.create")) {
            player.sendMessage(ChatColor.RED + msg().noPermissionCreate());
            event.setCancelled(true);
            return;
        }

        String[] sellParts = event.getLine(1).trim().split("\\s+");
        if (sellParts.length != 2) {
            player.sendMessage(ChatColor.RED + msg().invalidFormatLine2());
            event.setCancelled(true);
            return;
        }

        String[] priceParts = event.getLine(2).trim().split("\\s+");
        if (priceParts.length != 2) {
            player.sendMessage(ChatColor.RED + msg().invalidFormatLine3());
            event.setCancelled(true);
            return;
        }

        Material sellItem, priceItem;
        int sellAmount, priceAmount;

        try {
            sellItem = Material.valueOf(sellParts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + msg().unknownItem(sellParts[0]));
            event.setCancelled(true);
            return;
        }

        try {
            sellAmount = Integer.parseInt(sellParts[1]);
            if (sellAmount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + msg().invalidSellAmount());
            event.setCancelled(true);
            return;
        }

        try {
            priceItem = Material.valueOf(priceParts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + msg().unknownItem(priceParts[0]));
            event.setCancelled(true);
            return;
        }

        try {
            priceAmount = Integer.parseInt(priceParts[1]);
            if (priceAmount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + msg().invalidPriceAmount());
            event.setCancelled(true);
            return;
        }

        String line3 = event.getLine(3).trim();
        if (!line3.equalsIgnoreCase(TAG_CLOSED)) {
            player.sendMessage(ChatColor.RED + msg().line4MustBeClosed());
            event.setCancelled(true);
            return;
        }

        Block signBlock = event.getBlock();
        Block chestBlock = getAttachedChest(signBlock);

        if (chestBlock == null || !(chestBlock.getState() instanceof org.bukkit.block.Chest)) {
            player.sendMessage(ChatColor.RED + msg().mustPlaceOnChest());
            event.setCancelled(true);
            return;
        }

        if (shopManager.isShopChest(chestBlock.getLocation())) {
            player.sendMessage(ChatColor.RED + msg().chestAlreadyShop());
            event.setCancelled(true);
            return;
        }

        Shop shop = new Shop(
                player.getUniqueId(), player.getName(),
                chestBlock.getLocation(),
                sellItem, sellAmount, priceItem, priceAmount
        );
        shopManager.registerShop(shop);

        event.setLine(0, ChatColor.BLUE + HEADER_TAG);
        event.setLine(1, ChatColor.WHITE + sellParts[0].toUpperCase() + " " + sellAmount);
        event.setLine(2, ChatColor.GRAY + priceParts[0].toUpperCase() + " " + priceAmount);
        event.setLine(3, ChatColor.RED + TAG_CLOSED);

        player.sendMessage(ChatColor.GREEN + msg().shopCreated());
    }

    // -----------------------------------------------------------------------
    // 2. Sign break - remove shop
    // -----------------------------------------------------------------------
    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof Sign)) return;

        Sign sign = (Sign) block.getState();
        String line0 = ChatColor.stripColor(sign.getLine(0)).trim();
        if (!line0.equalsIgnoreCase(HEADER_TAG)) return;

        Player player = event.getPlayer();
        Block chestBlock = getAttachedChest(block);
        if (chestBlock == null) return;

        Shop shop = shopManager.getShopByChest(chestBlock.getLocation());
        if (shop == null) return;

        boolean isOwner = shop.getOwnerUUID().equals(player.getUniqueId());
        boolean isAdmin = player.hasPermission("itemshop.admin");

        if (!isOwner && !isAdmin) {
            player.sendMessage(ChatColor.RED + msg().cannotBreakOtherShop());
            event.setCancelled(true);
            return;
        }

        shopManager.removeShop(chestBlock.getLocation());
        player.sendMessage(ChatColor.YELLOW + msg().shopRemovedSignOnly());
    }

    // -----------------------------------------------------------------------
    // 3. Chest/sign interaction
    // -----------------------------------------------------------------------
    @EventHandler(priority = EventPriority.NORMAL)
    public void onChestClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            String line0 = ChatColor.stripColor(sign.getLine(0)).trim();
            if (!line0.equalsIgnoreCase(HEADER_TAG)) return;

            event.setCancelled(true);
            Block chestBlock = getAttachedChest(block);
            if (chestBlock == null) return;
            Shop shop = shopManager.getShopByChest(chestBlock.getLocation());
            if (shop == null) return;

            handlePurchase(event.getPlayer(), shop, chestBlock, sign);
            return;
        }

        if (!(block.getState() instanceof org.bukkit.block.Chest)) return;

        Location loc = block.getLocation();
        if (!shopManager.isShopChest(loc)) {
            Block otherHalf = getOtherChestHalf(block);
            if (otherHalf == null || !shopManager.isShopChest(otherHalf.getLocation())) return;
            block = otherHalf;
        }

        Shop shop = shopManager.getShopByChest(block.getLocation());
        if (shop == null) return;

        Player player = event.getPlayer();
        boolean isOwner = shop.getOwnerUUID().equals(player.getUniqueId());
        boolean isAdmin = player.hasPermission("itemshop.admin");

        if (!isOwner && !isAdmin) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + msg().cannotOpenOtherChest());
            return;
        }

        final Block finalChestBlock = block;
        final Shop finalShop = shop;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            updateSignStatus(finalChestBlock, finalShop);
        }, 1L);
    }

    // -----------------------------------------------------------------------
    // 4. Chest break protection
    // -----------------------------------------------------------------------
    @EventHandler(priority = EventPriority.NORMAL)
    public void onChestBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof org.bukkit.block.Chest)) return;

        Shop shop = shopManager.getShopByChest(block.getLocation());
        if (shop == null) {
            Block otherHalf = getOtherChestHalf(block);
            if (otherHalf != null) shop = shopManager.getShopByChest(otherHalf.getLocation());
        }
        if (shop == null) return;

        Player player = event.getPlayer();
        boolean isOwner = shop.getOwnerUUID().equals(player.getUniqueId());
        boolean isAdmin = player.hasPermission("itemshop.admin");

        if (!isOwner && !isAdmin) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + msg().cannotBreakOtherChest());
        } else {
            shopManager.removeShop(block.getLocation());
            player.sendMessage(ChatColor.YELLOW + msg().shopRemovedWithChest());
        }
    }

    // -----------------------------------------------------------------------
    // 5. Piston protection
    // -----------------------------------------------------------------------
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (shopManager.isShopChest(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (shopManager.isShopChest(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // -----------------------------------------------------------------------
    // 6. Explosion protection
    // -----------------------------------------------------------------------
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent event) {
        event.blockList().removeIf(b -> shopManager.isShopChest(b.getLocation()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(b -> shopManager.isShopChest(b.getLocation()));
    }

    // -----------------------------------------------------------------------
    // Purchase logic
    // -----------------------------------------------------------------------
    private void handlePurchase(Player buyer, Shop shop, Block chestBlock, Sign sign) {
        if (!buyer.hasPermission("itemshop.use")) {
            buyer.sendMessage(ChatColor.RED + msg().noPermissionUse());
            return;
        }

        if (shop.getOwnerUUID().equals(buyer.getUniqueId())) {
            buyer.sendMessage(ChatColor.YELLOW + msg().ownShop());
            return;
        }

        String statusLine = ChatColor.stripColor(sign.getLine(LINE_STATUS)).trim();
        if (!statusLine.equalsIgnoreCase(TAG_OPEN)) {
            buyer.sendMessage(ChatColor.RED + msg().shopClosed());
            return;
        }

        int priceInInventory = countItems(buyer.getInventory(), shop.getPriceItem());
        if (priceInInventory < shop.getPriceAmount()) {
            buyer.sendMessage(ChatColor.YELLOW + msg().notEnoughItems());
            return;
        }

        org.bukkit.block.Chest chestState = (org.bukkit.block.Chest) chestBlock.getState();
        Inventory chestInv = chestState.getInventory();

        int stockCount = countItems(chestInv, shop.getSellItem());
        if (stockCount < shop.getSellAmount()) {
            buyer.sendMessage(ChatColor.RED + msg().shopNoStock());
            updateSignStatus(chestBlock, shop);
            return;
        }

        removeItems(buyer.getInventory(), shop.getPriceItem(), shop.getPriceAmount());
        addItemsToInventory(chestInv, shop.getPriceItem(), shop.getPriceAmount());
        removeItems(chestInv, shop.getSellItem(), shop.getSellAmount());

        ItemStack toGive = new ItemStack(shop.getSellItem(), shop.getSellAmount());
        HashMap<Integer, ItemStack> leftover = buyer.getInventory().addItem(toGive);
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                buyer.getWorld().dropItemNaturally(buyer.getLocation(), item);
            }
            buyer.sendMessage(ChatColor.YELLOW + msg().inventoryFull());
        }

        buyer.sendMessage(ChatColor.GREEN + msg().transactionSuccess());
        updateSignStatus(chestBlock, shop);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    public void updateSignStatus(Block chestBlock, Shop shop) {
        Sign sign = findShopSign(chestBlock);
        if (sign == null) return;

        org.bukkit.block.Chest chestState = (org.bukkit.block.Chest) chestBlock.getState();
        Inventory inv = chestState.getInventory();
        int stock = countItems(inv, shop.getSellItem());

        if (stock >= shop.getSellAmount()) {
            sign.setLine(LINE_STATUS, ChatColor.GREEN + TAG_OPEN);
        } else {
            sign.setLine(LINE_STATUS, ChatColor.RED + TAG_CLOSED);
        }
        sign.update(true);
    }

    private Sign findShopSign(Block chestBlock) {
        Block above = chestBlock.getRelative(0, 1, 0);
        if (above.getState() instanceof Sign) {
            Sign s = (Sign) above.getState();
            if (ChatColor.stripColor(s.getLine(0)).trim().equalsIgnoreCase(HEADER_TAG)) return s;
        }
        int[][] sides = {{1,0,0},{-1,0,0},{0,0,1},{0,0,-1}};
        for (int[] d : sides) {
            Block side = chestBlock.getRelative(d[0], d[1], d[2]);
            if (side.getState() instanceof Sign) {
                Sign s = (Sign) side.getState();
                if (ChatColor.stripColor(s.getLine(0)).trim().equalsIgnoreCase(HEADER_TAG)) return s;
            }
        }
        return null;
    }

    private Block getAttachedChest(Block signBlock) {
        Block below = signBlock.getRelative(0, -1, 0);
        if (below.getState() instanceof org.bukkit.block.Chest) return below;

        if (signBlock.getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
            org.bukkit.block.data.type.WallSign wallSign =
                    (org.bukkit.block.data.type.WallSign) signBlock.getBlockData();
            Block attached = signBlock.getRelative(wallSign.getFacing().getOppositeFace());
            if (attached.getState() instanceof org.bukkit.block.Chest) return attached;
        }
        return null;
    }

    private Block getOtherChestHalf(Block chestBlock) {
        if (!(chestBlock.getBlockData() instanceof Chest)) return null;
        Chest chestData = (Chest) chestBlock.getBlockData();
        Chest.Type type = chestData.getType();
        if (type == Chest.Type.SINGLE) return null;

        org.bukkit.block.BlockFace facing = chestData.getFacing();
        org.bukkit.block.BlockFace left = rotateLeft(facing);
        org.bukkit.block.BlockFace right = rotateRight(facing);

        if (type == Chest.Type.LEFT) return chestBlock.getRelative(right);
        if (type == Chest.Type.RIGHT) return chestBlock.getRelative(left);
        return null;
    }

    private org.bukkit.block.BlockFace rotateLeft(org.bukkit.block.BlockFace face) {
        return switch (face) {
            case NORTH -> org.bukkit.block.BlockFace.WEST;
            case WEST  -> org.bukkit.block.BlockFace.SOUTH;
            case SOUTH -> org.bukkit.block.BlockFace.EAST;
            case EAST  -> org.bukkit.block.BlockFace.NORTH;
            default    -> face;
        };
    }

    private org.bukkit.block.BlockFace rotateRight(org.bukkit.block.BlockFace face) {
        return switch (face) {
            case NORTH -> org.bukkit.block.BlockFace.EAST;
            case EAST  -> org.bukkit.block.BlockFace.SOUTH;
            case SOUTH -> org.bukkit.block.BlockFace.WEST;
            case WEST  -> org.bukkit.block.BlockFace.NORTH;
            default    -> face;
        };
    }

    private int countItems(Inventory inv, Material material) {
        int count = 0;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() == material) count += item.getAmount();
        }
        return count;
    }

    private void removeItems(Inventory inv, Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != material) continue;
            if (item.getAmount() <= remaining) {
                remaining -= item.getAmount();
                contents[i] = null;
            } else {
                item.setAmount(item.getAmount() - remaining);
                remaining = 0;
            }
        }
        inv.setContents(contents);
    }

    private void addItemsToInventory(Inventory inv, Material material, int amount) {
        int remaining = amount;
        int maxStack = new ItemStack(material).getMaxStackSize();
        ItemStack[] contents = inv.getContents();

        for (ItemStack item : contents) {
            if (item != null && item.getType() == material && item.getAmount() < maxStack) {
                int space = maxStack - item.getAmount();
                int add = Math.min(space, remaining);
                item.setAmount(item.getAmount() + add);
                remaining -= add;
                if (remaining == 0) break;
            }
        }

        if (remaining > 0) {
            inv.setContents(contents);
            while (remaining > 0) {
                int add = Math.min(maxStack, remaining);
                inv.addItem(new ItemStack(material, add));
                remaining -= add;
            }
        } else {
            inv.setContents(contents);
        }
    }
}
