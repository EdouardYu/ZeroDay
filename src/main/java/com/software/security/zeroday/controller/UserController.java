package com.software.security.zeroday.controller;

import com.software.security.zeroday.dto.user.*;
import com.software.security.zeroday.security.JwtService;
import com.software.security.zeroday.service.LoginAttemptService;
import com.software.security.zeroday.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(path = "signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void signUp(@Valid @RequestBody RegistrationDTO userDTO) {
        this.userService.signUp(userDTO);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(path = "activate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void activate(@RequestBody ActivationDTO activationDTO) {
        this.userService.activate(activationDTO);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(path = "activate/new", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void newActivationCode(@RequestBody EmailDTO userDTO) {
        this.userService.newActivationCode(userDTO);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(
        path = "signin",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public TokenDTO signIn(
        @RequestBody AuthenticationDTO authenticationDTO,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String deviceId = this.loginAttemptService.getDeviceIdentifier(request, response);
        this.loginAttemptService.validateLoginAttempt(authenticationDTO.getEmail(), deviceId);

        try {
            this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authenticationDTO.getEmail(),
                    authenticationDTO.getPassword()
                )
            );

            this.loginAttemptService.resetAttempts(authenticationDTO.getEmail(), deviceId);
            return jwtService.generate(authenticationDTO.getEmail());

        } catch (BadCredentialsException e) {
            int remainingAttempts = this.loginAttemptService.recordFailedAttempt(authenticationDTO.getEmail(), deviceId);
            throw new BadCredentialsException("Invalid email or password. Remaining attempts: " + remainingAttempts);
        }
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(path = "signout")
    public void signOut() {
        this.jwtService.signOut();
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(path = "password/reset", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void resetPassword(@RequestBody EmailDTO userDTO) {
        this.userService.resetPassword(userDTO);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(path = "password/new", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void newPassword(@Valid @RequestBody PasswordResetDTO passwordResetDTO) {
        this.userService.newPassword(passwordResetDTO);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(path = "options", produces = MediaType.APPLICATION_JSON_VALUE)
    public OptionsDTO getOptions() {
        return this.userService.getOptions();
    }

    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(path = "profiles/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileDTO getProfile(@PathVariable Long id) {
        return this.userService.getProfile(id);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PutMapping(
        path = "profiles/{id}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ProfileDTO modifyProfile(
        @PathVariable Long id, @Valid @RequestBody ProfileModificationDTO userDTO) {
        return userService.modifyProfile(id, userDTO);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PutMapping(path = "profiles/{id}/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void modifyPassword(@PathVariable Long id, @Valid @RequestBody PasswordModificationDTO userDTO) {
        this.userService.modifyPassword(id, userDTO);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @DeleteMapping(path = "profiles/{id}")
    public void deleteProfile(@PathVariable Long id) {
        this.userService.deleteProfile(id);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(path = "admin/flag", produces = MediaType.APPLICATION_JSON_VALUE)
    public FlagDTO getFlag() {
        return FlagDTO.builder().flag("CTF{JWT_ADMIN_ACCESS_GRANTED}").build();
    }
}

