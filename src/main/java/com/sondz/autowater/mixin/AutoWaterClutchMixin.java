package com.sondz.autowater.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class AutoWaterClutchMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPlayerTick(CallbackInfo info) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        // 1. Kiểm tra trạng thái rơi (Trong Yarn: fallDistance, getAbilities().flying, isSpectator())
        if (player != null && player.fallDistance > 1.5F && !player.getAbilities().flying && !player.isSpectator()) {
            
            // 2. Xác định tay nào đang cầm xô nước
            Hand waterHand = null;
            if (player.getMainHandStack().isOf(Items.WATER_BUCKET)) {
                waterHand = Hand.MAIN_HAND;
            } else if (player.getOffHandStack().isOf(Items.WATER_BUCKET)) {
                waterHand = Hand.OFF_HAND;
            }

            // 3. Xử lý logic đặt nước nếu tìm thấy xô nước
            if (waterHand != null && player.getWorld() != null) {
                Vec3d playerPos = player.getPos(); // Lấy vị trí dạng Vec3d
                Vec3d eyePos = player.getEyePos();
                
                // Quét tia (Raycast) thẳng xuống dưới chân khoảng 3.5 block
                Vec3d targetVector = new Vec3d(playerPos.x, playerPos.y - 3.5, playerPos.z);

                BlockHitResult hitResult = player.getWorld().raycast(new RaycastContext(
                        eyePos,
                        targetVector,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        player
                ));

                // 4. Nếu tia va chạm trúng một Block
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockPos targetPos = hitResult.getBlockPos();
                    
                    // Kiểm tra block đó không phải là không khí hoặc nước sẵn có để tránh lãng phí hành động
                    if (!player.getWorld().getBlockState(targetPos).isAir()) {
                        if (client.interactionManager != null) {
                            // Giả lập tương tác (chuột phải) vào block dưới chân
                            client.interactionManager.interactBlock(player, waterHand, hitResult);
                        }
                    }
                }
            }
        }
    }
}