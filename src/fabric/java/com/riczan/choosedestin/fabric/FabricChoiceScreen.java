package com.riczan.choosedestin.fabric;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public final class FabricChoiceScreen extends Screen {
    private static final String NO_BONUS_LABEL = "no bonus";
    private static final String NEUTRAL_OPTION_LABEL = "Balanced path";
    private static final int PANEL_PADDING = 16;
    private static final int BUTTON_SPACING = 12;
    private static final int BUTTON_HEIGHT = 22;
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 210;
    private static final int CHOICE_PANEL_WIDTH = 190;
    private static final int CHOICE_PANEL_HEIGHT = 120;
    private static final int BLUE_PANEL_COLOR = 0xAA1E4FA3;
    private static final int ORANGE_PANEL_COLOR = 0xAAC45C12;
    private static final int BLUE_BUTTON_COLOR = 0xFF3C6FD9;
    private static final int ORANGE_BUTTON_COLOR = 0xFFE48A1D;

    private final String prompt;
    private final List<String> options;

    public FabricChoiceScreen(String prompt, List<String> options) {
        super(Text.literal("Choose Your Destin"));
        this.prompt = normalizePrompt(prompt);
        this.options = normalizeOptions(options);
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int panelTop = height / 2 - PANEL_HEIGHT / 2;
        int buttonWidth = (PANEL_WIDTH - PANEL_PADDING * 2 - BUTTON_SPACING) / 2;
        int buttonY = panelTop + PANEL_HEIGHT - PANEL_PADDING - BUTTON_HEIGHT;
        int leftButtonX = centerX - BUTTON_SPACING / 2 - buttonWidth;
        int rightButtonX = centerX + BUTTON_SPACING / 2;

        addDrawableChild(ButtonWidget.builder(Text.literal("Pick 1: " + shortLabel(options.get(0))), button -> sendSelection(0))
            .dimensions(leftButtonX, buttonY, buttonWidth, BUTTON_HEIGHT)
            .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Pick 2: " + shortLabel(options.get(1))), button -> sendSelection(1))
            .dimensions(rightButtonX, buttonY, buttonWidth, BUTTON_HEIGHT)
            .build());
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        int panelLeft = width / 2 - PANEL_WIDTH / 2;
        int panelTop = height / 2 - PANEL_HEIGHT / 2;
        int panelRight = panelLeft + PANEL_WIDTH;
        int panelBottom = panelTop + PANEL_HEIGHT;
        context.fill(panelLeft, panelTop, panelRight, panelBottom, 0xB0000000);

        int titleY = panelTop + PANEL_PADDING / 2;
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, titleY, 0xF5F5F5);

        int promptY = panelTop + PANEL_PADDING + 18;
        for (net.minecraft.text.OrderedText line : textRenderer.wrapLines(Text.literal(prompt), PANEL_WIDTH - PANEL_PADDING * 2)) {
            int lineWidth = textRenderer.getWidth(line);
            context.drawTextWithShadow(textRenderer, line, width / 2 - lineWidth / 2, promptY, 0xE0E0E0);
            promptY += textRenderer.fontHeight + 2;
        }

        int choicePanelTop = panelTop + PANEL_PADDING + 68;
        int leftPanelLeft = panelLeft + PANEL_PADDING;
        int rightPanelLeft = panelRight - PANEL_PADDING - CHOICE_PANEL_WIDTH;
        int choicePanelBottom = choicePanelTop + CHOICE_PANEL_HEIGHT;

        context.fill(leftPanelLeft, choicePanelTop, leftPanelLeft + CHOICE_PANEL_WIDTH, choicePanelBottom, BLUE_PANEL_COLOR);
        context.fill(rightPanelLeft, choicePanelTop, rightPanelLeft + CHOICE_PANEL_WIDTH, choicePanelBottom, ORANGE_PANEL_COLOR);

        int optionTextY = choicePanelTop + PANEL_PADDING;
        context.drawText(textRenderer, Text.literal("Option 1"), leftPanelLeft + PANEL_PADDING, optionTextY, 0xFFFFFF, false);
        int leftTextY = optionTextY + textRenderer.fontHeight + 4;
        for (net.minecraft.text.OrderedText line : textRenderer.wrapLines(Text.literal(options.get(0)), CHOICE_PANEL_WIDTH - PANEL_PADDING * 2)) {
            context.drawText(textRenderer, line, leftPanelLeft + PANEL_PADDING, leftTextY, 0xE6E6E6, false);
            leftTextY += textRenderer.fontHeight + 2;
        }

        context.drawText(textRenderer, Text.literal("Option 2"), rightPanelLeft + PANEL_PADDING, optionTextY, 0xFFFFFF, false);
        int rightTextY = optionTextY + textRenderer.fontHeight + 4;
        for (net.minecraft.text.OrderedText line : textRenderer.wrapLines(Text.literal(options.get(1)), CHOICE_PANEL_WIDTH - PANEL_PADDING * 2)) {
            context.drawText(textRenderer, line, rightPanelLeft + PANEL_PADDING, rightTextY, 0xFFF1E0, false);
            rightTextY += textRenderer.fontHeight + 2;
        }

        int buttonWidth = (PANEL_WIDTH - PANEL_PADDING * 2 - BUTTON_SPACING) / 2;
        int buttonY = panelBottom - PANEL_PADDING - BUTTON_HEIGHT;
        int leftButtonX = width / 2 - BUTTON_SPACING / 2 - buttonWidth;
        int rightButtonX = width / 2 + BUTTON_SPACING / 2;
        context.fill(leftButtonX - 2, buttonY - 2, leftButtonX + buttonWidth + 2, buttonY + BUTTON_HEIGHT + 2, BLUE_BUTTON_COLOR);
        context.fill(rightButtonX - 2, buttonY - 2, rightButtonX + buttonWidth + 2, buttonY + BUTTON_HEIGHT + 2, ORANGE_BUTTON_COLOR);
        super.render(context, mouseX, mouseY, delta);
    }

    private void sendSelection(int index) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(index);
        ClientPlayNetworking.send(ChooseYourDestinFabric.SELECT_CHOICE_PACKET, buf);
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
        if (value.length() <= 24) {
            return value;
        }
        return value.substring(0, 21) + "...";
    }
}
