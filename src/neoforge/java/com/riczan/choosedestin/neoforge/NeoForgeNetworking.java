package com.riczan.choosedestin.neoforge;

import com.riczan.choosedestin.Choice;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.DistExecutor;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.simple.SimpleChannel;

public final class NeoForgeNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("choose_your_destin", "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private NeoForgeNetworking() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, OpenChoicePacket.class, OpenChoicePacket::encode, OpenChoicePacket::decode, OpenChoicePacket::handle);
        CHANNEL.registerMessage(id++, SelectChoicePacket.class, SelectChoicePacket::encode, SelectChoicePacket::decode, SelectChoicePacket::handle);
    }

    public static void sendOpenChoice(ServerPlayer player, Choice choice) {
        List<String> options = new ArrayList<>();
        choice.getOptions().forEach(option -> options.add(option.getLabel()));
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenChoicePacket(choice.getPrompt(), options));
    }

    public static final class OpenChoicePacket {
        private final String prompt;
        private final List<String> options;

        public OpenChoicePacket(String prompt, List<String> options) {
            this.prompt = prompt;
            this.options = options;
        }

        static void encode(OpenChoicePacket packet, FriendlyByteBuf buf) {
            buf.writeUtf(packet.prompt);
            buf.writeInt(packet.options.size());
            for (String option : packet.options) {
                buf.writeUtf(option);
            }
        }

        static OpenChoicePacket decode(FriendlyByteBuf buf) {
            String prompt = buf.readUtf();
            int count = buf.readInt();
            List<String> options = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                options.add(buf.readUtf());
            }
            return new OpenChoicePacket(prompt, options);
        }

        static void handle(OpenChoicePacket packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft.getInstance().setScreen(new NeoForgeChoiceScreen(packet.prompt, packet.options));
            }));
            context.get().setPacketHandled(true);
        }
    }

    public static final class SelectChoicePacket {
        private final int index;

        public SelectChoicePacket(int index) {
            this.index = index;
        }

        static void encode(SelectChoicePacket packet, FriendlyByteBuf buf) {
            buf.writeInt(packet.index);
        }

        static SelectChoicePacket decode(FriendlyByteBuf buf) {
            return new SelectChoicePacket(buf.readInt());
        }

        static void handle(SelectChoicePacket packet, Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            ctx.enqueueWork(() -> {
                ServerPlayer player = ctx.getSender();
                if (player != null) {
                    ChooseYourDestinNeoForge.selectOption(player, packet.index);
                }
            });
            ctx.setPacketHandled(true);
        }
    }
}
