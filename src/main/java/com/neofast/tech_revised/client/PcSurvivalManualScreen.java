package com.neofast.tech_revised.client;

import com.neofast.tech_revised.multiblock.MachineLayout;
import com.neofast.tech_revised.multiblock.MachineLayouts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PcSurvivalManualScreen extends Screen {
    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 210;
    private static final int BODY_WIDTH = 272;
    private static final int MAX_BODY_LINES = 11;
    private static final int ICON_SIZE = 16;

    private boolean showingIndex = true;
    private int chapterIndex;
    private int subPage;
    private List<FormattedCharSequence> wrapped = List.of();
    private int subPageCount = 1;
    private Button prevButton;
    private Button nextButton;
    private Button indexButton;
    private Button previewButton;
    private List<ItemStack> icons = List.of();
    private String layoutId;

    public PcSurvivalManualScreen() {
        super(Component.translatable("screen.tech_revised.manual.title"));
    }

    @Override
    protected void init() {
        int x = (this.width - PANEL_WIDTH) / 2;
        int y = (this.height - PANEL_HEIGHT) / 2;
        indexButton = addRenderableWidget(Button.builder(Component.translatable("screen.tech_revised.manual.index"),
                        button -> showIndex())
                .bounds(x + 8, y + PANEL_HEIGHT - 24, 52, 16)
                .build());
        prevButton = addRenderableWidget(Button.builder(Component.translatable("screen.tech_revised.manual.prev"),
                        button -> turnPage(-1))
                .bounds(x + 64, y + PANEL_HEIGHT - 24, 48, 16)
                .build());
        nextButton = addRenderableWidget(Button.builder(Component.translatable("screen.tech_revised.manual.next"),
                        button -> turnPage(1))
                .bounds(x + PANEL_WIDTH - 56, y + PANEL_HEIGHT - 24, 48, 16)
                .build());
        previewButton = addRenderableWidget(Button.builder(Component.translatable("screen.tech_revised.manual.preview"),
                        button -> projectHologram())
                .bounds(x + 116, y + PANEL_HEIGHT - 24, 72, 16)
                .build());
        refreshBody();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, 0, 0, 0);
        int x = (this.width - PANEL_WIDTH) / 2;
        int y = (this.height - PANEL_HEIGHT) / 2;
        IndustrialGuiStyle.drawMetalPanel(graphics, x, y, PANEL_WIDTH, PANEL_HEIGHT);
        IndustrialGuiStyle.drawRecess(graphics, x + 8, y + 22, BODY_WIDTH, 158);

        String title = showingIndex
                ? Component.translatable("screen.tech_revised.manual.index").getString()
                : PcSurvivalManualPages.get(chapterIndex).title();
        IndustrialGuiStyle.drawTruncated(graphics, font, Component.literal(title),
                x + 12, y + 8, 200, IndustrialGuiStyle.TITLE_COLOR);

        String pageLabel;
        if (showingIndex) {
            pageLabel = (subPage + 1) + "/" + subPageCount;
        } else {
            pageLabel = (chapterIndex + 1) + "/" + PcSurvivalManualPages.size();
            if (subPageCount > 1) {
                pageLabel += "  " + (subPage + 1) + "/" + subPageCount;
            }
        }
        graphics.drawString(font, pageLabel, x + PANEL_WIDTH - 12 - font.width(pageLabel), y + 8, IndustrialGuiStyle.MUTED_COLOR, false);

        int start = subPage * MAX_BODY_LINES;
        int end = Math.min(wrapped.size(), start + MAX_BODY_LINES);
        int textY = y + 28;
        for (int i = start; i < end; i++) {
            graphics.drawString(font, wrapped.get(i), x + 14, textY, IndustrialGuiStyle.BODY_COLOR, false);
            textY += IndustrialGuiStyle.LINE_HEIGHT;
        }

        if (!showingIndex && !icons.isEmpty()) {
            int iconY = y + PANEL_HEIGHT - 46;
            int iconX = x + 12;
            for (int i = 0; i < icons.size(); i++) {
                int ix = iconX + i * 20;
                IndustrialGuiStyle.drawSlot(graphics, ix, iconY);
                graphics.renderItem(icons.get(i), ix, iconY);
                if (mouseX >= ix && mouseX < ix + ICON_SIZE && mouseY >= iconY && mouseY < iconY + ICON_SIZE) {
                    graphics.renderTooltip(font, icons.get(i), mouseX, mouseY);
                }
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!showingIndex && !icons.isEmpty() && (button == 0 || button == 1)) {
            int x = (this.width - PANEL_WIDTH) / 2;
            int y = (this.height - PANEL_HEIGHT) / 2;
            int iconY = y + PANEL_HEIGHT - 46;
            int iconX = x + 12;
            for (int i = 0; i < icons.size(); i++) {
                int ix = iconX + i * 20;
                if (mouseX >= ix && mouseX < ix + ICON_SIZE && mouseY >= iconY && mouseY < iconY + ICON_SIZE) {
                    OptionalJei.showRecipes(icons.get(i), button == 1);
                    return true;
                }
            }
        }
        if (showingIndex && button == 0) {
            int y = (this.height - PANEL_HEIGHT) / 2;
            int local = (int) ((mouseY - (y + 28)) / IndustrialGuiStyle.LINE_HEIGHT);
            if (local >= 0 && local < MAX_BODY_LINES) {
                int line = subPage * MAX_BODY_LINES + local;
                Integer target = chapterNumberOnLine(line);
                if (target != null) {
                    setChapter(target);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    private void showIndex() {
        showingIndex = true;
        subPage = 0;
        refreshBody();
    }

    private void turnPage(int delta) {
        int nextSub = subPage + delta;
        if (nextSub >= 0 && nextSub < subPageCount) {
            subPage = nextSub;
            updateButtons();
            return;
        }
        if (showingIndex) {
            if (delta > 0) {
                setChapter(0);
            }
            return;
        }
        int nextChapter = chapterIndex + delta;
        if (nextChapter >= 0 && nextChapter < PcSurvivalManualPages.size()) {
            setChapter(nextChapter);
            if (delta < 0) {
                subPage = subPageCount - 1;
                updateButtons();
            }
        } else if (nextChapter < 0) {
            showIndex();
        }
    }

    private void setChapter(int index) {
        showingIndex = false;
        chapterIndex = Math.max(0, Math.min(PcSurvivalManualPages.size() - 1, index));
        subPage = 0;
        refreshBody();
    }

    private void projectHologram() {
        if (layoutId == null) {
            return;
        }
        MachineLayout layout = MachineLayouts.get(layoutId);
        if (layout == null) {
            return;
        }
        minecraft.setScreen(null);
        MultiblockPreviewRenderer.previewInFrontOfPlayer(layout.toBlueprint());
    }

    private void refreshBody() {
        PcSurvivalManualPages.Chapter chapter = showingIndex ? null : PcSurvivalManualPages.get(chapterIndex);
        layoutId = chapter == null ? null : chapter.layoutId();
        icons = chapter == null ? List.of() : chapter.icons();
        String body = showingIndex ? buildIndexBody() : chapter.body();
        wrapped = new ArrayList<>(font.split(Component.literal(body), BODY_WIDTH - 12));
        subPageCount = Math.max(1, (wrapped.size() + MAX_BODY_LINES - 1) / MAX_BODY_LINES);
        subPage = Math.min(subPage, subPageCount - 1);
        updateButtons();
    }

    private void updateButtons() {
        boolean hasPrev = showingIndex ? subPage > 0 : chapterIndex > 0 || subPage > 0;
        boolean hasNext = showingIndex
                ? subPage < subPageCount - 1 || PcSurvivalManualPages.size() > 0
                : chapterIndex < PcSurvivalManualPages.size() - 1 || subPage < subPageCount - 1;
        if (prevButton != null) {
            prevButton.active = hasPrev;
        }
        if (nextButton != null) {
            nextButton.active = hasNext;
        }
        if (indexButton != null) {
            indexButton.active = !showingIndex;
        }
        if (previewButton != null) {
            previewButton.visible = !showingIndex && layoutId != null;
            previewButton.active = previewButton.visible;
        }
    }

    private String buildIndexBody() {
        StringBuilder builder = new StringBuilder(Component.translatable("screen.tech_revised.manual.click_chapter").getString());
        builder.append('\n');
        String lastCategory = "";
        for (int i = 0; i < PcSurvivalManualPages.size(); i++) {
            PcSurvivalManualPages.Chapter chapter = PcSurvivalManualPages.get(i);
            if (!chapter.category().equals(lastCategory)) {
                lastCategory = chapter.category();
                builder.append('\n');
                builder.append(PcSurvivalManualPages.categoryTitle(lastCategory));
                builder.append('\n');
            }
            builder.append(String.format("%2d  %s%n", i + 1, chapter.title()));
        }
        return builder.toString();
    }

    private Integer chapterNumberOnLine(int wrappedLine) {
        if (!showingIndex || wrappedLine < 0 || wrappedLine >= wrapped.size()) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        wrapped.get(wrappedLine).accept((index, style, codePoint) -> {
            text.appendCodePoint(codePoint);
            return true;
        });
        String line = text.toString().trim();
        int space = line.indexOf(' ');
        if (space <= 0) {
            return null;
        }
        try {
            int number = Integer.parseInt(line.substring(0, space));
            if (number >= 1 && number <= PcSurvivalManualPages.size()) {
                return number - 1;
            }
        } catch (NumberFormatException ignored) {
            return null;
        }
        return null;
    }
}
