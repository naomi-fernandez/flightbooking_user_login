package com.userlogin.flightbooking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.function.Function;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.security.Keys;
import java.security.Key;


@Service
public class JwtService {
    @Value("${app.jwtSecret}")
    private String secretKey;
    @Value("${app.jwtExpirationMs}")
    private long jwtExpiration;


    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }
     public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
     }

     public String generateToken(UserDetails userDetails, String role){
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role",role);
        return buildToken(extraClaims,userDetails,jwtExpiration);
     }
     /*public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
        return buildToken(extraClaims, userDetails, jwtExpiration);
     }*/



     public long getExpirationTime(){
        return jwtExpiration;
     }

     private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration){
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
     }
     public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
     }

     private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
     }

     private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
     }

     private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
     }
     private Key getSignInKey(){
         System.out.println("Using JWT Secret:" + secretKey);
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
     }

     public String extractUserRole(String token){
         return extractAllClaims(token).get("role", String.class);
     }
}
