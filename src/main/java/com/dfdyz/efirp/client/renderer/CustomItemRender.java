package com.dfdyz.efirp.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class CustomItemRender extends RenderItemBase {
    private final Joint bindedJoint;
    private final OpenMatrix4f transform;
    private final AdditionalModelRenderer[] additional;

    public CustomItemRender(Joint bindedJoint, OpenMatrix4f transform, AdditionalModelRenderer[] additional) {
        this.transform = transform;
        this.additional = additional;
        this.bindedJoint = bindedJoint;
    } 

    @Override
    public void renderItemInHand(ItemStack stack, LivingEntityPatch<?> entitypatch, InteractionHand hand, HumanoidArmature armature, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight) {
        //render self
        OpenMatrix4f modelMatrix = this.getCorrectionMatrix(stack, entitypatch, hand);
        modelMatrix.mulFront(transform);
        boolean isInMainhand = hand == InteractionHand.MAIN_HAND;
        OpenMatrix4f jointTransform = poses[bindedJoint.getId()];
        modelMatrix.mulFront(jointTransform);
        poseStack.pushPose();
        this.mulPoseStack(poseStack, modelMatrix);
        ItemTransforms.TransformType transformType = isInMainhand ? ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND : ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
        Minecraft.getInstance().getItemInHandRenderer().renderItem(entitypatch.getOriginal(), stack, transformType, false, poseStack, buffer, packedLight);
        poseStack.popPose();

        //render additional
        for (int i = 0; i < additional.length; i++) {
            additional[i].renderItemInHand(entitypatch, armature, poses, buffer, poseStack, packedLight);
        }
    }
}
