package com.riczan.choosedestin.fabric;

import java.util.List;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public final class FabricChoiceScreen extends Screen {
    private final String prompt;
    private final List<String> options;

    public FabricChoiceScreen(String prompt, List<String> options) {
        super(Text.literal("Choice"));
        this.prompt = prompt;
        this.options = options;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int startY = height / 2 - 20;
        int buttonWidth = 240;
        int buttonHeight = 20;
        addDrawableChild(ButtonWidget.builder(Text.literal(options.get(0)), button -> sendSelection(0))
            .dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight)
            .build());
        addDrawableChild(ButtonWidget.builder(Text.literal(options.get(1)), button -> sendSelection(1))
            .dimensions(centerX - buttonWidth / 2, startY + 30, buttonWidth, buttonHeight)
            .build());
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, prompt, width / 2, height / 2 - 60, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    private void sendSelection(int index) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(index);
        ClientPlayNetworking.send(ChooseYourDestinFabric.SELECT_CHOICE_PACKET, buf);
        Minecraft.getInstance().setScreen(null);
    }
}
