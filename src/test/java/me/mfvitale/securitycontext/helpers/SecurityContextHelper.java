package me.mfvitale.securitycontext.helpers;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

public class SecurityContextHelper {

    public static SecurityContext createContext(String username, String password) {
        SecurityContextImpl sc = new SecurityContextImpl();
        sc.setAuthentication(new UsernamePasswordAuthenticationToken(username, password));
        return sc;
    }
}
