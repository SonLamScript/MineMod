package com.sondz.autowater.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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

        // 1. Kiểm tra điều kiện rơi an toàn (Chỉ kích hoạt khi vận tốc đi xuống lớn hơn 0.5)
        if (player != null && player.getVelocity().y < -0.5 && !player.getAbilities().flying && !player.isSpectator()) {
            
            // 2. Kiểm tra xô nước trên tay
            Hand waterHand = null;
            if (player.getMainHandStack().isOf(Items.WATER_BUCKET)) {
                waterHand = Hand.MAIN_HAND;
            } else if (player.getOffHandStack().isOf(Items.WATER_BUCKET)) {
                waterHand = Hand.OFF_HAND;
            }

            World world = client.world;

            if (waterHand != null && world != null) {
                // Tọa độ hiện tại của người chơi
                double px = player.getX();
                double py = player.getY();
                double pz = player.getZ();

                // 3. Tính toán khoảng cách quét động dựa trên vận tốc rơi (Rơi càng nhanh quét càng xa, tối đa 5 block)
                double scanDistance = Math.max(3.5, Math.abs(player.getVelocity().y) * 3.0);
                
                // Vòng lặp kiểm tra các block bên dưới chân từ gần đến xa
                for (double currentCheck = 0.5; currentCheck <= scanDistance; currentCheck += 0.5) {
                    BlockPos checkPos = BlockPos.ofFloored(px, py - currentCheck, pz);
                    
                    // Nếu phát hiện block bên dưới chân là block rắn (đất, đá...) và block ngay phía trên nó là không khí
                    if (!world.getBlockState(checkPos).isAir() && world.getBlockState(checkPos.up()).isAir()) {
                        
                        if (client.interactionManager != null) {
                            // Tạo một kết quả va chạm giả lập: Nhắm vào MẶT TRÊN (Direction.UP) của block rắn dưới chân
                            BlockHitResult customHit = new BlockHitResult(
                                    new Vec3d(px, py - currentCheck + 1.0, pz), 
                                    Direction.UP, 
                                    checkPos, 
                                    false
                            );
                            
                            // Thực hiện hành động chuột phải để đặt nước
                            client.interactionManager.interactBlock(player, waterHand, customHit);
                            break; // Đặt thành công thì dừng vòng lặp quét của tick này
                        }
                    }
                }
            }
        }
    }
}