package com.sprint.mission.discodeit.config;

import static com.sprint.mission.discodeit.security.SecurityMatchers.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.sprint.mission.discodeit.security.SecurityMatchers;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth->
					auth
				.requestMatchers(
					SecurityMatchers.NON_API,
					SIGN_UP,
					CSRF_TOKEN
				).permitAll()
				.anyRequest().authenticated())

			.logout(AbstractHttpConfigurer::disable) //Logout필터 제거
			.formLogin(AbstractHttpConfigurer::disable) //formLogin 사용하지 않음
			;
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
