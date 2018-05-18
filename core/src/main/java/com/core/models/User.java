package com.core.models;

import com.core.util.MessengerType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * @author Taras Zubrei
 */
public class User {
    private UUID id;
    private Map<MessengerType, String> messengerIds = new HashMap<>();
    private Map<MessengerType, String> locales = new HashMap<>();

    public User() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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
