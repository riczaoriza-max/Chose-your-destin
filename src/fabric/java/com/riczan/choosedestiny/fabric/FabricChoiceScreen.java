package com.riczan.choosedestiny.fabric;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class FabricChoiceScreen extends Screen {
    private static final String NO_BONUS_LABEL = "no bonus";
    private static final String NEUTRAL_OPTION_LABEL = "Balanced path";
    private static final int PANEL_PADDING = 16;
    private static final int BUTTON_SPACING = 12;
    private static final int BUTTON_HEIGHT = 22;
    private static final int PANEL_WIDTH = 430;
    private static final int PANEL_HEIGHT = 235;
    private static final int CHOICE_PANEL_WIDTH = 194;
    private static final int CHOICE_PANEL_HEIGHT = 122;
    private static final int MAX_OPTION_LINES = 5;

    private static final int FRAME_BORDER_OUTER = 0xAA000000;
    private static final int FRAME_BORDER_INNER = 0x66FFFFFF;
    private static final int PANEL_BACKGROUND_TOP = 0xD0181C2A;
    private static final int PANEL_BACKGROUND_BOTTOM = 0xD010121B;
    private static final int TITLE_COLOR = 0xFFF4F5FF;
    private static final int SUBTITLE_COLOR = 0xFFB6BED2;

    private static final int BLUE_PANEL_COLOR = 0xB025579E;
    private static final int BLUE_PANEL_HOVER_COLOR = 0xCC2E68BA;
    private static final int BLUE_PANEL_ACCENT = 0xFF74A9FF;
    private static final int ORANGE_PANEL_COLOR = 0xB0AA5418;
    private static final int ORANGE_PANEL_HOVER_COLOR = 0xCCCD6A22;
    private static final int ORANGE_PANEL_ACCENT = 0xFFFFBF75;

    private static final int BLUE_BUTTON_COLOR = 0xFF3C6FD9;
    private static final int ORANGE_BUTTON_COLOR = 0xFFE48A1D;

    private final String prompt;
    private final List<String> options;
    private boolean submitted;

    public FabricChoiceScreen(String prompt, List<String> options) {
        super(Text.literal("Choose Your Destiny"));
        this.prompt = normalizePrompt(prompt);
        this.options = normalizeOptions(options);
        this.submitted = false;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int panelTop = height / 2 - PANEL_HEIGHT / 2;
        int buttonWidth = (PANEL_WIDTH - PANEL_PADDING * 2 - BUTTON_SPACING) / 2;
        int buttonY = panelTop + PANEL_HEIGHT - PANEL_PADDING - BUTTON_HEIGHT;
        int leftButtonX = centerX - BUTTON_SPACING / 2 - buttonWidth;
        int rightButtonX = centerX + BUTTON_SPACING / 2;

        addDrawableChild(ButtonWidget.builder(Text.literal("Choose 1: " + shortLabel(options.get(0))), button -> sendSelection(0))
            .dimensions(leftButtonX, buttonY, buttonWidth, BUTTON_HEIGHT)
            .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Choose 2: " + shortLabel(options.get(1))), button -> sendSelection(1))
            .dimensions(rightButtonX, buttonY, buttonWidth, BUTTON_HEIGHT)
            .build());
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void close() {
        if (submitted) {
            super.close();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        int panelLeft = width / 2 - PANEL_WIDTH / 2;
        int panelTop = height / 2 - PANEL_HEIGHT / 2;
        int panelRight = panelLeft + PANEL_WIDTH;
        int panelBottom = panelTop + PANEL_HEIGHT;

        drawFrame(context, panelLeft, panelTop, panelRight, panelBottom);

        int titleY = panelTop + PANEL_PADDING / 2;
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, titleY, TITLE_COLOR);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Select one option to activate for 3 minutes"), width / 2, titleY + 14, SUBTITLE_COLOR);

        int promptY = panelTop + PANEL_PADDING + 30;
        for (OrderedText line : textRenderer.wrapLines(Text.literal(prompt), PANEL_WIDTH - PANEL_PADDING * 2)) {
            int lineWidth = textRenderer.getWidth(line);
            context.drawTextWithShadow(textRenderer, line, width / 2 - lineWidth / 2, promptY, 0xFFE2E4ED);
            promptY += textRenderer.fontHeight + 1;
        }

        int choicePanelTop = panelTop + PANEL_PADDING + 76;
        int leftPanelLeft = panelLeft + PANEL_PADDING;
        int rightPanelLeft = panelRight - PANEL_PADDING - CHOICE_PANEL_WIDTH;

        boolean leftHovered = isInside(mouseX, mouseY, leftPanelLeft, choicePanelTop, CHOICE_PANEL_WIDTH, CHOICE_PANEL_HEIGHT);
        boolean rightHovered = isInside(mouseX, mouseY, rightPanelLeft, choicePanelTop, CHOICE_PANEL_WIDTH, CHOICE_PANEL_HEIGHT);

        drawChoiceCard(context, leftPanelLeft, choicePanelTop, CHOICE_PANEL_WIDTH, CHOICE_PANEL_HEIGHT, leftHovered,
            BLUE_PANEL_COLOR, BLUE_PANEL_HOVER_COLOR, BLUE_PANEL_ACCENT, "Option 1", options.get(0));
        drawChoiceCard(context, rightPanelLeft, choicePanelTop, CHOICE_PANEL_WIDTH, CHOICE_PANEL_HEIGHT, rightHovered,
            ORANGE_PANEL_COLOR, ORANGE_PANEL_HOVER_COLOR, ORANGE_PANEL_ACCENT, "Option 2", options.get(1));

        int buttonWidth = (PANEL_WIDTH - PANEL_PADDING * 2 - BUTTON_SPACING) / 2;
        int buttonY = panelBottom - PANEL_PADDING - BUTTON_HEIGHT;
        int leftButtonX = width / 2 - BUTTON_SPACING / 2 - buttonWidth;
        int rightButtonX = width / 2 + BUTTON_SPACING / 2;
        context.fill(leftButtonX - 2, buttonY - 2, leftButtonX + buttonWidth + 2, buttonY + BUTTON_HEIGHT + 2, BLUE_BUTTON_COLOR);
        context.fill(rightButtonX - 2, buttonY - 2, rightButtonX + buttonWidth + 2, buttonY + BUTTON_HEIGHT + 2, ORANGE_BUTTON_COLOR);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawFrame(DrawContext context, int left, int top, int right, int bottom) {
        context.fill(left - 1, top - 1, right + 1, bottom + 1, FRAME_BORDER_OUTER);
        context.fill(left, top, right, bottom, PANEL_BACKGROUND_BOTTOM);
        context.fillGradient(left + 1, top + 1, right - 1, bottom - 1, PANEL_BACKGROUND_TOP, PANEL_BACKGROUND_BOTTOM);
        context.fill(left + 2, top + 2, right - 2, top + 3, FRAME_BORDER_INNER);
    }

    private void drawChoiceCard(
        DrawContext context,
        int left,
        int top,
        int width,
        int height,
        boolean hovered,
        int baseColor,
        int hoverColor,
        int accentColor,
        String optionTitle,
        String optionValue
    ) {
        int right = left + width;
        int bottom = top + height;
        int bodyColor = hovered ? hoverColor : baseColor;
        context.fill(left - 1, top - 1, right + 1, bottom + 1, 0x60000000);
        context.fill(left, top, right, bottom, bodyColor);
        context.fillGradient(left, top, right, top + 18, 0x55FFFFFF, 0x00FFFFFF);
        context.fill(left, top, right, top + 2, accentColor);

        context.drawTextWithShadow(textRenderer, Text.literal(optionTitle), left + PANEL_PADDING - 4, top + 6, 0xFFFFFFFF);
        drawWrappedOption(context, optionValue, left + PANEL_PADDING - 4, top + 24, width - (PANEL_PADDING * 2) + 8);
    }

    private void drawWrappedOption(DrawContext context, String optionValue, int x, int y, int maxWidth) {
        List<OrderedText> lines = textRenderer.wrapLines(Text.literal(optionValue), maxWidth);
        int lineY = y;
        for (int i = 0; i < lines.size() && i < MAX_OPTION_LINES; i++) {
            OrderedText line = lines.get(i);
            if (i == MAX_OPTION_LINES - 1 && lines.size() > MAX_OPTION_LINES) {
                String trimmed = textRenderer.getTextHandler().trimToWidth(Text.literal(optionValue).getString(), maxWidth - 10, net.minecraft.text.Style.EMPTY);
                context.drawText(textRenderer, Text.literal(trimmed + "..."), x, lineY, 0xFFEDEFF8, false);
                return;
            }
            context.drawText(textRenderer, line, x, lineY, 0xFFEDEFF8, false);
            lineY += textRenderer.fontHeight + 2;
        }
    }

    private static boolean isInside(int mouseX, int mouseY, int left, int top, int width, int height) {
        return mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height;
    }

    private void sendSelection(int index) {
        if (submitted) {
            return;
        }
        submitted = true;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(index);
        ClientPlayNetworking.send(ChooseYourDestinyFabric.SELECT_CHOICE_PACKET, buf);
        MinecraftClient.getInstance().setScreen(null);
    }

    private static String normalizePrompt(String prompt) {
        if (prompt == null) {
            return "Do you prefer option 1 or option 2?";
        }
        return prompt.replaceAll("(?i)\\s+or\\s+no\\s+bonus\\??$", "");
    }

    private static List<String> normalizeOptions(List<String> options) {
        List<String> normalized = new ArrayList<>();
        if (options == null) {
            options = List.of();
        }
        for (int i = 0; i < options.size(); i++) {
            String label = options.get(i);
            if (label == null) {
                normalized.add("Choice " + (i + 1));
                continue;
            }
            String lower = label.toLowerCase(Locale.ROOT).trim();
            if (NO_BONUS_LABEL.equals(lower)) {
                normalized.add(NEUTRAL_OPTION_LABEL);
            } else {
                normalized.add(label);
            }
        }
        while (normalized.size() < 2) {
            normalized.add("Choice " + (normalized.size() + 1));
        }
        return normalized;
    }

    private static String shortLabel(String value) {
        if (value == null || value.isBlank()) {
            return "Choice";
        }
        if (value.length() <= 20) {
            return value;
        }
        return value.substring(0, 17) + "...";
    }
}
