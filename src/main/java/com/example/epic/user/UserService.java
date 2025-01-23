package com.example.epic.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SiteUser create(String username, String email, String password) {
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        // passwordEncoder 객체를 생성하지 않고 빈으로 등록된 객체로부터 주입받아 사용.
        user.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(user);
            return user;
    }
}
