package dev.infernal_coding.eidolonrecipes.jei;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirement;
import dev.infernal_coding.eidolonrecipes.spells.requirement.impl.AltarRequirement;
import dev.infernal_coding.eidolonrecipes.spells.requirement.impl.GobletRequirement;
import dev.infernal_coding.eidolonrecipes.util.SpellUtil;
import elucent.eidolon.ClientEvents;
import elucent.eidolon.ClientRegistry;
import elucent.eidolon.Eidolon;
import elucent.eidolon.Registry;
import elucent.eidolon.capability.IKnowledge;
import elucent.eidolon.codex.CodexGui;
import elucent.eidolon.spell.Sign;
import elucent.eidolon.spell.Signs;
import elucent.eidolon.util.RenderUtil;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.tags.ITag;
import net.minecraft.advancements.critereon.EntityTypePredicate;

import java.lang.reflect.Field;
import java.util.*;

import static dev.infernal_coding.eidolonrecipes.util.ChantPage2.colorBlit;

public class SpellCategory implements IRecipeCategory<SpellRecipeWrapper>  {

    static final Screen screen = Minecraft.getInstance().screen;
    static final Font font = Minecraft.getInstance().font;
    static final ResourceLocation UID = new ResourceLocation(Eidolon.MODID, "spell");
    static final ResourceLocation backgroundId = new ResourceLocation(ModRoot.ID, "textures/gui/spell_jei.png");
    final IDrawable background, icon, woodAltar, stoneAltar, goblet, strawEffigy, unholyEffigy;
    final IGuiHelper guiHelper;

    static final Field getTag = ObfuscationReflectionHelper.findField(EntityTypePredicate.TagPredicate.class, "f_37653_");
    static final Map<ResourceLocation, Sign> signs =
            ObfuscationReflectionHelper.getPrivateValue(Signs.class, null,
                    "signMap");
    public SpellCategory(IGuiHelper guiHelper) {
        this.guiHelper = guiHelper;
        this.background = guiHelper.createDrawable(backgroundId, 0, 0, 125, 200);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Registry.STONE_ALTAR.get()));
        this.woodAltar = guiHelper.createDrawable(backgroundId, 207, 220, 50, 45);
        this.stoneAltar = guiHelper.createDrawable(backgroundId, 9, 222, 100, 39);
        this.strawEffigy = guiHelper.createDrawable(backgroundId, 139, 89, 100, 100);
        this.unholyEffigy = guiHelper.createDrawable(backgroundId, 164, 132, 53, 68);
        this.goblet = guiHelper.createDrawable(backgroundId, 45, 195, 16, 35);

    }

    @Override
    public RecipeType<SpellRecipeWrapper> getRecipeType() {
        return JEIRegistry.SPELL;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei." + Eidolon.MODID + ".spell");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder layout, SpellRecipeWrapper spell, IFocusGroup foci) {
        layout.addSlot(RecipeIngredientRole.INPUT, 0, 0);
    }

    @Override
    public void draw(SpellRecipeWrapper spell, IRecipeSlotsView slotsView, PoseStack mStack, double mouseX, double mouseY) {
      drawAltarReqs(spell, mStack, (int) mouseX, (int) mouseY);
      drawSacrifice(spell, mStack, (int) mouseX, (int) mouseY);
      drawSigns(spell, mStack, (int) mouseX, (int) mouseY);
      //drawItems(spell, mStack, (int) mouseX, (int) mouseY);
    }
    public void drawAltarReqs(SpellRecipeWrapper spell, PoseStack mStack, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal("Altar Requirements"));

        for (ISpellRequirement requirement : spell.getRequirements()) {
            if (requirement instanceof AltarRequirement) {
                AltarRequirement altarRequirement = (AltarRequirement) requirement;

                if (altarRequirement.getRequiredAltar().orElse(Blocks.AIR).equals(Registry.STONE_ALTAR.get())) {
                    stoneAltar.draw(mStack, 18, 125);
                    tooltip.add(Registry.STONE_ALTAR.get().getName());
                    altarRequirement.getRequiredEffigy().ifPresent(effigy -> {
                        tooltip.add(effigy.getName());
                        if (effigy == Registry.UNHOLY_EFFIGY.get()) {
                            unholyEffigy.draw(mStack, 35, 70);
                        } else {
                            strawEffigy.draw(mStack, 35, 70);
                        }
                    });
                } else /*(altarRequirement.getRequiredAltar().orElse(Blocks.AIR).equals(Registry.WOODEN_ALTAR.get()))*/ {
                    woodAltar.draw(mStack, 43, 122);
                    tooltip.add(Registry.WOODEN_ALTAR.get().getName());
                    altarRequirement.getRequiredEffigy().ifPresent(effigy -> {
                            tooltip.add(effigy.getName());

                            if (effigy == Registry.UNHOLY_EFFIGY.get()) {
                                unholyEffigy.draw(mStack, 35, 70);
                            } else {
                                strawEffigy.draw(mStack, 35, 70);
                            }
                    });
                }
            }
        }
        if (SpellUtil.isInTooltipRange(18, 125, 125, 35, mouseX, mouseY)) {
            screen.renderComponentTooltip(mStack, tooltip, mouseX, mouseY, font);
        }
    }

    public void drawSacrifice(SpellRecipeWrapper spell, PoseStack mStack, int mouseX, int mouseY) {
        boolean hasSacrifices = Arrays.stream(spell.getRequirements()).anyMatch(req -> req instanceof GobletRequirement);

        if (hasSacrifices) {
            goblet.draw(mStack, 20, 100);
            List<Component> sacrifices =
                    Lists.newArrayList(Component.literal("Required Sacrifices:"));

            Arrays.stream(spell.getRequirements()).forEach(rq -> {
                if (rq instanceof GobletRequirement) {
                    GobletRequirement requirement = (GobletRequirement) rq;
                    requirement.getType().ifPresent(type -> {
                        if (type instanceof EntityTypePredicate.TypePredicate) {
                            ResourceLocation entityName = requirement.getSacrifice();
                            Component text = Component.translatable("entity."
                                    + entityName.getNamespace() + "." + entityName.getPath());
                            sacrifices.add(text);
                        } else if (type instanceof EntityTypePredicate.TagPredicate) {
                            TagKey<EntityType<?>> tag;
                            try {
                                tag = (TagKey<EntityType<?>>) getTag.get(type);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                            Component formattedTag = SpellUtil.getFormattedTag(tag);
                            sacrifices.add(formattedTag);
                        }
                    });

                    if (!requirement.getType().isPresent()) {
                        String gobletReq = requirement.getSacrifice().toString();

                        if (gobletReq.equals("eidolon:any")) {
                            sacrifices.add(Component.literal("Any"));
                        } else if (gobletReq.equals("eidolon:is_animal")) {
                            sacrifices.add(Component.literal("Animal"));
                        } else if (gobletReq.equals("eidolon:is_villager_or_player")) {
                            sacrifices.add(Component.literal("Villager or Player"));
                        }
                    }

                }
            });

            if (SpellUtil.isInTooltipRange(5, 100, 35, 20, mouseX, mouseY)) {
                screen.renderComponentTooltip(mStack, sacrifices, mouseX, mouseY, font);
            }
        }
    }

    /*public void drawItems(SpellRecipeWrapper spell, MatrixStack mStack, int mouseX, int mouseY) {

        if (spell.getSpell() instanceof TransmutationSpell) {

                int x = 20, y = 30;
                TransmutationSpell transmutationSpell = (TransmutationSpell) spell.getSpell();

                for (int i = 0; i < transmutationSpell.getTransmutations().size(); i++) {
                    AtomicInteger yInc = new AtomicInteger();
                    TransmutationSpell.TransmutationRecipe recipe = transmutationSpell.getTransmutations().get(i);
                    List<Ingredient> inputs = recipe.getIngredients();
                    List<ItemStack> outputs = recipe.getResults();
                    inputs.forEach(input -> {
                        ItemStack item = Arrays.stream(input.getMatchingStacks()).findFirst().orElse(ItemStack.EMPTY);
                        IDrawable drawable = guiHelper.createDrawableIngredient(item);
                        drawable.draw(mStack, x, y + yInc.get());

                        List<ITextComponent> tooltip = screen.getTooltipFromItem(item);

                        if (SpellUtil.isInTooltipRange(x, y + yInc.get(), 5, 5, mouseX, mouseY)) {
                            screen.renderWrappedToolTip(mStack, tooltip, mouseX, mouseY, font);
                        }
                        yInc.getAndAdd(20);
                    });
                    yInc.set(0);

                    outputs.forEach(output -> {
                        IDrawable drawable = guiHelper.createDrawableIngredient(output);
                        drawable.draw(mStack, x + 40, y + yInc.get());

                        List<ITextComponent> tooltip = screen.getTooltipFromItem(output);

                        if (SpellUtil.isInTooltipRange(x + 40, y + yInc.get(), 5, 5, mouseX, mouseY)) {
                            screen.renderWrappedToolTip(mStack, tooltip, mouseX, mouseY, font);
                        }
                        yInc.getAndAdd(20);
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
        }
    }*/

    public void drawSigns(SpellRecipeWrapper spell, PoseStack mStack, int mouseX, int mouseY) {
        int x = 5;
        int y = 3;

        List<Sign> signs = Arrays.asList(spell.getSigns());

        Objects.requireNonNull(Minecraft.getInstance().font);
        RenderSystem.setShaderTexture(0, backgroundId);

        int w = signs.size() * 24;
        int baseX = x + 64 - w / 2;
        GuiComponent.blit(mStack, baseX - 16, y + 28, 256.0F, 208.0F, 16, 32, 512, 512);

        for(int i = 0; i < signs.size(); ++i) {
            GuiComponent.blit(mStack, baseX + i * 24, y + 28, 272.0F, 208.0F, 24, 32, 512, 512);
        }

        GuiComponent.blit(mStack, baseX + w, y + 28, 296.0F, 208.0F, 16, 32, 512, 512);
        Tesselator tess = Tesselator.getInstance();
        RenderSystem.enableBlend();

        for(int i = 0; i < signs.size(); ++i) {
            RenderSystem.setShaderTexture(0, backgroundId);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GuiComponent.blit(mStack, x, y, 312.0F, 208.0F, 24, 24, 512, 512);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            Sign sign = signs.get(i);
            float flicker = 0.875F + 0.125F * (float)Math.sin(Math.toRadians((double)(12.0F * ClientEvents.getClientTicks())));
            RenderSystem.setShader(ClientRegistry::getGlowingSpriteShader);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderUtil.litQuad(mStack, MultiBufferSource.immediate(tess.getBuilder()), (double)(baseX + i * 24 + 4), (double)(y + 32), 16.0, 16.0, sign.getRed(), sign.getGreen(), sign.getBlue(), (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(sign.getSprite()));
            tess.end();
            RenderUtil.litQuad(mStack, MultiBufferSource.immediate(tess.getBuilder()), (double)(baseX + i * 24 + 4), (double)(y + 32), 16.0, 16.0, sign.getRed() * flicker, sign.getGreen() * flicker, sign.getBlue() * flicker, (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(sign.getSprite()));
            tess.end();
            x += 20;
        }

        RenderSystem.disableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }
}
