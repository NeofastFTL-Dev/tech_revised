package com.neofast.tech_revised.integration.jei;

import com.mojang.blaze3d.platform.InputConstants;
import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.item.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JEI multiblock page with:
 * - materials list (what you need)
 * - isometric 3D structure preview
 * - layer toggle (buttons + mouse wheel)
 */
public class MultiblockLayoutJeiCategory implements IRecipeCategory<MultiblockLayoutJeiRecipe> {
    public static final RecipeType<MultiblockLayoutJeiRecipe> RECIPE_TYPE =
            RecipeType.create(TechRevised.MOD_ID, "multiblock_layout", MultiblockLayoutJeiRecipe.class);

    private static final int WIDTH = 176;
    private static final int HEIGHT = 150;

    // Materials grid
    private static final int MAT_START_X = 6;
    private static final int MAT_START_Y = 20;
    private static final int MAT_COLUMNS = 4;
    private static final int MAT_MAX_SLOTS = 12;

    // 3D preview area
    private static final int PREVIEW_X = 90;
    private static final int PREVIEW_Y = 18;
    private static final int PREVIEW_W = 80;
    private static final int PREVIEW_H = 96;

    // Layer control buttons
    private static final int BTN_Y = 128;
    private static final int BTN_PREV_X = 90;
    private static final int BTN_NEXT_X = 130;
    private static final int BTN_ALL_X = 150;
    private static final int BTN_W = 16;
    private static final int BTN_H = 12;

    /** Layer state per blueprint id. Integer.MIN_VALUE = show all layers. */
    private static final Map<String, Integer> LAYER_STATE = new HashMap<>();

    private final IDrawable background;
    private final IDrawable icon;

    public MultiblockLayoutJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.CONFIGURATOR.get()));
    }

    @Override
    public RecipeType<MultiblockLayoutJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.tech_revised.category.multiblock_layout");
    }

    @Override
    @SuppressWarnings("removal")
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MultiblockLayoutJeiRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> parts = orderMaterials(recipe.getRequiredParts(), recipe.getController());
        int shown = Math.min(parts.size(), MAT_MAX_SLOTS);
        for (int i = 0; i < shown; i++) {
            int x = MAT_START_X + (i % MAT_COLUMNS) * 18;
            int y = MAT_START_Y + (i / MAT_COLUMNS) * 18;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addItemStack(parts.get(i));
        }
    }

    @Override
    public void draw(MultiblockLayoutJeiRecipe recipe,
                     mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics,
                     double mouseX,
                     double mouseY) {
        Font font = Minecraft.getInstance().font;
        MultiblockStructureBlueprint bp = recipe.getBlueprint();

        guiGraphics.drawString(font, recipe.getMachineName(), 6, 2, 0xFFFFFF, false);
        guiGraphics.drawString(font, Component.translatable("jei.tech_revised.layout.required_parts"), 6, 12, 0x8B8B8B, false);
        guiGraphics.drawString(font, recipe.getDimensions(), 6, 118, 0x8B8B8B, false);

        // Preview panel background + border
        fillRect(guiGraphics, PREVIEW_X - 2, PREVIEW_Y - 2, PREVIEW_X + PREVIEW_W + 2, PREVIEW_Y + PREVIEW_H + 2, 0xFF0E0E12);
        fillRect(guiGraphics, PREVIEW_X, PREVIEW_Y, PREVIEW_X + PREVIEW_W, PREVIEW_Y + PREVIEW_H, 0xFF22222C);
        // subtle grid
        for (int i = 1; i < 4; i++) {
            int gx = PREVIEW_X + (PREVIEW_W * i) / 4;
            fillRect(guiGraphics, gx, PREVIEW_Y, gx + 1, PREVIEW_Y + PREVIEW_H, 0x18FFFFFF);
        }
        for (int i = 1; i < 4; i++) {
            int gy = PREVIEW_Y + (PREVIEW_H * i) / 4;
            fillRect(guiGraphics, PREVIEW_X, gy, PREVIEW_X + PREVIEW_W, gy + 1, 0x18FFFFFF);
        }

        if (bp != null) {
            int layer = getLayer(bp);
            MultiblockStructureBlueprint.Cell hovered = findHoveredCell(bp, layer, mouseX, mouseY);
            drawIsometric(guiGraphics, bp, layer, hovered);
            drawLayerControls(guiGraphics, font, bp, layer);

            if (hovered != null) {
                Component name = hovered.stack().getHoverName();
                String pos = " @ " + hovered.x() + "," + hovered.y() + "," + hovered.z();
                guiGraphics.drawString(font, name.getString() + pos, 6, 138, 0xFFD070, false);
            } else {
                guiGraphics.drawString(font,
                        Component.translatable("jei.tech_revised.layout.layer_hint"),
                        6, 138, 0x666666, false);
            }
        } else {
            // Fallback: show notes when no blueprint
            int y = 20;
            for (Component note : recipe.getNotes()) {
                guiGraphics.drawString(font, note, 6, y, 0xAAAAAA, false);
                y += 10;
            }
        }
    }

    @Override
    public boolean handleInput(MultiblockLayoutJeiRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
        MultiblockStructureBlueprint bp = recipe.getBlueprint();
        if (bp == null) {
            return false;
        }

        // Mouse wheel over preview: cycle layers
        if (input.getType() == InputConstants.Type.MOUSE) {
            int value = input.getValue();
            boolean overPreview = mouseX >= PREVIEW_X && mouseX < PREVIEW_X + PREVIEW_W
                    && mouseY >= PREVIEW_Y && mouseY < PREVIEW_Y + PREVIEW_H;

            // GLFW_MOUSE_BUTTON_4/5 aren't used; scroll is typically key codes from JEI as mouse with value 0 left click
            if (value == 0) { // left click
                if (inBtn(mouseX, mouseY, BTN_PREV_X, BTN_Y)) {
                    cycleLayer(bp, -1);
                    return true;
                }
                if (inBtn(mouseX, mouseY, BTN_NEXT_X, BTN_Y)) {
                    cycleLayer(bp, 1);
                    return true;
                }
                if (inBtn(mouseX, mouseY, BTN_ALL_X, BTN_Y)) {
                    LAYER_STATE.put(bp.getId(), Integer.MIN_VALUE);
                    return true;
                }
                // Click preview to advance layer
                if (overPreview) {
                    cycleLayer(bp, 1);
                    return true;
                }
            }
        }

        // Keyboard: left/right arrows and A for all (when JEI routes keys)
        if (input.getType() == InputConstants.Type.KEYSYM) {
            int key = input.getValue();
            if (key == 263 || key == 265) { // left / up
                cycleLayer(bp, -1);
                return true;
            }
            if (key == 262 || key == 264) { // right / down
                cycleLayer(bp, 1);
                return true;
            }
            if (key == 65) { // A
                LAYER_STATE.put(bp.getId(), Integer.MIN_VALUE);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Component> getTooltipStrings(MultiblockLayoutJeiRecipe recipe,
                                               mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                                               double mouseX, double mouseY) {
        MultiblockStructureBlueprint bp = recipe.getBlueprint();
        List<Component> tips = new ArrayList<>();
        if (bp == null) {
            tips.addAll(recipe.getNotes());
            return tips;
        }
        if (inBtn(mouseX, mouseY, BTN_PREV_X, BTN_Y)) {
            return List.of(Component.translatable("jei.tech_revised.layout.layer_prev"));
        }
        if (inBtn(mouseX, mouseY, BTN_NEXT_X, BTN_Y)) {
            return List.of(Component.translatable("jei.tech_revised.layout.layer_next"));
        }
        if (inBtn(mouseX, mouseY, BTN_ALL_X, BTN_Y)) {
            return List.of(Component.translatable("jei.tech_revised.layout.layer_all"));
        }
        MultiblockStructureBlueprint.Cell hovered = findHoveredCell(bp, getLayer(bp), mouseX, mouseY);
        if (hovered != null) {
            return List.of(
                    hovered.stack().getHoverName(),
                    Component.literal("x=" + hovered.x() + " y=" + hovered.y() + " z=" + hovered.z()),
                    Component.translatable("jei.tech_revised.layout.local_coords")
            );
        }
        if (mouseX >= PREVIEW_X && mouseX < PREVIEW_X + PREVIEW_W
                && mouseY >= PREVIEW_Y && mouseY < PREVIEW_Y + PREVIEW_H) {
            tips.add(Component.translatable("jei.tech_revised.layout.click_preview"));
            tips.addAll(recipe.getNotes());
            return tips;
        }
        return List.of();
    }

    private static List<ItemStack> orderMaterials(List<ItemStack> parts, ItemStack controller) {
        List<ItemStack> ordered = new ArrayList<>();
        ItemStack controllerMatch = null;
        List<ItemStack> specials = new ArrayList<>();
        List<ItemStack> frames = new ArrayList<>();
        for (ItemStack stack : parts) {
            String path = stack.getItem().builtInRegistryHolder().key().location().getPath();
            if (controller != null && ItemStack.isSameItem(stack, controller)) {
                controllerMatch = stack;
            } else if (path.contains("frame")) {
                frames.add(stack);
            } else {
                specials.add(stack);
            }
        }
        if (controllerMatch != null) {
            ordered.add(controllerMatch);
        } else if (controller != null && !controller.isEmpty()) {
            ordered.add(controller.copy());
        }
        ordered.addAll(specials);
        ordered.addAll(frames);
        return ordered;
    }

    private static int getLayer(MultiblockStructureBlueprint bp) {
        // Tall shells open in "all layers" so the full cylinder is visible first.
        int defaultLayer = bp.getSizeY() >= 5 ? Integer.MIN_VALUE : bp.getMinY();
        return LAYER_STATE.getOrDefault(bp.getId(), defaultLayer);
    }

    private static void cycleLayer(MultiblockStructureBlueprint bp, int delta) {
        int current = getLayer(bp);
        if (current == Integer.MIN_VALUE) {
            LAYER_STATE.put(bp.getId(), delta > 0 ? bp.getMinY() : bp.getMaxY());
            return;
        }
        int next = current + delta;
        if (next < bp.getMinY()) {
            LAYER_STATE.put(bp.getId(), Integer.MIN_VALUE);
        } else if (next > bp.getMaxY()) {
            LAYER_STATE.put(bp.getId(), Integer.MIN_VALUE);
        } else {
            LAYER_STATE.put(bp.getId(), next);
        }
    }

    private static boolean inBtn(double mx, double my, int x, int y) {
        return mx >= x && mx < x + BTN_W && my >= y && my < y + BTN_H;
    }

    private void drawLayerControls(GuiGraphics g, Font font, MultiblockStructureBlueprint bp, int layer) {
        drawButton(g, BTN_PREV_X, BTN_Y, "<");
        drawButton(g, BTN_NEXT_X, BTN_Y, ">");
        drawButton(g, BTN_ALL_X, BTN_Y, "A");

        String label;
        if (layer == Integer.MIN_VALUE) {
            label = "ALL  Y" + bp.getMinY() + ".." + bp.getMaxY();
        } else {
            int idx = layer - bp.getMinY() + 1;
            int total = bp.getMaxY() - bp.getMinY() + 1;
            label = "Y=" + layer + "  (" + idx + "/" + total + ")";
        }
        g.drawString(font, label, 90, 118, 0xCCCCCC, false);
    }

    private void drawButton(GuiGraphics g, int x, int y, String text) {
        fillRect(g, x, y, x + BTN_W, y + BTN_H, 0xFF2A2A38);
        fillRect(g, x + 1, y + 1, x + BTN_W - 1, y + BTN_H - 1, 0xFF505068);
        Font font = Minecraft.getInstance().font;
        int tw = font.width(text);
        g.drawString(font, text, x + (BTN_W - tw) / 2, y + 2, 0xFFFFFF, false);
    }

    private void drawIsometric(GuiGraphics g, MultiblockStructureBlueprint bp, int layer,
                               @Nullable MultiblockStructureBlueprint.Cell hovered) {
        boolean all = layer == Integer.MIN_VALUE;
        List<MultiblockStructureBlueprint.Cell> cells = bp.getCellsSortedForDraw(
                y -> all || y == layer);

        float sizeX = bp.getSizeX();
        float sizeZ = bp.getSizeZ();
        float sizeY = Math.max(1, all ? bp.getSizeY() : 1);

        float tile = Math.min(
                (PREVIEW_W - 10f) / Math.max(2f, sizeX + sizeZ),
                (PREVIEW_H - 10f) / Math.max(2f, (sizeX + sizeZ) * 0.5f + sizeY)
        );
        // Dense shells (AOD / oxygen) get smaller tiles so the ring stays readable
        if (cells.size() > 80) {
            tile = Math.min(tile, 8f);
        }
        tile = Math.max(5f, Math.min(tile, 14f));

        float originX = PREVIEW_X + PREVIEW_W * 0.5f;
        float originY = PREVIEW_Y + PREVIEW_H * 0.68f;

        float itemScale = Math.min(0.85f, tile / 16f);

        for (MultiblockStructureBlueprint.Cell cell : cells) {
            float lx = cell.x() - (bp.getMinX() + bp.getSizeX() * 0.5f - 0.5f);
            float lz = cell.z() - (bp.getMinZ() + bp.getSizeZ() * 0.5f - 0.5f);
            float ly = cell.y() - (all ? bp.getMinY() : layer);

            float sx = originX + (lx - lz) * tile * 0.5f;
            float sy = originY + (lx + lz) * tile * 0.25f - ly * tile * 0.55f;

            boolean isHovered = hovered != null
                    && hovered.x() == cell.x() && hovered.y() == cell.y() && hovered.z() == cell.z();

            int base = colorFor(cell.stack());
            if (isHovered) {
                base = brighten(base, 55);
            }
            int top = brighten(base, 40);
            int left = darken(base, 25);
            int right = darken(base, 45);

            int cx = Math.round(sx);
            int cy = Math.round(sy);
            int hw = Math.max(3, Math.round(tile * 0.45f));
            int hh = Math.max(2, Math.round(tile * 0.25f));
            int depth = Math.max(3, Math.round(tile * 0.45f));

            // Drop shadow
            fillRect(g, cx - hw + 1, cy + depth + 1, cx + hw + 1, cy + depth + 3, 0x55000000);

            // Isometric cube faces (top / left / right)
            drawDiamond(g, cx, cy - hh, hw, hh, top | 0xFF000000);
            fillRect(g, cx - hw, cy, cx, cy + depth, left | 0xFF000000);
            fillRect(g, cx, cy, cx + hw, cy + depth, right | 0xFF000000);

            if (isHovered) {
                // Soft outline so the selected cell reads clearly in dense shells
                fillRect(g, cx - hw - 1, cy - hh - 1, cx + hw + 1, cy - hh, 0xAAFFE080);
                fillRect(g, cx - hw - 1, cy + depth, cx + hw + 1, cy + depth + 1, 0xAAFFE080);
            }

            // Item icon on top of cube for specialty blocks; frames stay solid color only when dense
            boolean specialty = isSpecialty(cell.stack());
            if (specialty || cells.size() <= 60 || isHovered) {
                g.pose().pushPose();
                g.pose().translate(sx - 8 * itemScale, sy - 10 * itemScale, 100);
                g.pose().scale(itemScale, itemScale, 1f);
                g.renderItem(cell.stack(), 0, 0);
                g.pose().popPose();
            }
        }
    }

    private static void drawDiamond(GuiGraphics g, int cx, int cy, int hw, int hh, int argb) {
        // Approximate diamond with horizontal scanlines
        for (int row = -hh; row <= hh; row++) {
            float t = 1f - (Math.abs(row) / (float) Math.max(1, hh));
            int half = Math.max(1, Math.round(hw * t));
            fillRect(g, cx - half, cy + row, cx + half, cy + row + 1, argb);
        }
    }

    @Nullable
    private MultiblockStructureBlueprint.Cell findHoveredCell(MultiblockStructureBlueprint bp, int layer,
                                                              double mouseX, double mouseY) {
        if (mouseX < PREVIEW_X || mouseX > PREVIEW_X + PREVIEW_W
                || mouseY < PREVIEW_Y || mouseY > PREVIEW_Y + PREVIEW_H) {
            return null;
        }

        boolean all = layer == Integer.MIN_VALUE;
        List<MultiblockStructureBlueprint.Cell> cells = bp.getCellsSortedForDraw(y -> all || y == layer);

        float sizeX = bp.getSizeX();
        float sizeZ = bp.getSizeZ();
        float sizeY = Math.max(1, all ? bp.getSizeY() : 1);
        float tile = Math.min(
                (PREVIEW_W - 10f) / Math.max(2f, sizeX + sizeZ),
                (PREVIEW_H - 10f) / Math.max(2f, (sizeX + sizeZ) * 0.5f + sizeY)
        );
        if (cells.size() > 80) {
            tile = Math.min(tile, 8f);
        }
        tile = Math.max(5f, Math.min(tile, 14f));
        float originX = PREVIEW_X + PREVIEW_W * 0.5f;
        float originY = PREVIEW_Y + PREVIEW_H * 0.68f;
        float hit = Math.max(5f, tile * 0.55f);

        MultiblockStructureBlueprint.Cell best = null;
        for (int i = cells.size() - 1; i >= 0; i--) {
            MultiblockStructureBlueprint.Cell cell = cells.get(i);
            float lx = cell.x() - (bp.getMinX() + bp.getSizeX() * 0.5f - 0.5f);
            float lz = cell.z() - (bp.getMinZ() + bp.getSizeZ() * 0.5f - 0.5f);
            float ly = cell.y() - (all ? bp.getMinY() : layer);
            float sx = originX + (lx - lz) * tile * 0.5f;
            float sy = originY + (lx + lz) * tile * 0.25f - ly * tile * 0.55f;
            if (Math.abs(mouseX - sx) <= hit && Math.abs(mouseY - sy) <= hit) {
                best = cell;
                break;
            }
        }
        return best;
    }

    private static boolean isSpecialty(ItemStack stack) {
        String path = stack.getItem().builtInRegistryHolder().key().location().getPath();
        return path.contains("controller") || path.contains("heater") || path.contains("drill")
                || path.contains("bus") || path.contains("hatch") || path.contains("energy");
    }

    private static int colorFor(ItemStack stack) {
        String path = stack.getItem().builtInRegistryHolder().key().location().getPath();
        if (path.contains("controller")) return 0x3A7BD5;
        if (path.contains("heater") || path.contains("drill")) return 0xE07020;
        if (path.contains("fluid_input")) return 0x2A80C0;
        if (path.contains("fluid_output")) return 0x2060A0;
        if (path.contains("input_bus")) return 0x40B040;
        if (path.contains("output_bus")) return 0xC04040;
        if (path.contains("energy")) return 0xE0C020;
        if (path.contains("frame")) return 0x707880;
        return 0x909090;
    }

    private static int brighten(int rgb, int amount) {
        int r = Math.min(255, ((rgb >> 16) & 0xFF) + amount);
        int g = Math.min(255, ((rgb >> 8) & 0xFF) + amount);
        int b = Math.min(255, (rgb & 0xFF) + amount);
        return (r << 16) | (g << 8) | b;
    }

    private static int darken(int rgb, int amount) {
        int r = Math.max(0, ((rgb >> 16) & 0xFF) - amount);
        int g = Math.max(0, ((rgb >> 8) & 0xFF) - amount);
        int b = Math.max(0, (rgb & 0xFF) - amount);
        return (r << 16) | (g << 8) | b;
    }

    private static void fillRect(GuiGraphics g, int x0, int y0, int x1, int y1, int argb) {
        if (x1 <= x0 || y1 <= y0) {
            return;
        }
        g.fill(x0, y0, x1, y1, argb);
    }
}
