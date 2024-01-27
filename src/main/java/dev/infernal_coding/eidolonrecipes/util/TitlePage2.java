package dev.infernal_coding.eidolonrecipes.util;

import com.mojang.blaze3d.vertex.PoseStack;
import elucent.eidolon.Eidolon;
import elucent.eidolon.codex.CodexGui;
import elucent.eidolon.codex.Page;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;

public class TitlePage2 extends Page {
    public static final ResourceLocation BACKGROUND = new ResourceLocation(Eidolon.MODID, "textures/gui/codex_title_page.png");
    String text, title;

    public TitlePage2(String title, String text) {
        super(BACKGROUND);
        this.text = text;
        this.title = title;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(CodexGui gui, PoseStack mStack, int x, int y, int mouseX, int mouseY) {
        String title = I18n.get(this.title, new Object[0]);
        int titleWidth = Minecraft.getInstance().font.width(title);
        int var10003 = x + 64 - titleWidth / 2;
        int var10004 = y + 15;
        Objects.requireNonNull(Minecraft.getInstance().font);
        drawText(gui, mStack, title, var10003, var10004 - 9);
        drawWrappingText(gui, mStack, I18n.get(this.text, new Object[0]), x + 4, y + 24, 120);
    }
}
