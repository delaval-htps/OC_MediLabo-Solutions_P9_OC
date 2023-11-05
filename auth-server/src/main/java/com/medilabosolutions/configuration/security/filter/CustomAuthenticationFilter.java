package com.medilabosolutions.configuration.security.filter;

import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CustomAuthenticationFilter that implements UsernamePasswordAuthenticationFilter used by formLogin. Why this custom authentication filter just to override method successfullAuthentication to add a
 * jwtoken in response when authenticate request of login is sucessfull.
 */
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private Environment environment;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, Environment env) {
        super(authenticationManager);
        this.environment = env;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        // TODO create a jwt for authresult and add it to response header

        String username = ((User) authResult.getPrincipal()).getUsername();
        String tokenSecret = environment.getProperty("token.secret.key");
        byte[] secretKeyBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);

        Instant now = Instant.now();

        String jwToken = Jwts.builder()
                .subject(username)
                .id(username)
                .expiration(Date.from(now.plusMillis(Long.parseLong(environment.getProperty("token.expiration.time")))))
                .issuedAt(Date.from(now))
                .signWith(secretKey)
                .compact();

        response.addHeader("jwtoken", jwToken);

    }

}
