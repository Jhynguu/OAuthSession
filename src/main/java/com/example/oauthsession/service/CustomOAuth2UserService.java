package com.example.oauthsession.service;

import com.example.oauthsession.dto.CustomOAuth2User;
import com.example.oauthsession.dto.GoogleResponse;
import com.example.oauthsession.dto.NaverResponse;
import com.example.oauthsession.dto.OAuth2Response;
import com.example.oauthsession.entity.UserEntity;
import com.example.oauthsession.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
  //DefaultOAuth2UserService OAuth2UserService의 구현체

  private final UserRepository userRepository;

  public CustomOAuth2UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    OAuth2User oAuth2User = super.loadUser(userRequest);
    System.out.println(oAuth2User.getAttributes());

    // Naver 또는 Google.. 어떤 인증 provide 인지에 대한 변수를 담아옴
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    OAuth2Response oAuth2Response = null;

    if (registrationId.equals("naver")) {
      oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
    } else if (registrationId.equals("google")) {
      oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
    } else {
      return null;
    }

    String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

    UserEntity existData = userRepository.findByUsername(username);

    String role = "ROLE_USER";

    if (existData == null) {

      UserEntity userEntity = new UserEntity();

      userEntity.setUsername(username);
      userEntity.setEmail(oAuth2Response.getEmail());
      userEntity.setRole("ROLE_USER");

      userRepository.save(userEntity);

    } else {

      role = existData.getRole();

      existData.setEmail(oAuth2Response.getEmail());

      userRepository.save(existData);

    }

    return new CustomOAuth2User(oAuth2Response, role);
  }
}
