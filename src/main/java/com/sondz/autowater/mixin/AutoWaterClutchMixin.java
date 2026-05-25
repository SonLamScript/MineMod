package com.sondz.autowater.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
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

        // Chỉ xử lý nếu người chơi đang rơi tự do và không ở trong chế độ sáng tạo/khán giả
        if (player != null && player.fallDistance > 1.5F && !player.isAbilitiesFlying() && !player.isSpectator()) {
            
            // Kiểm tra xem người chơi có đang cầm xô nước ở tay chính hoặc tay phụ không
            Hand waterHand = null;
            if (player.getMainHandStack().isOf(Items.WATER_BUCKET)) {
                waterHand = Hand.MAIN_HAND;
            } else if (player.getOffHandStack().isOf(Items.WATER_BUCKET)) {
                waterHand = Hand.OFF_HAND;
            }

            // Nếu tìm thấy xô nước trên tay
            if (waterHand != null) {
                // Raycast thẳng xuống dưới theo hướng nhìn để tìm block bề mặt sắp va chạm
                BlockHitResult hitResult = player.getWorld().raycast(new RaycastContext(
                        player.getEyePos(),
                        player.getPos().add(0, -3.0, 0), // Quét khoảng cách 3 block dưới chân
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        player
                ));

                // Nếu phát hiện có block rắn bên dưới và không phải là nước có sẵn
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockPos targetPos = hitResult.getBlockPos();
                    
                    // Đảm bảo không cố đặt nước vào block không khí hoặc block chất lỏng
                    if (!player.getWorld().getBlockState(targetPos).isAir()) {
                        // Giả lập hành động nhấn chuột phải để đặt nước
                        if (client.interactionManager != null) {
                            client.interactionManager.interactBlock(player, waterHand, hitResult);
                        }
                    }
                }
            }
        }
    }
}