package com.loucaskreger.automaticelytra.mixin;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    private static final int CHEST_INDEX = 6;
    private int lastIndex = -1;

    @Inject(method = "tickMovement", at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"))
    private void onPlayerTickMovement(CallbackInfo ci) {
        var player = (ClientPlayerEntity) (Object) this;
        var interactionManager = MinecraftClient.getInstance().interactionManager;
        // Injects when the elytra should be deployed
        if (!player.isOnGround() && !player.isFallFlying() && !player.isTouchingWater() && !player.hasStatusEffect(StatusEffects.LEVITATION)) {
            // [Future] Replace with an event that fires before elytra take off.
            this.equipElytra(player, interactionManager);
        }
    }


    @Inject(method = "tickMovement", at = @At(value = "TAIL"))
    private void endTickMovement(CallbackInfo ci) {
        var player = (ClientPlayerEntity) (Object) this;
        var interactionManager = MinecraftClient.getInstance().interactionManager;
        if (player.isOnGround() || player.isTouchingWater()) {
            player.stopFallFlying();
            if (this.lastIndex != -1) {
                interactionManager.clickSlot(player.playerScreenHandler.syncId, CHEST_INDEX, lastIndex, SlotActionType.SWAP, player);
                lastIndex = -1;
            }
        }
    }

    private void equipElytra(ClientPlayerEntity player, ClientPlayerInteractionManager interactionManager) {
        int firstElytraIndex = this.getElytraIndex(player);
        if (firstElytraIndex != -1) {
            this.lastIndex = firstElytraIndex;
            interactionManager.clickSlot(player.playerScreenHandler.syncId, CHEST_INDEX, firstElytraIndex, SlotActionType.SWAP, player);
            player.startFallFlying();
        }
    }

    /**
     *
     * @param player The player
     * @return the first index of an elytra in the specified player's inventory
     */
    private int getElytraIndex(ClientPlayerEntity player) {
        var inv = player.getInventory().main;
        for (int i = 0; i < inv.size(); i++) {
            if (inv.get(i).getItem() == Items.ELYTRA) {
                return i;
            }
        }
        return -1;
    }
}
