package dev.infernal_coding.eidolonrecipes.rituals;

import com.google.gson.JsonObject;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IRitualResultSerializer {

    RitualManager.ResultColorPair getColorAndResult(JsonObject json, int color, boolean isColorPreset, String type);

    RitualManager.ResultColorPair getColorAndResult(PacketBuffer buffer, int color, boolean isColorPreset, String type);

    void writeResult(RitualRecipeWrapper.Result result, PacketBuffer buffer);

    void startRitual(RitualRecipeWrapper.Result result, World world, BlockPos pos);

    boolean onRitualTick(RitualRecipeWrapper.Result result, World world, BlockPos pos);

    boolean getRunsOnTick();


    static RitualManager.ResultColorPair getClassTypeAndColor(String resultType, String className,
                                                              int color, int count, boolean isColorPreset) {

        if (className == null) return new RitualManager.ResultColorPair(color, count, LivingEntity.class, resultType);

        if (className.equals("entity")) {
            if (resultType.equals("absorb") || resultType.equals("allure") || resultType.equals("repel")) {
                return new RitualManager.ResultColorPair(color, count, LivingEntity.class, resultType);
            }
        }


        switch (className) {
            case "animal": {

                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(40, 10, 20, 18);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(95, 3, 12, 34);
                }
                return new RitualManager.ResultColorPair(color, count, AnimalEntity.class, resultType);
            }

            case "creature":
                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(23, 11, 225, 12);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(21, 121, 11, 20);
                }
                return new RitualManager.ResultColorPair(color, count, CreatureEntity.class, resultType);

            case "monster": {

                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(83, 24, 241, 12);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(100, 35, 80, 175);
                }
                return new RitualManager.ResultColorPair(color, count, MonsterEntity.class, resultType);
            }

            case "zombie": {
                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(100, 30, 25, 40);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(100, 35, 80, 175);
                }
                return new RitualManager.ResultColorPair(color, count, ZombieEntity.class, resultType);
            }

            case "villager": {
                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(100, 30, 25, 40);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(100, 35, 80, 175);
                }
                return new RitualManager.ResultColorPair(color, count, AbstractVillagerEntity.class, resultType);
            }

            case "skeleton": {
                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(100, 30, 25, 40);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(100, 35, 80, 175);
                }
                return new RitualManager.ResultColorPair(color, count, AbstractSkeletonEntity.class, resultType);

            }

            case "raider": {
                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(100, 30, 25, 40);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(100, 35, 80, 175);
                }
                return new RitualManager.ResultColorPair(color, count, AbstractRaiderEntity.class, resultType);
            }

            case "illager": {
                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(100, 30, 25, 40);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(100, 35, 80, 175);
                }
                return new RitualManager.ResultColorPair(color, count, AbstractIllagerEntity.class, resultType);
            }
        }
        return new RitualManager.ResultColorPair(color, count, LivingEntity.class, resultType);
    }

    default void writeClassName(String className, PacketBuffer buffer) {
        switch (className) {
            case "Entity":
                buffer.writeString("entity");
                break;
            case "AnimalEntity":
                buffer.writeString("animal");
                break;
            case "CreatureEntity":
                buffer.writeString("creature");
                break;
            case "MonsterEntity":
                buffer.writeString("monster");
                break;
            case "ZombieEntity":
                buffer.writeString("zombie");
                break;
            case "AbstractVillagerEntity":
                buffer.writeString("villager");
                break;
            case "AbstractSkeletonEntity":
                buffer.writeString("skeleton");
                break;
            case "AbstractRaiderEntity":
                buffer.writeString("raider");
                break;
            case "AbstractIllagerEntity":
                buffer.writeString("illager");
                break;
            default:
                buffer.writeString("living");
                break;
        }
    }

    ItemStack getIcon(RitualRecipeWrapper.Result result);
}
