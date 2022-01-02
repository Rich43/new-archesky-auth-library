package com.archesky.auth.library.util;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

public class GetHost {
    public static String getHost(final HttpServletRequest request) {
        try {
            return request.getHeader("hostname");
        } catch (final IllegalStateException e) {
            // Do nothing
        }
        try {
            return request.getHeader("Host");
        } catch (final IllegalStateException e) {
            // Do nothing
        }
        try {
            return new URL(request.getHeader("Origin")).getHost();
        } catch (final IllegalStateException | MalformedURLException e) {
            // Do nothing
        }
        return "localhost";
    }
}
