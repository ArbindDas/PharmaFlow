package com.JSR.PharmaFlow.oauth;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Collection;
import java.util.Map;
public class CustomOAuth2User implements OAuth2User {
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;
    @Getter
    private final String email;
    @Getter
    private final String fullName;

    public CustomOAuth2User(Map<String, Object> attributes,
                            Collection<? extends GrantedAuthority> authorities,
                            String email,
                            String fullName) {
        this.attributes = attributes;
        this.authorities = authorities;
        this.email = email;
        this.fullName = fullName;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return fullName; // Or email if you prefer
    }

}