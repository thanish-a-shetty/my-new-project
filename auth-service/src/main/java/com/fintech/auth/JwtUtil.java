package com.fintech.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // Default 24 hours in milliseconds
    private Long expiration;

    /**
     * Generate JWT token for user
     * @param email user's email address
     * @return JWT token string
     */
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email);
    }

    /**
     * Create JWT token with claims
     * @param claims additional claims to include in token
     * @param subject the subject (usually email) of the token
     * @return JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract username (email) from JWT token
     * @param token JWT token
     * @return username (email) from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from JWT token
     * @param token JWT token
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from JWT token
     * @param token JWT token
     * @param claimsResolver function to extract specific claim
     * @param <T> type of claim
     * @return extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from JWT token
     * @param token JWT token
     * @return all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if JWT token is expired
     * @param token JWT token
     * @return true if token is expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate JWT token
     * @param token JWT token
     * @param email user's email address
     * @return true if token is valid, false otherwise
     */
    public Boolean validateToken(String token, String email) {
        final String username = extractUsername(token);
        return (username.equals(email) && !isTokenExpired(token));
    }

    /**
     * Get signing key for JWT
     * @return SecretKey for signing JWT tokens
     */
    private SecretKey getSigningKey() {
        // REVIEW: production hardening required - ensure secret is properly configured
        // and has sufficient length (at least 256 bits)
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract token from Authorization header
     * @param authHeader Authorization header value (e.g., "Bearer <token>")
     * @return JWT token string or null if not found
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Get token expiration time in milliseconds
     * @return token expiration time
     */
    public Long getExpirationTime() {
        return expiration;
    }
}
