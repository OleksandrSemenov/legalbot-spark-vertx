package com.spark.handler.messenger;

import com.spark.models.UOUpdate;
import com.spark.util.MessengerType;

/**
 * @author Taras Zubrei
 */
public interface MessengerHandler {
    MessengerType type();

    void onUOUpdate(UOUpdate update);
}