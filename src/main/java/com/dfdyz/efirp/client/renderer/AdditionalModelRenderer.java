package com.dfdyz.efirp.client.renderer;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class AdditionalModelRenderer {
    protected static final OpenMatrix4f mainhandcorrectionMatrix = new OpenMatrix4f().translate(0.0F, -0.13F, -0.5F);
    protected static final OpenMatrix4f offhandCorrectionMatrix = new OpenMatrix4f().translate(0.0F, -0.13F, -0.5F);
    public final ItemStack modelHolder;
    public final OpenMatrix4f transform;
    public final Joint bindedJoint;

    public final boolean rightHand;


    public AdditionalModelRenderer(Item item, OpenMatrix4f transform, Joint bindedJoint, boolean rightHand) {
        this.modelHolder = new ItemStack(item);
        OpenMatrix4f modelMatrix = new OpenMatrix4f(transform);
        this.transform = modelMatrix;
        this.bindedJoint = bindedJoint;
        this.rightHand = rightHand;
    }
    public void renderItemInHand(LivingEntityPatch<?> entitypatch,
                                 HumanoidArmature armature,
                                 OpenMatrix4f[] poses, MultiBufferSource buffer,
                                 PoseStack poseStack, int packedLight) {
        OpenMatrix4f jointTransform = poses[bindedJoint.getId()];

        OpenMatrix4f modelMatrix = new OpenMatrix4f(transform);
        modelMatrix.mulFront(jointTransform);

        poseStack.pushPose();
        this.mulPoseStack(poseStack, modelMatrix);
        ItemTransforms.TransformType transformType = rightHand ? ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND : ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
        Minecraft.getInstance().getItemInHandRenderer().renderItem(entitypatch.getOriginal(), modelHolder, transformType, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
    protected void mulPoseStack(PoseStack poseStack, OpenMatrix4f pose) {
        OpenMatrix4f transposed = pose.transpose(null);
        MathUtils.translateStack(poseStack, pose);
        MathUtils.rotateStack(poseStack, transposed);
        MathUtils.scaleStack(poseStack, transposed);
    }
}
