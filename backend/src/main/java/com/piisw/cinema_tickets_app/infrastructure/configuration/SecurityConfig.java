package com.piisw.cinema_tickets_app.infrastructure.configuration;

import com.piisw.cinema_tickets_app.domain.authentication.boundary.AuthenticationController;
import com.piisw.cinema_tickets_app.domain.genre.boundary.GenreController;
import com.piisw.cinema_tickets_app.domain.movie.boundary.MovieController;
import com.piisw.cinema_tickets_app.infrastructure.security.AuthenticationEntryPointImpl;
import com.piisw.cinema_tickets_app.infrastructure.security.AuthenticationFilter;
import com.piisw.cinema_tickets_app.infrastructure.security.TokenHandler;
import com.piisw.cinema_tickets_app.infrastructure.security.UserDetailsServiceImpl;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String WILDCARD_PATH = "/**";

    private @NonNull final UserDetailsServiceImpl userDetailsService;
    private @NonNull final AuthenticationEntryPointImpl unauthorizedHandler;
    private @NonNull final TokenHandler tokenHandler;

    @Bean
    public AuthenticationFilter authenticationFilter() {
        return new AuthenticationFilter(tokenHandler, userDetailsService);
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/",
                        "/favicon.ico",
                        "/**/*.png",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.jpg",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js")
                .permitAll()
                .antMatchers(
                        AuthenticationController.MAIN_RESOURCE + WILDCARD_PATH)
                .permitAll()
                .antMatchers(
                        MovieController.MAIN_RESOURCE + WILDCARD_PATH)
                .permitAll()
                .antMatchers(
                        GenreController.MAIN_RESOURCE + WILDCARD_PATH)
                .permitAll()
                .antMatchers(
                        "/v2/api-docs",
                    "/configuration/ui",
                    "/swagger-resources/**",
                    "/configuration/security",
                    "/swagger-ui.html",
                    "/webjars/**")
                .permitAll()
                .anyRequest()
                .authenticated();

        http.addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    }
}
