package com.github.sgwhp.openapm.sample.util;

/**
 * Created by user on 2016/8/8.
 */
public class NewRelicCanary {
    private String sound;

    public NewRelicCanary(final String sound) {
        this.sound = sound;
    }

    public static void canaryMethod() {
        final NewRelicCanary canary = new NewRelicCanary("tweet!");
    }
}
