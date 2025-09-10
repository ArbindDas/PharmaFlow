package com.JSR.PharmaFlow.Services;
//
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Enums.OAuthProvider;
import com.JSR.PharmaFlow.Enums.Role;
import com.JSR.PharmaFlow.Services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Set;

//@Service
//public class CustomOAuth2UserService extends DefaultOAuth2UserService {
//
//    @Autowired
//    private UsersService userService;
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//
//        // Extract user information from Google
//        String email = oAuth2User.getAttribute("email");
//        String name = oAuth2User.getAttribute("name");
//        String picture = oAuth2User.getAttribute("picture");
//
//        // Process the OAuth user
//        userService.processOAuthPostLogin(email, name, picture);
//
//        return oAuth2User;
//    }
//}

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private OAuthService oauthService;  // Use the new service

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        oauthService.processOAuthPostLogin(email, name, picture);

        return oAuth2User;
    }
}