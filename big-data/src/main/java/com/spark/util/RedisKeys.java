package com.spark.util;

/**
 * @author Taras Zubrei
 */
public interface RedisKeys {
    String UFOP_LAST_UPDATE_DATE = "ufop/date";
    String USER_TEMPlATE = "user/%s";
    String USER_SUBSCRIPTION_TEMPlATE = "user/%s/subscriptions/%s";
    String USERS = "users";
    String UO_TEMPLATE = "uo/%s";
    String FOP = "fop";
}