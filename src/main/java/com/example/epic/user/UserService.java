package com.example.epic.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Autowired
    private final JwtTokenUtil jwtTokenUtil;
    @Value("${jwt.secret}")
    private String secretkey;
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
        user.setUser_level(null);
        user.setLast_tested_at(null);
        user.setRole(UserRole.USER);
        // 유저 생성 성공 시 로그 출력
        SiteUser created = userRepository.save(user);
        log.info("{} 이름의 {} 이메일을 사용하는 유저 생성 성공. 사용자 권한은 {}", userCreateDto.getUsername(), userCreateDto.getEmail(), UserRole.USER);
        return created;
    }

    // 유저 삭제
    public SiteUser deleteUser(Long id, String authorizationHeader) {
        // token 추출
        String _token = authorizationHeader.replace("Bearer ", "");
        // 토큰 유효 확인
        if(JwtTokenUtil.isExpired(_token, secretkey)) {
            log.info("토큰이 만료됨");
            return null;
        }

        // URL id의 유저 찾기
        SiteUser deleted1 = userRepository.findById(id).orElse(null);
        if(deleted1 == null) {
            log.info("id : {} 의 유저 존재하지 않음", id);
            return null;
        }
        // token 주인 유저 찾기
        // token payload의 이메일
        String tokenEmail = JwtTokenUtil.getLoginId(_token, secretkey);
        SiteUser deleted2 = userRepository.findByEmail(tokenEmail).orElse(null);
        if(deleted2 == null) {
            log.info("토큰에 담긴 email : {} 의 유저 존재하지 않음", tokenEmail);
            return null;
        }

        // 토큰 유효. id 유저 존재. 토큰의 유저 존재. 이제 두 유저가 같은 유저인지 검사
        if(!deleted1.getId().equals(deleted2.getId())) {
            log.info("id의 유저와 token의 유저가 일치하지 않음");
            // log.info("id1 : {}, id2 : {}", deleted1.getId(), deleted2.getId());
            // log.info("email1 : {}, email2 : {}", deleted1.getEmail(), deleted2.getEmail());
            return null;
        }
        log.info("id의 유저와 token의 유저가 일치함");
        // 유저 삭제
        userRepository.deleteById(id);
        log.info("id : {} 의 삭제됨", id);
        return deleted1;
    }

    // 유저 업데이트
    public SiteUser updateUser(long id, String authorizationHeader, String oldPassword, String newPassword) {
        // token 추출
        String _token = authorizationHeader.replace("Bearer ", "");
        // 토큰 유효 확인
        if(JwtTokenUtil.isExpired(_token, secretkey)) {
            log.info("토큰이 만료됨");
            return null;
        }

        // id의 유저가 db에 존재하는지 검사
        SiteUser updated1 = userRepository.findById(id).orElse(null);
        if(updated1 == null) {
            log.info("id : {} 의 사용자가 존재하지 않음", id);
            return null;
        }

        // token 주인 유저 찾기
        // token payload의 이메일
        String tokenEmail = JwtTokenUtil.getLoginId(_token, secretkey);
        SiteUser updated2 = userRepository.findByEmail(tokenEmail).orElse(null);
        if(updated2 == null) {
            log.info("토큰에 담긴 email : {} 의 유저 존재하지 않음", tokenEmail);
            return null;
        }
        // 토큰 유효. id 유저 존재. 토큰의 유저 존재. 이제 두 유저가 같은 유저인지 검사
        if(!updated1.getId().equals(updated2.getId())) {
            log.info("id의 유저와 token의 유저가 일치하지 않음");
            // log.info("id1 : {}, id2 : {}", updated1.getId(), updated2.getId());
            // log.info("email1 : {}, email2 : {}", updated1.getEmail(), updated2.getEmail());
            return null;
        }
        // 토큰 유효. id 유저 존재. 토큰 주인 유저 존재. 두 유저 같음
        // 조회한 유저의 비밀번호와 입력한 기존 비밀번호 비교
        if(!passwordEncoder.matches(oldPassword, updated1.getPassword())) {
            log.info("입력한 비밀번호 {} 는 현재 유저의 비밀번호가 아님", oldPassword);
            return null;
        }
        // id의 유저도 존재하고 폼에서 입력한 비밀번호와 현재 유저의 비밀번호 일치 -> 비밀번호 수정
        updated1.setPassword(passwordEncoder.encode(newPassword));
        log.info("id : {} 유저의 비밀번호 수정 성공", id);
        return userRepository.save(updated1);
    }
}
