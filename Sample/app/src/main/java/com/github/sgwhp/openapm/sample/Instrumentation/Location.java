package com.github.sgwhp.openapm.sample.Instrumentation;

/**
 * Created by rarshion on 16/8/11.
 */
public class Location {

    private final String countryCode;
    private final String region;

    public Location(final String countryCode, final String region) {
        if (countryCode == null || region == null) {
            throw new IllegalArgumentException("Country code and region must not be null.");
        }
        this.countryCode = countryCode;
        this.region = region;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public String getRegion() {
        return this.region;
    }
}
