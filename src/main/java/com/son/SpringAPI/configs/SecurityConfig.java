package com.son.SpringAPI.configs;

import com.son.SpringAPI.accounts.Account;
import com.son.SpringAPI.accounts.AccountService;
import lombok.RequiredArgsConstructor;
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
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    /*
     동작 흐름
     1. 요청이 들어온다
     2. 서블릿 필터가 가로챈다
     3. Security Filter로 이 요청을 넘긴 후 인증을 적용할지 말지를 결정
     4. 적용한다면 Security Interceptor가 받은 후
        a. Security Context Holder 를 확인하고 접근하는 사용자가 인증을 해야하는지 확인한다
        b. AuthenticationManager (두개의 인터페이스 UserDetailsService, PasswordEncoder 를 사용하는) 를 활용하여 인증을 하고
           인증 정보를 Security Context Holder에 인증 정보를 저장한다
        c. 권한 정보 (Account의 role 같은경우) 를 확인한 후 리소스 접근 여부를 결정한다
     */

    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public TokenStore tokenStore() {
        // 토큰의 저장은 인메모리 형식으로 제공할 것이다
        return new InMemoryTokenStore();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        // AuthenticationManager를 다른 곳에서 참조할 수 있도록 Bean으로 등록해 놓는다
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // AuthenticationManager를 어떻게 생성할 것인가 정의하는 메서드
        auth.userDetailsService(accountService)
                .passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // Security Filter를 적용 할지 말지에 대한 설정 메서드, 정적인 리소스에 대한 접근은 모두 무시한다
        // 웹 단에서 필터링 한다
        web.ignoring().mvcMatchers("/docs/index.html");
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    /*
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 웹 단에서 필터링 하지 않고 서블릿의 Security Filter 에 들어온 경우이다
        http.authorizeRequests()
                .mvcMatchers("/docs/index.html").anonymous()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).anonymous();

        http.anonymous()
                .and()
                .formLogin()
                .and()
                .authorizeRequests()
                .mvcMatchers(HttpMethod.GET,"/api/**").anonymous() // 이와 같은 요청은 아무나 가능
                .anyRequest().authenticated(); // 나머지는 인증 필요
    }
    */

}
