package com.bot.facebook.template;

/**
 * @author Taras Zubrei
 */
public class MenuTemplate {
    private final String title;
    private String viewButton;
    private String showSubscriptions;
    private String changeLocaleButton;

    public MenuTemplate(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getViewButton() {
        return viewButton;
    }

    public MenuTemplate setViewButton(String viewButton) {
        this.viewButton = viewButton;
        return this;
    }

    public String getShowSubscriptions() {
        return showSubscriptions;
    }

    public MenuTemplate setShowSubscriptions(String showSubscriptions) {
        this.showSubscriptions = showSubscriptions;
        return this;
    }

    public String getChangeLocaleButton() {
        return changeLocaleButton;
    }

    public MenuTemplate setChangeLocaleButton(String changeLocaleButton) {
        this.changeLocaleButton = changeLocaleButton;
        return this;
    }
}
