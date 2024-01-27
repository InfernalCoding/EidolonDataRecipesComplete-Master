package dev.infernal_coding.eidolonrecipes.spells.result;

import com.google.gson.JsonObject;
import dev.infernal_coding.eidolonrecipes.data.ISerializer;
import net.minecraft.resources.ResourceLocation;

public interface ISpellResultSerializer<T extends ISpellResult> extends ISerializer<JsonObject, T> {

    @Override
    default JsonObject serialize(T result) {
        JsonObject json = new JsonObject();
        json.addProperty("type", this.getId().toString());
        this.serialize(json, result);
        return json;
    }

    void serialize(JsonObject json, T result);

    ResourceLocation getId();
}
