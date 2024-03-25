package com.github.alantr7.prepo.dto;

import java.io.IOException;

import com.github.alantr7.prepo.entity.UserNotificationEntity;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class NotificationSerializer extends StdSerializer<UserNotificationEntity> {

    public NotificationSerializer() {
        this(null);
    }

    public NotificationSerializer(Class<UserNotificationEntity> klass) {
        super(klass);
    }

    @Override
    public void serialize(UserNotificationEntity notif, JsonGenerator gen, SerializerProvider arg2) throws IOException {
        gen.writeStartObject();
        
        switch (notif.getType()) {
            case NEW_BUILD: {
                gen.writeObjectField("new_build", notif.getNewBuild());
                break;
            }
            case NEW_ISSUE: {
                
                break;
            }
            case TEXT: {
                gen.writeStringField("text", notif.getText());
                break;
            }
        }

        gen.writeNumberField("id", notif.id);
        gen.writeStringField("type", notif.getType().name().toLowerCase());
        gen.writeObjectField("date", notif.getDate());
        gen.writeEndObject();
    }
    
}
