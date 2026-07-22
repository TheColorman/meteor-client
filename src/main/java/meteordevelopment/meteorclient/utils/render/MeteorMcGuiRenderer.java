/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.render;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.gui.render.state.GuiRenderState;

import java.util.List;

public class MeteorMcGuiRenderer extends GuiRenderer {
    public MeteorMcGuiRenderer(GuiRenderState renderState, VertexConsumerProvider.Immediate bufferSource, OrderedRenderCommandQueue submitNodeCollector, RenderDispatcher featureRenderDispatcher, List<SpecialGuiElementRenderer<?>> pictureInPictureRenderers) {
        super(renderState, bufferSource, submitNodeCollector, featureRenderDispatcher, pictureInPictureRenderers);
    }
}
