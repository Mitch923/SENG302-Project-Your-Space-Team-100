package nz.ac.canterbury.seng302.homehelper.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Custom Security Configuration Such functionality was previously handled by
 * WebSecurityConfigurerAdapter
 */
@Configuration
@EnableWebSecurity
public class CustomSecurityConfiguration {

    /**
     * Our Custom Authentication Failure Handler {@link CustomAuthenticationFailureHandler}
     */
    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;

    /**
     * Our Custom Auth Provider {@link CustomAuthenticationProvider}
     */
    @Autowired
    private CustomAuthenticationProvider authProvider;

    /**
     * Create an Authentication Manager with our {@link CustomAuthenticationProvider}
     *
     * @param http http security configuration object from Spring
     * @return a new authentication manager
     * @throws Exception if the AuthenticationManager can not be built
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(
                AuthenticationManagerBuilder.class);
        authBuilder.authenticationProvider(authProvider);
        return authBuilder.build();
    }


    /**
     * This filter chain has been adapted from the SENG302 Spring Security Handout by Morgan English
     * method chaining has been deprecated in favour of the lambda format methods of request
     * matching have changed due to deprecations this new filter chain was developed using the help
     * of ChatGPT
     *
     * @param http http security config object from Spring (beaned in)
     * @return Custom SecurityFilterChain
     * @throws Exception if the SecurityFilterChain can not be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        RequestMatcher h2ConsoleRequestMatcher = new AntPathRequestMatcher("/h2/**");

        http
                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(h2ConsoleRequestMatcher).permitAll()
                                .requestMatchers("/verification").permitAll()
                                .requestMatchers("/", "/register/**", "/login", "/landing", "/reset",
                                        "getIPGeolocation", "getMapboxForwardGeocoding")
                                .permitAll()
                                .requestMatchers("/webjars/**").permitAll() // Allow requests for css
                                .requestMatchers("/js/**").permitAll() // Allow requests for js
                                .requestMatchers("/css/**").permitAll() // Allow requests for css
                                .requestMatchers("/img/**").permitAll() // Allow requests for images
                                .requestMatchers("/admin").hasAuthority("ROLE_ADMIN")
                                .anyRequest().hasAuthority("ROLE_USER")
                        // Allow verified users to access every other page
                )

                .headers(headers -> headers
                                .frameOptions(
                                        frame -> frame.disable())
                        // Disable frame options to allow H2 Console
                )
                // Disable CSRF for H2 console
                .csrf(csrf -> csrf.ignoringRequestMatchers(h2ConsoleRequestMatcher))
                // Configure form login
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureHandler(failureHandler)
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/home")
                        .permitAll()
                )
                // Configure logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }


}