package com.riczan.choosedestin.neoforge;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class NeoForgeChoiceScreen extends Screen {
    private final String prompt;
    private final List<String> options;

    public NeoForgeChoiceScreen(String prompt, List<String> options) {
        super(Component.literal("Choice"));
        this.prompt = prompt;
        this.options = options;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int startY = height / 2 - 20;
        int buttonWidth = 240;
        int buttonHeight = 20;
        addRenderableWidget(Button.builder(Component.literal(options.get(0)), button -> sendSelection(0))
            .bounds(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight)
            .build());
        addRenderableWidget(Button.builder(Component.literal(options.get(1)), button -> sendSelection(1))
            .bounds(centerX - buttonWidth / 2, startY + 30, buttonWidth, buttonHeight)
            .build());
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, prompt, width / 2, height / 2 - 60, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }

    private void sendSelection(int index) {
        NeoForgeNetworking.CHANNEL.sendToServer(new NeoForgeNetworking.SelectChoicePacket(index));
        Minecraft.getInstance().setScreen(null);
    }
}
