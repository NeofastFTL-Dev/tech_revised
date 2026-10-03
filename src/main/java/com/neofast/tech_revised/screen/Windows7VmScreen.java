package com.neofast.tech_revised.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.client.IndustrialGuiStyle;
import com.neofast.tech_revised.client.VmScreenClientState;
import com.neofast.tech_revised.integration.vm.VmCommandIntegration;
import com.neofast.tech_revised.networking.ModNetworking;
import com.neofast.tech_revised.networking.packet.SaveVmConfigPacket;
import com.neofast.tech_revised.networking.packet.VmKeyboardInputPacket;
import com.neofast.tech_revised.networking.packet.VmMouseInputPacket;
import com.neofast.tech_revised.networking.packet.VmStreamControlPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.io.ByteArrayInputStream;
import java.util.UUID;

public class Windows7VmScreen extends AbstractContainerScreen<Windows7VmMenu> {
    private static final int LEFT_PANEL_X = 12;
    private static final int FIELD_WIDTH = 170;
    private static final int PREVIEW_X = 198;
    private static final int PREVIEW_Y = 34;
    private static final int PREVIEW_WIDTH = 160;
    private static final int PREVIEW_HEIGHT = 112;

    private EditBox vmNameBox;
    private EditBox startCommandBox;
    private EditBox stopCommandBox;
    private Button saveButton;
    private Button refreshButton;
    private Button closeButton;
    private Button fullscreenButton;
    private ResourceLocation previewTextureLocation;
    private DynamicTexture previewTexture;
    private int previewTextureWidth = 1;
    private int previewTextureHeight = 1;
    private long lastPreviewUpdateId = -1L;
    private String previewStatus = "";
    private boolean liveStreamActive = false;
    private boolean vmFullscreen = false;
    private boolean previewFocused = false;
    private int previewAreaX = 0;
    private int previewAreaY = 0;
    private int previewAreaWidth = 0;
    private int previewAreaHeight = 0;
    private int previewDrawX = -1;
    private int previewDrawY = -1;
    private int previewDrawWidth = 0;
    private int previewDrawHeight = 0;
    private int heldMouseButtons = 0;
    private long lastPointerSendMs = 0L;
    private int lastSentMask = -1;
    private double lastSentX = Double.NaN;
    private double lastSentY = Double.NaN;

    public Windows7VmScreen(Windows7VmMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 370;
        this.imageHeight = 206;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 12;
        this.titleLabelY = 10;

        int left = this.leftPos + LEFT_PANEL_X;
        int fieldWidth = FIELD_WIDTH;

        vmNameBox = new EditBox(this.font, left, this.topPos + 34, fieldWidth, 20,
                Component.translatable("screen.tech_revised.vm.field.vm_name"));
        vmNameBox.setMaxLength(64);
        vmNameBox.setValue(menu.getVmName());
        addRenderableWidget(vmNameBox);

        startCommandBox = new EditBox(this.font, left, this.topPos + 78, fieldWidth, 20,
                Component.translatable("screen.tech_revised.vm.field.start_command"));
        startCommandBox.setMaxLength(512);
        startCommandBox.setValue(menu.getStartCommand());
        addRenderableWidget(startCommandBox);

        stopCommandBox = new EditBox(this.font, left, this.topPos + 122, fieldWidth, 20,
                Component.translatable("screen.tech_revised.vm.field.stop_command"));
        stopCommandBox.setMaxLength(512);
        stopCommandBox.setValue(menu.getStopCommand());
        addRenderableWidget(stopCommandBox);

        saveButton = Button.builder(
                        Component.translatable("screen.tech_revised.vm.button.save"),
                        button -> saveConfig())
                .bounds(left, this.topPos + 156, 54, 20)
                .build();
        addRenderableWidget(saveButton);

        refreshButton = Button.builder(
                        Component.translatable("screen.tech_revised.vm.button.refresh"),
                        button -> restartLiveStream())
                .bounds(left + 58, this.topPos + 156, 54, 20)
                .build();
        addRenderableWidget(refreshButton);

        closeButton = Button.builder(
                        Component.translatable("screen.tech_revised.vm.button.close"),
                        button -> onClose())
                .bounds(left + fieldWidth - 54, this.topPos + 156, 54, 20)
                .build();
        addRenderableWidget(closeButton);

        fullscreenButton = Button.builder(
                        Component.translatable("screen.tech_revised.vm.button.fullscreen"),
                        button -> toggleVmFullscreen())
                .bounds(this.leftPos + PREVIEW_X, this.topPos + 176, 80, 20)
                .build();
        addRenderableWidget(fullscreenButton);

        VmScreenClientState.clear(menu.getBlockPos());
        lastPreviewUpdateId = -1L;
        previewStatus = Component.translatable("screen.tech_revised.vm.preview.connecting").getString();
        vmFullscreen = false;
        previewFocused = false;
        setConfigWidgetsVisible(true);
        updatePreviewArea();
        startLiveStream();
        setInitialFocus(vmNameBox);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        pollNativePointer();
        if (vmNameBox.visible) {
            // EditBox no longer ticks in 1.21.1.
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        updatePreviewArea();

        if (vmFullscreen) {
            guiGraphics.fill(0, 0, this.width, this.height, 0xFF050607);
        } else {
            int left = this.leftPos;
            int top = this.topPos;
            IndustrialGuiStyle.drawMetalPanel(guiGraphics, left, top, this.imageWidth, this.imageHeight);
            IndustrialGuiStyle.drawRecess(guiGraphics, left + 10, top + 28, FIELD_WIDTH + 4, 122);
            IndustrialGuiStyle.drawRecess(guiGraphics, left + PREVIEW_X - 4, top + 28, PREVIEW_WIDTH + 8, 148);
        }

        int previewLeft = previewAreaX;
        int previewTop = previewAreaY;
        int previewWidth = previewAreaWidth;
        int previewHeight = previewAreaHeight;
        guiGraphics.fill(previewLeft, previewTop, previewLeft + previewWidth, previewTop + previewHeight, 0xFF111111);
        guiGraphics.fill(previewLeft + 1, previewTop + 1, previewLeft + previewWidth - 1, previewTop + previewHeight - 1, 0xFF000000);
        if (previewFocused) {
            guiGraphics.fill(previewLeft, previewTop, previewLeft + previewWidth, previewTop + 1, 0xFF6AA9FF);
            guiGraphics.fill(previewLeft, previewTop + previewHeight - 1, previewLeft + previewWidth, previewTop + previewHeight, 0xFF6AA9FF);
            guiGraphics.fill(previewLeft, previewTop, previewLeft + 1, previewTop + previewHeight, 0xFF6AA9FF);
            guiGraphics.fill(previewLeft + previewWidth - 1, previewTop, previewLeft + previewWidth, previewTop + previewHeight, 0xFF6AA9FF);
        }

        int inset = vmFullscreen ? 0 : 1;
        previewDrawX = previewLeft + inset;
        previewDrawY = previewTop + inset;
        previewDrawWidth = Math.max(1, previewWidth - inset * 2);
        previewDrawHeight = Math.max(1, previewHeight - inset * 2);
        if (previewTextureLocation != null) {
            int availableWidth = previewDrawWidth;
            int availableHeight = previewDrawHeight;
            float scale = Math.min(availableWidth / (float) previewTextureWidth, availableHeight / (float) previewTextureHeight);
            int drawWidth = Math.max(1, Math.round(previewTextureWidth * scale));
            int drawHeight = Math.max(1, Math.round(previewTextureHeight * scale));
            int drawX = previewLeft + inset + (availableWidth - drawWidth) / 2;
            int drawY = previewTop + inset + (availableHeight - drawHeight) / 2;

            previewDrawX = drawX;
            previewDrawY = drawY;
            previewDrawWidth = drawWidth;
            previewDrawHeight = drawHeight;
            if (previewTexture != null) {
                previewTexture.setFilter(true, false);
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.blit(previewTextureLocation, drawX, drawY, drawWidth, drawHeight,
                    0.0F, 0.0F, previewTextureWidth, previewTextureHeight, previewTextureWidth, previewTextureHeight);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (vmFullscreen) {
            return;
        }

        IndustrialGuiStyle.drawTruncated(guiGraphics, font, title, titleLabelX, titleLabelY, 180, IndustrialGuiStyle.TITLE_COLOR);
        IndustrialGuiStyle.drawTruncated(guiGraphics, font,
                Component.translatable("screen.tech_revised.vm.field.vm_name"),
                12, 23, FIELD_WIDTH, IndustrialGuiStyle.BODY_COLOR);
        IndustrialGuiStyle.drawTruncated(guiGraphics, font,
                Component.translatable("screen.tech_revised.vm.field.start_command"),
                12, 67, FIELD_WIDTH, IndustrialGuiStyle.BODY_COLOR);
        IndustrialGuiStyle.drawTruncated(guiGraphics, font,
                Component.translatable("screen.tech_revised.vm.field.stop_command"),
                12, 111, FIELD_WIDTH, IndustrialGuiStyle.BODY_COLOR);
        IndustrialGuiStyle.drawTruncated(guiGraphics, font,
                Component.translatable("screen.tech_revised.vm.preview"),
                PREVIEW_X, 23, PREVIEW_WIDTH, IndustrialGuiStyle.BODY_COLOR);
        IndustrialGuiStyle.drawTruncated(guiGraphics, font, previewStatus, PREVIEW_X, 150, PREVIEW_WIDTH, IndustrialGuiStyle.MUTED_COLOR);
        IndustrialGuiStyle.drawTruncated(guiGraphics, font,
                Component.translatable(previewFocused
                        ? "screen.tech_revised.vm.preview.focused"
                        : "screen.tech_revised.vm.preview.click_to_focus"),
                PREVIEW_X, 162, PREVIEW_WIDTH, IndustrialGuiStyle.MUTED_COLOR);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (fullscreenButton.visible && fullscreenButton.isMouseOver(mouseX, mouseY)) {
            return fullscreenButton.mouseClicked(mouseX, mouseY, button);
        }

        if (!vmFullscreen) {
            if (vmNameBox.mouseClicked(mouseX, mouseY, button)
                    || startCommandBox.mouseClicked(mouseX, mouseY, button)
                    || stopCommandBox.mouseClicked(mouseX, mouseY, button)) {
                previewFocused = false;
                return true;
            }
        }

        if (isInPreview(mouseX, mouseY)) {
            previewFocused = true;
            vmNameBox.setFocused(false);
            startCommandBox.setFocused(false);
            stopCommandBox.setFocused(false);
            setFocused(null);

            int bit = buttonMaskFor(button);
            if (bit != 0) {
                heldMouseButtons |= bit;
                sendMouseEventToVm(mouseX, mouseY, 0, heldMouseButtons, true);
            }
            previewStatus = Component.translatable("screen.tech_revised.vm.preview.mouse_click_sent").getString();
            return true;
        }

        previewFocused = false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (heldMouseButtons != 0) {
            int bit = buttonMaskFor(button);
            if (bit != 0) {
                int mask = heldMouseButtons & ~bit;
                heldMouseButtons = mask;
                sendMouseEventToVm(mouseX, mouseY, 0, mask, true);
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (heldMouseButtons != 0) {
            sendMouseEventToVm(mouseX, mouseY, 0, heldMouseButtons, false);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (vmFullscreen || isInPreview(mouseX, mouseY)) {
            int steps = (int) Math.round(scrollY);
            if (steps == 0) {
                steps = scrollY > 0 ? 1 : -1;
            }
            sendMouseEventToVm(mouseX, mouseY, steps, heldMouseButtons, true);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (previewFocused || vmFullscreen || heldMouseButtons != 0) {
            sendMouseEventToVm(mouseX, mouseY, 0, heldMouseButtons, false);
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (vmFullscreen && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            toggleVmFullscreen();
            return true;
        }

        if (previewFocused) {
            if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0 && keyCode == GLFW.GLFW_KEY_V) {
                sendTextToVm(Minecraft.getInstance().keyboardHandler.getClipboard());
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                previewFocused = false;
                return true;
            }

            VmCommandIntegration.VmSpecialKey specialKey = mapSpecialKey(keyCode);
            if (specialKey != null) {
                ModNetworking.CHANNEL.sendToServer(VmKeyboardInputPacket.forSpecialKey(menu.getBlockPos(), specialKey));
            }
            return true;
        }

        if (vmNameBox.keyPressed(keyCode, scanCode, modifiers)
                || startCommandBox.keyPressed(keyCode, scanCode, modifiers)
                || stopCommandBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (previewFocused) {
            if (!Character.isISOControl(codePoint)) {
                sendTextToVm(String.valueOf(codePoint));
            }
            return true;
        }

        if (vmNameBox.charTyped(codePoint, modifiers)
                || startCommandBox.charTyped(codePoint, modifiers)
                || stopCommandBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        pollScreenshotUpdate();
        pollNativePointer();
        if (vmFullscreen) {
            renderBackground(guiGraphics, 0, 0, 0);
            renderBg(guiGraphics, delta, mouseX, mouseY);
            fullscreenButton.render(guiGraphics, mouseX, mouseY, delta);
        } else {
            super.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public void removed() {
        if (heldMouseButtons != 0) {
            sendMouseEventToVm(previewDrawX, previewDrawY, 0, 0, true);
            heldMouseButtons = 0;
        }
        stopLiveStream();
        releasePreviewTexture();
        super.removed();
    }

    private void saveConfig() {
        ModNetworking.CHANNEL.sendToServer(new SaveVmConfigPacket(
                menu.getBlockPos(),
                vmNameBox.getValue(),
                startCommandBox.getValue(),
                stopCommandBox.getValue()));
    }

    private void startLiveStream() {
        if (liveStreamActive) {
            return;
        }
        ModNetworking.CHANNEL.sendToServer(new VmStreamControlPacket(menu.getBlockPos(), true));
        liveStreamActive = true;
    }

    private void stopLiveStream() {
        if (!liveStreamActive) {
            return;
        }
        ModNetworking.CHANNEL.sendToServer(new VmStreamControlPacket(menu.getBlockPos(), false));
        liveStreamActive = false;
    }

    private void restartLiveStream() {
        stopLiveStream();
        VmScreenClientState.clear(menu.getBlockPos());
        lastPreviewUpdateId = -1L;
        previewStatus = Component.translatable("screen.tech_revised.vm.preview.connecting").getString();
        startLiveStream();
    }

    private void pollScreenshotUpdate() {
        VmScreenClientState.VmFrameData frameData = VmScreenClientState.get(menu.getBlockPos());
        if (frameData == null || frameData.updateId() == lastPreviewUpdateId) {
            return;
        }

        lastPreviewUpdateId = frameData.updateId();
        previewStatus = frameData.message();
        if (frameData.success() && frameData.imageBytes().length > 8) {
            updatePreviewTexture(frameData.imageBytes());
        }
    }

    private void updatePreviewTexture(byte[] imageBytes) {
        NativeImage image = null;
        try (ByteArrayInputStream stream = new ByteArrayInputStream(imageBytes)) {
            image = NativeImage.read(stream);
            if (image == null) {
                previewStatus = Component.translatable("screen.tech_revised.vm.preview.decode_failed").getString();
                return;
            }

            int imageWidth = Math.max(1, image.getWidth());
            int imageHeight = Math.max(1, image.getHeight());
            NativeImage previewPixels = previewTexture == null ? null : previewTexture.getPixels();

            if (previewPixels == null
                    || previewTextureWidth != imageWidth
                    || previewTextureHeight != imageHeight) {
                rebuildPreviewTexture(image, imageWidth, imageHeight);
                image = null; // ownership transferred to DynamicTexture
                return;
            }

            previewPixels.copyFrom(image);
            previewTexture.upload();
            previewTexture.setFilter(true, false);
        } catch (Exception exception) {
            previewStatus = Component.translatable("screen.tech_revised.vm.preview.decode_failed").getString();
        } finally {
            if (image != null) {
                image.close();
            }
        }
    }

    private void rebuildPreviewTexture(NativeImage image, int imageWidth, int imageHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (previewTextureLocation == null) {
            previewTextureLocation = ResourceLocation.fromNamespaceAndPath(TechRevised.MOD_ID, "vm_preview/" + UUID.randomUUID());
        } else {
            minecraft.getTextureManager().release(previewTextureLocation);
            if (previewTexture != null) {
                previewTexture.close();
                previewTexture = null;
            }
        }

        previewTexture = new DynamicTexture(image);
        previewTextureWidth = imageWidth;
        previewTextureHeight = imageHeight;
        minecraft.getTextureManager().register(previewTextureLocation, previewTexture);
        previewTexture.setFilter(true, false);
    }

    private void releasePreviewTexture() {
        if (previewTextureLocation != null) {
            Minecraft.getInstance().getTextureManager().release(previewTextureLocation);
            previewTextureLocation = null;
        }
        if (previewTexture != null) {
            previewTexture.close();
            previewTexture = null;
        }
    }

    private boolean isInPreview(double mouseX, double mouseY) {
        return mouseX >= previewAreaX + 1
                && mouseX < previewAreaX + previewAreaWidth - 1
                && mouseY >= previewAreaY + 1
                && mouseY < previewAreaY + previewAreaHeight - 1;
    }

    private void sendTextToVm(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        int maxChunk = 200;
        for (int i = 0; i < text.length(); i += maxChunk) {
            String chunk = text.substring(i, Math.min(text.length(), i + maxChunk));
            ModNetworking.CHANNEL.sendToServer(VmKeyboardInputPacket.forText(menu.getBlockPos(), chunk));
        }
    }

    private void pollNativePointer() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        long window = minecraft.getWindow().getWindow();
        double mouseX = minecraft.mouseHandler.xpos()
                * (double) minecraft.getWindow().getGuiScaledWidth()
                / Math.max(1, minecraft.getWindow().getScreenWidth());
        double mouseY = minecraft.mouseHandler.ypos()
                * (double) minecraft.getWindow().getGuiScaledHeight()
                / Math.max(1, minecraft.getWindow().getScreenHeight());

        int mask = 0;
        if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
            mask |= 1;
        }
        if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS) {
            mask |= 2;
        }
        if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS) {
            mask |= 4;
        }

        if (mask == 0 && heldMouseButtons != 0) {
            heldMouseButtons = 0;
            sendMouseEventToVm(mouseX, mouseY, 0, 0, true);
            return;
        }

        if (!previewFocused && !vmFullscreen && heldMouseButtons == 0) {
            return;
        }

        boolean overPreview = vmFullscreen || isInPreview(mouseX, mouseY);
        if (heldMouseButtons == 0 && !overPreview) {
            return;
        }

        sendMouseEventToVm(mouseX, mouseY, 0, heldMouseButtons, false);
    }

    private static int buttonMaskFor(int button) {
        return switch (button) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> 1;
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> 2;
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> 4;
            default -> 0;
        };
    }

    private boolean sendMouseEventToVm(double mouseX, double mouseY, int wheelDelta, int buttonMask, boolean force) {
        int[] mapped = mapPointerToVm(mouseX, mouseY, force || heldMouseButtons != 0 || vmFullscreen || wheelDelta != 0);
        if (mapped == null) {
            return false;
        }

        boolean contentChanged = wheelDelta != 0 || buttonMask != lastSentMask;
        boolean positionChanged = !Double.isNaN(lastSentX) && !Double.isNaN(lastSentY)
                && (Math.abs(mouseX - lastSentX) >= 1.0D || Math.abs(mouseY - lastSentY) >= 1.0D);
        if (!force && !contentChanged && !positionChanged) {
            return true;
        }

        long now = System.currentTimeMillis();
        long minInterval = heldMouseButtons != 0 ? 16L : 33L;
        if (!force && now - lastPointerSendMs < minInterval) {
            return true;
        }
        lastPointerSendMs = now;
        lastSentMask = buttonMask;
        lastSentX = mouseX;
        lastSentY = mouseY;
        ModNetworking.CHANNEL.sendToServer(new VmMouseInputPacket(menu.getBlockPos(), mapped[0], mapped[1], wheelDelta, buttonMask));
        return true;
    }

    private int[] mapPointerToVm(double mouseX, double mouseY, boolean clampOutside) {
        if (previewTextureLocation == null || previewTextureWidth <= 0 || previewTextureHeight <= 0) {
            return null;
        }
        if (previewDrawWidth <= 0 || previewDrawHeight <= 0) {
            return null;
        }

        if (!clampOutside && (mouseX < previewDrawX || mouseX >= previewDrawX + previewDrawWidth
                || mouseY < previewDrawY || mouseY >= previewDrawY + previewDrawHeight)) {
            return null;
        }

        double localX = mouseX - previewDrawX;
        double localY = mouseY - previewDrawY;
        double normalizedX = previewDrawWidth <= 1 ? 0.0D : localX / (double) previewDrawWidth;
        double normalizedY = previewDrawHeight <= 1 ? 0.0D : localY / (double) previewDrawHeight;
        normalizedX = Math.max(0.0D, Math.min(1.0D, normalizedX));
        normalizedY = Math.max(0.0D, Math.min(1.0D, normalizedY));

        int vmX = (int) Math.round(normalizedX * 65535.0D);
        int vmY = (int) Math.round(normalizedY * 65535.0D);
        return new int[]{Math.max(0, Math.min(65535, vmX)), Math.max(0, Math.min(65535, vmY))};
    }

    public Rect2i getJeiExclusionArea() {
        return new Rect2i(0, 0, this.width, this.height);
    }

    private void toggleVmFullscreen() {
        if (heldMouseButtons != 0) {
            sendMouseEventToVm(previewDrawX, previewDrawY, 0, 0, true);
            heldMouseButtons = 0;
        }
        vmFullscreen = !vmFullscreen;
        setConfigWidgetsVisible(!vmFullscreen);
        updatePreviewArea();

        if (vmFullscreen) {
            previewFocused = true;
            vmNameBox.setFocused(false);
            startCommandBox.setFocused(false);
            stopCommandBox.setFocused(false);
            setFocused(null);
        } else {
            previewFocused = false;
            setInitialFocus(vmNameBox);
        }
    }

    private void setConfigWidgetsVisible(boolean visible) {
        vmNameBox.visible = visible;
        vmNameBox.setEditable(visible);
        startCommandBox.visible = visible;
        startCommandBox.setEditable(visible);
        stopCommandBox.visible = visible;
        stopCommandBox.setEditable(visible);

        saveButton.visible = visible;
        saveButton.active = visible;
        refreshButton.visible = visible;
        refreshButton.active = visible;
        closeButton.visible = visible;
        closeButton.active = visible;

        fullscreenButton.visible = true;
        fullscreenButton.active = true;
        fullscreenButton.setMessage(Component.translatable(vmFullscreen
                ? "screen.tech_revised.vm.button.exit_fullscreen"
                : "screen.tech_revised.vm.button.fullscreen"));
        if (vmFullscreen) {
            fullscreenButton.setX(8);
            fullscreenButton.setY(8);
            fullscreenButton.setWidth(90);
        } else {
            fullscreenButton.setX(this.leftPos + PREVIEW_X);
            fullscreenButton.setY(this.topPos + 176);
            fullscreenButton.setWidth(80);
        }
    }

    private void updatePreviewArea() {
        if (vmFullscreen) {
            previewAreaX = 0;
            previewAreaY = 0;
            previewAreaWidth = Math.max(1, this.width);
            previewAreaHeight = Math.max(1, this.height);
        } else {
            previewAreaX = this.leftPos + PREVIEW_X;
            previewAreaY = this.topPos + PREVIEW_Y;
            previewAreaWidth = PREVIEW_WIDTH;
            previewAreaHeight = PREVIEW_HEIGHT;
        }
    }

    private VmCommandIntegration.VmSpecialKey mapSpecialKey(int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> VmCommandIntegration.VmSpecialKey.ENTER;
            case GLFW.GLFW_KEY_BACKSPACE -> VmCommandIntegration.VmSpecialKey.BACKSPACE;
            case GLFW.GLFW_KEY_TAB -> VmCommandIntegration.VmSpecialKey.TAB;
            case GLFW.GLFW_KEY_ESCAPE -> VmCommandIntegration.VmSpecialKey.ESCAPE;
            case GLFW.GLFW_KEY_LEFT -> VmCommandIntegration.VmSpecialKey.LEFT;
            case GLFW.GLFW_KEY_RIGHT -> VmCommandIntegration.VmSpecialKey.RIGHT;
            case GLFW.GLFW_KEY_UP -> VmCommandIntegration.VmSpecialKey.UP;
            case GLFW.GLFW_KEY_DOWN -> VmCommandIntegration.VmSpecialKey.DOWN;
            case GLFW.GLFW_KEY_DELETE -> VmCommandIntegration.VmSpecialKey.DELETE;
            case GLFW.GLFW_KEY_HOME -> VmCommandIntegration.VmSpecialKey.HOME;
            case GLFW.GLFW_KEY_END -> VmCommandIntegration.VmSpecialKey.END;
            case GLFW.GLFW_KEY_PAGE_UP -> VmCommandIntegration.VmSpecialKey.PAGE_UP;
            case GLFW.GLFW_KEY_PAGE_DOWN -> VmCommandIntegration.VmSpecialKey.PAGE_DOWN;
            case GLFW.GLFW_KEY_F1 -> VmCommandIntegration.VmSpecialKey.F1;
            case GLFW.GLFW_KEY_F2 -> VmCommandIntegration.VmSpecialKey.F2;
            case GLFW.GLFW_KEY_F3 -> VmCommandIntegration.VmSpecialKey.F3;
            case GLFW.GLFW_KEY_F4 -> VmCommandIntegration.VmSpecialKey.F4;
            case GLFW.GLFW_KEY_F5 -> VmCommandIntegration.VmSpecialKey.F5;
            case GLFW.GLFW_KEY_F6 -> VmCommandIntegration.VmSpecialKey.F6;
            case GLFW.GLFW_KEY_F7 -> VmCommandIntegration.VmSpecialKey.F7;
            case GLFW.GLFW_KEY_F8 -> VmCommandIntegration.VmSpecialKey.F8;
            case GLFW.GLFW_KEY_F9 -> VmCommandIntegration.VmSpecialKey.F9;
            case GLFW.GLFW_KEY_F10 -> VmCommandIntegration.VmSpecialKey.F10;
            case GLFW.GLFW_KEY_F12 -> VmCommandIntegration.VmSpecialKey.F12;
            default -> null;
        };
    }
}
