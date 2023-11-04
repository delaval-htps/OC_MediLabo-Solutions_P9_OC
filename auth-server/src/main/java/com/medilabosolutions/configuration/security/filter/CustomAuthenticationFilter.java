package com.medilabosolutions.configuration.security.filter;

import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CustomAuthenticationFilter that implements UsernamePasswordAuthenticationFilter used by formLogin.
 * Why this custom authentication filter just to override method successfullAuthentication to add a
 * jwtoken in response when authenticate request of login is sucessfull.
 */
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // TODO create a jwt for authresult and add it to response header
        super.successfulAuthentication(request, response, chain, authResult);
    }

}
