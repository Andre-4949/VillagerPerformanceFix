package de.andre.villagerperformancefix.Commands;

import de.andre.villagerperformancefix.Util;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TradeMove implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        Entity targetEntity = player.getTargetEntity(3);
        if (!(targetEntity instanceof Villager villager)) {
            player.sendMessage(Component.text("[Villagermerge]").color(TextColor.color(255, 0, 0)).append(Component.text(" Please look at the villager, from whom you want to switch the order of trades.").color(TextColor.color(255, 255, 255))));
            return true;
        }
        if (args.length == 1 && args[0].equals("list")) {
            return listTrades(player, villager);
        }
        if (args.length == 2) {
            int selectPosition, newPosition = 0;
            selectPosition = Integer.parseInt(args[0]);
            newPosition = Integer.parseInt(args[1]);
            ArrayList<MerchantRecipe> merchantRecipes = new ArrayList<>(villager.getRecipes());
            Util.reArrange(merchantRecipes, selectPosition, newPosition);
            villager.setRecipes(merchantRecipes);
        }
        return true;
    }

    private boolean listTrades(Player player, Villager villager) {
        Book.Builder bookBuilder = Book.builder();
        bookBuilder.author(Component.text("[TradeMove]")).title(Component.text("Villager Trades"));
        for (int i = 0; i < villager.getRecipes().size(); i++) {
            MerchantRecipe recipe = villager.getRecipe(i);
            bookBuilder.addPage(
                    Component.empty()
                            .append(
                                    Component.text("Number of Trade: ").decorate(TextDecoration.BOLD))
                            .append(
                                    Component.text(i).decorate(TextDecoration.BOLD).color(TextColor.color(255, 0, 0)))
                            .append(
                                    Component.text("\n\n"))
                            .append(
                                    Component.text(
                                            String.join("\n",
                                                    recipe.getIngredients().stream().filter(x -> x.getType() != Material.AIR)
                                                            .map(x -> x.getType().getKey() +
                                                                    (x.getAmount() == 1 ? "" : " x" + x.getAmount())
                                                            ).toList()
                                            )
                                    ))
                            .append(
                                    Component.text("\n->\n").color(TextColor.color(0, 0, 255)))
                            .append(
                                    Component.text(
                                            merchantRecipeResultToString(recipe) +
                                                    (
                                                            recipe.getResult().getAmount() == 1 ? "" : " x" + recipe.getResult().getAmount()
                                                    )
                                    )
                            )
            );
        }
        player.openBook(bookBuilder.build());
        return true;
    }

    private String merchantRecipeResultToString(MerchantRecipe recipe) {
        ItemStack result = recipe.getResult();
        if (result.getType().equals(Material.ENCHANTED_BOOK)) {
            Map<Enchantment, Integer> c2EnchantmentMap = ((EnchantmentStorageMeta) result.getItemMeta()).getStoredEnchants();
            Enchantment enchant = new ArrayList<>(c2EnchantmentMap.keySet()).get(0);
            int enchantLevel = c2EnchantmentMap.get(enchant);
            return PlainTextComponentSerializer.plainText().serialize(enchant.displayName(enchantLevel));
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(result.getType().getKey());
        if (!result.getEnchantments().keySet().isEmpty()) {
            stringBuilder.append("\n  ");
            result.getEnchantments().forEach(((enchantment, level) -> {
                stringBuilder.append(PlainTextComponentSerializer.plainText().serialize(enchantment.displayName(level)));
                stringBuilder.append("\n  ");
            }));
        }
        return stringBuilder.toString();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player))return new ArrayList<>();
        if (args.length==2 || args.length == 1){
            return new ArrayList<>(){{
                Entity targetEntity = player.getTargetEntity(3);
                if (targetEntity instanceof Villager villager){
                    villager.setAI(false);
                    Bukkit.getScheduler().runTaskLaterAsynchronously(Util.main, () -> {
                        villager.setAI(true);
                    }, 5*20);
                    for (int i = 0; i < villager.getRecipes().size(); i++) {
                        add(String.valueOf(i));
                    }
                }
            }};
        }
        return new ArrayList<>();
    }
}
