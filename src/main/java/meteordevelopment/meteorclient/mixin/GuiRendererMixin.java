/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.MeteorMcGuiRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.util.profiler.Profilers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Unique
    private GuiRenderState renderState;

    @Unique
    private MeteorMcGuiRenderer guiRenderer;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init$meteor(GuiRenderState renderState, VertexConsumerProvider.Immediate bufferSource, OrderedRenderCommandQueue submitNodeCollector, RenderDispatcher featureRenderDispatcher, List<SpecialGuiElementRenderer<?>> pictureInPictureRenderers, CallbackInfo ci) {
        if ((GuiRenderer) (Object) this instanceof MeteorMcGuiRenderer) return;

        this.renderState = new GuiRenderState();

        guiRenderer = new MeteorMcGuiRenderer(
            this.renderState,
            bufferSource,
            submitNodeCollector,
            featureRenderDispatcher,
            pictureInPictureRenderers
        );
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void render$preGui(CallbackInfo ci) {
        if ((GuiRenderer) (Object) this instanceof MeteorMcGuiRenderer) return;
        var mc = MinecraftClient.getInstance();

        if (mc.currentScreen == null || mc.currentScreen instanceof WidgetScreen) return;
        meteor$render2D(mc);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void render$postGui(CallbackInfo ci) {
        if ((GuiRenderer) (Object) this instanceof MeteorMcGuiRenderer) return;
        var mc = MinecraftClient.getInstance();

        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(mc.getFramebuffer().getDepthAttachment(), 1.0);

        if (mc.currentScreen == null || mc.currentScreen instanceof WidgetScreen) {
            meteor$render2D(mc);
        }

        guiRenderer.incrementFrame();
    }

    @Unique
    private void meteor$render2D(MinecraftClient mc) {
        var mouseX = (int) mc.mouse.getScaledX(mc.getWindow());
        var mouseY = (int) mc.mouse.getScaledX(mc.getWindow());
        var fogRenderer = ((GameRendererAccessor) mc.gameRenderer).meteor$fogRenderer();

        if (Utils.canUpdate() || HudEditorScreen.isOpen()) {
            Profilers.get().push(MeteorClient.MOD_ID + "_render_2d");
            Utils.unscaledProjection();

            var graphics = new DrawContext(mc, renderState, mouseX, mouseY);
            var tickDelta = mc.getRenderTickCounter().getTickProgress(true);

            MeteorClient.EVENT_BUS.post(Render2DEvent.get(graphics, graphics.getScaledWindowWidth(), graphics.getScaledWindowHeight(), tickDelta));
            guiRenderer.render(fogRenderer.getFogBuffer(FogRenderer.FogType.NONE));

            Utils.scaledProjection();
            Profilers.get().pop();
        }

        if (mc.currentScreen instanceof WidgetScreen widgetScreen) {
            var graphics = new DrawContext(mc, renderState, mouseX, mouseY);
            var guiDelta = mc.getRenderTickCounter().getDynamicDeltaTicks();

            widgetScreen.renderCustom(graphics, mouseX, mouseY, guiDelta);
            guiRenderer.render(fogRenderer.getFogBuffer(FogRenderer.FogType.NONE));
        }
    }
}
