package dev.infernal_coding.eidolonrecipes.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import elucent.eidolon.codex.CodexGui;
import elucent.eidolon.codex.RitualPage;
import elucent.eidolon.ritual.Ritual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;

/**A subclass of RitualPage to allow configuring the String displayed by a chapter's title
 */
public class TitledRitualPage2 extends RitualPage {

    String title;

    public TitledRitualPage2(String title, Ritual ritual, ItemStack center, RitualIngredient... inputs) {
        super(ritual, center, inputs);
        this.title = I18n.get(title);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(CodexGui gui, PoseStack mStack, int x, int y, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, BACKGROUND);
        gui.blit(mStack, x, y, 128, 64, 128, 24);
        String title = I18n.get(this.title, new Object[0]);
        int titleWidth = Minecraft.getInstance().font.width(title);
        int var10003 = x + 64 - titleWidth / 2;
        int var10004 = y + 15;
        Objects.requireNonNull(Minecraft.getInstance().font);
        drawText(gui, mStack, title, var10003, var10004 - 9);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        super.render(gui, mStack, x, y, mouseX, mouseY);
    }
}
