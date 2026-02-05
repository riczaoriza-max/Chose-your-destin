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
    private static final int PANEL_PADDING = 16;
    private static final int BUTTON_SPACING = 12;
    private static final int BUTTON_HEIGHT = 22;
    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 160;

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
        int buttonWidth = PANEL_WIDTH - PANEL_PADDING * 2;
        int firstButtonY = panelTop + PANEL_HEIGHT - PANEL_PADDING - BUTTON_HEIGHT * 2 - BUTTON_SPACING;

        addDrawableChild(ButtonWidget.builder(Text.literal("Opção 1"), button -> sendSelection(0))
            .dimensions(centerX - buttonWidth / 2, firstButtonY, buttonWidth, BUTTON_HEIGHT)
            .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Opção 2"), button -> sendSelection(1))
            .dimensions(centerX - buttonWidth / 2, firstButtonY + BUTTON_HEIGHT + BUTTON_SPACING, buttonWidth, BUTTON_HEIGHT)
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

        int promptY = panelTop + PANEL_PADDING + 16;
        for (Text line : textRenderer.wrapLines(Text.literal(prompt), PANEL_WIDTH - PANEL_PADDING * 2)) {
            context.drawCenteredTextWithShadow(textRenderer, line, width / 2, promptY, 0xE0E0E0);
            promptY += textRenderer.fontHeight + 2;
        }

        int optionTextY = panelTop + PANEL_HEIGHT - PANEL_PADDING - BUTTON_HEIGHT * 2 - BUTTON_SPACING - (textRenderer.fontHeight * 2);
        context.drawText(textRenderer, Text.literal(options.get(0)), panelLeft + PANEL_PADDING, optionTextY, 0xCFCFCF, false);
        context.drawText(textRenderer, Text.literal(options.get(1)), panelLeft + PANEL_PADDING, optionTextY + textRenderer.fontHeight + 2, 0xCFCFCF, false);
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
            return "Preferes a opção 1 ou a opção 2?";
        }
        return prompt.replaceAll("(?i)\\s+or\\s+no\\s+bonus\\??$", "");
    }

    private static List<String> normalizeOptions(List<String> options) {
        List<String> normalized = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            String label = options.get(i);
            if (label == null) {
                normalized.add("Escolha " + (i + 1));
                continue;
            }
            String lower = label.toLowerCase(Locale.ROOT).trim();
            if (NO_BONUS_LABEL.equals(lower)) {
                normalized.add(i == 0 ? "Caminho A" : "Caminho B");
            } else {
                normalized.add(label);
            }
        }
        while (normalized.size() < 2) {
            normalized.add("Escolha " + (normalized.size() + 1));
        }
        return normalized;
    }
}
