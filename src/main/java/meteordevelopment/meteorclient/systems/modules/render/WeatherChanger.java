/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;

public class WeatherChanger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> rainLevel = sgGeneral.add(new DoubleSetting.Builder()
        .name("rain-level")
        .description("The specified rain level to be set.")
        .defaultValue(0)
        .sliderRange(0, 1)
        .build()
    );

    private final Setting<Double> thunderLevel = sgGeneral.add(new DoubleSetting.Builder()
        .name("thunder-level")
        .description("The specified thunder level to be set.")
        .defaultValue(0)
        .sliderRange(0, 1)
        .build()
    );

    private float oldThunderLevel;
    private float oldRainLevel;

    public WeatherChanger() {
        super(Categories.Render, "weather-changer", "Allows you to override the world's current weather.");
    }

    @Override
    public void onActivate() {
        if (mc.world == null) return;

        oldThunderLevel = mc.world.getThunderGradient(1f);
        oldRainLevel = mc.world.getRainGradient(1f);
    }

    @Override
    public void onDeactivate() {
        if (mc.world == null) return;

        mc.world.setRainGradient(oldRainLevel);
        mc.world.setThunderGradient(oldThunderLevel);
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.packet instanceof GameStateChangeS2CPacket packet)) return;

        GameStateChangeS2CPacket.Reason type = packet.getReason();
        if (!isWeatherPacket(type)) return;

        if (type == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED) {
            oldThunderLevel = packet.getValue();
        } else if (type == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED) {
            oldRainLevel = packet.getValue();
        }

        event.cancel();
    }

    private boolean isWeatherPacket(GameStateChangeS2CPacket.Reason type) {
        return type == GameStateChangeS2CPacket.RAIN_STARTED
            || type == GameStateChangeS2CPacket.RAIN_STOPPED
            || type == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED
            || type == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null) return;

        mc.world.setRainGradient(rainLevel.get().floatValue());
        mc.world.setThunderGradient(thunderLevel.get().floatValue());
    }
}
