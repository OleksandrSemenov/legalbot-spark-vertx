package com.bot.facebook.template;

import com.bot.facebook.util.Utf8ResourceBundleControl;
import com.core.util.Resource;
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
                .setViewButton(resourceBundle.getString(TemplateNames.MENU_BUTTON_VIEW))
                .setShowSubscriptions(resourceBundle.getString(TemplateNames.MENU_BUTTON_SUBSCRIPTIONS))
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

    public String getResource(Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return resourceBundle.getString(TemplateNames.GET_RESOURCE);
    }

    public String getEmptySubscriptions(Resource to, Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return resourceBundle.getString(String.format(TemplateNames.EMPTY_SUBSCRIPTIONS_TEMPLATE, to.getName()));
    }

    public String getSubscriptions(Resource to, Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return resourceBundle.getString(String.format(TemplateNames.SHOW_SUBSCRIPTIONS_TEMPLATE, to.getName()));
    }

    public String getResourceName(Resource resource, Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return resourceBundle.getString(String.format(TemplateNames.RESOURCE_NAME_TEMPLATE, resource.getName()));
    }

    public String getNextButton(Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return resourceBundle.getString(TemplateNames.NEXT);
    }
}
