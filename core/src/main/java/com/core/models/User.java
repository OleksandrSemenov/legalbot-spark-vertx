package com.core.models;

import com.core.util.MessengerType;

import java.util.Map;
import java.util.UUID;

/**
 * @author Taras Zubrei
 */
public class User {
    private UUID id;
    private Map<MessengerType, String> messengerIds;

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
}
