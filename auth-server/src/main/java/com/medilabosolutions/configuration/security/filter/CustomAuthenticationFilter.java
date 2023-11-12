package com.medilabosolutions.configuration.security.filter;

import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilabosolutions.model.LoginRequestModel;
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
    private boolean postOnly = true;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, Environment env) {
        super(authenticationManager);
        this.environment = env;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        try {

            LoginRequestModel userCredential = new ObjectMapper().readValue(request.getInputStream(), LoginRequestModel.class);
            UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(userCredential.getUsername(),
                    userCredential.getPassword());
            return this.getAuthenticationManager().authenticate(authRequest);

        } catch (IOException e) {
            throw new AuthenticationServiceException("User can't be authenticated cause of problem of credential", e);
        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        // add JWT for authenticated user in headers

        String username = ((User) authResult.getPrincipal()).getUsername();
        String tokenSecret = environment.getProperty("token.secret.key");
        byte[] secretKeyBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);

        Instant now = Instant.now();

        String jwToken = Jwts.builder()
                .header().add("typ", "JWT")
                .and()
                .subject(username)
                .id(DigestUtils.md5Hex(username))
                .expiration(Date.from(now.plusMillis(Long.parseLong(environment.getProperty("token.expiration.time")))))
                .issuedAt(Date.from(now))
                .signWith(secretKey)
                .compact();

        response.addHeader("jwtoken", jwToken); //TODO change jwtoken by authorization and Bearer

    }

}
