package com.helloworld.contract;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Request-level context: locale, timezone. Extensible for other meta. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestMeta {
    private String locale;   // e.g. "en-IN"
    private String timezone; // e.g. "Asia/Kolkata"

    public RequestMeta() {
    }

    public RequestMeta(String locale, String timezone) {
        this.locale = locale;
        this.timezone = timezone;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
