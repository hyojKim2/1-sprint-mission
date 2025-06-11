package com.sprint.mission.discodeit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.security.CustomSessionInformationExpiredStrategy;
import com.sprint.mission.discodeit.security.JsonUsernamePasswordAuthenticationFilter;
import com.sprint.mission.discodeit.security.SecurityMatchers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http, ObjectMapper objectMapper,
      DaoAuthenticationProvider daoAuthenticationProvider,
      SessionRegistry sessionRegistry
  )

      throws Exception {
    http
        .authenticationProvider(daoAuthenticationProvider)
        .authorizeHttpRequests(auth ->
            auth
                .requestMatchers(
                    SecurityMatchers.NON_API,
                    SecurityMatchers.SIGN_UP,
                    SecurityMatchers.CSRF_TOKEN
                ).permitAll()
                .anyRequest().authenticated())

        .logout(AbstractHttpConfigurer::disable) //Logout필터 제거
        .with(new JsonUsernamePasswordAuthenticationFilter.Configure(objectMapper),
            Customizer.withDefaults()) //custom 인증 필터 등록(로그인 요청 url설정, 성공/실패 핸들링 등 관리)
        .formLogin(AbstractHttpConfigurer::disable) //formLogin 사용하지 않음
        .sessionManagement(session -> session.sessionFixation().migrateSession().maximumSessions(1)
            .maxSessionsPreventsLogin(false)
            .sessionRegistry(sessionRegistry)
            .expiredSessionStrategy(new CustomSessionInformationExpiredStrategy(objectMapper))
        )
    ;
    ;
    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


  //username, password기반 인증하고 사용자 정보를 가져온다.
  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);

    return provider;
  }

  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }


}
