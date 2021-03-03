package io.quarkus.mongodb.panache.jsonb;

import java.lang.reflect.Type;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;

import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

public class ObjectIdDeserializer implements JsonbDeserializer<ObjectId> {

    private static final Logger LOGGER = Logger.getLogger(ObjectIdDeserializer.class);

    @Override
    public ObjectId deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        String id = parser.getString();
        if (id != null) {
            return new ObjectId(id);
        }
        return null;
    }
}
