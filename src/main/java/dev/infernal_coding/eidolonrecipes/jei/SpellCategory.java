package dev.infernal_coding.eidolonrecipes.jei;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.infernal_coding.eidolonrecipes.ModRoot;
import dev.infernal_coding.eidolonrecipes.mixin.getters.TagPredicateMixin;
import dev.infernal_coding.eidolonrecipes.spells.SpellRecipeWrapper;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirement;
import dev.infernal_coding.eidolonrecipes.spells.requirement.impl.AltarRequirement;
import dev.infernal_coding.eidolonrecipes.spells.requirement.impl.GobletRequirement;
import dev.infernal_coding.eidolonrecipes.spells.type.impl.TransmutationSpell;
import dev.infernal_coding.eidolonrecipes.util.SpellUtil;
import elucent.eidolon.ClientEvents;
import elucent.eidolon.Eidolon;
import elucent.eidolon.Registry;
import elucent.eidolon.codex.CodexGui;
import elucent.eidolon.spell.Sign;
import elucent.eidolon.spell.Signs;
import elucent.eidolon.util.ColorUtil;
import elucent.eidolon.util.RenderUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.commons.lang3.concurrent.ConcurrentRuntimeException;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.*;

public class SpellCategory implements IRecipeCategory<SpellRecipeWrapper>  {

    static final Screen screen = Minecraft.getInstance().currentScreen;
    static final FontRenderer font = Minecraft.getInstance().fontRenderer;
    static final ResourceLocation UID = new ResourceLocation(Eidolon.MODID, "spell");
    static final ResourceLocation backgroundId = new ResourceLocation(ModRoot.ID, "textures/gui/spell_jei.png");
    final IDrawable background, icon, woodAltar, stoneAltar, goblet, strawEffigy, unholyEffigy;
    final IGuiHelper guiHelper;
    static final Map<ResourceLocation, Sign> signs =
            ObfuscationReflectionHelper.getPrivateValue(Signs.class, null,
                    "signMap");
    public SpellCategory(IGuiHelper guiHelper) {
        this.guiHelper = guiHelper;
        this.background = guiHelper.createDrawable(backgroundId, 0, 0, 125, 200);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(Registry.STONE_ALTAR.get()));
        this.woodAltar = guiHelper.createDrawable(backgroundId, 207, 220, 50, 45);
        this.stoneAltar = guiHelper.createDrawable(backgroundId, 9, 222, 100, 39);
        this.strawEffigy = guiHelper.createDrawable(backgroundId, 139, 89, 100, 100);
        this.unholyEffigy = guiHelper.createDrawable(backgroundId, 164, 132, 53, 68);
        this.goblet = guiHelper.createDrawable(backgroundId, 45, 195, 16, 35);

    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<SpellRecipeWrapper> getRecipeClass() {
        return SpellRecipeWrapper.class;
    }

    @Override
    public String getTitle() {
        return I18n.format("jei." + Eidolon.MODID + ".spell");
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
    public void setIngredients(SpellRecipeWrapper spell, IIngredients ingredients) {

        List<Ingredient> inputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();


        if (spell.getSpell() instanceof TransmutationSpell) {
            TransmutationSpell transmutationSpell = (TransmutationSpell) spell.getSpell();

            for (TransmutationSpell.TransmutationRecipe match : transmutationSpell.getTransmutations()) {
                inputs.addAll(match.getIngredients());
                outputs.addAll(match.getResults());
            }
        }
        ingredients.setInputIngredients(inputs);
        ingredients.setOutputs(VanillaTypes.ITEM, outputs);
    }

    @Override
    public void setRecipe(IRecipeLayout layout, SpellRecipeWrapper spell, IIngredients ingredients) {

    }

    @Override
    public void draw(SpellRecipeWrapper spell, MatrixStack mStack, double mouseX, double mouseY) {
      drawAltarReqs(spell, mStack, (int) mouseX, (int) mouseY);
      drawSacrifice(spell, mStack, (int) mouseX, (int) mouseY);
      drawSigns(spell, mStack);
      //drawItems(spell, mStack, (int) mouseX, (int) mouseY);
    }
    public void drawAltarReqs(SpellRecipeWrapper spell, MatrixStack mStack, int mouseX, int mouseY) {
        List<ITextComponent> tooltip = new ArrayList<>();
        tooltip.add(new StringTextComponent("Altar Requirements"));

        for (ISpellRequirement requirement : spell.getRequirements()) {
            if (requirement instanceof AltarRequirement) {
                AltarRequirement altarRequirement = (AltarRequirement) requirement;

                if (altarRequirement.getRequiredAltar().orElse(Blocks.AIR).equals(Registry.STONE_ALTAR.get())) {
                    stoneAltar.draw(mStack, 18, 125);
                    tooltip.add(new TranslationTextComponent(Registry.STONE_ALTAR.get().getTranslationKey()));
                    altarRequirement.getRequiredEffigy().ifPresent(effigy -> {
                        tooltip.add(new TranslationTextComponent(effigy.getTranslationKey()));
                        if (effigy == Registry.UNHOLY_EFFIGY.get()) {
                            unholyEffigy.draw(mStack, 35, 70);
                        } else {
                            strawEffigy.draw(mStack, 35, 70);
                        }
                    });
                } else /*(altarRequirement.getRequiredAltar().orElse(Blocks.AIR).equals(Registry.WOODEN_ALTAR.get()))*/ {
                    woodAltar.draw(mStack, 43, 122);
                    tooltip.add(new TranslationTextComponent(Registry.WOODEN_ALTAR.get().getTranslationKey()));
                    altarRequirement.getRequiredEffigy().ifPresent(effigy -> {
                            tooltip.add(new TranslationTextComponent(effigy.getTranslationKey()));

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
            screen.renderWrappedToolTip(mStack, tooltip, mouseX, mouseY, font);
        }
    }

    public void drawSacrifice(SpellRecipeWrapper spell, MatrixStack mStack, int mouseX, int mouseY) {
        boolean hasSacrifices = Arrays.stream(spell.getRequirements()).anyMatch(req -> req instanceof GobletRequirement);

        if (hasSacrifices) {
            goblet.draw(mStack, 20, 100);
            List<ITextComponent> sacrifices =
                    Lists.newArrayList(new StringTextComponent("Required Sacrifices:"));

            Arrays.stream(spell.getRequirements()).forEach(rq -> {
                if (rq instanceof GobletRequirement) {
                    GobletRequirement requirement = (GobletRequirement) rq;
                    requirement.getType().ifPresent(type -> {
                        if (type instanceof EntityTypePredicate.TypePredicate) {
                            ResourceLocation entityName = requirement.getSacrifice();
                            TranslationTextComponent text = new TranslationTextComponent("entity."
                                    + entityName.getNamespace() + "." + entityName.getPath());
                            sacrifices.add(text);
                        } else if (type instanceof EntityTypePredicate.TagPredicate) {
                            ITag<EntityType<?>> tag = ((TagPredicateMixin) type).getTag();
                            StringTextComponent formattedTag = SpellUtil.getFormattedTag(tag);
                            sacrifices.add(formattedTag);
                        }
                    });

                    if (!requirement.getType().isPresent()) {
                        String gobletReq = requirement.getSacrifice().toString();

                        if (gobletReq.equals("eidolon:any")) {
                            sacrifices.add(new StringTextComponent("Any"));
                        } else if (gobletReq.equals("eidolon:is_animal")) {
                            sacrifices.add(new StringTextComponent("Animal"));
                        } else if (gobletReq.equals("eidolon:is_villager_or_player")) {
                            sacrifices.add(new StringTextComponent("Villager or Player"));
                        }
                    }

                }
            });

            if (SpellUtil.isInTooltipRange(5, 100, 35, 20, mouseX, mouseY)) {
                screen.renderWrappedToolTip(mStack, sacrifices, mouseX, mouseY, font);
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

    public void drawSigns(SpellRecipeWrapper spell, MatrixStack mStack) {
        int x = 5;
        int y = 3;

        List<Sign> signs = Arrays.asList(spell.getSigns());

        RenderSystem.enableBlend();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.alphaFunc(GL11.GL_GEQUAL, 1f / 256f);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);

        RenderSystem.enableTexture();
        Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        for (int j = 0; j < signs.size(); j++) {
                Sign sign = signs.get(j);
                for (int i = 0; i < 2; i++) {
                    float flicker = 0.875f + 0.125f * (float)Math.sin(Math.toRadians(12 * ClientEvents.getClientTicks()));
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                    RenderUtil.litQuad(mStack, IRenderTypeBuffer.getImpl(tess.getBuffer()), x, y, 16, 16,
                            sign.getRed(), sign.getGreen(), sign.getBlue(), Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(sign.getSprite()));
                    tess.draw();
                    RenderUtil.litQuad(mStack, IRenderTypeBuffer.getImpl(tess.getBuffer()), x, y, 16, 16,
                            sign.getRed() * flicker, sign.getGreen() * flicker, sign.getBlue() * flicker, Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(sign.getSprite()));
                    tess.draw();
                }
                x+=20;
        }
        RenderSystem.defaultAlphaFunc();
        RenderSystem.disableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

}
