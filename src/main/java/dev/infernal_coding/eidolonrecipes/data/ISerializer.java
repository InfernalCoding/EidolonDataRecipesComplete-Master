package dev.infernal_coding.eidolonrecipes.data;

import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;

public interface ISerializer<D extends JsonElement, T> {

	D serialize(T obj);

	T deserialize(D json);

	void write(FriendlyByteBuf buf, T obj);

	T read(FriendlyByteBuf buf);

}
