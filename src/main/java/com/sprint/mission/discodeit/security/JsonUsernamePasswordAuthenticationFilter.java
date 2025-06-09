package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.LoginRequest;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@RequiredArgsConstructor
public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  /**
   * 클라이언트의 로그인 요청을 가로채 사용자 인증을 처리하는 필터 로그인 요청시 제일 먼저 실행되며 AuthencicationManger를 호출하고 그 안에서 Dao...를
   * 호출함. 리멤버미, 세션컨텍스트 등을 설정한다. 성공, 실패 결과를 처리한다.
   **/
  private final ObjectMapper objectMapper;

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws
      AuthenticationException {

    if (!request.getMethod().equals("POST")) {
      throw new AuthenticationServiceException(
          "Authentication method not supported: " + request.getMethod());
    }

    try {
      //json 요청 본문 파싱
      LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(),
          LoginRequest.class);

      UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
          loginRequest.username(), loginRequest.password());

      setDetails(request, authRequest);
      return this.getAuthenticationManager().authenticate(authRequest);
    } catch (IOException e) {
      throw new AuthenticationServiceException("Request parsing failed", e);
    }
  }

  public static JsonUsernamePasswordAuthenticationFilter createDefault(ObjectMapper objectMapper,
      AuthenticationManager authenticationManager,
      SessionAuthenticationStrategy sessionAuthenticationStrategy,
      RememberMeServices rememberMeServices) {
    JsonUsernamePasswordAuthenticationFilter filter = new JsonUsernamePasswordAuthenticationFilter(
        objectMapper);
    filter.setRequiresAuthenticationRequestMatcher(SecurityMatchers.LOGIN);
    filter.setAuthenticationManager(authenticationManager);
    filter.setAuthenticationSuccessHandler(new CustomLoginSuccessHandler(objectMapper));
    filter.setAuthenticationFailureHandler(new CustomLoginFailureHandler(objectMapper));
    filter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());
    filter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy);
    filter.setRememberMeServices(rememberMeServices);

    return filter;
  }


  public static class Configure
      extends
      AbstractAuthenticationFilterConfigurer<HttpSecurity, Configure, JsonUsernamePasswordAuthenticationFilter> {

    private final ObjectMapper objectMapper;

    public Configure(ObjectMapper objectMapper) {
      super(new JsonUsernamePasswordAuthenticationFilter(objectMapper), SecurityMatchers.LOGIN_URL);
      this.objectMapper = objectMapper;
    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
      return new AntPathRequestMatcher(loginProcessingUrl, HttpMethod.POST.name());
    }

    @Override
    public void init(HttpSecurity http) throws Exception {
      loginProcessingUrl(SecurityMatchers.LOGIN_URL);
      successHandler(new CustomLoginSuccessHandler(objectMapper));
      failureHandler(new CustomLoginFailureHandler(objectMapper));
    }


  }


}
