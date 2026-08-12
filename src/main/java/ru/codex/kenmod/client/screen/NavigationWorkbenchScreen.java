package ru.codex.kenmod.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import ru.codex.kenmod.KenMod;
import ru.codex.kenmod.menu.NavigationWorkbenchMenu;
import ru.codex.kenmod.network.ApplyNavigationTargetPayload;

public class NavigationWorkbenchScreen extends AbstractContainerScreen<NavigationWorkbenchMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(KenMod.MOD_ID, "textures/gui/navigation_workbench.png");

    private EditBox coordinatesField;
    private String lastSubmittedCoordinates = "";

    public NavigationWorkbenchScreen(NavigationWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 62;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 18;
        titleLabelY = 4;

        coordinatesField = addRenderableWidget(createCoordinatesField(leftPos + 45, topPos + 28, 106));
        addRenderableWidget(Button.builder(Component.literal("\u2713"), button -> submitCoordinates())
                .bounds(leftPos + 148, topPos + 41, 18, 18)
                .build());

        lastSubmittedCoordinates = formatCoordinates(menu.getTargetX(), menu.getTargetY(), menu.getTargetZ());
    }

    private EditBox createCoordinatesField(int x, int y, int width) {
        EditBox field = new EditBox(font, x, y, width, 18, Component.translatable("gui.create_autonavigation.navigation_workbench.coordinates"));
        field.setBordered(false);
        field.setTextColor(0x404040);
        field.setTextColorUneditable(0x404040);
        field.setMaxLength(48);
        field.setValue(formatCoordinates(menu.getTargetX(), menu.getTargetY(), menu.getTargetZ()));
        field.setFilter(value -> value.length() <= 48 && value.matches("[-0-9,;\\s]*"));
        return field;
    }

    private void submitCoordinates() {
        int[] parsed = parseCoordinates(coordinatesField.getValue());
        String normalized = formatCoordinates(parsed[0], parsed[1], parsed[2]);
        coordinatesField.setValue(normalized);
        lastSubmittedCoordinates = normalized;
        PacketDistributor.sendToServer(new ApplyNavigationTargetPayload(
                menu.getBlockPos(),
                parsed[0],
                parsed[1],
                parsed[2]
        ));
    }

    private static int normalizeNumber(String value) {
        try {
            return Mth.clamp(Integer.parseInt(value), -30_000_000, 30_000_000);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static int[] parseCoordinates(String value) {
        String cleaned = value == null ? "" : value
                .replace(",", " ")
                .replace(";", " ")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();
        String[] parts = cleaned.isEmpty() ? new String[0] : cleaned.split("\\s+");
        int[] parsed = new int[] {0, 0, 0};
        for (int i = 0; i < Math.min(3, parts.length); i++) {
            parsed[i] = normalizeNumber(parts[i]);
        }
        return parsed;
    }

    private static String formatCoordinates(int x, int y, int z) {
        return x + " " + y + " " + z;
    }

    @Override
    public void removed() {
        submitIfChanged();
        super.removed();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, 62, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void submitIfChanged() {
        if (coordinatesField == null) {
            return;
        }
        int[] parsed = parseCoordinates(coordinatesField.getValue());
        String normalized = formatCoordinates(parsed[0], parsed[1], parsed[2]);
        if (!normalized.equals(lastSubmittedCoordinates)) {
            coordinatesField.setValue(normalized);
            submitCoordinates();
        }
    }
}
