package dev.infernal_coding.eidolonrecipes.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.ibm.icu.impl.Pair;
import dev.infernal_coding.eidolonrecipes.recipes.CrucibleRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.recipes.WorktableRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.rituals.RitualManager;
import dev.infernal_coding.eidolonrecipes.rituals.RitualRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.util.ChantPage2;
import dev.infernal_coding.eidolonrecipes.util.TitlePage2;
import dev.infernal_coding.eidolonrecipes.util.TitledRitualPage2;
import elucent.eidolon.Eidolon;
import elucent.eidolon.Registry;
import elucent.eidolon.capability.Facts;
import elucent.eidolon.codex.*;
import elucent.eidolon.recipe.CrucibleRecipe;
import elucent.eidolon.recipe.CrucibleRegistry;
import elucent.eidolon.recipe.WorktableRecipe;
import elucent.eidolon.recipe.WorktableRegistry;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.ritual.RitualRegistry;
import elucent.eidolon.spell.Sign;
import elucent.eidolon.spell.Signs;
import elucent.eidolon.spell.Spell;
import elucent.eidolon.spell.Spells;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dev.infernal_coding.eidolonrecipes.util.RitualUtil.getRitualInputs;


public class EidolonReflectedRegistries {
    static Chapter WICKED_SIGN, SACRED_SIGN, SOUL_SIGN, MIND_SIGN, BLOOD_SIGN;
    static List<Category> categories =
            ObfuscationReflectionHelper.getPrivateValue(CodexChapters.class, null, "categories");

    static Category originalNature = ObfuscationReflectionHelper.getPrivateValue(CodexChapters.class, null, "NATURE");
    static Category originalRituals = ObfuscationReflectionHelper.getPrivateValue(CodexChapters.class, null, "RITUALS");
    static Category originalArtifice = ObfuscationReflectionHelper.getPrivateValue(CodexChapters.class, null, "ARTIFICE");
    static Category originalTheurgy = ObfuscationReflectionHelper.getPrivateValue(CodexChapters.class, null, "THEURGY");
    static Category originalSigns = ObfuscationReflectionHelper.getPrivateValue(CodexChapters.class, null, "SIGNS");

    static Category NATURE, RITUALS, ARTIFICE, THEURGY, SIGNS;

    static Chapter NATURE_INDEX, MONSTERS, ORES, PEWTER, ENCHANTED_ASH,
            RITUALS_INDEX, BRAZIER, ITEM_PROVIDERS,
            ARTIFICE_INDEX, WOODEN_STAND, TALLOW, CRUCIBLE, MISC_CRUCIBLE, ARCANE_GOLD, REAGENTS, SOUL_GEMS, SHADOW_GEM, WARPED_SPROUTS, BASIC_ALCHEMY, INLAYS, BASIC_BAUBLES, MAGIC_WORKBENCH, MISC_WORKBENCH, VOID_AMULET, WARDED_MAIL, SOULFIRE_WAND, BONECHILL_WAND, REAPER_SCYTHE, CLEAVING_AXE, SOUL_ENCHANTER, REVERSAL_PICK, WARLOCK_ARMOR, GRAVITY_BELT, PRESTIGIOUS_PALM, MIND_SHIELDING_PLATE, RESOLUTE_BELT, GLASS_HAND,
            THEURGY_INDEX, INTRO_SIGNS, EFFIGY, ALTARS, ALTAR_LIGHTS, ALTAR_SKULLS, ALTAR_HERBS, GOBLET, DARK_PRAYER, ANIMAL_SACRIFICE, DARK_TOUCH, STONE_ALTAR, UNHOLY_EFFIGY, VILLAGER_SACRIFICE,
            MISC_SPELLS, SIGNS_INDEX;

    public static final Map<ResourceLocation, CrucibleRecipe> CRUCIBLE_RECIPES = ObfuscationReflectionHelper.getPrivateValue(CrucibleRegistry.class, null, "recipes");

    public static final HashMap<ResourceLocation, WorktableRecipe> WORKTABLE_RECIPES = ObfuscationReflectionHelper.getPrivateValue(WorktableRegistry.class, null, "recipes");

    public static final List<Spell> SPELLS = ObfuscationReflectionHelper.getPrivateValue(Spells.class, null, "spells");

    public static final Map<ResourceLocation, Spell> SPELL_MAP = ObfuscationReflectionHelper.getPrivateValue(Spells.class, null, "spellMap");

    public static final Map<ResourceLocation, Ritual> RITUAL_MAP = ObfuscationReflectionHelper.getPrivateValue(RitualRegistry.class, null, "rituals");

    public static final BiMap<Object, Ritual> MATCHES = ObfuscationReflectionHelper.getPrivateValue(RitualRegistry.class, null, "matches");

    public static BiMap<Ritual, Object> sacrificeMap = MATCHES.inverse();

    public static final Comparator<RitualRecipeWrapper> backwardsComparator = (r1, r2) -> {

        String title1 = I18n.format(r1.title);
        String title2 = I18n.format(r2.title);
        return title1.compareTo(title2) * -1;
    };


    public static void onDataPackReloaded(RecipeManager manager) {
        Map<ResourceLocation, CrucibleRecipeWrapper> crucibleRecipes = getRecipes(manager, RecipeTypes.CRUCIBLE);
        clearEntriesIf(CRUCIBLE_RECIPES, (id, recipe) -> manager.getKeys().anyMatch(id::equals));
        CRUCIBLE_RECIPES.putAll(crucibleRecipes);

        Map<ResourceLocation, WorktableRecipeWrapper> worktableRecipes = getRecipes(manager, RecipeTypes.WORKTABLE);
        clearEntriesIf(WORKTABLE_RECIPES, (id, recipe) -> manager.getKeys().anyMatch(id::equals));
        WORKTABLE_RECIPES.putAll(worktableRecipes);

        Map<ResourceLocation, SpellRecipeWrapper> spellRecipes = getRecipes(manager, RecipeTypes.SPELL);
        clearEntriesIf(SPELL_MAP, (id, recipe) -> manager.getKeys().anyMatch(id::equals));
        SPELL_MAP.putAll(spellRecipes);
        SPELLS.clear();
        SPELLS.addAll(SPELL_MAP.values());

        Map<ResourceLocation, RitualRecipeWrapper> rituals = getRecipes(manager, RecipeTypes.RITUAL);
        clearEntriesIf(RITUAL_MAP, (id, recipe) -> manager.getKeys().anyMatch(id::equals));
        rituals.values().forEach(ritualRecipeWrapper -> {
            if (ritualRecipeWrapper.getBrazierItemRequirement() != null &&
                    ritualRecipeWrapper.getBrazierItemRequirement() != ItemStack.EMPTY) {
                RITUAL_MAP.put(ritualRecipeWrapper.getId(), ritualRecipeWrapper);
            }
        });

        clearEntriesIf(MATCHES, (requirement, recipe) -> rituals.values().stream().anyMatch(ritualWrapper ->
                        ritualWrapper.getId().equals(recipe.getRegistryName())
                )
        );
        rituals.values().forEach(ritualRecipeWrapper -> {
            if (ritualRecipeWrapper.getSacrifice() != null) {
                MATCHES.put(ritualRecipeWrapper.getSacrifice(), ritualRecipeWrapper);
            }
        });
        reloadCodexChapters(manager);
    }

    public static <C extends IInventory, T extends IRecipe<C>> Map<ResourceLocation, T> getRecipes(RecipeManager manager, IRecipeType<T> type) {
        return manager.getRecipesForType(type).stream().collect(Collectors.toMap(IRecipe::getId, Function.identity()));
    }

    private static <T> void clearEntriesIf(Map<ResourceLocation, T> map, BiFunction<ResourceLocation, T, Boolean> shouldClear) {
        map.entrySet().removeIf(entry -> shouldClear.apply(entry.getKey(), entry.getValue()));
    }

    private static <T> void clearEntriesIf(BiMap<Object, T> map, BiFunction<Object, T, Boolean> shouldClear) {
        map.entrySet().removeIf(entry -> shouldClear.apply(entry.getKey(), entry.getValue()));
    }



    private static void reloadCodexChapters(RecipeManager manager) {
        sacrificeMap = MATCHES.inverse();
        categories.remove(NATURE);
        categories.remove(RITUALS);
        categories.remove(ARTIFICE);
        categories.remove(THEURGY);
        categories.remove(SIGNS);
        categories.remove(originalNature);
        categories.remove(originalRituals);
        categories.remove(originalArtifice);
        categories.remove(originalTheurgy);
        categories.remove(originalSigns);

        MONSTERS = new Chapter(
                "eidolon.codex.chapter.monsters",
                new TitlePage("eidolon.codex.page.monsters.zombie_brute"),
                new EntityPage(Registry.ZOMBIE_BRUTE.get()),
                new TitlePage("eidolon.codex.page.monsters.wraith"),
                new EntityPage(Registry.WRAITH.get()),
                new TitlePage("eidolon.codex.page.monsters.chilled")
        );

        Map<ResourceLocation, BlastingRecipe> blastingRecipes =
                getRecipes(manager, IRecipeType.BLASTING);

        Map<ResourceLocation, ICraftingRecipe> craftingRecipes =
                getRecipes(manager, IRecipeType.CRAFTING);

        Map<ResourceLocation, WorktableRecipeWrapper> worktableRecipes =
                getRecipes(manager, RecipeTypes.WORKTABLE);

        Map<ResourceLocation, CrucibleRecipeWrapper> crucibleRecipes =
                getRecipes(manager, RecipeTypes.CRUCIBLE);

        Map<ResourceLocation, SpellRecipeWrapper> spellRecipes =
                getRecipes(manager, RecipeTypes.SPELL);

        Map<ResourceLocation, RitualRecipeWrapper> ritualRecipes =
                getRecipes(manager, RecipeTypes.RITUAL);

        Optional<ICraftingRecipe> leadBlockRecipe =
                Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID,
                        "lead_block")));

        Optional<ICraftingRecipe> leadIngotRecipe =
                Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID,
                        "lead_ingot")));

        Optional<BlastingRecipe> blastingRecipe = Optional.ofNullable(blastingRecipes.get(new ResourceLocation(Eidolon.MODID, "blast_lead_ore")));

        ItemStack blastInput = blastingRecipe
                .map(EidolonReflectedRegistries::getCraftingInputs)
                .map(itemStacks -> itemStacks[0]).orElse(ItemStack.EMPTY);

        ItemStack blastOutput = blastingRecipe.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);


        ItemStack[] leadBlockIngredients = leadBlockRecipe.
                map(EidolonReflectedRegistries::getCraftingInputs).orElseGet(() -> new ItemStack[0]);
        ItemStack leadBlockOutput = leadBlockRecipe.map(ICraftingRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        ItemStack[] leadIngotIngredients = leadIngotRecipe.
                map(EidolonReflectedRegistries::getCraftingInputs).orElseGet(() -> new ItemStack[0]);
        ItemStack leadIngotOutput = leadIngotRecipe.map(ICraftingRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);


        ORES = new Chapter(
                "eidolon.codex.chapter.ores",
                new TitlePage("eidolon.codex.page.ores.lead_ore"),
                new SmeltingPage(blastInput, blastOutput),
                new CraftingPage(leadBlockOutput, leadBlockIngredients),
                new CraftingPage(leadIngotOutput, leadIngotIngredients));

        Optional<ICraftingRecipe> pewterRecipe = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "pewter_blend")));
        ItemStack[] pewterInputs = pewterRecipe.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack pewterOutput = pewterRecipe.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        Optional<BlastingRecipe> blastPewterIngot = Optional.ofNullable(blastingRecipes.get(new ResourceLocation(Eidolon.MODID, "blast_pewter_blend")));

        ItemStack blastPewterIngotInput = blastPewterIngot
                .map(EidolonReflectedRegistries::getCraftingInputs)
                .map(itemStacks -> itemStacks[0]).orElse(ItemStack.EMPTY);

        ItemStack blastPewterIngotOutput = blastPewterIngot.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        Optional<ICraftingRecipe> pewterBlockRecipe =
                Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID,
                        "pewter_block")));
        ItemStack[] pewterBlockInputs = pewterBlockRecipe.
                map(EidolonReflectedRegistries::getCraftingInputs).orElseGet(() -> new ItemStack[0]);
        ItemStack pewterBlockOutput = pewterBlockRecipe.map(ICraftingRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        Optional<ICraftingRecipe> pewterIngot =
                Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID,
                        "lead_ingot")));
        ItemStack[] pewterIngotInputs = pewterIngot.
                map(EidolonReflectedRegistries::getCraftingInputs).orElseGet(() -> new ItemStack[0]);
        ItemStack pewterIngotOutput = pewterBlockRecipe.map(ICraftingRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        PEWTER = new Chapter(
                "eidolon.codex.chapter.pewter",
                new TitlePage("eidolon.codex.page.pewter"),
                new CraftingPage(pewterOutput, pewterInputs),
                new SmeltingPage(blastPewterIngotOutput, blastPewterIngotInput),
                new CraftingPage(pewterBlockOutput, pewterBlockInputs),
                new CraftingPage(pewterIngotOutput, pewterIngotInputs));


        Optional<BlastingRecipe> enchantedAsh = Optional.ofNullable(blastingRecipes.get(new ResourceLocation(Eidolon.MODID, "blast_enchanted_ash")));

        ItemStack enchantedAshInput = enchantedAsh
                .map(EidolonReflectedRegistries::getCraftingInputs)
                .map(itemStacks -> itemStacks[0]).orElse(ItemStack.EMPTY);

        ItemStack enchantedAshOutput = enchantedAsh.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        ENCHANTED_ASH = new Chapter(
                "eidolon.codex.chapter.enchanted_ash",
                new TitlePage("eidolon.codex.page.enchanted_ash"),
                new SmeltingPage(enchantedAshOutput, enchantedAshInput));

        NATURE_INDEX = new Chapter(
                "eidolon.codex.chapter.nature_index",
                new TitledIndexPage("eidolon.codex.page.nature_index.0",
                        new IndexPage.IndexEntry(MONSTERS, new ItemStack(Registry.TATTERED_CLOTH.get())),
                        new IndexPage.IndexEntry(ORES, new ItemStack(Registry.LEAD_ORE.get())),
                        new IndexPage.IndexEntry(PEWTER, new ItemStack(Registry.PEWTER_INGOT.get())),
                        new IndexPage.IndexEntry(ENCHANTED_ASH, new ItemStack(Registry.ENCHANTED_ASH.get()))
                )
        );

        categories.add(NATURE = new Category(
                "nature",
                new ItemStack(Registry.ZOMBIE_HEART.get()),
                ColorUtil.packColor(255, 89, 143, 76),
                NATURE_INDEX
        ));

        Optional<ICraftingRecipe> brazier = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "brazier")));
        ItemStack[] brazerInputs = brazier.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack brazierOutput = brazier.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        BRAZIER = new Chapter(
                "eidolon.codex.chapter.brazier",
                new TitlePage("eidolon.codex.page.brazier.0"),
                new TextPage("eidolon.codex.page.brazier.1"),
                new CraftingPage(brazierOutput, brazerInputs));

        Optional<ICraftingRecipe> stoneHand = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "stone_hand")));
        ItemStack[] stoneHandInputs = stoneHand.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack stoneHandOutput = stoneHand.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        Optional<ICraftingRecipe> necroFocus = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "necrotic_focus")));
        ItemStack[] focusInputs = necroFocus.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack focusOutput = necroFocus.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        ITEM_PROVIDERS = new Chapter(
                "eidolon.codex.chapter.item_providers",
                new TitlePage("eidolon.codex.page.item_providers.0"),
                new CraftingPage(stoneHandOutput, stoneHandInputs),
                new TitlePage("eidolon.codex.page.item_providers.1"),
                new CraftingPage(focusOutput, focusInputs));

        Set<RitualRecipeWrapper> rituals = new TreeSet<>(backwardsComparator);
        rituals.addAll(ritualRecipes.values());

        List<IndexPage> ritualPages = new ArrayList<>();
        List<IndexPage.IndexEntry> pageEntries = new ArrayList<>();
        int currentEntry = 0;

        for (RitualRecipeWrapper ritual : rituals) {
            currentEntry++;
            Pair<ItemStack, RitualPage.RitualIngredient[]> inputs = getRitualInputs(ritual);
            ItemStack icon = getRitualIcon(ritual);
            ItemStack sacrifice = inputs.first;
            pageEntries.add(new IndexPage.IndexEntry(new Chapter(ritual.title,
                    new TitledRitualPage2(ritual.title, ritual, sacrifice,
                            inputs.second), new TextPage(ritual.description)), icon));
            if (currentEntry >= 8) {
                ritualPages.add(new IndexPage(pageEntries.toArray(new IndexPage.IndexEntry[0])));
                pageEntries.clear();
                currentEntry = 0;
            }
        }

        if (!pageEntries.isEmpty()) {
            ritualPages.add(new IndexPage(pageEntries.toArray(new IndexPage.IndexEntry[0])));
        }


        RITUALS_INDEX = new Chapter(
                "eidolon.codex.chapter.rituals", ritualPages.toArray(new IndexPage[0]));

        categories.add(RITUALS = new Category(
                "rituals",
                new ItemStack(Registry.LESSER_SOUL_GEM.get()),
                ColorUtil.packColor(255, 223, 178, 43),
                RITUALS_INDEX
        ));

        Optional<ICraftingRecipe> woodenStand = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "wooden_brewing_stand")));
        ItemStack[] standInputs = woodenStand.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack standOutput = woodenStand.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        Optional<CrucibleRecipeWrapper> fungusSprouts = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "fungus_sprouts")));
        CruciblePage.CrucibleStep[] fungiInputs = fungusSprouts.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack fungiOutput = fungusSprouts.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(new ResourceLocation(Eidolon.MODID, "fungus_sprouts"));

        WOODEN_STAND = new Chapter(
                "eidolon.codex.chapter.wooden_stand",
                new TitlePage("eidolon.codex.page.wooden_stand.0"),
                new CraftingPage(standOutput, standInputs),
                new TitlePage("eidolon.codex.page.wooden_stand.1"),
                new CruciblePage(fungiOutput, fungiInputs)
        );

        Optional<BlastingRecipe> tallow = Optional.ofNullable(blastingRecipes.get(new ResourceLocation(Eidolon.MODID, "tallow")));
        ItemStack tallowInput = tallow.map(EidolonReflectedRegistries::getCraftingInputs).map(itemStacks -> itemStacks[0]).orElse(ItemStack.EMPTY);
        ItemStack tallowOutput = tallow.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        Optional<ICraftingRecipe> candle = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "candle")));
        ItemStack[] candleInputs = candle.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack candleOutput = candle.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        Optional<ICraftingRecipe> candlestick = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "candlestick")));
        ItemStack[] stickInput = candlestick.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack stickOutput = candlestick.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        TALLOW = new Chapter(
                "eidolon.codex.chapter.tallow",
                new TitlePage("eidolon.codex.page.tallow.0"),
                new SmeltingPage(tallowOutput, tallowInput),
                new TitlePage("eidolon.codex.page.tallow.1"),
                new CraftingPage(candleOutput, candleInputs),
                new CraftingPage(stickOutput, stickInput)
        );

        Optional<ICraftingRecipe> crucible = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "crucible")));
        ItemStack[] crucibleInput = crucible.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack crucibleOutput = crucible.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        CRUCIBLE = new Chapter(
                "eidolon.codex.chapter.crucible",
                new TitlePage("eidolon.codex.page.crucible.0"),
                new TextPage("eidolon.codex.page.crucible.1"),
                new CraftingPage(crucibleOutput, crucibleInput)
        );

        Optional<CrucibleRecipeWrapper> arcaneGold = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "arcane_gold")));
        CruciblePage.CrucibleStep[] goldSteps = arcaneGold.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack goldOutput = arcaneGold.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(new ResourceLocation(Eidolon.MODID, "arcane_gold"));

        Optional<ICraftingRecipe> arcaneGoldBlock = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "arcane_gold_block")));
        ItemStack[] blockInput = arcaneGoldBlock.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack blockOutput = arcaneGoldBlock.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        Optional<ICraftingRecipe> arcaneGoldIngot = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "arcane_gold_ingot")));
        ItemStack[] ingotInput = arcaneGoldIngot.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack ingotOutput = arcaneGoldIngot.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);


        ARCANE_GOLD = new Chapter(
                "eidolon.codex.chapter.arcane_gold",
                new TitlePage("eidolon.codex.page.arcane_gold"),
                new CruciblePage(goldOutput, goldSteps),
                new CraftingPage(blockOutput, blockInput),
                new CraftingPage(ingotOutput, ingotInput)
        );

        List<Page> reagents = new ArrayList<>();


        Optional<CrucibleRecipeWrapper> sulfur = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "sulfur")));
        CruciblePage.CrucibleStep[] sulfurSteps = sulfur.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack sulfurOutput = sulfur.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(sulfur.map(IRecipe::getId).orElse(null));


        Optional<CrucibleRecipeWrapper> deathEssence = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "death_essence")));
        CruciblePage.CrucibleStep[] deathSteps = deathEssence.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack deathOutput = deathEssence.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(deathEssence.map(IRecipe::getId).orElse(null));


        Optional<CrucibleRecipeWrapper> crimsonEssenceFungus = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "crimson_essence_fungus")));
        CruciblePage.CrucibleStep[] crimsonFungusSteps = crimsonEssenceFungus.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack crimsonFungusOutput = crimsonEssenceFungus.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(crimsonEssenceFungus.map(IRecipe::getId).orElse(null));


        Optional<CrucibleRecipeWrapper> crimsonEssenceRoots = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "crimson_essence_roots")));
        CruciblePage.CrucibleStep[] crimsonRootsSteps = crimsonEssenceRoots.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack crimsonRootsOutput = crimsonEssenceRoots.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(crimsonEssenceRoots.map(IRecipe::getId).orElse(null));


        Optional<CrucibleRecipeWrapper> crimsonEssenceVines = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "crimson_essence_vines")));
        CruciblePage.CrucibleStep[] crimsonVinesSteps = crimsonEssenceVines.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack crimsonVinesOutput = crimsonEssenceVines.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(crimsonEssenceVines.map(IRecipe::getId).orElse(null));


        Optional<CrucibleRecipeWrapper> enderCalx = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "ender_calx")));
        CruciblePage.CrucibleStep[] calxSteps = enderCalx.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack calxOutput = enderCalx.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(enderCalx.map(IRecipe::getId).orElse(null));

        reagents.addAll(Lists.newArrayList(
                new TitlePage("eidolon.codex.page.reagents.0"),
                new CruciblePage(sulfurOutput, sulfurSteps),
                new TitlePage("eidolon.codex.page.reagents.1"),
                new CruciblePage(deathOutput, deathSteps),
                new TitlePage("eidolon.codex.page.reagents.2"),
                new CruciblePage(crimsonFungusOutput, crimsonFungusSteps),
                new CruciblePage(crimsonRootsOutput, crimsonRootsSteps),
                new CruciblePage(crimsonVinesOutput, crimsonVinesSteps),
                new TitlePage("eidolon.codex.page.reagents.3"),
                new CruciblePage(calxOutput, calxSteps)));

        reagents.addAll(getCruciblePagesOf(crucibleRecipes, CrucibleRecipeWrapper.Category.REAGENTS));


        REAGENTS = new Chapter(
                "eidolon.codex.chapter.reagents",
                reagents.toArray(new Page[0])
        );

        Optional<CrucibleRecipeWrapper> soulGem = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "lesser_soul_gem")));
        CruciblePage.CrucibleStep[] gemSteps = soulGem.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack gemOutput = soulGem.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(new ResourceLocation(Eidolon.MODID, "lesser_soul_gem"));

        List<Page> soulGems = Lists.newArrayList(
                new TitlePage("eidolon.codex.page.soul_gems"),
                new CruciblePage(gemOutput, gemSteps));

        soulGems.addAll(getCruciblePagesOf(crucibleRecipes, CrucibleRecipeWrapper.Category.SOUL_GEMS));

        SOUL_GEMS = new Chapter(
                "eidolon.codex.chapter.soul_gems",
                soulGems.toArray(new Page[0])
        );

        Optional<CrucibleRecipeWrapper> shadowGem = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "shadow_gem")));
        CruciblePage.CrucibleStep[] shadowSteps = shadowGem.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack shadowOutput = shadowGem.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(shadowGem.map(IRecipe::getId).orElse(null));


        SHADOW_GEM = new Chapter(
                "eidolon.codex.chapter.shadow_gem",
                new TitlePage("eidolon.codex.page.shadow_gem"),
                new CruciblePage(shadowOutput, shadowSteps)
        );

        Optional<CrucibleRecipeWrapper> warpedSprouts = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "warped_sprouts")));
        CruciblePage.CrucibleStep[] sproutsSteps = warpedSprouts.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack sproutsOutput = warpedSprouts.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(warpedSprouts.map(IRecipe::getId).orElse(null));

        List<Page> sprouts = Lists.newArrayList(
                new TitlePage("eidolon.codex.page.warped_sprouts.0"),
                new CruciblePage(sproutsOutput, sproutsSteps),
                new TitlePage("eidolon.codex.page.warped_sprouts.1"));
        sprouts.addAll(getCruciblePagesOf(crucibleRecipes, CrucibleRecipeWrapper.Category.WARPED_SPROUTS));

        WARPED_SPROUTS = new Chapter(
                "eidolon.codex.chapter.warped_sprouts",
                sprouts.toArray(new Page[0])
        );

        Optional<CrucibleRecipeWrapper> leather = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "leather_from_flesh")));
        CruciblePage.CrucibleStep[] leatherSteps = leather.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack leatherOutput = leather.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(leather.map(IRecipe::getId).orElse(null));


        Optional<CrucibleRecipeWrapper> rottenBeef = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "rotten_beef")));
        CruciblePage.CrucibleStep[] beefSteps = rottenBeef.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack beefOutput = rottenBeef.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(rottenBeef.map(IRecipe::getId).orElse(null));


        Optional<CrucibleRecipeWrapper> gunpowder = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "gunpowder")));
        CruciblePage.CrucibleStep[] powderSteps = gunpowder.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack powderOutput = gunpowder.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(gunpowder.map(IRecipe::getId).orElse(null));


        Optional<CrucibleRecipeWrapper> goldenApple = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "gilded_apple")));
        CruciblePage.CrucibleStep[] appleSteps = goldenApple.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack appleOutput = goldenApple.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(goldenApple.map(IRecipe::getId).orElse(null));


        Optional<CrucibleRecipeWrapper> goldCarrot = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "gilded_carrot")));
        CruciblePage.CrucibleStep[] carrotSteps = goldCarrot.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack carrotOutput = goldCarrot.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(goldCarrot.map(IRecipe::getId).orElse(null));


        Optional<CrucibleRecipeWrapper> gildedMelon = Optional.ofNullable(crucibleRecipes.get(new ResourceLocation(Eidolon.MODID, "gilded_melon")));
        CruciblePage.CrucibleStep[] melonSteps = gildedMelon.map(CrucibleRecipe::getSteps).map(EidolonReflectedRegistries::getCrucibleSteps).orElse(new CruciblePage.CrucibleStep[0]);
        ItemStack melonOutput = gildedMelon.map(CrucibleRecipe::getResult).orElse(ItemStack.EMPTY);
        crucibleRecipes.remove(gildedMelon.map(IRecipe::getId).orElse(null));

        List<Page> alchemy = Lists.newArrayList(
                new TitlePage("eidolon.codex.page.basic_alchemy.0"),
                new CruciblePage(leatherOutput, leatherSteps),
                new TitlePage("eidolon.codex.page.basic_alchemy.1"),
                new CruciblePage(beefOutput, beefSteps),
                new TitlePage("eidolon.codex.page.basic_alchemy.2"),
                new CruciblePage(powderOutput, powderSteps),
                new TitlePage("eidolon.codex.page.basic_alchemy.3"),
                new CruciblePage(appleOutput, appleSteps),
                new CruciblePage(carrotOutput, carrotSteps),
                new CruciblePage(melonOutput, melonSteps));

        alchemy.addAll(getCruciblePagesOf(crucibleRecipes, CrucibleRecipeWrapper.Category.BASIC_ALCHEMY));

        BASIC_ALCHEMY = new Chapter(
                "eidolon.codex.chapter.basic_alchemy",
                alchemy.toArray(new Page[0])
        );

        List<Page> miscCrucible = Lists.newArrayList(new TitlePage("eidolon.codex.page.misc_crucible"));
        miscCrucible.addAll(getCruciblePagesOf(crucibleRecipes, CrucibleRecipeWrapper.Category.MISC));

        MISC_CRUCIBLE = new Chapter("eidolon.codex.page.misc_crucible.title",
                miscCrucible.toArray(new Page[0]));


        Optional<ICraftingRecipe> pewterInlay = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "pewter_inlay")));
        ItemStack[] pewterInlayInputs = pewterInlay.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack pewterInlayOutput = pewterInlay.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        Optional<ICraftingRecipe> goldInlay = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "gold_inlay")));
        ItemStack[] goldInlayInputs = goldInlay.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack goldInlayOutput = goldInlay.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);


        INLAYS = new Chapter(
                "eidolon.codex.chapter.inlays",
                new TitlePage("eidolon.codex.page.inlays"),
                new CraftingPage(pewterInlayOutput, pewterInlayInputs),
                new CraftingPage(goldInlayOutput, goldInlayInputs)
        );

        Optional<ICraftingRecipe> basicAmulet = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "basic_amulet")));
        ItemStack[] amuletInputs = basicAmulet.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack amuletOutput = basicAmulet.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        Optional<ICraftingRecipe> basicRing = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "basic_ring")));
        ItemStack[] ringInputs = basicRing.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack ringOutput = basicRing.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        Optional<ICraftingRecipe> basicBelt = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "basic_belt")));
        ItemStack[] beltInputs = basicBelt.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack beltOutput = basicBelt.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);


        BASIC_BAUBLES = new Chapter(
                "eidolon.codex.chapter.basic_baubles",
                new TitlePage("eidolon.codex.page.basic_baubles"),
                new CraftingPage(amuletOutput, amuletInputs),
                new CraftingPage(ringOutput, ringInputs),
                new CraftingPage(beltOutput, beltInputs)
        );


        Optional<ICraftingRecipe> worktable = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "worktable")));
        ItemStack[] tableInputs = worktable.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack tableOutput = worktable.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        MAGIC_WORKBENCH = new Chapter(
                "eidolon.codex.chapter.magic_workbench",
                new TitlePage("eidolon.codex.page.magic_workbench"),
                new CraftingPage(tableOutput, tableInputs)
        );

        Optional<WorktableRecipeWrapper> voidAmulet = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "void_amulet")));
        ItemStack[] voidAmuletInputs = voidAmulet.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack voidAmuletOutput = voidAmulet.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "void_amulet"));

        VOID_AMULET = new Chapter(
                "eidolon.codex.chapter.void_amulet",
                new TitlePage("eidolon.codex.page.void_amulet"),
                new WorktablePage(voidAmuletOutput, voidAmuletInputs)
        );

        Optional<WorktableRecipeWrapper> wardedMail = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "warded_mail")));
        ItemStack[] mailInputs = wardedMail.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack mailOutput = wardedMail.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "warded_mail"));


        WARDED_MAIL = new Chapter(
                "eidolon.codex.chapter.warded_mail",
                new TitlePage("eidolon.codex.page.warded_mail"),
                new WorktablePage(mailOutput, mailInputs)
        );

        Optional<WorktableRecipeWrapper> soulWand = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "soulfire_wand")));
        ItemStack[] soulWandInputs = soulWand.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack soulWandOutput = soulWand.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "soulfire_wand"));


        SOULFIRE_WAND = new Chapter(
                "eidolon.codex.chapter.soulfire_wand",
                new TitlePage("eidolon.codex.page.soulfire_wand"),
                new WorktablePage(soulWandOutput, soulWandInputs)
        );

        Optional<WorktableRecipeWrapper> boneWand = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "bonechill_wand")));
        ItemStack[] boneWandInputs = boneWand.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack boneWandOutput = boneWand.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "bonechill_wand"));


        BONECHILL_WAND = new Chapter(
                "eidolon.codex.chapter.bonechill_wand",
                new TitlePage("eidolon.codex.page.bonechill_wand"),
                new WorktablePage(boneWandOutput, boneWandInputs)
        );

        Optional<WorktableRecipeWrapper> reaperScythe = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "reaper_scythe")));
        ItemStack[] scytheInputs = reaperScythe.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack scytheOutput = reaperScythe.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "reaper_scythe"));


        REAPER_SCYTHE = new Chapter(
                "eidolon.codex.chapter.reaper_scythe",
                new TitlePage("eidolon.codex.page.reaper_scythe"),
                new WorktablePage(scytheOutput, scytheInputs)
        );

        Optional<WorktableRecipeWrapper> cleavingAxe = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "cleaving_axe")));
        ItemStack[] axeInputs = cleavingAxe.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack axeOutput = cleavingAxe.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "cleaving_axe"));


        CLEAVING_AXE = new Chapter(
                "eidolon.codex.chapter.cleaving_axe",
                new TitlePage("eidolon.codex.page.cleaving_axe"),
                new WorktablePage(axeOutput, axeInputs)
        );

        Optional<WorktableRecipeWrapper> soulEnchanter = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "soul_enchanter")));
        ItemStack[] enchanterInputs = soulEnchanter.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack enchanterOutput = soulEnchanter.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "soul_enchanter"));


        SOUL_ENCHANTER = new Chapter(
                "eidolon.codex.chapter.soul_enchanter",
                new TitlePage("eidolon.codex.page.soul_enchanter.0"),
                new TextPage("eidolon.codex.page.soul_enchanter.1"),
                new WorktablePage(enchanterOutput, enchanterInputs)
        );

        Optional<WorktableRecipeWrapper> reversalPick = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "reversal_pick")));
        ItemStack[] pickInputs = reversalPick.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack pickOutput = reversalPick.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "reversal_pick"));


        REVERSAL_PICK = new Chapter(
                "eidolon.codex.chapter.reversal_pick",
                new TitlePage("eidolon.codex.page.reversal_pick"),
                new WorktablePage(pickOutput, pickInputs)
        );

        Optional<WorktableRecipeWrapper> wickedWeave = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "wicked_weave")));
        ItemStack[] weaveInputs = wickedWeave.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack weaveOutput = wickedWeave.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "wicked_weave"));


        Optional<WorktableRecipeWrapper> warlockHat = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "warlock_hat")));
        ItemStack[] hatInputs = warlockHat.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack hatOutput = warlockHat.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "warlock_hat"));


        Optional<WorktableRecipeWrapper> warlockCloak = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "warlock_cloak")));
        ItemStack[] cloakInputs = warlockCloak.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack cloakOutput = warlockCloak.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "warlock_cloak"));


        Optional<WorktableRecipeWrapper> warlockBoots = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "warlock_boots")));
        ItemStack[] bootsInput = warlockBoots.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack bootsOutput = warlockBoots.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "warlock_boots"));


        WARLOCK_ARMOR = new Chapter(
                "eidolon.codex.chapter.warlock_armor",
                new TitlePage("eidolon.codex.page.warlock_armor.0"),
                new WorktablePage(weaveOutput, weaveInputs),
                new TitlePage("eidolon.codex.page.warlock_armor.1"),
                new WorktablePage(hatOutput, hatInputs),
                new TitlePage("eidolon.codex.page.warlock_armor.2"),
                new WorktablePage(cloakOutput, cloakInputs),
                new TitlePage("eidolon.codex.page.warlock_armor.3"),
                new WorktablePage(bootsOutput, bootsInput)
        );

        Optional<WorktableRecipeWrapper> gravityBelt = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "gravity_belt")));
        ItemStack[] gravityInputs = gravityBelt.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack gravityOutput = gravityBelt.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "gravity_belt"));


        GRAVITY_BELT = new Chapter(
                "eidolon.codex.chapter.gravity_belt",
                new TitlePage("eidolon.codex.page.gravity_belt"),
                new WorktablePage(gravityOutput, gravityInputs)
        );

        Optional<WorktableRecipeWrapper> prestigiousPalm = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "prestigious_palm")));
        ItemStack[] palmInputs = prestigiousPalm.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack palmOutput = prestigiousPalm.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "prestigious_palm"));


        PRESTIGIOUS_PALM = new Chapter(
                "eidolon.codex.chapter.prestigious_palm",
                new TitlePage("eidolon.codex.page.prestigious_palm"),
                new WorktablePage(palmOutput, palmInputs)
        );

        Optional<WorktableRecipeWrapper> mindShieldingPlate = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "mind_shielding_plate")));
        ItemStack[] plateInputs = mindShieldingPlate.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack plateOutput = mindShieldingPlate.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "mind_shielding_plate"));


        MIND_SHIELDING_PLATE = new Chapter(
                "eidolon.codex.chapter.mind_shielding_plate",
                new TitlePage("eidolon.codex.page.mind_shielding_plate"),
                new WorktablePage(plateOutput, plateInputs)
        );

        Optional<WorktableRecipeWrapper> resoluteBelt = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "resolute_belt")));
        ItemStack[] resoluteInputs = resoluteBelt.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack resoluteOutput = resoluteBelt.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "resolute_belt"));


        RESOLUTE_BELT = new Chapter(
                "eidolon.codex.chapter.resolute_belt",
                new TitlePage("eidolon.codex.page.resolute_belt"),
                new WorktablePage(resoluteOutput, resoluteInputs)
        );

        Optional<WorktableRecipeWrapper> glassHand = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "glass_hand")));
        ItemStack[] handInputs = glassHand.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack handOutput = glassHand.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "glass_hand"));


        GLASS_HAND = new Chapter(
                "eidolon.codex.chapter.glass_hand",
                new TitlePage("eidolon.codex.page.glass_hand"),
                new WorktablePage(handOutput, handInputs)
        );

        List<Page> miscWorkbench = new ArrayList<>();
        miscWorkbench.add(new TitlePage("eidolon.codex.page.misc_workbench"));
        miscWorkbench.addAll(worktableRecipes.values().stream().filter(recipe ->
                !recipe.getId().equals(getName("unholy_effigy"))
                && !recipe.getId().equals(getName("stone_altar"))).map(recipe ->
                new WorktablePage(recipe.getRecipeOutput(), getWorktableInputs(recipe))).collect(Collectors.toList()));

        MISC_WORKBENCH = new Chapter("eidolon.codex.page.misc_workbench.title",
                miscWorkbench.toArray(new Page[0]));

        ARTIFICE_INDEX = new Chapter(
                "eidolon.codex.chapter.artifice",
                new TitledIndexPage("eidolon.codex.page.artifice",
                        new IndexPage.IndexEntry(WOODEN_STAND, new ItemStack(Registry.WOODEN_STAND.get())),
                        new IndexPage.IndexEntry(TALLOW, new ItemStack(Registry.TALLOW.get())),
                        new IndexPage.IndexEntry(CRUCIBLE, new ItemStack(Registry.CRUCIBLE.get())),
                        new IndexPage.IndexEntry(ARCANE_GOLD, new ItemStack(Registry.ARCANE_GOLD_INGOT.get())),
                        new IndexPage.IndexEntry(REAGENTS, new ItemStack(Registry.DEATH_ESSENCE.get())),
                        new IndexPage.IndexEntry(SOUL_GEMS, new ItemStack(Registry.LESSER_SOUL_GEM.get())),
                        new IndexPage.IndexEntry(MISC_CRUCIBLE, new ItemStack(Registry.LEAD_INGOT.get()))
                ),
                new IndexPage(
                        new IndexPage.IndexEntry(SHADOW_GEM, new ItemStack(Registry.SHADOW_GEM.get())),
                        new IndexPage.IndexEntry(BASIC_ALCHEMY, new ItemStack(Items.GUNPOWDER)),
                        new IndexPage.IndexEntry(WARPED_SPROUTS, new ItemStack(Registry.WARPED_SPROUTS.get())),
                        new IndexPage.IndexEntry(INLAYS, new ItemStack(Registry.GOLD_INLAY.get())),
                        new IndexPage.IndexEntry(BASIC_BAUBLES, new ItemStack(Registry.BASIC_RING.get())),
                        new IndexPage.IndexEntry(MAGIC_WORKBENCH, new ItemStack(Registry.WORKTABLE.get())),
                        new IndexPage.IndexEntry(VOID_AMULET, new ItemStack(Registry.VOID_AMULET.get()))
                ),
                new IndexPage(
                        new IndexPage.IndexEntry(WARDED_MAIL, new ItemStack(Registry.WARDED_MAIL.get())),
                        new IndexPage.IndexEntry(SOULFIRE_WAND, new ItemStack(Registry.SOULFIRE_WAND.get())),
                        new IndexPage.IndexEntry(BONECHILL_WAND, new ItemStack(Registry.BONECHILL_WAND.get())),
                        new IndexPage.IndexEntry(REAPER_SCYTHE, new ItemStack(Registry.REAPER_SCYTHE.get())),
                        new IndexPage.IndexEntry(CLEAVING_AXE, new ItemStack(Registry.CLEAVING_AXE.get())),
                        new IndexPage.IndexEntry(SOUL_ENCHANTER, new ItemStack(Registry.SOUL_ENCHANTER.get())),
                        new IndexPage.IndexEntry(REVERSAL_PICK, new ItemStack(Registry.REVERSAL_PICK.get()))
                ),
                new IndexPage(
                        new IndexPage.IndexEntry(WARLOCK_ARMOR, new ItemStack(Registry.WARLOCK_HAT.get())),
                        new IndexPage.IndexEntry(GRAVITY_BELT, new ItemStack(Registry.GRAVITY_BELT.get())),
                        new IndexPage.IndexEntry(PRESTIGIOUS_PALM, new ItemStack(Registry.PRESTIGIOUS_PALM.get())),
                        new IndexPage.IndexEntry(MIND_SHIELDING_PLATE, new ItemStack(Registry.MIND_SHIELDING_PLATE.get())),
                        new IndexPage.IndexEntry(RESOLUTE_BELT, new ItemStack(Registry.RESOLUTE_BELT.get())),
                        new IndexPage.IndexEntry(GLASS_HAND, new ItemStack(Registry.GLASS_HAND.get())),
                        new IndexPage.IndexEntry(MISC_WORKBENCH, new ItemStack(Registry.WICKED_WEAVE.get()))
                )
        );

        categories.add(ARTIFICE = new Category(
                "artifice",
                new ItemStack(Registry.GOLD_INLAY.get()),
                ColorUtil.packColor(255, 204, 57, 72),
                ARTIFICE_INDEX
        ));

        INTRO_SIGNS = new Chapter(
                "eidolon.codex.chapter.intro_signs",
                new TitlePage("eidolon.codex.page.intro_signs.0"),
                new TextPage("eidolon.codex.page.intro_signs.1")
        );

        Optional<ICraftingRecipe> strawEffigy = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "straw_effigy")));
        ItemStack[] effigyInputs = strawEffigy.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack effigyOutput = strawEffigy.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        EFFIGY = new Chapter(
                "eidolon.codex.chapter.effigy",
                new TitlePage("eidolon.codex.page.effigy"),
                new CraftingPage(effigyOutput, effigyInputs)
        );

        Optional<ICraftingRecipe> woodenAltar = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "wooden_altar")));
        ItemStack[] woodInputs = woodenAltar.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack woodOutput = woodenAltar.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        ALTARS = new Chapter(
                "eidolon.codex.chapter.altars",
                new TitlePage("eidolon.codex.page.altars.0"),
                new TextPage("eidolon.codex.page.altars.1"),
                new CraftingPage(woodOutput, woodInputs)
        );

        ALTAR_LIGHTS = new Chapter(
                "eidolon.codex.chapter.altar_lights",
                new TitlePage("eidolon.codex.page.altar_lights.0"),
                new ListPage("eidolon.codex.page.altar_lights.1",
                        new ListPage.ListEntry("torch", new ItemStack(Items.TORCH)),
                        new ListPage.ListEntry("lantern", new ItemStack(Items.LANTERN)),
                        new ListPage.ListEntry("candle", new ItemStack(Registry.CANDLE.get())),
                        new ListPage.ListEntry("candlestick", new ItemStack(Registry.CANDLESTICK.get())))
        );

        ALTAR_SKULLS = new Chapter(
                "eidolon.codex.chapter.altar_skulls",
                new TitlePage("eidolon.codex.page.altar_skulls.0"),
                new ListPage("eidolon.codex.page.altar_skulls.1",
                        new ListPage.ListEntry("skull", new ItemStack(Items.SKELETON_SKULL)),
                        new ListPage.ListEntry("zombie", new ItemStack(Items.ZOMBIE_HEAD)),
                        new ListPage.ListEntry("wither_skull", new ItemStack(Items.WITHER_SKELETON_SKULL)))
        );

        ALTAR_HERBS = new Chapter(
                "eidolon.codex.chapter.altar_herbs",
                new TitlePage("eidolon.codex.page.altar_herbs.0"),
                new ListPage("eidolon.codex.page.altar_herbs.1",
                        new ListPage.ListEntry("crimson_fungus", new ItemStack(Items.CRIMSON_FUNGUS)),
                        new ListPage.ListEntry("warped_fungus", new ItemStack(Items.WARPED_FUNGUS)),
                        new ListPage.ListEntry("wither_rose", new ItemStack(Items.WITHER_ROSE)))
        );

        Optional<ICraftingRecipe> goblet = Optional.ofNullable(craftingRecipes.get(new ResourceLocation(Eidolon.MODID, "goblet")));
        ItemStack[] gobletInputs = goblet.map(EidolonReflectedRegistries::getCraftingInputs).orElse(new ItemStack[0]);
        ItemStack gobletOutput = goblet.map(IRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);

        GOBLET = new Chapter(
                "eidolon.codex.chapter.goblet",
                new TitlePage("eidolon.codex.page.goblet"),
                new CraftingPage(gobletOutput, gobletInputs)
        );

        Optional<SpellRecipeWrapper> darkPrayer = Optional.ofNullable(spellRecipes.get(new ResourceLocation(Eidolon.MODID, "dark_prayer")));
        Sign[] prayerSigns = darkPrayer.map(SpellRecipeWrapper::getSigns).orElse(new Sign[0]);
        String prayerTitle = darkPrayer.map(SpellRecipeWrapper::getTitle).orElse("");
        String prayerChant = darkPrayer.map(SpellRecipeWrapper::getChant).orElse("");
        String prayerText = darkPrayer.map(SpellRecipeWrapper::getText).orElse("");

        DARK_PRAYER = new Chapter(prayerTitle,
                new ChantPage2(prayerTitle, prayerChant, prayerSigns),
                new TextPage(prayerText)
        );

        spellRecipes.remove(new ResourceLocation(Eidolon.MODID, "dark_prayer"));

        Optional<SpellRecipeWrapper> animalSacrifice = Optional.ofNullable(spellRecipes.get(new ResourceLocation(Eidolon.MODID, "dark_animal_sacrifice")));
        Sign[] sacrificeSigns = animalSacrifice.map(SpellRecipeWrapper::getSigns).orElse(new Sign[0]);
        String sacrificeTitle = animalSacrifice.map(SpellRecipeWrapper::getTitle).orElse("");
        String sacrificeChant = animalSacrifice.map(SpellRecipeWrapper::getChant).orElse("");
        String sacrificeText = animalSacrifice.map(SpellRecipeWrapper::getText).orElse("");

        ANIMAL_SACRIFICE = new Chapter(sacrificeTitle,
                new ChantPage2(sacrificeTitle, sacrificeChant, sacrificeSigns),
                new TextPage(sacrificeText)
        );

        spellRecipes.remove(new ResourceLocation(Eidolon.MODID, "dark_animal_sacrifice"));


        Optional<SpellRecipeWrapper> darkTouch = Optional.ofNullable(spellRecipes.get(new ResourceLocation(Eidolon.MODID, "dark_touch")));
        Sign[] touchSigns = darkTouch.map(SpellRecipeWrapper::getSigns).orElse(new Sign[0]);
        String touchTitle = darkTouch.map(SpellRecipeWrapper::getTitle).orElse("");
        String touchChant = darkTouch.map(SpellRecipeWrapper::getChant).orElse("");
        String touchText = darkTouch.map(SpellRecipeWrapper::getText).orElse("");

        DARK_TOUCH = new Chapter(touchTitle,
                new ChantPage2(touchTitle, touchChant, touchSigns),
                new TextPage(touchText)
        );
        spellRecipes.remove(new ResourceLocation(Eidolon.MODID, "dark_touch"));

        Optional<WorktableRecipeWrapper> stoneAltar = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "stone_altar")));
        ItemStack[] stoneInputs = stoneAltar.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack stoneOutput = stoneAltar.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "stone_altar"));


        STONE_ALTAR = new Chapter(
                "eidolon.codex.chapter.stone_altar",
                new TitlePage("eidolon.codex.page.stone_altar"),
                new WorktablePage(stoneOutput, stoneInputs)
        );

        Optional<WorktableRecipeWrapper> unholyEffigy = Optional.ofNullable(worktableRecipes.get(new ResourceLocation(Eidolon.MODID, "unholy_effigy")));
        ItemStack[] unholyInputs = unholyEffigy.map(EidolonReflectedRegistries::getWorktableInputs).orElse(new ItemStack[0]);
        ItemStack unholyOutput = unholyEffigy.map(WorktableRecipe::getRecipeOutput).orElse(ItemStack.EMPTY);
        worktableRecipes.remove(new ResourceLocation(Eidolon.MODID, "unholy_effigy"));


        UNHOLY_EFFIGY = new Chapter(
                "eidolon.codex.chapter.unholy_effigy",
                new TitlePage("eidolon.codex.page.unholy_effigy"),
                new WorktablePage(unholyOutput, unholyInputs)
        );

        Optional<SpellRecipeWrapper> villagerSacrifice = Optional.ofNullable(spellRecipes.get(new ResourceLocation(Eidolon.MODID, "dark_villager_sacrifice")));
        Sign[] villagerSigns = villagerSacrifice.map(SpellRecipeWrapper::getSigns).orElse(new Sign[0]);
        String villagerTitle = villagerSacrifice.map(SpellRecipeWrapper::getTitle).orElse("");
        String villagerChant = villagerSacrifice.map(SpellRecipeWrapper::getChant).orElse("");
        String villagerText = villagerSacrifice.map(SpellRecipeWrapper::getText).orElse("");

        VILLAGER_SACRIFICE = new Chapter(villagerTitle,
                new ChantPage2(villagerTitle, villagerChant, villagerSigns),
                new TextPage(villagerText)
        );
        spellRecipes.remove(new ResourceLocation(Eidolon.MODID, "dark_villager_sacrifice"));

        List<Page> miscSpells = new ArrayList<>();
        spellRecipes.values().forEach(spell -> {
            miscSpells.add(new ChantPage2(spell.getTitle(), spell.getChant(), spell.getSigns()));
            miscSpells.add(new TextPage(spell.getText()));
        });

        MISC_SPELLS = new Chapter("eidolon.codex.page.misc_spell.title", miscSpells.toArray(new Page[0]));

        THEURGY_INDEX = new Chapter(
                "eidolon.codex.chapter.theurgy",
                new TitledIndexPage(
                        "eidolon.codex.page.theurgy",
                        new IndexPage.IndexEntry(INTRO_SIGNS, new ItemStack(Items.PAPER)),
                        new IndexPage.IndexEntry(EFFIGY, new ItemStack(Registry.STRAW_EFFIGY.get())),
                        new IndexPage.IndexEntry(ALTARS, new ItemStack(Registry.WOODEN_ALTAR.get())),
                        new IndexPage.IndexEntry(ALTAR_LIGHTS, new ItemStack(Registry.CANDLE.get())),
                        new IndexPage.IndexEntry(ALTAR_SKULLS, new ItemStack(Items.SKELETON_SKULL)),
                        new IndexPage.IndexEntry(ALTAR_HERBS, new ItemStack(Items.WITHER_ROSE))
                ),
                new IndexPage(
                        new IndexPage.IndexEntry(GOBLET, new ItemStack(Registry.GOBLET.get())),
                        new IndexPage.SignLockedEntry(DARK_PRAYER, new ItemStack(Registry.SHADOW_GEM.get()), Signs.WICKED_SIGN),
                        new IndexPage.SignLockedEntry(ANIMAL_SACRIFICE, new ItemStack(Items.PORKCHOP), Signs.BLOOD_SIGN),
                        new IndexPage.SignLockedEntry(DARK_TOUCH, new ItemStack(Registry.UNHOLY_SYMBOL.get()), Signs.SOUL_SIGN, Signs.WICKED_SIGN),
                        new IndexPage.SignLockedEntry(STONE_ALTAR, new ItemStack(Registry.STONE_ALTAR.get()), Signs.SOUL_SIGN),
                        new IndexPage.SignLockedEntry(UNHOLY_EFFIGY, new ItemStack(Registry.UNHOLY_EFFIGY.get()), Signs.WICKED_SIGN, Signs.SOUL_SIGN, Signs.WICKED_SIGN),
                        new IndexPage.FactLockedEntry(VILLAGER_SACRIFICE, new ItemStack(Items.IRON_SWORD), Facts.VILLAGER_SACRIFICE),
                        new IndexPage.FactLockedEntry(MISC_SPELLS, new ItemStack(Registry.PRESTIGIOUS_PALM.get()), Facts.VILLAGER_SACRIFICE)
                )
        );

        categories.add(THEURGY = new Category(
                "theurgy",
                new ItemStack(Registry.GOBLET.get()),
                ColorUtil.packColor(255, 94, 90, 219),
                THEURGY_INDEX
        ));


        /*List<Sign> signs = Signs.getSigns();

        List<SignIndexPage> indexPages = new ArrayList<>();

        SignIndexPage.SignEntry[] signChapters = new SignIndexPage.SignEntry[signs.size()];
        List<SignIndexPage.SignEntry> signCache = Arrays.asList(signChapters);

        int i = 0;
        for (Sign sign: signs) {
            signChapters[i] = new SignIndexPage.SignEntry(
                    new Chapter("eidolon.codex.chapter." + sign.getRegistryName().getPath() + "_sign",
                    new TitlePage("eidolon.codex.page." + sign.getRegistryName().getPath() + "_sign"),
                    new SignPage(sign)), sign);
            i++;

            if (i % 6 == 0) {
                indexPages.add(new SignIndexPage())
                signCache.removeIf(signEntry -> {

                    for (SignIndexPage.SignEntry signChapter : signChapters) {
                        if (signEntry == signChapter) {
                            return true;
                        }
                    }
                    return false;
                });
            }
        }*/

        WICKED_SIGN = new Chapter(
                "eidolon.codex.chapter.wicked_sign",
                new TitlePage("eidolon.codex.page.wicked_sign"),
                new SignPage(Signs.WICKED_SIGN)
        );

        SACRED_SIGN = new Chapter(
                "eidolon.codex.chapter.sacred_sign",
                new TitlePage("eidolon.codex.page.sacred_sign"),
                new SignPage(Signs.SACRED_SIGN)
        );

        BLOOD_SIGN = new Chapter(
                "eidolon.codex.chapter.blood_sign",
                new TitlePage("eidolon.codex.page.blood_sign"),
                new SignPage(Signs.BLOOD_SIGN)
        );

        SOUL_SIGN = new Chapter(
                "eidolon.codex.chapter.soul_sign",
                new TitlePage("eidolon.codex.page.soul_sign"),
                new SignPage(Signs.SOUL_SIGN)
        );

        MIND_SIGN = new Chapter(
                "eidolon.codex.chapter.mind_sign",
                new TitlePage("eidolon.codex.page.mind_sign"),
                new SignPage(Signs.MIND_SIGN)
        );

        SIGNS_INDEX = new Chapter(
                "eidolon.codex.chapter.signs_index",
                new SignIndexPage(
                        new SignIndexPage.SignEntry(WICKED_SIGN, Signs.WICKED_SIGN),
                        new SignIndexPage.SignEntry(SACRED_SIGN, Signs.SACRED_SIGN),
                        new SignIndexPage.SignEntry(BLOOD_SIGN, Signs.BLOOD_SIGN),
                        new SignIndexPage.SignEntry(SOUL_SIGN, Signs.SOUL_SIGN),
                        new SignIndexPage.SignEntry(MIND_SIGN, Signs.MIND_SIGN)
                )
        );

        categories.add(SIGNS = new Category(
                "signs",
                new ItemStack(Registry.UNHOLY_SYMBOL.get()),
                ColorUtil.packColor(255, 163, 74, 207),
                SIGNS_INDEX
        ));
    }

    public static ResourceLocation getName(String path) {
        return new ResourceLocation(Eidolon.MODID, path);
    }

    private static ItemStack[] getCraftingInputs(IRecipe<?> recipe) {
        ItemStack[] inputs = new ItemStack[recipe.getIngredients().size()];
        int i = 0;

        for (Ingredient ingredient : recipe.getIngredients()) {
            ItemStack input = Arrays.stream(ingredient.getMatchingStacks()).findFirst().orElse(ItemStack.EMPTY);
            inputs[i] = input;
            i++;

        }
        return inputs;
    }

    private static CruciblePage.CrucibleStep[] getCrucibleSteps(List<CrucibleRecipe.Step> steps) {
        List<CruciblePage.CrucibleStep> pageSteps = new ArrayList<>();

        steps.forEach(step -> {
            List<Object> ingredients = step.matches;
            List<ItemStack> inputs = new ArrayList<>();

            ingredients.forEach(o -> {
                Ingredient ingredient = (Ingredient) o;
                ItemStack input = Arrays.stream(ingredient.getMatchingStacks()).findFirst().orElse(ItemStack.EMPTY).copy();
                if (inputs.stream().anyMatch(stack -> stack.getItem().equals(input.getItem()))) {
                    ItemStack newStack = inputs
                            .stream()
                            .filter(stack -> stack.getItem() == input.getItem()).findFirst().get();
                    inputs.removeIf(stack -> stack.getItem() == input.getItem());
                    input.setCount(newStack.getCount() + 1);
                }
                inputs.add(input);
            });

            if (step.stirs > 0) {
                pageSteps.add(new CruciblePage.CrucibleStep(step.stirs, inputs.toArray(new ItemStack[0])));
            } else {
                pageSteps.add(new CruciblePage.CrucibleStep(inputs.toArray(new ItemStack[0])));
            }
        });
        return pageSteps.toArray(new CruciblePage.CrucibleStep[0]);
    }

    private static ItemStack[] getWorktableInputs(WorktableRecipeWrapper wrapper) {
        ItemStack[] inputs = new ItemStack[wrapper.getCore().size() + wrapper.getOuter().size()];
        Arrays.fill(inputs, ItemStack.EMPTY);
        int i = 0;

        for (Object o : wrapper.getCore()) {

            if (o instanceof ItemStack) {
                inputs[i] = (ItemStack) o;
            } else if (o instanceof Ingredient) {
                Ingredient ingredient = (Ingredient) o;
                ItemStack input = ingredient.getMatchingStacks()[0];
                inputs[i] = input;
            }
            i++;
        }

        for (Object o : wrapper.getOuter()) {
            if (o instanceof ItemStack) {
                inputs[i] = (ItemStack) o;
            } else if (o instanceof Ingredient) {
                Ingredient ingredient = (Ingredient) o;
                ItemStack input = ingredient.getMatchingStacks()[0];
                inputs[i] = input;
            }
            i++;
        }
        return inputs;
    }

    private static List<Page> getCruciblePagesOf(Map<ResourceLocation, CrucibleRecipeWrapper> crucibleRecipes,
                                                 CrucibleRecipeWrapper.Category category) {
        Map<ResourceLocation, CrucibleRecipeWrapper> reagents = new HashMap<>();
        List<Page> pages = new ArrayList<>();

        for (CrucibleRecipeWrapper wrapper : crucibleRecipes.values()) {
            if (wrapper.category == category) {
                reagents.put(wrapper.getId(), wrapper);
            }
        }
        reagents.forEach((id, wrapper) -> {
            if (!wrapper.title.isEmpty()) {
                pages.add(new TitlePage2(wrapper.title, wrapper.description));
            }
            CruciblePage.CrucibleStep[] steps = getCrucibleSteps(wrapper.getSteps());
            pages.add(new CruciblePage(wrapper.getResult(), steps));
        });
        return pages;
    }

    private static ItemStack getRitualIcon(Ritual ritual) {
        if (ritual instanceof RitualRecipeWrapper) {
            RitualRecipeWrapper wrapper = (RitualRecipeWrapper) ritual;
            Optional<RitualRecipeWrapper.Result> firstResult = wrapper.getResults().stream().findFirst();
            if (firstResult.isPresent()) {
                RitualRecipeWrapper.Result result = firstResult.get();
                return RitualManager.RESULTS.get(result.getVariant()).getIcon(result);
            }
        }
        return new ItemStack(Registry.BRAZIER.get());
    }
}
