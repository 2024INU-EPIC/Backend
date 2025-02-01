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
        // passwordEncoder 객체를 생성하지 않고 빈으로 등록된 객체로부터 주입받아 사용.
        user.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(user);
            return user;
    }

    public Optional<SiteUser> getByUsername(String username) {
        Optional<SiteUser> _siteUser = this.userRepository.findByusername(username);
        if (_siteUser.isEmpty()) {
            // 이메일과 사용자 이름 모두 없는 경우에 대한 예외 처리
            if (username.contains("@")) {
                throw new   UsernameNotFoundException("입력한 이메일로 사용자를 찾을 수 없습니다.");
            } else {
                throw new UsernameNotFoundException("입력한 사용자 이름으로 사용자를 찾을 수 없습니다.");
            }
        }
        SiteUser siteUser = _siteUser.get();
        return Optional.of(siteUser);
    }
}
