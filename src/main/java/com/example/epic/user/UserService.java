package com.example.epic.user;

import com.azure.json.implementation.jackson.core.JsonProcessingException;
import com.example.epic.Assessment.TestGradeRepository;
import com.example.epic.security.JwtTokenUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

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
    
    // 액세스 토큰 만료 확인 상태
    // 리프레쉬 토큰 상태 확인
    // 만료 안됐을 경우 새로운 액세스 토큰 리턴. 만료되었을 경우 쿠키 파기 후 null 반환. 응답 관련 사항을 해당 메서드에서 미리 처리
    public String handleExpiredToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies)
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue(); // 쿠키 값 가져오기
                    break;
                }
        }
        // 액세스 토큰/리프레쉬 토큰 모두 만료. 관련 쿠키 파기
        if(jwtTokenUtil.isExpired(refreshToken)) {
            // 액세스 토큰 파기
            Cookie cookie1 = new Cookie("jwtToken", "");
            cookie1.setMaxAge(0);
            cookie1.setHttpOnly(true);
            cookie1.setPath("/");
            response.addCookie(cookie1);
            // 리프레쉬 토큰 파기
            Cookie cookie2 = new Cookie("refreshToken", "");
            cookie2.setMaxAge(0);
            cookie2.setHttpOnly(true);
            cookie2.setPath("/");
            response.addCookie(cookie2);
            return null;
        } else {
            // 리프레쉬 토큰 살아있음
            // 새로운 액세스 토큰 생성
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            String _token = authorizationHeader.replace("Bearer ", "");
            return jwtTokenUtil.recreateAccessToken(_token);
        }
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
    @Transactional
    public SiteUser create(UserCreateDto userCreateDto) {
        log.info("유저 생성 진입");
        SiteUser user = new SiteUser();
        user.setUsername(userCreateDto.getUsername());
        // passwordEncoder 객체를 생성하지 않고 빈으로 등록된 객체로부터 주입받아 사용.
        user.setPassword(passwordEncoder.encode(userCreateDto.getPassword2()));
        user.setEmail(userCreateDto.getEmail());
        user.setUserLevel(null);
        user.setLastTestedAt(null);
        user.setRole(UserRole.USER);
        // 유저 생성 성공 시 로그 출력
        SiteUser created = userRepository.save(user);
        log.info("{} 이름의 {} 이메일을 사용하는 유저 생성 성공. 사용자 권한은 {}", userCreateDto.getUsername(), userCreateDto.getEmail(), UserRole.USER);
        return created;
    }

    // 유저 삭제
    @Transactional
    public SiteUser deleteUser(Long id, String _token) {
        // URL id의 유저 찾기
        SiteUser deleted1 = userRepository.findById(id).orElse(null);
        if(deleted1 == null) {
            log.info("id : {} 의 유저 존재하지 않음", id);
            return null;
        }
        // token 주인 유저 찾기
        // token payload의 이메일
        String tokenEmail = jwtTokenUtil.getLoginId(_token);
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
    @Transactional
    public SiteUser updateUser(long id, String _token, String oldPassword, String newPassword) {
        // id의 유저가 db에 존재하는지 검사
        SiteUser updated1 = userRepository.findById(id).orElse(null);
        if(updated1 == null) {
            log.info("id : {} 의 사용자가 존재하지 않음", id);
            return null;
        }
        // token 주인 유저 찾기
        // token payload의 이메일
        String tokenEmail = jwtTokenUtil.getLoginId(_token);
        SiteUser updated2 = userRepository.findByEmail(tokenEmail).orElse(null);
        if(updated2 == null) {
            log.info("토큰에 담긴 email : {} 의 유저 존재하지 않음", tokenEmail);
            return null;
        }
        // 토큰 유효. id 유저 존재. 토큰의 유저 존재. 이제 두 유저가 같은 유저인지 검사
        if(!updated1.getId().equals(updated2.getId())) {
            log.info("id의 유저와 token의 유저가 일치하지 않음");
            log.info("id1 : {}, id2 : {}", updated1.getId(), updated2.getId());
            log.info("email1 : {}, email2 : {}", updated1.getEmail(), updated2.getEmail());
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

    public ResponseEntity<?> getMainInfo(long id, String _token) {
        SiteUser user1 = userRepository.findById(id).orElse(null);
        if(user1 == null) {
            log.info("id : {} 의 유저가 존재하지 않음", id);
            return null;
        }
        String email = jwtTokenUtil.getLoginId(_token);
        SiteUser user2 = userRepository.findByEmail(email).orElse(null);
        if(user2 == null) {
            log.info("email : {} 의 유저가 존재하지 않음", email);
            return null;
        }
        if(user1 != user2) {
            log.info("id의 유저와 email의 유저가 일치하지 않음");
            return null;
        }
        if(user1.getUserLevel() == null) {
            log.info("유저의 모의고사 기록이 존재하지 않음");
        }
        HashMap<String, String> maps = new HashMap<>();
        maps.put("name", user1.getUsername());
        maps.put("level", user1.getUserLevel());

        return ResponseEntity.status(HttpStatus.OK).body(maps);
    }
}
