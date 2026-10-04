package org.acme.user.control;

import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.user.entity.User;

@ApplicationScoped
public class SessionTokens {

    static final String SCHEME = "Token ";

    @Inject
    JWTAuthContextInfo context;

    public String issue(User user) {
        return Jwt.subject(String.valueOf(user.id))
                .upn(user.username)
                .sign();
    }

    public long verify(String authorization) {
        if (authorization == null || !authorization.startsWith(SCHEME)) {
            throw Rejection.tokenMissing();
        }
        try {
            var jwt = JWTCallerPrincipalFactory.instance().parse(authorization.substring(SCHEME.length()), context);
            return Long.parseLong(jwt.getSubject());
        } catch (Exception _) {
            throw Rejection.tokenInvalid();
        }
    }
}
