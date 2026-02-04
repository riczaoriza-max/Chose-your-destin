package com.riczan.choosedestin.quilt;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public final class ChooseYourDestinQuiltClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        ClientPlayNetworking.registerGlobalReceiver(QuiltGameAdapter.OPEN_CHOICE_PACKET, (client, handler, buf, responseSender) -> {
            String prompt = buf.readUtf();
            int optionCount = buf.readInt();
            List<String> options = new ArrayList<>();
            for (int i = 0; i < optionCount; i++) {
                options.add(buf.readUtf());
            }
            client.execute(() -> Minecraft.getInstance().setScreen(new QuiltChoiceScreen(prompt, options)));
        });
    }
}
