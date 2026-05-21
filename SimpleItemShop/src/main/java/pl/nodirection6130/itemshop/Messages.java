package pl.nodirection6130.itemshop;

public class Messages {

    private final String lang;

    public Messages(String lang) {
        this.lang = lang.toLowerCase();
    }

    private boolean isEn() {
        return lang.equals("en");
    }

    public String noPermissionCreate() {
        return isEn() ? "You don't have permission to create shops." : "Nie masz permisji do tworzenia sklepow.";
    }

    public String invalidFormatLine2() {
        return isEn() ? "Invalid format on line 2. Use: ITEM_ID AMOUNT" : "Nieprawidlowy format linii 2. Uzyj: ITEM_ID ILOSC";
    }

    public String invalidFormatLine3() {
        return isEn() ? "Invalid format on line 3. Use: ITEM_ID AMOUNT" : "Nieprawidlowy format linii 3. Uzyj: ITEM_ID ILOSC";
    }

    public String unknownItem(String item) {
        return isEn() ? "Unknown item: " + item : "Nieznany przedmiot: " + item;
    }

    public String invalidSellAmount() {
        return isEn() ? "Invalid sell item amount." : "Nieprawidlowa ilosc sprzedawanego przedmiotu.";
    }

    public String invalidPriceAmount() {
        return isEn() ? "Invalid price item amount." : "Nieprawidlowa ilosc przedmiotu-ceny.";
    }

    public String line4MustBeClosed() {
        return isEn() ? "Line 4 must contain [Closed]." : "Linia 4 musi zawierac [Closed].";
    }

    public String mustPlaceOnChest() {
        return isEn() ? "The sign must be placed on a chest!" : "Tabliczka musi byc polozona na skrzyni!";
    }

    public String chestAlreadyShop() {
        return isEn() ? "This chest is already a shop!" : "Ta skrzynia jest juz sklepem!";
    }

    public String shopCreated() {
        return isEn() ? "Shop created! Put items in the chest to open it." : "Sklep zostal utworzony! Wloz przedmioty do skrzyni aby go otworzyc.";
    }

    public String cannotBreakOtherShop() {
        return isEn() ? "You cannot destroy someone else's shop!" : "Nie mozesz zniszczyc cudzego sklepu!";
    }

    public String shopRemovedSignOnly() {
        return isEn() ? "Shop removed. The chest is now a regular chest." : "Sklep zostal usuniety. Skrzynia jest teraz zwykla skrzynia.";
    }

    public String cannotOpenOtherChest() {
        return isEn() ? "This is someone else's shop! Click the sign to buy." : "To jest cudzy sklep! Kliknij w tabliczke aby kupic.";
    }

    public String cannotBreakOtherChest() {
        return isEn() ? "You cannot destroy someone else's shop chest!" : "Nie mozesz zniszczyc skrzyni cudzego sklepu!";
    }

    public String shopRemovedWithChest() {
        return isEn() ? "Shop removed along with the chest." : "Sklep zostal usuniety razem ze skrzynia.";
    }

    public String noPermissionUse() {
        return isEn() ? "You don't have permission to use shops." : "Nie masz permisji do korzystania ze sklepow.";
    }

    public String ownShop() {
        return isEn() ? "This is your own shop." : "To jest Twoj wlasny sklep.";
    }

    public String shopClosed() {
        return isEn() ? "This shop is closed - no stock available." : "Ten sklep jest zamkniety - brak towaru na stanie.";
    }

    public String notEnoughItems() {
        return isEn() ? "You don't have the required items in your inventory!" : "Nie posiadasz odpowiednich przedmiotow w ekwipunku!";
    }

    public String shopNoStock() {
        return isEn() ? "The shop is out of stock!" : "W sklepie brakuje towaru!";
    }

    public String inventoryFull() {
        return isEn() ? "Your inventory was full - some items dropped on the ground." : "Twoj ekwipunek byl pelny - czesc przedmiotow wypadla na ziemie.";
    }

    public String transactionSuccess() {
        return isEn() ? "Transaction successful." : "Transakcja poszla pomyslnie.";
    }

    public String languageChanged(String newLang) {
        return isEn() ? "Language changed to: " + newLang : "Jezyk zmieniony na: " + newLang;
    }

    public String invalidLanguage() {
        return isEn() ? "Invalid language. Use: pl or en" : "Nieprawidlowy jezyk. Uzyj: pl lub en";
    }

    public String configReloaded() {
        return isEn() ? "SimpleItemShop config reloaded." : "Konfiguracja SimpleItemShop przeladowana.";
    }

    public String commandUsage() {
        return isEn() ? "Usage: /itemshop <language|reload> [pl|en]" : "Uzycie: /itemshop <language|reload> [pl|en]";
    }

    public String noPermissionCommand() {
        return isEn() ? "You don't have permission to use this command." : "Nie masz permisji do tej komendy.";
    }
}
