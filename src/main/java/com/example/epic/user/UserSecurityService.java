package com.example.epic.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Optional<SiteUser> _siteUser = this.userRepository.findByUsername(identifier)
            .or(() -> this.userRepository.findByEmail(identifier));
        if (_siteUser.isEmpty()) {
            // 이메일과 사용자 이름 모두 없는 경우에 대한 예외 처리
            if (identifier.contains("@")) {
                throw new   UsernameNotFoundException("입력한 이메일로 사용자를 찾을 수 없습니다.");
            } else {
                throw new UsernameNotFoundException("입력한 사용자 이름으로 사용자를 찾을 수 없습니다.");
            }
        }
        SiteUser siteUser = _siteUser.get();
        List<GrantedAuthority> authorities = new ArrayList<>();
        if ("admin".equals(siteUser.getUsername())) {
            authorities.add(new SimpleGrantedAuthority(com.example.epic.user.UserRole.ADMIN.getValue()));
        } else {
            authorities.add(new SimpleGrantedAuthority(com.example.epic.user.UserRole.USER.getValue()));
        }
        return new User(siteUser.getUsername(), siteUser.getPassword(), authorities);
    }
    /*
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        Optional<SiteUser> _siteUser = this.userRepository.findByemail(email);
        if (_siteUser.isEmpty()) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다");
        }
        SiteUser siteUser = _siteUser.get();
        List<GrantedAuthority> authorities = new ArrayList<>();
        if ("admin".equals(email)) {
            authorities.add(new SimpleGrantedAuthority(com.example.epic.user.UserRole.ADMIN.getValue()));
        } else {
            authorities.add(new SimpleGrantedAuthority(com.example.epic.user.UserRole.USER.getValue()));
        }
        return new User(siteUser.getUsername(), siteUser.getPassword(), authorities);
    }
     */
}
