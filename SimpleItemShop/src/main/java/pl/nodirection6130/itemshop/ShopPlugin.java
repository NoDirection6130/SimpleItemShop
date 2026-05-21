package pl.nodirection6130.itemshop;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlugin extends JavaPlugin {

    private ShopManager shopManager;
    private Messages messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMessages();
        shopManager = new ShopManager(this);
        getServer().getPluginManager().registerEvents(new ShopListener(this, shopManager), this);
        getLogger().info("SimpleItemShop enabled.");
    }

    @Override
    public void onDisable() {
        if (shopManager != null) shopManager.saveShops();
        getLogger().info("SimpleItemShop disabled.");
    }

    public void loadMessages() {
        String lang = getConfig().getString("language", "pl");
        messages = new Messages(lang);
    }

    public Messages getMessages() {
        return messages;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("itemshop")) return false;

        if (!sender.hasPermission("itemshop.admin")) {
            sender.sendMessage(ChatColor.RED + messages.noPermissionCommand());
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + messages.commandUsage());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                reloadConfig();
                loadMessages();
                sender.sendMessage(ChatColor.GREEN + messages.configReloaded());
            }
            case "language", "lang" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.YELLOW + messages.commandUsage());
                    return true;
                }
                String newLang = args[1].toLowerCase();
                if (!newLang.equals("pl") && !newLang.equals("en")) {
                    sender.sendMessage(ChatColor.RED + messages.invalidLanguage());
                    return true;
                }
                getConfig().set("language", newLang);
                saveConfig();
                loadMessages();
                sender.sendMessage(ChatColor.GREEN + messages.languageChanged(newLang));
            }
            default -> sender.sendMessage(ChatColor.YELLOW + messages.commandUsage());
        }

        return true;
    }
}
