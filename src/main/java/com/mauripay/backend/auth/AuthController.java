package com.mauripay.backend.auth;

import com.mauripay.backend.auth.dto.LoginRequest;
import com.mauripay.backend.auth.dto.RegisterRequest;
import com.mauripay.backend.auth.dto.UserResponse;
import com.mauripay.backend.auth.exception.InvalidCredentialsException;
import com.mauripay.backend.common.ApiException;
import com.mauripay.backend.common.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(AuthService authService, AuthenticationManager authenticationManager,
                          LoginAttemptService loginAttemptService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request,
                                 HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        UserResponse user = authService.register(request);
        // Auto-login so the client lands authenticated and can immediately load
        // balance/transactions without a separate login round-trip.
        establishSession(request.phone(), request.password(), httpRequest, httpResponse);
        log.info("Registered and logged in user {} (phone={})", user.id(), request.phone());
        return user;
    }

    @PostMapping("/auth/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request,
                              HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        UUID userId = establishSession(request.phone(), request.password(), httpRequest, httpResponse);
        log.info("Login success for user {} (phone={})", userId, request.phone());
        return authService.currentUser(userId);
    }

    /** Authenticates the credentials and persists the SecurityContext into a new session. */
    private UUID establishSession(String phone, String password,
                                  HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        // Brute-force guard: reject early if locked, count failures, reset on success.
        loginAttemptService.assertNotLocked(phone);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(phone, password));
        } catch (BadCredentialsException ex) {
            loginAttemptService.onFailedLogin(phone);
            throw new InvalidCredentialsException("Invalid phone number or password.");
        }
        loginAttemptService.onSuccessfulLogin(phone);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        // Persist into a new session so subsequent requests are authenticated via JSESSIONID.
        httpRequest.getSession(true);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return ((AppUserDetails) authentication.getPrincipal()).getId();
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        var session = request.getSession(false);
        boolean wasLoggedIn = SecurityContextHolder.getContext().getAuthentication() != null;
        SecurityContextHolder.clearContext();
        if (session != null) {
            // Invalidating drops the row from the JDBC session store.
            session.invalidate();
        }
        // Expire the cookie on the client so it is not resent.
        Cookie expired = new Cookie("JSESSIONID", "");
        expired.setPath("/");
        expired.setHttpOnly(true);
        expired.setMaxAge(0);
        response.addCookie(expired);
        log.info("Logout (hadSession={}, wasAuthenticated={})", session != null, wasLoggedIn);
    }

    @GetMapping("/me")
    public UserResponse me(@org.springframework.security.core.annotation.AuthenticationPrincipal AppUserDetails principal) {
        if (principal == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.NOT_AUTHENTICATED, "Not authenticated");
        }
        return authService.currentUser(principal.getId());
    }
}
