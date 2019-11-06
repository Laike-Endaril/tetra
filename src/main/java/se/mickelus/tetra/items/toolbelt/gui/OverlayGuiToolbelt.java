package se.mickelus.tetra.items.toolbelt.gui;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.mgui.gui.GuiRoot;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryPotions;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryQuickslot;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryQuiver;
import se.mickelus.tetra.items.toolbelt.inventory.ToolbeltSlotType;

public class OverlayGuiToolbelt extends GuiRoot {

    private static final ResourceLocation toolbeltTexture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/toolbelt-inventory.png");

    private OverlayGuiQuickslotGroup quickslotGroup;
    private OverlayGuiPotionGroup potionGroup;
    private OverlayGuiQuiverGroup quiverGroup;

    private boolean hasMouseMoved = false;

    public OverlayGuiToolbelt(Minecraft mc) {
        super(mc);

        quickslotGroup = new OverlayGuiQuickslotGroup(37, 0);
        addChild(quickslotGroup);

        potionGroup = new OverlayGuiPotionGroup(0, 30);
        addChild(potionGroup);

        quiverGroup = new OverlayGuiQuiverGroup(-30, -30);
        addChild(quiverGroup);

    }

    public void setInventories(ItemStack itemStack) {
        quickslotGroup.setInventory(new InventoryQuickslot(itemStack));
        potionGroup.setInventory(new InventoryPotions(itemStack));
        quiverGroup.setInventory(new InventoryQuiver(itemStack));
    }

    public void setVisible(boolean visible) {
        if (visible) {
            hasMouseMoved = false;
            quickslotGroup.setVisible(true);
            potionGroup.setVisible(true);
            quiverGroup.setVisible(true);
        } else {
            quickslotGroup.setVisible(false);
            potionGroup.setVisible(false);
            quiverGroup.setVisible(false);
        }
    }

    @Override
    public void draw() {
        if (isVisible()) {
            MainWindow window = this.mc.mainWindow;
            int width = window.getScaledWidth();
            int height = window.getScaledHeight();
            double mouseX, mouseY;

            if (!hasMouseMoved && (this.mc.mouseHelper.getXVelocity() > 0 || this.mc.mouseHelper.getYVelocity() > 0)) {
                hasMouseMoved = true;
            }

            if (hasMouseMoved) {
                mouseX = this.mc.mouseHelper.getMouseX() * (double)width / (double)window.getWidth();
                mouseY = (double)height - this.mc.mouseHelper.getMouseY() * (double)height / (double)window.getHeight() - 1.0D;
            } else {
                mouseX = width / 2d;
                mouseY = height / 2d;
            }

            this.drawChildren(0, 0, width, height, (int)mouseX, (int)mouseY, 1.0F);
        }
    }

    public ToolbeltSlotType getFocusType() {
        if (quickslotGroup.getFocus() != -1) {
            return ToolbeltSlotType.quickslot;
        }

        if (potionGroup.getFocus() != -1) {
            return ToolbeltSlotType.potion;
        }

        if (quiverGroup.getFocus() != -1) {
            return ToolbeltSlotType.quiver;
        }

        return ToolbeltSlotType.quickslot;
    }

    public int getFocusIndex() {
        int quickslotFocus = quickslotGroup.getFocus();
        if (quickslotFocus != -1) {
            return quickslotFocus;
        }

        int potionFocus = potionGroup.getFocus();
        if (potionFocus != -1) {
            return potionFocus;
        }

        int quiverFocus = quiverGroup.getFocus();
        if (quiverFocus != -1) {
            return quiverFocus;
        }

        return -1;
    }
}
