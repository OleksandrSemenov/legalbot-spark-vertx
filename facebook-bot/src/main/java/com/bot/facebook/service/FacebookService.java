package com.bot.facebook.service;

import com.restfb.types.Message;

/**
 * @author Taras Zubrei
 */
public interface FacebookService {
    void sendMessage(String userId, Message message);
}
