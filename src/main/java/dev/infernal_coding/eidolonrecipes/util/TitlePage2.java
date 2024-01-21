package dev.infernal_coding.eidolonrecipes.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import elucent.eidolon.Eidolon;
import elucent.eidolon.codex.CodexGui;
import elucent.eidolon.codex.Page;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TitlePage2 extends Page {
    public static final ResourceLocation BACKGROUND = new ResourceLocation(Eidolon.MODID, "textures/gui/codex_title_page.png");
    String text, title;

    public TitlePage2(String title, String text) {
        super(BACKGROUND);
        this.text = text;
        this.title = title;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(CodexGui gui, MatrixStack mStack, int x, int y, int mouseX, int mouseY) {
        String title = I18n.format(this.title);
        int titleWidth = Minecraft.getInstance().fontRenderer.getStringWidth(title);
        drawText(gui, mStack, title, x + 64 - titleWidth / 2, y + 15 - Minecraft.getInstance().fontRenderer.FONT_HEIGHT);
        drawWrappingText(gui, mStack, I18n.format(text), x + 4, y + 24, 120);
    }
}
