package com.core.models;

import com.core.util.MessengerType;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Taras Zubrei
 */
@Entity("user")
public class User {
    @Id
    private ObjectId id;
    private Map<MessengerType, String> messengerIds = new HashMap<>();
    private Map<MessengerType, String> locales = new HashMap<>();

    public User() {
    }

    public String getId() {
        return id != null ? id.toString() : null;
    }

    public void setId(String id) {
        if (id != null) this.id = new ObjectId(id);
    }

    public Map<MessengerType, String> getMessengerIds() {
        return messengerIds;
    }

    public void setMessengerIds(Map<MessengerType, String> messengerIds) {
        this.messengerIds = messengerIds;
    }

    public Map<MessengerType, String> getLocales() {
        return locales;
    }

    public void setLocales(Map<MessengerType, String> locales) {
        this.locales = locales;
    }

    public Locale getLocale(MessengerType type) {
        return Locale.forLanguageTag(locales.get(type));
    }

    public void addMessenger(MessengerType type, String userId, Locale locale) {
        messengerIds.put(type, userId);
        locales.put(type, locale.toLanguageTag());
    }

    public String getMessengerId(MessengerType type) {
        return this.messengerIds.get(type);
    }
}
