package com.loucaskreger.automaticelytra.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    private static final int CHESTPLATE_INDEX = 6;
    private int lastIndex = -1;

    @Inject(method = "aiStep", at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;getItemBySlot(Lnet/minecraft/inventory/EquipmentSlotType;)Lnet/minecraft/item/ItemStack;"))
    private void onPlayerTickMovement(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        PlayerController pc  = Minecraft.getInstance().gameMode;
        // Injects when the elytra should be deployed
        if (!player.isOnGround() && !player.isFallFlying() && !player.hasEffect(Effects.LEVITATION)) {
            // [Future] Replace with an event that fires before elytra take off.
            this.equipElytra(player, pc);
        }
    }

    @Inject(method = "aiStep", at = @At(value = "TAIL"))
    private void endTickMovement(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        PlayerController pc = Minecraft.getInstance().gameMode;
        if (player.isOnGround() || player.isInWater()) {
            player.tryToStartFallFlying();
            if (this.lastIndex != -1) {
                pc.handleInventoryMouseClick(player.containerMenu.containerId, CHESTPLATE_INDEX, lastIndex, ClickType.SWAP, player);
                lastIndex = -1;
            }
        }
    }

    private void equipElytra(ClientPlayerEntity player, PlayerController pc) {
        int firstElytraIndex = this.getElytraIndex(player);
        if (firstElytraIndex != -1) {
            this.lastIndex = firstElytraIndex;
            pc.handleInventoryMouseClick(player.containerMenu.containerId, CHESTPLATE_INDEX, firstElytraIndex, ClickType.SWAP, player);
            // Send packet so server knows player is falling
            player.connection.send(new CEntityActionPacket(player, CEntityActionPacket.Action.START_FALL_FLYING));
        }
    }

    /**
     * @param player The player
     * @return the first index of an elytra in the specified player's inventory
     */
    private int getElytraIndex(ClientPlayerEntity player) {
        NonNullList<ItemStack> inv = player.inventory.items;
        for (int i = 0; i < inv.size(); i++) {
            if (inv.get(i).getItem() == Items.ELYTRA) {
                return i;
            }
        }
        return -1;
    }

}
