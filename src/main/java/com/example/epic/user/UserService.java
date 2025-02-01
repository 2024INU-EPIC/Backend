package com.example.epic.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SiteUser create(String username, String email, String password) {
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        // Encode the password using the injected PasswordEncoder
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return user;
    }

    public Optional<SiteUser> getByUsername(String username) {
        Optional<SiteUser> _siteUser = userRepository.findByusername(username);
        if (_siteUser.isEmpty()) {
            // If the username contains '@', treat it as an email
            if (username.contains("@")) {
                throw new UsernameNotFoundException("입력한 이메일로 사용자를 찾을 수 없습니다.");
            } else {
                throw new UsernameNotFoundException("입력한 사용자 이름으로 사용자를 찾을 수 없습니다.");
            }
        }
        return _siteUser;
    }

    public boolean checkPassword(SiteUser user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void updatePassword(SiteUser user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deleteUser(SiteUser user) {
        userRepository.delete(user);
    }
}