package com.sl.mecm.service.gateway.authorize;

import com.sl.mecm.core.commons.web.UserAccountInfo;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MECMUserAuthenticationToken implements Authentication {

    private final UserAccountInfo userAccountInfo;
    private boolean isAuthenticated;
    private final List<SimpleGrantedAuthority> roleList;

    public MECMUserAuthenticationToken(UserAccountInfo userAccountInfo, boolean isAuthenticated) {
        this.userAccountInfo = userAccountInfo;
        this.isAuthenticated = isAuthenticated;
        this.roleList = parseRoles(userAccountInfo.getMerchantRoles());
    }

    @Override
    public List<SimpleGrantedAuthority> getAuthorities() {
        return roleList;
    }

    @Override
    public Object getCredentials() {
        return "[CREDENTIAL-PROTECTED]";
    }

    @Override
    public Object getDetails() {
        return "[NON-DETAILS]";
    }

    @Override
    public UserAccountInfo getPrincipal() {
        return userAccountInfo;
    }

    @Override
    public boolean isAuthenticated() {
        return this.isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return userAccountInfo.getUsername();
    }

    private List<SimpleGrantedAuthority> parseRoles(String roleStr){
        String[] roles = StringUtils.tokenizeToStringArray(roleStr, ",");
        return Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
