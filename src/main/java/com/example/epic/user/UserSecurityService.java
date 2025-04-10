package com.example.epic.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

// 스프링 시큐리티가 로그인 시 사용하는 서비스
@RequiredArgsConstructor
@Service
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;

    // 입력받은 비밀번호가 해당 유저의 비밀번호와 일치하는지 검사하는 로직은 loadUserByUsername 내부에 기능이 구현되어있음
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<SiteUser> _siteUser = userRepository.findByEmail(email);
        // 이메일 사용자 없는 경우에 대한 예외 처리
        if (_siteUser.isEmpty()) {
            throw new UsernameNotFoundException("입력한 이메일로 사용자를 찾을 수 없습니다.");
        }

        // 해당 사용자 발견
        SiteUser siteUser = _siteUser.get();
        List<GrantedAuthority> authorities = new ArrayList<>();
        // 해당 유저에 권한 부여
        if ("admin".equals(siteUser.getUsername())) {
            authorities.add(new SimpleGrantedAuthority(com.example.epic.user.UserRole.ADMIN.getValue()));
        } else {
            authorities.add(new SimpleGrantedAuthority(com.example.epic.user.UserRole.USER.getValue()));
        }
        return new User(siteUser.getUsername(), siteUser.getPassword(), authorities);
    }
}
