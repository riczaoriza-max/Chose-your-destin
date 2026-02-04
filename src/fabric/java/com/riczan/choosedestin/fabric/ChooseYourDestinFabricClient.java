package com.riczan.choosedestin.fabric;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public final class ChooseYourDestinFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(FabricGameAdapter.OPEN_CHOICE_PACKET, (client, handler, buf, responseSender) -> {
            String prompt = buf.readString();
            int optionCount = buf.readInt();
            List<String> options = new ArrayList<>();
            for (int i = 0; i < optionCount; i++) {
                options.add(buf.readString());
            }
            client.execute(() -> MinecraftClient.getInstance().setScreen(new FabricChoiceScreen(prompt, options)));
        });
    }
}
