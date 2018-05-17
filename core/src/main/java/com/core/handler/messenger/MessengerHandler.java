package com.core.handler.messenger;

import com.core.models.UOUpdate;
import com.core.models.User;
import com.core.util.MessengerType;

/**
 * @author Taras Zubrei
 */
public interface MessengerHandler {
    MessengerType type();

    void onUOUpdate(User user, UOUpdate update);
}
