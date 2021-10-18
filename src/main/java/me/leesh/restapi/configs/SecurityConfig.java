package me.leesh.restapi.configs;

import me.leesh.restapi.accounts.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
@EnableWebSecurity // WebSecurityConfigurerAdapter, EnableWebSecurity 하는 순간 우리가 커스텀으로 시큐리티 설정이 적용된다.
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    AccountService accountService; // AccountService가 곧 UserDetailsService

    @Autowired
    PasswordEncoder passwordEncoder;

    /**
     * OAuth 토큰을 저장하는 저장소
     */
    @Bean
    TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }

    /**
     * AuthenticationManager 를 Bean으로 노출을 시켜줘야 다른 Authorization Server나 Resource 서버에서 참조할 수 있다.
     * Authorization Server : OAuth2 토큰 발행(/oauth/token) 및 토큰 인증(/oauth/authorize) - Order 0
     * Resource Server : 리소스 요청 인증 처리 (OAuth2 토큰 검사) - Order 3
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService)
                .passwordEncoder(passwordEncoder);
    }

    /**
     * Security Filter 를 적용할지 말지를 여기서 결정
     * 순서는 Web -> http 임
     * Web 에서 거르면 Spring Security 안 까지 못들어옴
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers("/docs/index.html");
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

//    /**
//     * 폼 인증 설정
//     * @param http
//     * @throws Exception
//     */
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//
//        http
//                .anonymous()
//                .and()
//                .formLogin()
//                .and()
//                .authorizeRequests()
//                .mvcMatchers(HttpMethod.GET, "/api/**").authenticated()
//                .anyRequest().authenticated()
//        ;
//    }
}
