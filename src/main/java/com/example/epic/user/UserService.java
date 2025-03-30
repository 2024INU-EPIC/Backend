package com.example.epic.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    // 로그인 시, DB에 해당 유저 조회
    public SiteUser findSiteUser(String email, String password) {
        SiteUser _loginUser = userRepository.findByEmail(email).orElse(null);
        if(_loginUser != null) {
            if(passwordEncoder.matches(password, _loginUser.getPassword())) {
                return _loginUser;
            }
            else {
                log.info("아이디 {}의 유저의 비밀번호를 잘못 입력함", email);
                return null;
            }
        }
        else {
            log.info("{}를 아이디로 가지는 유저 존재하지 않음", email);
            return null;
        }
    }


    public SiteUser create(UserCreateDto userCreateDto) {
        SiteUser user = new SiteUser();
        user.setUsername(userCreateDto.getUsername());
        // passwordEncoder 객체를 생성하지 않고 빈으로 등록된 객체로부터 주입받아 사용.
        user.setPassword(passwordEncoder.encode(userCreateDto.getPassword2()));
        user.setEmail(userCreateDto.getEmail());
        user.setUser_level(userCreateDto.getUser_level());
        user.setLast_tested_at(userCreateDto.getLast_tested_at());
        this.userRepository.save(user);
            return user;
    }

    public Optional<SiteUser> getByUsername(String username) {
        Optional<SiteUser> _siteUser = this.userRepository.findByUsername(username);
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
