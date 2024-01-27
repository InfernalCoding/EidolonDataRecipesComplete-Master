package dev.infernal_coding.eidolonrecipes.spells;

import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirement;
import dev.infernal_coding.eidolonrecipes.spells.requirement.ISpellRequirementSerializer;
import dev.infernal_coding.eidolonrecipes.spells.requirement.impl.AltarRequirement;
import dev.infernal_coding.eidolonrecipes.spells.requirement.impl.GobletRequirement;
import dev.infernal_coding.eidolonrecipes.spells.requirement.impl.ReputationRequirement;
import dev.infernal_coding.eidolonrecipes.spells.result.ISpellResult;
import dev.infernal_coding.eidolonrecipes.spells.result.ISpellResultSerializer;
import dev.infernal_coding.eidolonrecipes.spells.result.impl.EmptyGobletResult;
import dev.infernal_coding.eidolonrecipes.spells.result.impl.PrayResult;
import dev.infernal_coding.eidolonrecipes.spells.result.impl.ReputationResult;
import dev.infernal_coding.eidolonrecipes.spells.result.impl.UnlockResult;
import dev.infernal_coding.eidolonrecipes.spells.type.ISpell;
import dev.infernal_coding.eidolonrecipes.spells.type.ISpellSerializer;
import dev.infernal_coding.eidolonrecipes.spells.type.impl.BasicSpell;
import dev.infernal_coding.eidolonrecipes.spells.type.impl.TransmutationSpell;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SpellManager {

	public static final Map<ResourceLocation, ISpellSerializer> SPELLS = new HashMap<>();
	public static final Map<ResourceLocation, ISpellRequirementSerializer> REQUIREMENTS = new HashMap<>();
	public static final Map<ResourceLocation, ISpellResultSerializer> RESULTS = new HashMap<>();

    public static void init() {
        SPELLS.put(BasicSpell.ID, new BasicSpell.Serializer());
        SPELLS.put(TransmutationSpell.ID, new TransmutationSpell.Serializer());

        REQUIREMENTS.put(AltarRequirement.ID, new AltarRequirement.Serializer());
        REQUIREMENTS.put(ReputationRequirement.ID, new ReputationRequirement.Serializer());
        REQUIREMENTS.put(GobletRequirement.ID, new GobletRequirement.Serializer());

        RESULTS.put(EmptyGobletResult.ID, new EmptyGobletResult.Serializer());
        RESULTS.put(PrayResult.ID, new PrayResult.Serializer());
        RESULTS.put(ReputationResult.ID, new ReputationResult.Serializer());
        RESULTS.put(UnlockResult.ID, new UnlockResult.Serializer());
    }

    public static ISpellSerializer<ISpell> getSpellSerializer(ResourceLocation name) {
        return SPELLS.get(name);
    }

    public static ISpellRequirementSerializer<ISpellRequirement> getRequirementSerializer(ResourceLocation name) {
        return REQUIREMENTS.get(name);
    }

    public static ISpellResultSerializer<ISpellResult> getResultSerializer(ResourceLocation name) {
        return RESULTS.get(name);
    }
}
