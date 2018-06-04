package com.bot.facebook.template;

import com.bot.facebook.util.Utf8ResourceBundleControl;
import com.core.util.Resource;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Taras Zubrei
 */
public class MessageTemplates {
    private final String properties;
    private final Map<Locale, ResourceBundle> bundles = new HashMap<>();
    private final ResourceBundle.Control utf8Support = new Utf8ResourceBundleControl();

    @Inject
    public MessageTemplates(@Named("i18n") String properties) {
        this.properties = properties;
    }

    public String getUOUpdateMessage(Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return resourceBundle.getString(TemplateNames.UO_UPDATE);
    }

    public String getUOUpdateActualMessage(Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return resourceBundle.getString(TemplateNames.UO_UPDATE_ACTUAL);
    }

    private ResourceBundle getBundle(Locale locale) {
        return bundles.computeIfAbsent(locale, __ -> ResourceBundle.getBundle(properties, locale, utf8Support));
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

    public UOTemplate getUOTemplate(Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return new UOTemplate(
                resourceBundle.getString(TemplateNames.UO_ID),
                resourceBundle.getString(TemplateNames.UO_NAME),
                resourceBundle.getString(TemplateNames.UO_SHORT_NAME),
                resourceBundle.getString(TemplateNames.UO_ADDRESS),
                resourceBundle.getString(TemplateNames.UO_BOSS),
                resourceBundle.getString(TemplateNames.UO_KVED),
                resourceBundle.getString(TemplateNames.UO_STAN),
                resourceBundle.getString(TemplateNames.UO_FOUNDER),
                resourceBundle.getString(TemplateNames.UO_FOUNDERS)
        );
    }

    public String getEmptyResource(Resource resource, Locale locale) {
        final ResourceBundle resourceBundle = getBundle(locale);
        return resourceBundle.getString(String.format(TemplateNames.EMPTY_RESOURCE_TEMPLATE, resource.getName()));
    }
}
