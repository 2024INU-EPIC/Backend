package com.example.epic.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    // JWT 인증 시 사용하는 메서드들

    /*
     * Email 중복 체크
     * 회원가입 기능 구현 시 사용
     * 중복되면 true return
     */
    public boolean checkEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    /*
     * nickname 중복 체크
     * 회원가입 기능 구현 시 사용
     * 중복되면 true return
     */
    public boolean checkNicknameDuplicate(String username) {
        return userRepository.existsByUsername(username);
    }
    public SiteUser login(UserLoginDto userLoginDto) {
        Optional<SiteUser> optional_siteuser = userRepository.findByEmail(userLoginDto.getEmail());

        // email의 사용자 존재X
        if(optional_siteuser.isEmpty()) {
            return null;
        }

        SiteUser user = optional_siteuser.get();

        // DB에서 찾은 유저와 dto에서 입력된 비밀번호가 다를 때
        log.info("비밀번호 일치 여부: {}", passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword()));
        if (!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            return null;
        }

        return user;
    }

    /*
     * userId(Long)를 입력받아 SiteUser을 return 해주는 기능
     * 인증, 인가 시 사용
     * userId가 null이거나(로그인 X) userId로 찾아온 SiteUser가 없으면 null return
     * userId로 찾아온 SiteUser가 존재하면 SieUser return
     */
    public SiteUser getLoginUserById(Long userId) {
        if(userId == null) {
            return null;
        }

        Optional<SiteUser> optional_siteuser = userRepository.findById(userId);
        if(optional_siteuser.isEmpty()) { return null; }

        return optional_siteuser.get();
    }

    /*
     * email(String)를 입력받아 User을 return 해주는 기능
     * 인증, 인가 시 사용
     * email이 null이거나(로그인 X) email로 찾아온 SiteUser가 없으면 null return
     * email로 찾아온 SiteUser가 존재하면 SiteUser return
     */
    public SiteUser getLoginUserByEmail(String email) {
        if(email == null) {
            return null;
        }

        Optional<SiteUser> optional_siteuser = userRepository.findByEmail(email);
        if(optional_siteuser.isEmpty()) { return null; }

        return optional_siteuser.get();
    }

// <------------------------------------------------------------------------------------------------------------------>

    // 회원가입 시, DB에 해당 유저 조회
    public Boolean findSiteUser(String email) {
        SiteUser _loginUser = userRepository.findByEmail(email).orElse(null);
        if(_loginUser != null) {
            log.warn("{} 이메일을 사용하는 유저 존재함", email);
            return true;
        }
        else {
            log.info("{} 이메일을 사용하는 유저 존재하지 않음", email);
            return false;
        }
    }

    // 유저 생성
    public SiteUser create(UserCreateDto userCreateDto) {
        log.info("유저 생성 진입");
        SiteUser user = new SiteUser();
        user.setUsername(userCreateDto.getUsername());
        // passwordEncoder 객체를 생성하지 않고 빈으로 등록된 객체로부터 주입받아 사용.
        user.setPassword(passwordEncoder.encode(userCreateDto.getPassword2()));
        user.setEmail(userCreateDto.getEmail());
        user.setUser_level(userCreateDto.getUser_level());
        user.setLast_tested_at(userCreateDto.getLast_tested_at());
        user.setRole(UserRole.USER);
        // 유저 생성 성공 시 로그 출력
        SiteUser created = userRepository.save(user);
        log.info("{} 이름의 {} 이메일을 사용하는 유저 생성 성공. 사용자 권한은 {}", userCreateDto.getUsername(), userCreateDto.getEmail(), UserRole.USER.toString());
        return created;
    }
}
