package com.josyantl.joblens.identity.interfaces.rest;

import com.josyantl.joblens.identity.application.service.UserRegistrationService;
import com.josyantl.joblens.identity.domain.model.UserAccount;
import com.josyantl.joblens.identity.domain.repository.UserAccountRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final UserRegistrationService registrationService;
    private final UserAccountRepository repository;

    public AuthenticationController(UserRegistrationService registrationService,
                                    UserAccountRepository repository) {
        this.registrationService = registrationService;
        this.repository = repository;
    }

    public record RegisterRequest(@NotBlank @Email @Size(max = 254) String email,
                                  @NotBlank @Size(min = 12, max = 72) String password,
                                  @NotBlank @Size(max = 100) String displayName) {}
    public record LoginRequest(@NotBlank @Email @Size(max = 254) String email,
                               @NotBlank String password) {}
    public record UserResponse(Long id, String email, String displayName) {
        static UserResponse from(UserAccount account) {
            return new UserResponse(account.id(), account.email(), account.displayName());
        }
    }
    public record CsrfResponse(String headerName, String token) {}

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(token.getHeaderName(), token.getToken());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest body,
                                 HttpServletRequest request) {
        UserAccount account = registrationService.register(body.email(), body.password(), body.displayName());
        loginRequest(request, account.email(), body.password());
        return UserResponse.from(account);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        String email = UserAccount.normalizeEmail(body.email());
        loginRequest(request, email, body.password());
        return repository.findByEmail(email).map(UserResponse::from)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return repository.findByEmail(authentication.getName()).map(UserResponse::from)
                .orElseThrow(() -> new BadCredentialsException("Authentication required"));
    }

    private void loginRequest(HttpServletRequest request, String email, String password) {
        try {
            request.login(email, password);
        } catch (ServletException exception) {
            throw new BadCredentialsException("Invalid email or password", exception);
        }
    }
}
