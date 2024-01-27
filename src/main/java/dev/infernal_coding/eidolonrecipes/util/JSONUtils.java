package dev.infernal_coding.eidolonrecipes.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import net.minecraft.resources.ResourceLocation;


import java.util.Optional;

public class JSONUtils {

    public static Optional<String> getOptionalString(JsonObject json, String name) {
        return Optional.ofNullable(json.has(name) ? JsonUtils.getStringOr(name, json, "") : null);
    }

    public static Optional<ResourceLocation> getOptionalResourceLocation(JsonObject json, String name) {
        return Optional.ofNullable(json.has(name) ? new ResourceLocation(JsonUtils.getStringOr(name, json, "")) : null);
    }

    public static Optional<Integer> getOptionalInt(JsonObject json, String name) {
        return Optional.ofNullable(json.has(name) ? JsonUtils.getIntOr(name, json, 0) : null);
    }

    public static Optional<Float> getOptionalFloat(JsonObject json, String name) {
        return Optional.ofNullable(json.has(name) ? getFloat(json, name, 0) : null);
    }

    public static String getString(JsonObject json, String name, String fallback) {
        JsonElement jsonelement = json.get(name);
        if (jsonelement != null) {
            return jsonelement.isJsonNull() ? fallback : jsonelement.getAsString();
        } else {
            return fallback;
        }
    }

    public static float getFloat(JsonObject json, String name, float fallback) {
        JsonElement jsonelement = json.get(name);
        if (jsonelement != null) {
            return jsonelement.isJsonNull() ? fallback : jsonelement.getAsFloat();
        } else {
            return fallback;
        }
    }

    public static int getInt(JsonObject json, String name, int fallback) {
        JsonElement jsonelement = json.get(name);
        if (jsonelement != null) {
            return jsonelement.isJsonNull() ? fallback : jsonelement.getAsInt();
        } else {
            return fallback;
        }
    }

    public static Boolean getBoolean(JsonObject json, String name, boolean fallback) {
        JsonElement jsonelement = json.get(name);
        if (jsonelement != null) {
            return jsonelement.isJsonNull() ? fallback : jsonelement.getAsBoolean();
        } else {
            return fallback;
        }
    }

    public static JsonArray getJSONArray(JsonObject json, String name) {
        JsonArray array = json.getAsJsonArray(name);
        if (array != null) {
            return array.isJsonNull() ? new JsonArray() : array.getAsJsonArray();
        } else {
            return new JsonArray();
        }
    }
    public static JsonObject getJsonObject(JsonObject json, String name) {
        JsonElement element = json.get(name);
        if (element instanceof JsonObject object) {
            return object;
        } else {
            return new JsonObject();
        }
    }
}
