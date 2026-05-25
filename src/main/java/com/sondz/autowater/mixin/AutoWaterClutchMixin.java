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
import net.minecraft.world.World;
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

        // Kiểm tra thực thể hợp lệ và trạng thái đang rơi tự do
        if (player != null && player.fallDistance > 1.5F && !player.getAbilities().flying && !player.isSpectator()) {
            
            // Xác định tay nào đang giữ xô nước
            Hand waterHand = null;
            if (player.getMainHandStack().isOf(Items.WATER_BUCKET)) {
                waterHand = Hand.MAIN_HAND;
            } else if (player.getOffHandStack().isOf(Items.WATER_BUCKET)) {
                waterHand = Hand.OFF_HAND;
            }

            // Lấy World an toàn trực tiếp từ client world (Tránh dùng player.getWorld() lỗi mapping)
            World world = client.world;

            if (waterHand != null && world != null) {
                // Thay thế getPos() bằng cách tự tạo Vec3d từ getX(), getY(), getZ()
                double px = player.getX();
                double py = player.getY();
                double pz = player.getZ();
                
                Vec3d playerPos = new Vec3d(px, py, pz);
                Vec3d eyePos = player.getEyePos();
                
                // Tạo vector tia quét thẳng xuống dưới chân 3.5 block
                Vec3d targetVector = new Vec3d(px, py - 3.5, pz);

                BlockHitResult hitResult = world.raycast(new RaycastContext(
                        eyePos,
                        targetVector,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        player
                ));

                // Nếu tia va chạm trúng một bề mặt block cứng
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockPos targetPos = hitResult.getBlockPos();
                    
                    // Nếu block mục tiêu không phải là không khí, thực hiện đặt nước luôn
                    if (!world.getBlockState(targetPos).isAir()) {
                        if (client.interactionManager != null) {
                            client.interactionManager.interactBlock(player, waterHand, hitResult);
                        }
                    }
                }
            }
        }
    }
}