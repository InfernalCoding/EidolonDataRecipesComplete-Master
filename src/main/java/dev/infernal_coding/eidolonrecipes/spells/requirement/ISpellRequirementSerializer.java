package dev.infernal_coding.eidolonrecipes.spells.requirement;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.data.ISerializer;
import net.minecraft.resources.ResourceLocation;

public interface ISpellRequirementSerializer<T extends ISpellRequirement> extends ISerializer<JsonObject, T> {

    @Override
    default JsonObject serialize(T requirement) {
        JsonObject json = new JsonObject();
        json.addProperty("type", this.getId().toString());
        this.serialize(json, requirement);
        return json;
    }

    void serialize(JsonObject json, T requirement);

    ResourceLocation getId();

}
