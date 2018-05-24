package com.bot.facebook.template;

import com.bot.facebook.util.Utf8ResourceBundleControl;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Taras Zubrei
 */
public class MessageTemplates {
    private final String properties;
    private final ResourceBundle.Control utf8Support = new Utf8ResourceBundleControl();

    @Inject
    public MessageTemplates(@Named("i18n") String properties) {
        this.properties = properties;
    }

    public String getUOUpdateMessage(Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return resourceBundle.getString(TemplateNames.UO_UPDATE);
    }

    private ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(properties, locale, utf8Support);
    }

    public MenuTemplate getBasicMenuTemplate(Locale locale, Locale language) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return new MenuTemplate(resourceBundle.getString(TemplateNames.MENU_TITLE))
                .setViewUOButton(resourceBundle.getString(TemplateNames.MENU_BUTTON_VIEW_UO))
                .setChangeLocaleButton(
                        new StrSubstitutor(ImmutableMap.of(
                                "locale", language.getDisplayLanguage(language)
                        )).replace(getBundle(language).getString(TemplateNames.MENU_BUTTON_CHANGE_LANG))
                );
    }

    public String getWrongCommandTemplate(Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return resourceBundle.getString(TemplateNames.WRONG_COMMAND);
    }

    public String getUnsubscribeButton(Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return resourceBundle.getString(TemplateNames.UNSUBSCRIBE);
    }

    public String getSubscribeButton(Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return resourceBundle.getString(TemplateNames.SUBSCRIBE);
    }

    public String getUOId(Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return resourceBundle.getString(TemplateNames.VIEW_UO_ID);
    }
}
