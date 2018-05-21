package com.core.models;

import com.core.util.MessengerType;
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
    private String id;
    private Map<MessengerType, String> messengerIds = new HashMap<>();
    private Map<MessengerType, String> locales = new HashMap<>();

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
