package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@RequiredArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    DiscodeitUserDetails principal = (DiscodeitUserDetails) authentication.getPrincipal(); //인증 객체에서 Principal 꺼냄
    response.setStatus(HttpServletResponse.SC_OK); //응답코드 설정
    response.setContentType(MediaType.APPLICATION_JSON_VALUE); //응답 content-type 설정
    response.getWriter().write(
        objectMapper.writeValueAsString(principal.getUserDto())); //userDto를 JSON직렬화하여 응답에 포함.
  }
}
