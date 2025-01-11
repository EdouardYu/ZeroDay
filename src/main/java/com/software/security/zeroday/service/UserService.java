package com.software.security.zeroday.service;

import com.software.security.zeroday.dto.user.*;
import com.software.security.zeroday.entity.User;
import com.software.security.zeroday.entity.Validation;
import com.software.security.zeroday.entity.enumeration.FileType;
import com.software.security.zeroday.entity.enumeration.Gender;
import com.software.security.zeroday.entity.enumeration.Nationality;
import com.software.security.zeroday.entity.enumeration.Role;
import com.software.security.zeroday.repository.UserRepository;
import com.software.security.zeroday.security.util.AuthorizationUtil;
import com.software.security.zeroday.service.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ValidationService validationService;
    private final PostService postService;
    private final FileService fileService;
    private final AuthorizationUtil authorizationUtil;

    public void signUp(RegistrationDTO userDTO) {
        Optional<User> dbUser = this.userRepository.findByEmail(userDTO.getEmail());
        String encryptedPassword = this.passwordEncoder.encode(userDTO.getPassword());
        Instant now = Instant.now();

        User user;
        if(dbUser.isPresent()){
            user = dbUser.get();
            if(user.isEnabled()) throw new AlreadyUsedException("Email already used");

            user.setPassword(encryptedPassword);
            user.setFirstname(userDTO.getFirstname());
            user.setLastname(userDTO.getLastname());
            user.setBirthday(userDTO.getBirthday());
            user.setGender(userDTO.getGender());
            user.setNationality(userDTO.getNationality());
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
        } else {
            user = User.builder()
                .email(userDTO.getEmail())
                .password(encryptedPassword)
                .firstname(userDTO.getFirstname())
                .lastname(userDTO.getLastname())
                .username(userDTO.getFirstname() + " " + userDTO.getLastname())
                .birthday(userDTO.getBirthday())
                .gender(userDTO.getGender())
                .nationality(userDTO.getNationality())
                .bio("Hey! I use ZeroDay")
                .createdAt(now)
                .updatedAt(now)
                .enabled(false)
                .role(Role.USER)
                .build();
        }

        this.userRepository.save(user);

        this.validationService.register(user);
    }
    public void activate(ActivationDTO activationDTO) {
        Validation validation = this.validationService.findUserActivationCode(
            activationDTO.getEmail(),
            activationDTO.getCode()
        );

        Instant now =Instant.now();

        if(now.isAfter(validation.getExpiredAt()))
            throw new ValidationCodeException("Expired activation code");

        if(!validation.isEnabled())
            throw new ValidationCodeException("Disabled activation code");

        User user = validation.getUser();
        if(user.isEnabled())
            throw new AlreadyProcessedException("User already enabled");

        user.setEnabled(true);
        this.userRepository.save(user);

        this.postService.createWelcomePost(user, now);
    }

    public void newActivationCode(EmailDTO userDTO) {
        User user = this.loadUserByUsername(userDTO.getEmail());

        if(user.isEnabled())
            throw new AlreadyProcessedException("User already enabled");

        this.validationService.register(user);
    }

    public void resetPassword(EmailDTO userDTO) {
        User user = this.loadUserByUsername(userDTO.getEmail());

        if(!user.isEnabled())
            throw new NotYetEnabledException("User not yet enabled");

        this.validationService.resetPassword(user);
    }

    public void newPassword(PasswordResetDTO passwordResetDTO) {
        Validation validation = this.validationService.findUserPasswordResetCode(
            passwordResetDTO.getEmail(),
            passwordResetDTO.getCode()
        );

        User user = validation.getUser();
        if(!user.isEnabled())
            throw new NotYetEnabledException("User not yet enabled");

        if(Instant.now().isAfter(validation.getExpiredAt()))
            throw new ValidationCodeException("Expired password reset code");

        if(!validation.isEnabled())
            throw new ValidationCodeException("Disabled password reset code");

        String encryptedPassword = this.passwordEncoder.encode(passwordResetDTO.getPassword());
        user.setPassword(encryptedPassword);
        this.userRepository.save(user);
    }

    public OptionsDTO getOptions() {
        return OptionsDTO.builder()
            .genders(EnumSet.allOf(Gender.class))
            .nationalities(EnumSet.allOf(Nationality.class))
            .build();
    }

    public ProfileDTO getProfile(Long id) {
        User user = this.userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return this.toProfileDTO(user);
    }

    public ProfileDTO modifyProfile(Long id, ProfileModificationDTO userDTO) {
        User user = this.authorizationUtil.verifyAuthorization(id);
        Instant now = Instant.now();

        user.setEmail(userDTO.getEmail());
        user.setUsername(userDTO.getUsername());
        user.setFirstname(userDTO.getFirstname());
        user.setLastname(userDTO.getLastname());
        user.setBirthday(userDTO.getBirthday());
        user.setGender(userDTO.getGender());
        user.setNationality(userDTO.getNationality());
        user.setBio(userDTO.getBio());
        user.setUpdatedAt(now);

        user = userRepository.save(user);

        if(userDTO.getPictureId() != null)
            this.postService.createProfilePictureUpdatePost(user, userDTO.getPictureId(), now);

        return this.toProfileDTO(user);
    }

    public void modifyPassword(Long id, PasswordModificationDTO userDTO) {
        User user = this.authorizationUtil.verifyAuthorization(id);

        if (!passwordEncoder.matches(userDTO.getOldPassword(), user.getPassword()))
            throw new BadPasswordException("Incorrect password");

        String newEncryptedPassword = this.passwordEncoder.encode(userDTO.getNewPassword());

        user.setPassword(newEncryptedPassword);
        user.setUpdatedAt(Instant.now());

        this.userRepository.save(user);
    }

    public void deleteProfile(Long id) {
        User user = this.authorizationUtil.verifyAuthorization(id);
        this.fileService.deleteAllUserFiles(user);
        this.userRepository.delete(user);
    }

    private ProfileDTO toProfileDTO(User user) {
        String fileName = this.fileService.getProfilePicture(user.getId());

        return ProfileDTO.builder()
            .email(user.getEmail())
            .firstname(user.getFirstname())
            .lastname(user.getLastname())
            .username(user.getUsername())
            .birthday(user.getBirthday())
            .gender(user.getGender())
            .nationality(user.getNationality())
            .pictureUrl(FileType.IMAGE.getFolder() + "/" + fileName)
            .bio(user.getBio())
            .role(user.getRole())
            .build();
    }

    @Override
    public User loadUserByUsername(String email) {
        return this.userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
