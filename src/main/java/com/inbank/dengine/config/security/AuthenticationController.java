package com.inbank.dengine.config.security;

import com.inbank.dengine.config.model.AuthenticationRequest;
import com.inbank.dengine.config.model.AuthenticationResponse;
import com.inbank.dengine.config.user.AppUserDetailsService;
import com.inbank.dengine.config.util.JwtUtil;
import com.inbank.dengine.exception.exceptionType.BadCredentialException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    @PostMapping("/api/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest jwtRequest) throws Exception{

        // authenticate user
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    jwtRequest.getUsername(), jwtRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialException("INVALID CREDENTIALS");
        }

        // if authenticated
        final UserDetails userDetails = appUserDetailsService.loadUserByUsername(jwtRequest.getUsername());

        final String token =  jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok((new AuthenticationResponse(token)));

    }
}
