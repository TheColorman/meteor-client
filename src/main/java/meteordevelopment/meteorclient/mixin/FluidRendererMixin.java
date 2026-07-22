/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
    @Unique
    private static final ThreadLocal<Integer> ALPHAS = ThreadLocal.withInitial(() -> -1);
    @Unique
    private static final ThreadLocal<Boolean> AMBIENT = ThreadLocal.withInitial(() -> false);
    @Unique
    private static final ThreadLocal<Boolean> FORCE_XRAY_FLUID_SIDES = ThreadLocal.withInitial(() -> false);
    @Unique
    private Xray xray;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        xray = Modules.get().get(Xray.class);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo info) {
        Ambience ambience = Modules.get().get(Ambience.class);
        AMBIENT.set(ambience.isActive() && ambience.customLavaColor.get() && fluidState.isIn(FluidTags.LAVA));

        // Xray and Wallhack
        int alpha = Xray.getFluidAlpha(fluidState, pos);

        if (alpha == 0) {
            FORCE_XRAY_FLUID_SIDES.set(false);
            info.cancel();
            return;
        }

        ALPHAS.set(alpha);
        FORCE_XRAY_FLUID_SIDES.set(xray.isActive());
    }

    @WrapOperation(
        method = "tesselate",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/FluidRenderer;shouldSkipRendering(Lnet/minecraft/util/math/Direction;FLnet/minecraft/block/BlockState;)Z")
    )
    private boolean onIsFaceOccludedByNeighbor(Direction direction, float height, BlockState neighborState, Operation<Boolean> original) {
        boolean occluded = original.call(direction, height, neighborState);

        if (!occluded) return false;
        if (direction.getAxis().isVertical()) return true;
        if (!FORCE_XRAY_FLUID_SIDES.get()) return true;
        return !xray.isBlocked(neighborState.getBlock(), null);
    }

    @Inject(method = "vertex", at = @At("HEAD"), cancellable = true)
    private void onVertex(VertexConsumer builder, float x, float y, float z, float red, float green, float blue, float u, float v, int lightcoords, CallbackInfo ci) {
        int alpha = ALPHAS.get();

        if (AMBIENT.get()) {
            Color c = Modules.get().get(Ambience.class).lavaColor.get();
            vertex(builder, x, y, z, c.r, c.g, c.b, (alpha != -1 ? alpha : c.a), u, v, lightcoords);
            ci.cancel();
        }
        else if (alpha != -1) {
            int _red = (int) (red * 255);
            int _green = (int) (green * 255);
            int _blue = (int) (blue * 255);
            vertex(builder, x, y, z, _red, _green, _blue, alpha, u, v, lightcoords);
            ci.cancel();
        }
    }

    @Unique
    private void vertex(VertexConsumer vertexConsumer, float x, float y, float z, int red, int green, int blue, int alpha, float u, float v, int light) {
        vertexConsumer.vertex(x, y, z).color(red, green, blue, alpha).texture(u, v).light(light).normal(0.0f, 1.0f, 0.0f);
    }
}
