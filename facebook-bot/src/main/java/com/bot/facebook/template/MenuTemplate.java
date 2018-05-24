package com.bot.facebook.template;

/**
 * @author Taras Zubrei
 */
public class MenuTemplate {
    private final String title;
    private String viewUOButton;
    private String changeLocaleButton;

    public MenuTemplate(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getViewUOButton() {
        return viewUOButton;
    }

    public MenuTemplate setViewUOButton(String viewUOButton) {
        this.viewUOButton = viewUOButton;
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
