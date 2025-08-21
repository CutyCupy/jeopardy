package de.ciupka.jeopardy.game.questions.answer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = SortOptions.Serializer.class)
@JsonDeserialize(using = SortOptions.Deserializer.class)
public class SortOptions implements Stringable {

    private SortOption[] options;

    public SortOptions(SortOption... options) {
        this.options = options;
        Arrays.sort(this.options);
    }

    @Override
    public String toString() {
        return Arrays.stream(options).map(SortOption::toString).collect(Collectors.joining(", "));
    }

    @Override
    public String asShortString() {
        return Arrays.stream(options).map(SortOption::getName).collect(Collectors.joining(", "));
    }

    public SortOption[] getOptions() {
        return options;
    }

    public SortOptions asSortedList(boolean descending) {
        if(descending) {
            Arrays.sort(options, Collections.reverseOrder());
            return this;
        }
        Arrays.sort(options);
        return this;
    }

    public static class Serializer extends JsonSerializer<SortOptions> {
        @Override
        public void serialize(SortOptions value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray();
            for (SortOption option : value.getOptions()) {
                gen.writeObject(option);
            }
            gen.writeEndArray();
        }
    }

    public static class Deserializer extends JsonDeserializer<SortOptions> {
        @Override
        public SortOptions deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            ObjectMapper mapper = new ObjectMapper();
            return new SortOptions(mapper.convertValue(node, SortOption[].class));
        }
    }
}
