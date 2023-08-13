package com.inbank.dengine.config.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    public static final long JWT_VALIDITY = 5*60*60;

    @Value("${jwt.secret}")
    private String secret;

    public String getUsernameForToken(String tokenString) {
        return getClaimForToken(tokenString, Claims::getSubject);
    }

    public Date getExpirationDateForToken(String tokenString) {
        return getClaimForToken(tokenString, Claims::getExpiration);
    }

    public <T> T getClaimForToken(String tokenString, Function<Claims, T> claimResolver) {
        Claims claims = getAllClaimsFromToken(tokenString);
        return claimResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String tokenString) {
        return
                Jwts.parser().setSigningKey(secret).parseClaimsJws(tokenString).getBody();
    }

    private Boolean isTokenExpired(String tokenStirng) {
        final Date expirationDate = getExpirationDateForToken(tokenStirng);
        return expirationDate.before(new Date());
    }


    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return generateTokenFromClaims(claims, userDetails.getUsername());
    }

    private String generateTokenFromClaims(Map<String, Object> claims, String subject) {
        return
                Jwts
                        .builder()
                        .setClaims(claims)
                        .setSubject(subject)
                        .setIssuedAt(new Date(System.currentTimeMillis()))
                        .setExpiration(new Date(System.currentTimeMillis() + JWT_VALIDITY * 1000))
                        .signWith(SignatureAlgorithm.HS512, secret)
                        .compact();
    }

    public Boolean validateToken(String tokenString, UserDetails userDetails) {
        final String username = getUsernameForToken(tokenString);
        boolean isUsernameMatchedAndNotExpired = username.equals(userDetails.getUsername()) && !isTokenExpired(tokenString);
        return isUsernameMatchedAndNotExpired;
    }
}
