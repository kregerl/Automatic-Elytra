package com.loucaskreger.automaticelytra.mixin;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.event.ItemListener;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    private static final int CHESTPLATE_INDEX = 6;
    private int lastIndex = -1;

    @Inject(method = "tickMovement", at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"))
    private void onPlayerTickMovement(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;
        // Injects when the elytra should be deployed
        if (!player.isOnGround() && !player.isFallFlying() && !player.hasStatusEffect(StatusEffects.LEVITATION)) {
            // [Future] Replace with an event that fires before elytra take off.
            this.equipElytra(player, interactionManager);
        }
    }


    @Inject(method = "tickMovement", at = @At(value = "TAIL"))
    private void endTickMovement(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;
        if (player.isOnGround() || player.isTouchingWater()) {
            player.checkFallFlying();
            if (this.lastIndex != -1) {
                interactionManager.clickSlot(player.playerScreenHandler.syncId, CHESTPLATE_INDEX, lastIndex, SlotActionType.SWAP, player);
                lastIndex = -1;
            }
        }
    }

    private void equipElytra(ClientPlayerEntity player, ClientPlayerInteractionManager interactionManager) {
        int firstElytraIndex = this.getElytraIndex(player);
        if (firstElytraIndex != -1) {
            this.lastIndex = firstElytraIndex;
            interactionManager.clickSlot(player.playerScreenHandler.syncId, CHESTPLATE_INDEX, firstElytraIndex, SlotActionType.SWAP, player);
            // Send packet so server knows player is falling
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    /**
     * @param player The player
     * @return the first index of an elytra in the specified player's inventory
     */
    private int getElytraIndex(ClientPlayerEntity player) {
        DefaultedList<ItemStack> inv = player.inventory.main;
        for (int i = 0; i < inv.size(); i++) {
            if (inv.get(i).getItem() == Items.ELYTRA) {
                return i;
            }
        }
        return -1;
    }
}
