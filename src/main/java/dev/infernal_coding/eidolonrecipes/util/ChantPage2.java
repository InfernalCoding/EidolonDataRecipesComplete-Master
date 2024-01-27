package dev.infernal_coding.eidolonrecipes.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import elucent.eidolon.ClientEvents;
import elucent.eidolon.ClientRegistry;
import elucent.eidolon.Eidolon;
import elucent.eidolon.capability.IKnowledge;
import elucent.eidolon.codex.CodexGui;
import elucent.eidolon.codex.Page;
import elucent.eidolon.spell.Sign;
import elucent.eidolon.util.ColorUtil;
import elucent.eidolon.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;

public class ChantPage2 extends Page {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(Eidolon.MODID, "textures/gui/codex_chant_page.png");
    Sign[] chant;
    String text, title;

    public ChantPage2(String title, String text, Sign... chant) {
        super(BACKGROUND);
        this.text = text;
        this.title = title;
        this.chant = chant;
    }

    @OnlyIn(Dist.CLIENT)
    public static void colorBlit(PoseStack mStack, int x, int y, int uOffset, int vOffset, int width, int height, int textureWidth, int textureHeight, int color) {
        Matrix4f matrix = mStack.last().pose();
        int maxX = x + width;
        int maxY = y + height;
        float minU = (float)uOffset / (float)textureWidth;
        float minV = (float)vOffset / (float)textureHeight;
        float maxU = minU + (float)width / (float)textureWidth;
        float maxV = minV + (float)height / (float)textureHeight;
        int r = ColorUtil.getRed(color);
        int g = ColorUtil.getGreen(color);
        int b = ColorUtil.getBlue(color);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(matrix, (float)x, (float)maxY, 0.0F).uv(minU, maxV).color(r, g, b, 255).endVertex();
        bufferbuilder.vertex(matrix, (float)maxX, (float)maxY, 0.0F).uv(maxU, maxV).color(r, g, b, 255).endVertex();
        bufferbuilder.vertex(matrix, (float)maxX, (float)y, 0.0F).uv(maxU, minV).color(r, g, b, 255).endVertex();
        bufferbuilder.vertex(matrix, (float)x, (float)y, 0.0F).uv(minU, minV).color(r, g, b, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    @OnlyIn(Dist.CLIENT)
    public void render(CodexGui gui, PoseStack mStack, int x, int y, int mouseX, int mouseY) {
        String title = I18n.get(this.title, new Object[0]);
        int titleWidth = Minecraft.getInstance().font.width(title);
        int var10003 = x + 64 - titleWidth / 2;
        int var10004 = y + 15;
        Objects.requireNonNull(Minecraft.getInstance().font);
        drawText(gui, mStack, title, var10003, var10004 - 9);
        RenderSystem.setShaderTexture(0, CodexGui.CODEX_BACKGROUND);
        Player entity = Minecraft.getInstance().player;
        IKnowledge knowledge = (IKnowledge)entity.getCapability(IKnowledge.INSTANCE, (Direction)null).resolve().get();
        int w = this.chant.length * 24;
        int baseX = x + 64 - w / 2;
        CodexGui.blit(mStack, baseX - 16, y + 28, 256.0F, 208.0F, 16, 32, 512, 512);

        for(int i = 0; i < this.chant.length; ++i) {
            CodexGui.blit(mStack, baseX + i * 24, y + 28, 272.0F, 208.0F, 24, 32, 512, 512);
        }

        CodexGui.blit(mStack, baseX + w, y + 28, 296.0F, 208.0F, 16, 32, 512, 512);
        Tesselator tess = Tesselator.getInstance();
        RenderSystem.enableBlend();

        for(int i = 0; i < this.chant.length; ++i) {
            RenderSystem.setShaderTexture(0, CodexGui.CODEX_BACKGROUND);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            CodexGui.blit(mStack, baseX + i * 24, y + 28, 312.0F, 208.0F, 24, 24, 512, 512);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            Sign sign = this.chant[i];
            float flicker = 0.875F + 0.125F * (float)Math.sin(Math.toRadians((double)(12.0F * ClientEvents.getClientTicks())));
            RenderSystem.setShader(ClientRegistry::getGlowingSpriteShader);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderUtil.litQuad(mStack, MultiBufferSource.immediate(tess.getBuilder()), (double)(baseX + i * 24 + 4), (double)(y + 32), 16.0, 16.0, sign.getRed(), sign.getGreen(), sign.getBlue(), (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(sign.getSprite()));
            tess.end();
            RenderUtil.litQuad(mStack, MultiBufferSource.immediate(tess.getBuilder()), (double)(baseX + i * 24 + 4), (double)(y + 32), 16.0, 16.0, sign.getRed() * flicker, sign.getGreen() * flicker, sign.getBlue() * flicker, (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(sign.getSprite()));
            tess.end();
        }

        RenderSystem.disableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        drawWrappingText(gui, mStack, I18n.get(this.text, new Object[0]), x + 4, y + 72, 120);
    }

}
