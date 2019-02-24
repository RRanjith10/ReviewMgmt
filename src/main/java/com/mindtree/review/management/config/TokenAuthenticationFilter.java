package com.mindtree.review.management.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.web.filter.GenericFilterBean;

import com.google.gson.Gson;
import com.mindtree.review.management.model.OAuthUser;

public class TokenAuthenticationFilter extends GenericFilterBean {

	private static final String X_ACCESS_TOKEN = "X-ACCESS-TOKEN"; // Raw token, can be used to make other service call

	private static final String X_USER_INFO = "X_USER_INFO"; // Holds user info like email, name etc..

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        final String accessToken = httpRequest.getHeader(X_ACCESS_TOKEN);

        if (accessToken != null) {
            try {
                Gson gson = new Gson();
                OAuthUser user = gson.fromJson(new String(Base64.decodeBase64(accessToken)), OAuthUser.class);
                HttpSession session = httpRequest.getSession(true);
                session.setAttribute(X_USER_INFO, user);
                session.setAttribute(X_ACCESS_TOKEN, accessToken);
            }
            catch (Exception exp) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token.");
                return;
            }
        }
        else {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token.");
            return;
        }
        chain.doFilter(request, response);
    }

}
