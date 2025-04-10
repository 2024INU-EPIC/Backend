package com.example.epic;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 인증되지 않은 페이지들의 요청을 모두 허용
        http
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        // 아래 요청은 인증되지 않은 유저도 로그인 할 수 없게 만듬
                        // .requestMatchers("/api/auth/login").authenticated()
                        // 나머지 요청도 모두 인가
                        .requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
                .exceptionHandling(exception -> exception
                        // 인증이 되지 않은 요청에 대한 처리
                        .authenticationEntryPoint((request, response, authException) -> {
                            if(!request.getRequestURI().contains("api")) {
                                log.info("인증되지 않은 사용자가 잘못된 URL 에 요청");
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                            } else {
                                log.info("인증되지 않은 사용자의 URL 요청");
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                            }
                        })
                        // 인증은 되었지만 권한이 부여되지 않은 요청에 대한 처리
                        .accessDeniedHandler(((request, response, accessDeniedException) -> {
                            if(!request.getRequestURI().contains("api")) {
                                log.info("인증된 사용자가 액세스 할 수 없는 권한을 잘못된 URL 에 요청");
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                            } else {
                                log.info("인증된 사용자가 액세스 할 수 없는 권한 요청");
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                            }
                        })))
                // 세션 생성 안함
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // /api 인 모든 URL에 대해 csrf 무시
                .csrf((csrf) -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/api/**")))
                .headers((headers) -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
                .httpBasic((basic) -> {})
                .formLogin((form) -> form.disable())
                        /*.loginPage("/api/auth/login")
                        .defaultSuccessUrl("/"))*/
                .logout((logout) -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true));

        return http.build();
    }
    // 비밀번호 암호화
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 스프링 시큐리티의 인증 처리
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
