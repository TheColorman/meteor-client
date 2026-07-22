/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.entity.Entity;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientWorld.class)
public abstract class ClientLevelMixin {
    @Shadow
    @Nullable
    public abstract Entity getEntityById(int id);

    @Inject(method = "addEntity", at = @At("TAIL"))
    private void onAddEntity(Entity entity, CallbackInfo ci) {
        if (entity != null) MeteorClient.EVENT_BUS.post(EntityAddedEvent.get(entity));
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void onRemoveEntity(int id, Entity.RemovalReason reason, CallbackInfo ci) {
        if (getEntityById(id) != null)
            MeteorClient.EVENT_BUS.post(EntityRemovedEvent.get(getEntityById(id)));
    }

    @Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
    private void onAddDestroyBlockEffect(BlockPos pos, BlockState blockState, CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noParticle(ParticleTypes.BLOCK)) ci.cancel();
    }

    @Inject(method = "spawnBlockBreakingParticle", at = @At("HEAD"), cancellable = true)
    private void onAddBlockBreakingParticles(BlockPos pos, Direction direction, CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noParticle(ParticleTypes.BLOCK)) ci.cancel();
    }

    @ModifyArgs(method = "doRandomBlockDisplayTicks", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;randomBlockDisplayTick(IIIILnet/minecraft/util/math/random/Random;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos$Mutable;)V"))
    private void doRandomBlockDisplayTicks(Args args) {
        if (Modules.get().get(NoRender.class).noBarrierInvis()) {
            args.set(5, Blocks.BARRIER);
        }
    }


}
