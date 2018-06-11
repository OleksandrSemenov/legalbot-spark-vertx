package com.core.util;

/**
 * @author Taras Zubrei
 */
public interface RedisKeys {
    String UFOP_LAST_UPDATE_DATE = "ufop/date";
    String USER_SUBSCRIPTION_TEMPlATE = "user/%s/subscriptions/%s";
    String UO_TEMPLATE = "uo/%s";
    String FACEBOOK_STATE_TEMPLATE = "facebook/%s/state";
    String FACEBOOK_PREVIOUS_MESSAGE_TEMPLATE = "facebook/%s/prev";
}
