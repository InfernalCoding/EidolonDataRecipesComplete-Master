package dev.infernal_coding.eidolonrecipes.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import elucent.eidolon.codex.CodexGui;
import elucent.eidolon.codex.RitualPage;
import elucent.eidolon.codex.TitledRitualPage;
import elucent.eidolon.ritual.Ritual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

/**A subclass of RitualPage to allow configuring the String displayed by a chapter's title
 */
public class TitledRitualPage2 extends RitualPage {

    String title;

    public TitledRitualPage2(String title, Ritual ritual, ItemStack center, RitualIngredient... inputs) {
        super(ritual, center, inputs);
        this.title = I18n.format(title);
    }

    @Override
    public void render(CodexGui gui, MatrixStack mStack, int x, int y, int mouseX, int mouseY) {
        Minecraft.getInstance().getTextureManager().bindTexture(BACKGROUND);
        gui.blit(mStack, x, y, 128, 64, 128, 24);

        int titleWidth = Minecraft.getInstance().fontRenderer.getStringWidth(title);
        drawText(gui, mStack, title, x + 64 - titleWidth / 2, y + 15 - Minecraft.getInstance().fontRenderer.FONT_HEIGHT);

        Minecraft.getInstance().getTextureManager().bindTexture(BACKGROUND);
        super.render(gui, mStack, x, y, mouseX, mouseY);
    }
}
