package net.nullcoil.paritem.gui.hoppers.gold;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GoldHopperScreen extends AbstractContainerScreen<GoldHopperScreenHandler> {
    private static Identifier TEXTURE = Identifier.parse("paritem:textures/gui/container/gold_hopper.png");

    public GoldHopperScreen(GoldHopperScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 151);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo, 0f, 0f, this.imageWidth, this.imageHeight, 256, 256);
    }
}
