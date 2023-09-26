package de.andre.villagerperformancefix.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VillagerMerge implements CommandExecutor, TabCompleter {
    private final String MERGEDVILLAGERS = "mergedvillagers";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        Entity targetEntity = player.getTargetEntity(3);
        if (!(targetEntity instanceof Villager mainVillager)) {
            player.sendMessage(Component.text("[Villagermerge]").color(TextColor.color(255, 0, 0)).append(Component.text(" Please look at the villager you want the surrounding villagers in a 15 block radius to be merged to.").color(TextColor.color(255,255,255))));
            return true;
        }

        Location playerLoc = player.getLocation();
        ArrayList<Villager> villagers = new ArrayList<>(playerLoc.getNearbyEntitiesByType(Villager.class, 15).stream().filter(x->x.getProfession().equals(mainVillager.getProfession())).toList());

        VillagerTrades villagerTrades = new VillagerTrades(mainVillager);

        mainVillager.addPotionEffects(List.of(new PotionEffect(PotionEffectType.GLOWING, 10, 10, true)));
        villagers.remove(mainVillager);
        for (Villager villager : villagers) {
            villagerTrades.addRecipes(villager.getRecipes());
            villager.remove();
        }
        villagerTrades.updateTrades();

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }

    private class VillagerTrades {
        private final Villager villager;
        int mergedVillagers = 0;
        private ArrayList<MerchantRecipe> recipes;

        public VillagerTrades(Villager v) {
            villager = v;
            this.recipes = new ArrayList<>(v.getRecipes());
        }

        public VillagerTrades addRecipes(List<MerchantRecipe> merchantRecipes) {
            mergedVillagers++;
            ArrayList<MerchantRecipe> newRecipes = new ArrayList<>(recipes);
            for (MerchantRecipe merchantRecipe : merchantRecipes) {
                boolean knownRecipe = false;
                for (MerchantRecipe recipe : newRecipes) {
                    if (merchantRecipe.getResult().equals(recipe.getResult())) {
                        recipe.setMaxUses(recipe.getMaxUses() + merchantRecipe.getMaxUses());
                        knownRecipe = true;
                    }
                }
                if (!knownRecipe)
                    newRecipes.add(merchantRecipe);
            }
            this.recipes = newRecipes;
            return this;
        }

        public void updateTrades() {
            List<String> mergedVillagerList = villager.getScoreboardTags().stream().filter(x -> x.startsWith(MERGEDVILLAGERS)).toList();
            if (!mergedVillagerList.isEmpty()) {
                this.mergedVillagers += Integer.parseInt(mergedVillagerList.get(0).split(" ")[1]);
            }
            for (String villagers : mergedVillagerList) {
                villager.removeScoreboardTag(villagers);
            }
            recipes.sort((c1, c2) -> {
                ItemStack c1Result = c1.getResult();
                ItemStack c2Result = c2.getResult();
                Material c1ResultType = c1Result.getType();
                Material c2ResultType = c2Result.getType();
                if (c1ResultType != c2ResultType) {
                    return c1ResultType.compareTo(c2ResultType);
                }
                if (c1ResultType.equals(Material.ENCHANTED_BOOK)) {
                    Map<Enchantment, Integer> c1EnchantmentMap = ((EnchantmentStorageMeta) c1Result.getItemMeta()).getStoredEnchants();
                    Map<Enchantment, Integer> c2EnchantmentMap = ((EnchantmentStorageMeta) c2Result.getItemMeta()).getStoredEnchants();
                    if ((c1EnchantmentMap.keySet().size() != c2EnchantmentMap.keySet().size()) || c1EnchantmentMap.keySet().size() != 1) {
                        boolean enchantmentCount = c1EnchantmentMap.keySet().size() > c2EnchantmentMap.keySet().size();
                        return enchantmentCount ? 1 : -1;
                    }
                    Enchantment c1Enchant = new ArrayList<>(c1EnchantmentMap.keySet()).get(0);
                    Enchantment c2Enchant = new ArrayList<>(c2EnchantmentMap.keySet()).get(0);
                    int c1EnchantLevel = c1EnchantmentMap.get(c1Enchant);
                    int c2EnchantLevel = c2EnchantmentMap.get(c2Enchant);
                    if (c1Enchant != c2Enchant) {
                        return PlainTextComponentSerializer.plainText().serialize(c1Enchant.displayName(c1EnchantLevel)).compareToIgnoreCase(
                                PlainTextComponentSerializer.plainText().serialize(c2Enchant.displayName(c2EnchantLevel)));
                    }

                    if (c1EnchantLevel != c2EnchantLevel) {
                        return c1EnchantLevel < c2EnchantLevel ? -1 : 1;
                    }
                }
                return c1ResultType.getKey().compareTo(c2ResultType.getKey());
            });
            villager.setRecipes(recipes);
            villager.addScoreboardTag(MERGEDVILLAGERS + ": " + mergedVillagers);
        }
    }
}
