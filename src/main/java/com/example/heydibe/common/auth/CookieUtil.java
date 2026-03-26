package com.example.heydibe.common.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Component
public class CookieUtil {

    public String getAccessToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, "accessToken");
        if (cookie == null) {
            return null;
        }
        return cookie.getValue();
    }
}