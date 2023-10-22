package com.github.yukkuritaku.legacyfontprovider.util;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class JsonUtil {

    /**
     * Gets the string value of the given JsonElement.  Expects the second parameter to be the name of the element's field if an error message needs to be thrown.
     */
    public static String getString(JsonElement json, String memberName) {
        if (json.isJsonPrimitive()) {
            return json.getAsString();
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be a string, was " + toString(json));
        }
    }

    /**
     * Gets the string value of the field on the JsonObject with the given name.
     */
    public static String getString(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return getString(json.get(memberName), memberName);
        } else {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a string");
        }
    }

    /**
     * Gets the integer value of the given JsonElement.  Expects the second parameter to be the name of the element's field if an error message needs to be thrown.
     */
    public static int getInt(JsonElement json, String memberName) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            return json.getAsInt();
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be a Int, was " + toString(json));
        }
    }

    /**
     * Gets the integer value of the field on the JsonObject with the given name.
     */
    public static int getInt(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return getInt(json.get(memberName), memberName);
        } else {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Int");
        }
    }

    /**
     * Gets the integer value of the field on the JsonObject with the given name, or the given default value if the field is missing.
     */
    public static int getInt(JsonObject json, String memberName, int fallback) {
        return json.has(memberName) ? getInt(json.get(memberName), memberName) : fallback;
    }

    /**
     * Gets the given JsonElement as a JsonObject.  Expects the second parameter to be the name of the element's field if an error message needs to be thrown.
     */
    public static JsonObject getJsonObject(JsonElement json, String memberName) {
        if (json.isJsonObject()) {
            return json.getAsJsonObject();
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be a JsonObject, was " + toString(json));
        }
    }

    /**
     * Gets the given JsonElement as a JsonArray.  Expects the second parameter to be the name of the element's field if an error message needs to be thrown.
     */
    public static JsonArray getJsonArray(JsonElement json, String memberName) {
        if (json.isJsonArray()) {
            return json.getAsJsonArray();
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be a JsonArray, was " + toString(json));
        }
    }

    /**
     * Gets the JsonArray field on the JsonObject with the given name.
     */
    public static JsonArray getJsonArray(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return getJsonArray(json.get(memberName), memberName);
        } else {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a JsonArray");
        }
    }

    /**
     * Gets a human-readable description of the given JsonElement's type.  For example: "a number (4)"
     */
    public static String toString(JsonElement json) {
        String string = org.apache.commons.lang3.StringUtils.abbreviateMiddle(String.valueOf(json), "...", 10);
        if (json == null) {
            return "null (missing)";
        } else if (json.isJsonNull()) {
            return "null (json)";
        } else if (json.isJsonArray()) {
            return "an array (" + string + ")";
        } else if (json.isJsonObject()) {
            return "an object (" + string + ")";
        } else {
            if (json.isJsonPrimitive()) {
                JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
                if (jsonPrimitive.isNumber()) {
                    return "a number (" + string + ")";
                }

                if (jsonPrimitive.isBoolean()) {
                    return "a boolean (" + string + ")";
                }
            }

            return string;
        }
    }

    @Nullable
    public static <T> T gsonDeserialize(Gson gsonIn, Reader readerIn, Class<T> adapter, boolean lenient) {
        try {
            JsonReader jsonReader = new JsonReader(readerIn);
            jsonReader.setLenient(lenient);
            return gsonIn.getAdapter(adapter).read(jsonReader);
        } catch (IOException var5) {
            throw new JsonParseException(var5);
        }
    }

    @Nullable
    public static <T> T gsonDeserialize(Gson gsonIn, String json, Class<T> adapter, boolean lenient) {
        return gsonDeserialize(gsonIn, new StringReader(json), adapter, lenient);
    }

    @Nullable
    public static <T> T gsonDeserialize(Gson gsonIn, String json, Class<T> adapter) {
        return gsonDeserialize(gsonIn, json, adapter, false);
    }
}
