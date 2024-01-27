package dev.infernal_coding.eidolonrecipes.rituals;

import com.google.gson.JsonObject;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.core.BlockPos;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public interface IRitualResultSerializer {

    RitualManager.ResultColorPair getColorAndResult(JsonObject json, int color, boolean isColorPreset, String type);

    RitualManager.ResultColorPair getColorAndResult(FriendlyByteBuf buffer, int color, boolean isColorPreset, String type);

    void writeResult(RitualRecipeWrapper.Result result, FriendlyByteBuf buffer);

    void startRitual(Ritual ritual, RitualRecipeWrapper.Result result, Level world, BlockPos pos);

    boolean onRitualTick(RitualRecipeWrapper.Result result, Level world, BlockPos pos);

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
                return new RitualManager.ResultColorPair(color, count, Animal.class, resultType);
            }

            case "creature":
                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(23, 11, 225, 12);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(21, 121, 11, 20);
                }
                return new RitualManager.ResultColorPair(color, count, PathfinderMob.class, resultType);

            case "monster": {

                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(83, 24, 241, 12);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(100, 35, 80, 175);
                }
                return new RitualManager.ResultColorPair(color, count, PathfinderMob.class, resultType);
            }

            case "zombie": {
                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(100, 30, 25, 40);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(100, 35, 80, 175);
                }
                return new RitualManager.ResultColorPair(color, count, Zombie.class, resultType);
            }

            case "villager": {
                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(100, 30, 25, 40);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(100, 35, 80, 175);
                }
                return new RitualManager.ResultColorPair(color, count, AbstractVillager.class, resultType);
            }

            case "skeleton": {
                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(100, 30, 25, 40);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(100, 35, 80, 175);
                }
                return new RitualManager.ResultColorPair(color, count, AbstractSkeleton.class, resultType);

            }

            case "raider": {
                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(100, 30, 25, 40);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(100, 35, 80, 175);
                }
                return new RitualManager.ResultColorPair(color, count, Raider.class, resultType);
            }

            case "illager": {
                if (color > 0 && !isColorPreset) {
                    color -= ColorUtil.packColor(100, 30, 25, 40);
                } else if (!isColorPreset) {
                    color += ColorUtil.packColor(100, 35, 80, 175);
                }
                return new RitualManager.ResultColorPair(color, count, AbstractIllager.class, resultType);
            }
        }
        return new RitualManager.ResultColorPair(color, count, LivingEntity.class, resultType);
    }

    default void writeClassName(String className, FriendlyByteBuf buffer) {
        switch (className) {
            case "Entity":
                buffer.writeUtf("entity");
                break;
            case "AnimalEntity":
                buffer.writeUtf("animal");
                break;
            case "CreatureEntity":
                buffer.writeUtf("creature");
                break;
            case "MonsterEntity":
                buffer.writeUtf("monster");
                break;
            case "ZombieEntity":
                buffer.writeUtf("zombie");
                break;
            case "AbstractVillagerEntity":
                buffer.writeUtf("villager");
                break;
            case "AbstractSkeletonEntity":
                buffer.writeUtf("skeleton");
                break;
            case "AbstractRaiderEntity":
                buffer.writeUtf("raider");
                break;
            case "AbstractIllagerEntity":
                buffer.writeUtf("illager");
                break;
            default:
                buffer.writeUtf("living");
                break;
        }
    }

    ItemStack getIcon(RitualRecipeWrapper.Result result);
}
