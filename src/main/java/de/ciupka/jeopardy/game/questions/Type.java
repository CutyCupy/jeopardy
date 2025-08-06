package de.ciupka.jeopardy.game.questions;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

// @JsonSerialize(using = Type.Serializer.class)
// @JsonDeserialize(using = Type.Deserializer.class)
public enum Type {
    NORMAL("Buzzerfrage", true),
    TEXT("Textfrage", false),
    ESTIMATE("Schätzfrage", false),
    SORT("Sortierfrage", false),
    VIDEO("Videofrage", false),
    HINT("'Wer bin ich?' Frage", true);

    private Type(String name, boolean hasPenalty) {
        this.title = name;
        this.hasPenalty = hasPenalty;
    }

    private String title;
    private boolean hasPenalty;

    public String getTitle() {
        return this.title;
    }

    public boolean getHasPenalty() {
        return this.hasPenalty;
    }

    public static class Serializer extends JsonSerializer<Type> {
        @Override
        public void serialize(Type value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("name", value.name());
            gen.writeStringField("title", value.getTitle());
            gen.writeBooleanField("penalty", value.getHasPenalty());
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<Type> {
        @Override
        public Type deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            String code = node.get("name").asText();
            return Type.valueOf(code.toUpperCase());
        }
    }

}
