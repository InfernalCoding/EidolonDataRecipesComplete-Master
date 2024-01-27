package dev.infernal_coding.eidolonrecipes.util;

import com.google.gson.JsonObject;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class JsonUtil {

    public static Optional<String> getOptionalString(JsonObject json, String name) {
        return Optional.ofNullable(json.has(name) ? JSONUtils.getString(json, name) : null);
    }

    public static Optional<ResourceLocation> getOptionalResourceLocation(JsonObject json, String name) {
        return Optional.ofNullable(json.has(name) ? new ResourceLocation(JSONUtils.getString(json, name)) : null);
    }

    public static Optional<Integer> getOptionalInt(JsonObject json, String name) {
        return Optional.ofNullable(json.has(name) ? JSONUtils.getInt(json, name) : null);
    }

    public static Optional<Float> getOptionalFloat(JsonObject json, String name) {
        return Optional.ofNullable(json.has(name) ? JSONUtils.getFloat(json, name) : null);
    }
}
