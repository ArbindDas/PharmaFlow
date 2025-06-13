package com.JSR.PharmaFlow.oauth;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Enums.OAuthProvider;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UsersRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String fullName = oauth2User.getAttribute("name");
        Map<String, Object> attributes = oauth2User.getAttributes();

        Optional<Users> existingUser = userRepository.findByEmail(email);

        Users user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // If user was previously local auth, convert to Google auth
            if (user.getAuthProvider() == null || user.getAuthProvider() == OAuthProvider.LOCAL) {
                user.setAuthProvider(OAuthProvider.GOOGLE);
                user.setPassword(null); // Remove password since they're using OAuth now
            }

            // Update name if changed
            if (fullName != null && !fullName.equals(user.getFullName())) {
                user.setFullName(fullName);
            }

            userRepository.save(user);
        } else {
            // New user
            user = new Users();
            user.setEmail(email);
            user.setFullName(fullName);
            user.setAuthProvider(OAuthProvider.GOOGLE);
            user.setPassword(null);
            user.setCreatedAt(Instant.now());
            user = userRepository.save(user);
        }

        return new CustomOAuth2User(
                attributes,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                email,
                fullName
        );
    }
}