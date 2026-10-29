
package com.backend.config;

import com.backend.security.AuthInterceptor;
import com.backend.security.RoleAuthorizationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final RoleAuthorizationInterceptor roleAuthorizationInterceptor;

    public WebConfig(
            AuthInterceptor authInterceptor,
            RoleAuthorizationInterceptor roleAuthorizationInterceptor) {
        this.authInterceptor = authInterceptor;
        this.roleAuthorizationInterceptor = roleAuthorizationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // First, authenticate protected API requests.
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register"
                );

        // Next, check roles on endpoints annotated with @RequireRole.
        registry.addInterceptor(roleAuthorizationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register"
                );
    }
}
