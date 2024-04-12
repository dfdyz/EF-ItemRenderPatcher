package com.dfdyz.efirp.client.Loader;

import com.dfdyz.efirp.EFItemRenderPatcher;
import com.dfdyz.efirp.client.renderer.AdditionalModelRenderer;
import com.dfdyz.efirp.client.renderer.CustomItemRender;
import com.dfdyz.efirp.utils.ReflectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.gameasset.Armatures;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class PatchedItemSkinLoader extends SimpleJsonResourceReloadListener {

    public PatchedItemSkinLoader() {
        super((new GsonBuilder()).create(), "item_skin_patch");
    }
    private final Map<ResourceLocation, CustomItemRender> ItemCollection = Maps.newHashMap();

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elems, ResourceManager rsMgr, ProfilerFiller filler) {
        ItemCollection.clear();
        elems.forEach((k,v) -> {
            if (!ForgeRegistries.ITEMS.containsKey(k)) {
                EFItemRenderPatcher.LOGGER.warn("[Item Skin Patcher] Item named " + k + " does not exist");
            } else {
                CustomItemRender render = load(v.getAsJsonObject());
                if(render != null) ItemCollection.put(k, render);
                else EFItemRenderPatcher.LOGGER.warn("[Item Skin Patcher] Item named " + k + " format error.");
            }
        });

        Map<Item, RenderItemBase> itemRendererMapByInstance = ReflectionUtils.GetField(ClientEngine.getInstance().renderEngine, "itemRendererMapByInstance");
        ItemCollection.forEach((k, v) -> {
            Item item = ForgeRegistries.ITEMS.getValue(k);
            itemRendererMapByInstance.put(item, v);
        });
    }

    public static CustomItemRender load(JsonObject json){
        Vec3f rotation;
        Vec3f translation;
        Vec3f scale;

        if(!json.has("joint")) return null;
        String jointName = json.get("joint").getAsString();
        Joint joint = Armatures.BIPED.searchJointByName(jointName);
        if(joint == null) return null;


        OpenMatrix4f mat;
        if(json.has("transform_chain")){
            mat = bakeTransformChain(json.getAsJsonArray("transform_chain"));
        }else {
            mat = new OpenMatrix4f();
            if(json.has("rotation")) rotation = dumpVec3(json.getAsJsonArray("rotation"));
            else rotation = new Vec3f();

            if(json.has("translation")) translation = dumpVec3(json.getAsJsonArray("translation"));
            else translation = new Vec3f();

            if(json.has("scale")) scale = dumpVec3(json.getAsJsonArray("scale"));
            else scale = new Vec3f();

            mat.scale(scale);

            mat.rotateDeg(rotation.z, Vec3f.Z_AXIS);
            mat.rotateDeg(rotation.x, Vec3f.X_AXIS);
            mat.rotateDeg(rotation.y, Vec3f.Y_AXIS);

            mat.translate(translation);
        }
        if(mat == null) return null;

        ArrayList<AdditionalModelRenderer> additionalModelRenderers = Lists.newArrayList();
        if(json.has("attachment")){
            AtomicBoolean sucess = new AtomicBoolean(true);
            JsonArray attachments = json.getAsJsonArray("attachment");
            attachments.forEach((e) -> {
                AdditionalModelRenderer add = loadAdditional(e.getAsJsonObject());
                if(add != null){
                    additionalModelRenderers.add(add);
                }else {
                    sucess.set(false);
                }
            });
            if (!sucess.get()) return null;
        }

        return new CustomItemRender(joint, mat, additionalModelRenderers.toArray(new AdditionalModelRenderer[0]));
    }

    private static AdditionalModelRenderer loadAdditional(JsonObject json){
        Vec3f rotation;
        Vec3f translation;
        Vec3f scale;

        if(!json.has("joint")) return null;
        String jointName = json.get("joint").getAsString();
        Joint joint = Armatures.BIPED.searchJointByName(jointName);
        if(joint == null) return null;

        OpenMatrix4f mat = null;
        if(json.has("transform_chain")){
            mat = bakeTransformChain(json.getAsJsonArray("transform_chain"));
        } else {
            mat = new OpenMatrix4f();
            if(json.has("rotation")) rotation = dumpVec3(json.getAsJsonArray("rotation"));
            else rotation = new Vec3f();

            if(json.has("translation")) translation = dumpVec3(json.getAsJsonArray("translation"));
            else translation = new Vec3f();

            if(json.has("scale")) scale = dumpVec3(json.getAsJsonArray("scale"));
            else scale = new Vec3f();

            mat.scale(scale);

            mat.rotateDeg(rotation.z, Vec3f.Z_AXIS);
            mat.rotateDeg(rotation.x, Vec3f.X_AXIS);
            mat.rotateDeg(rotation.y, Vec3f.Y_AXIS);

            mat.translate(translation);
        }
        if(mat == null) return null;


        Item item = null;
        if(json.has("item")){
            ResourceLocation itemId = new ResourceLocation(json.get("item").getAsString());
            if(!ForgeRegistries.ITEMS.containsKey(itemId)) return null;
            item = ForgeRegistries.ITEMS.getValue(itemId);
        }
        if(item == null) return null;

        boolean rightHand = true;
        if(json.has("main_hand")) rightHand = json.get("main_hand").getAsBoolean();

        return new AdditionalModelRenderer(item, mat, joint, rightHand);
    }

    private static Vec3f dumpVec3(JsonArray jsonArray){
        try {
            return new Vec3f(jsonArray.get(0).getAsFloat(),
                    jsonArray.get(1).getAsFloat(),
                    jsonArray.get(2).getAsFloat()
            );
        }catch (Exception e){
            return new Vec3f();
        }
    }

    private static OpenMatrix4f bakeTransformChain(JsonArray chain){
        OpenMatrix4f tf = new OpenMatrix4f();

        AtomicBoolean sucess = new AtomicBoolean(true);
        chain.forEach((e)->{
            try{
                JsonObject o = e.getAsJsonObject();
                if(o.has("translate")) {
                    tf.translate(dumpVec3(o.getAsJsonArray("translate")));
                }
                else if(o.has("rotation")) {
                    JsonArray aa = o.getAsJsonArray("rotation");
                    tf.rotateDeg(aa.get(0).getAsFloat(), new Vec3f(
                            aa.get(1).getAsFloat(),
                            aa.get(2).getAsFloat(),
                            aa.get(3).getAsFloat()
                    ));
                }
                else if(o.has("scale")) {
                    tf.scale(dumpVec3(o.getAsJsonArray("translate")));
                }
                else sucess.set(false);
            } catch (Exception ex) {
                sucess.set(false);
            }
        });

        if(!sucess.get()) return null;
        return tf;
    }

    @Override
    public String getName() {
        return "item_skin_patcher";
    }
}
