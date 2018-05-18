package com.bot.facebook.template;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Taras Zubrei
 */
public class MessageTemplates {
    private final String properties;


    @Inject
    public MessageTemplates(@Named("i18n") String properties) {
        this.properties = properties;
    }

    public String getUOUpdateMessage(Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return resourceBundle.getString(TemplateNames.UO_UPDATE);
    }

    private ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(properties, locale);
    }
}
